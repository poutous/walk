package org.walkframework.cache.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.CollectionUtils;
import org.walkframework.cache.AbstractCache;
import org.walkframework.cache.redis.ext.CustomRedisCacheManager;
import org.walkframework.cache.util.CollectionHelper;
import org.walkframework.cache.util.LockingRedisCacheCallback;
import org.walkframework.cache.util.RedisCacheMetadataHelper;
import org.walkframework.cache.util.ReflectHelper;
import org.walkframework.redis.BaseJedisClusterConnection;
import org.walkframework.redis.BaseJedisClusterConnection.JedisClusterCommandCallback;

import redis.clients.jedis.Jedis;

/**
 * 基于redis实现的缓存
 * 
 * @author shf675
 */
@SuppressWarnings("unchecked")
public class RedisCacheDecorator extends AbstractCache {

	protected final static Logger log = LoggerFactory.getLogger(RedisCacheDecorator.class);

	private static final long DEFAULT_COUNT = 100;

	private static final int PAGE_SIZE = 128;

	private RedisOperations redisOperations;

	private byte[] prefix;

	private RedisSerializer<String> stringSerializer;

	private final RedisCacheMetadataHelper redisCacheMetadataHelper;

	// 获取总数lua脚本
	private static final String COUNT_KEYS_BY_PATTERN_LUA = "local keys = redis.call('KEYS', ARGV[1]); local keysCount = table.getn(keys); return keysCount;";

	// 清除key lua脚本
	private static final String REMOVE_KEYS_BY_PATTERN_LUA = "local keys = redis.call('KEYS', ARGV[1]); local keysCount = table.getn(keys); if(keysCount > 0) then for _, key in ipairs(keys) do redis.call('del', key); end; end; return keysCount;";

	// 按分页获取key lua脚本
	private static final StringBuilder PAGE_KEYS_BY_PATTERN_LUA = new StringBuilder();

	static {
		PAGE_KEYS_BY_PATTERN_LUA.append(" local pagekeys = {};");
		PAGE_KEYS_BY_PATTERN_LUA.append(" local keyPattern = ARGV[1];");
		PAGE_KEYS_BY_PATTERN_LUA.append(" local start = (tonumber(ARGV[2]) > 0 and tonumber(ARGV[2])) or 0;");
		PAGE_KEYS_BY_PATTERN_LUA.append(" local size = tonumber(ARGV[3]);");
		PAGE_KEYS_BY_PATTERN_LUA.append(" if(size <= 0) then");
		PAGE_KEYS_BY_PATTERN_LUA.append(" 	return pagekeys;");
		PAGE_KEYS_BY_PATTERN_LUA.append(" end;");
		PAGE_KEYS_BY_PATTERN_LUA.append(" local keys = redis.call('KEYS', keyPattern);");
		PAGE_KEYS_BY_PATTERN_LUA.append(" local keysCount = table.getn(keys);");
		PAGE_KEYS_BY_PATTERN_LUA.append(" if(start == 0 and size >= keysCount) then");
		PAGE_KEYS_BY_PATTERN_LUA.append(" 	return keys;");
		PAGE_KEYS_BY_PATTERN_LUA.append(" end;");
		PAGE_KEYS_BY_PATTERN_LUA.append(" if(start < keysCount) then");
		PAGE_KEYS_BY_PATTERN_LUA.append(" 	for i=start+1, start+size do");
		PAGE_KEYS_BY_PATTERN_LUA.append(" 		table.insert(pagekeys, keys[i]);");
		PAGE_KEYS_BY_PATTERN_LUA.append(" 	end;");
		PAGE_KEYS_BY_PATTERN_LUA.append(" end;");
		PAGE_KEYS_BY_PATTERN_LUA.append(" return pagekeys;");
	}

	public RedisCacheDecorator(CacheManager cacheManager, Cache cache, RedisSerializer<String> stringSerializer) {
		super(cacheManager, cache);
		this.stringSerializer = stringSerializer;
		this.redisOperations = (RedisOperations) cache.getNativeCache();
		this.prefix = getPrefix();
		this.redisCacheMetadataHelper = new RedisCacheMetadataHelper((RedisCache)cache);
	}

	@Override
	public void expire(Object key, final long expireSeconds) {
		final byte[] keyBytes = computeKey(key);
		redisOperations.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) {
				byte[] bs = connection.get(keyBytes);
				if (bs == null) {
					return null;
				}
				if (expireSeconds > -1) {
					connection.expire(keyBytes, expireSeconds);
				} else {
					connection.persist(keyBytes);
				}
				return null;
			}
		});
	}

	@Override
	public Long ttl(Object key) {
		final byte[] keyBytes = computeKey(key);
		return (Long) redisOperations.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) {
				return connection.ttl(keyBytes);
			}
		}) * 1000L;
	}

	/**
	 * 使用scan命令
	 * 
	 * @return
	 */
	@Override
	public Iterator<Object> keys() {
		return (Iterator<Object>) redisOperations.execute(new RedisCallback<Iterator<Object>>() {
			public Iterator<Object> doInRedis(RedisConnection connection) {
				if (usesKeyPrefix()) {
					Iterator<byte[]> iter = null;
					// cluster模式不支持scan命令，只能使用keys命令
					if (getCacheManager().isClusterConnection(connection)) {
						Set<byte[]> keys = connection.keys(getKeysPrefix());
						if (!CollectionUtils.isEmpty(keys)) {
							iter = keys.iterator();
						}
					} else {
						// 普通模式用scan命令进行遍历
						ScanOptions scanOptions = ScanOptions.scanOptions().count(DEFAULT_COUNT).match(stringSerializer.deserialize(getKeysPrefix())).build();
						iter = connection.scan(scanOptions);
					}
					final Iterator<byte[]> cursor = iter;
					return new KeyIterator<Object>(cursor) {
						@Override
						public Object next() {
							return convertKeyToObject(cursor.next());
						}
					};
				} else {
					// spring redis cache里判断如果没用前缀，则默认把key放置一个有序集合里，用zScan命令进行遍历
					ScanOptions scanOptions = ScanOptions.scanOptions().count(DEFAULT_COUNT).build();
					final Cursor<Tuple> cursor = connection.zScan(getKeyNoPrefix(), scanOptions);
					return new KeyIterator<Object>(cursor) {
						@Override
						public Object next() {
							return convertKeyToObject(cursor.next().getValue());
						}
					};
				}
			}
		});
	}

	/**
	 * 使用keys命令会造成redis阻塞，弃之
	 * 
	 * @return
	 */
//	@Override
//	public Iterator<Object> keys() {
//		return (Iterator<Object>) redisOperations.execute(new RedisCallback<Iterator<Object>>() {
//			public Iterator<Object> doInRedis(RedisConnection connection) {
//				if (usesKeyPrefix()) {
//					Set<byte[]> keys = connection.keys(getKeysPrefix());
//					if (!CollectionUtils.isEmpty(keys)) {
//						final Iterator<byte[]> iter = keys.iterator();
//						return new KeyIterator<Object>(iter) {
//							@Override
//							public Object next() {
//								return convertKeyToObject(iter.next());
//							}
//						};
//					}
//					return null;
//				} else {
//					int offset = 0;
//					boolean finished = false;
//					Set<byte[]> keys = new HashSet<byte[]>();
//					do {
//						// need to paginate the keys
//						Set<byte[]> pageKeys = connection.zRange(getKeyNoPrefix(), (offset) * PAGE_SIZE, (offset + 1) * PAGE_SIZE - 1);
//						finished = pageKeys.size() < PAGE_SIZE;
//						offset++;
//						if (!CollectionUtils.isEmpty(pageKeys)) {
//							keys.addAll(pageKeys);
//						}
//					} while (!finished);
//
//					final Iterator<byte[]> iter = keys.iterator();
//					return new KeyIterator<Object>(iter) {
//						@Override
//						public Object next() {
//							return convertKeyToObject(iter.next());
//						}
//					};
//				}
//			}
//		});
//	}

	/**
	 * 按分页取keys
	 * 
	 * @param start
	 * @param size
	 * @return
	 */
	public Iterator<Object> keys(final int start, final int size) {
		return keys(start, size, "*");
	}
	
	/**
	 * 按分页取keys。指定key匹配模式
	 * 
	 * @param start
	 * @param size
	 * @param keyPattern
	 * @return
	 */
	public Iterator<Object> keys(final int start, final int size, final String keyPattern) {
		return (Iterator<Object>) redisOperations.execute(new RedisCallback<Iterator<Object>>() {
			public Iterator<Object> doInRedis(RedisConnection connection) {
				if (usesKeyPrefix()) {

					Collection<byte[]> keys = null;
					// cluster模式
					if (getCacheManager().isClusterConnection(connection)) {
						keys = CollectionHelper.subCollection(connection.keys(getKeysPrefix(keyPattern)), start, size);
					} else {
						// 普通模式，使用lua脚本，提高性能
						byte[] pageKeysByPatternLua = stringSerializer.serialize(PAGE_KEYS_BY_PATTERN_LUA.toString());
						keys = connection.eval(pageKeysByPatternLua, ReturnType.fromJavaType(ArrayList.class), 0, getKeysPrefix(keyPattern), stringSerializer.serialize(String.valueOf(start)), stringSerializer.serialize(String.valueOf(size)));
					}

					if (!CollectionUtils.isEmpty(keys)) {
						final Iterator<byte[]> iter = keys.iterator();
						return new KeyIterator<Object>(iter) {
							@Override
							public Object next() {
								return convertKeyToObject(iter.next());
							}
						};
					}
					return null;
				} else {
					Set<Object> keys = new HashSet<Object>();
					Set<byte[]> pageKeys = connection.zRange(getKeyNoPrefix(), start, start + size - 1);
					if (!CollectionUtils.isEmpty(pageKeys)) {
						keys.addAll(pageKeys);
					}
					final Iterator<Object> iter = keys.iterator();
					return new KeyIterator<Object>(iter) {
						@Override
						public Object next() {
							return convertKeyToObject((byte[]) iter.next());
						}
					};
				}
			}
		});
	}

	@Override
	public Long size() {
		return size("*");
	}

	/**
	 * 获取key总数。指定key匹配模式
	 * 
	 * @param keyPattern
	 * @return
	 */
	public Long size(final String keyPattern) {
		return (Long) redisOperations.execute(new LockingRedisCacheCallback<Long>(redisCacheMetadataHelper) {
			public Long doInLock(final RedisConnection connection) {
				if (usesKeyPrefix()) {
					// cluster模式
					if (getCacheManager().isClusterConnection(connection)) {
						Collection<Long> sizeAllNode = getCacheManager().getClusterCommandExecutor(connection).executeCommandOnAllNodes(new JedisClusterCommandCallback<Long>() {
							@Override
							public Long doInCluster(Jedis client) {
								return getSizeBySingleConnection(connection, client, keyPattern, true);
							}
						}).resultsAsList();
						Long size = 0L;
						if (!CollectionUtils.isEmpty(sizeAllNode)) {
							for (Long perNodeSize : sizeAllNode) {
								size += perNodeSize;
							}
						}
						return size;
					}

					// 普通模式
					return getSizeBySingleConnection(connection, null, keyPattern, false);
				} else {
					// spring redis cache里判断如果没用前缀，则默认把key放置一个有序集合里，用zcard命令取出总数
					return connection.zCard(getKeyNoPrefix());
				}
			}
		});
	}

	@Override
	public void clear() {
		// 不使用前缀模式
		if (!usesKeyPrefix()) {
			super.clear();
		}
		// 使用前缀模式，spring的逻辑是取出所有的keys然后遍历清除，这样会造成网络传输性能损耗，现改用lua脚本实现，直接在redis节点内存内处理。
		redisOperations.execute(new LockingRedisCacheCallback<Void>(redisCacheMetadataHelper) {
			public Void doInLock(final RedisConnection connection) {
				// cluster模式
				if (getCacheManager().isClusterConnection(connection)) {
					getCacheManager().getClusterCommandExecutor(connection).executeCommandOnAllNodes(new JedisClusterCommandCallback<Void>() {
						@Override
						public Void doInCluster(Jedis client) {
							clearBySingleConnection(connection, client, true);
							return null;
						}
					});
				} else {
					// 普通模式
					clearBySingleConnection(connection, null, false);
				}

				return null;
			}
		});
	}

	/**
	 * 获取key总数。指定key匹配模式
	 * 
	 * @param connection
	 * @param client
	 * @param keyPattern
	 * @return
	 */
	private Long getSizeBySingleConnection(RedisConnection connection, Jedis client, String keyPattern, boolean isClusterConnection) {
		// 使用lua脚本取出总数，避免直接使用keys命令获取总数的性能问题
		byte[] countKeysByPatternLua = stringSerializer.serialize(COUNT_KEYS_BY_PATTERN_LUA);
		if (isClusterConnection) {
			return ((BaseJedisClusterConnection) connection).eval(client, countKeysByPatternLua, ReturnType.INTEGER, 0, getKeysPrefix(keyPattern));
		}
		return connection.eval(countKeysByPatternLua, ReturnType.INTEGER, 0, getKeysPrefix(keyPattern));
	}

	/**
	 * 获取key总数。指定key匹配模式
	 * 
	 * @param connection
	 * @param client
	 * @return
	 */
	private void clearBySingleConnection(RedisConnection connection, Jedis client, boolean isClusterConnection) {
		// 使用lua脚本取出总数，避免直接使用keys命令获取总数的性能问题
		byte[] removeKeysByPatternLua = stringSerializer.serialize(REMOVE_KEYS_BY_PATTERN_LUA);
		if (isClusterConnection) {
			((BaseJedisClusterConnection) connection).eval(client, removeKeysByPatternLua, ReturnType.INTEGER, 0, getKeysPrefix());
			return;
		}
		connection.eval(removeKeysByPatternLua, ReturnType.INTEGER, 0, getKeysPrefix());
	}

	@Override
	public CustomRedisCacheManager getCacheManager() {
		return (CustomRedisCacheManager) super.getCacheManager();
	}

	public boolean usesKeyPrefix() {
		return (Boolean) ReflectHelper.invokeMethod(getCacheManager(), getCacheManager().getClass(), "isUsePrefix", null, null);
	}

	public byte[] getKeysPrefix() {
		return getKeysPrefix("*");
	}

	public byte[] getKeysPrefix(String pattern) {
		byte[] WILD_CARD = stringSerializer.serialize(pattern);
		byte[] keysPrefix = Arrays.copyOf(prefix, prefix.length + WILD_CARD.length);
		System.arraycopy(WILD_CARD, 0, keysPrefix, prefix.length, WILD_CARD.length);
		return keysPrefix;
	}

	public byte[] getKeyNoPrefix() {
		return stringSerializer.serialize(getName() + "~keys");
	}

	public byte[] computeKey(Object key) {
		byte[] keyBytes = convertToBytesIfNecessary(redisOperations.getKeySerializer(), key);
		if (prefix == null || prefix.length == 0) {
			return keyBytes;
		}
		byte[] result = Arrays.copyOf(prefix, prefix.length + keyBytes.length);
		System.arraycopy(keyBytes, 0, result, prefix.length, keyBytes.length);
		return result;
	}

	public Object convertKeyToObject(byte[] keyByte) {
		byte[] inverseKeyByte = inverseKey(keyByte);
		return redisOperations.getKeySerializer() != null ? redisOperations.getKeySerializer().deserialize(inverseKeyByte) : inverseKeyByte;
	}

	private byte[] getPrefix() {
		if (usesKeyPrefix()) {
			RedisCachePrefix cachePrefix = (RedisCachePrefix) ReflectHelper.invokeMethod(getCacheManager(), getCacheManager().getClass(), "getCachePrefix", null, null);
			return cachePrefix.prefix(getName());
		}
		return null;
	}

	private byte[] inverseKey(byte[] keyBytes) {
		if (prefix == null || prefix.length == 0) {
			return keyBytes;
		}

		byte[] result = Arrays.copyOf(prefix, keyBytes.length - prefix.length);
		System.arraycopy(keyBytes, prefix.length, result, 0, result.length);
		return result;
	}

	private byte[] convertToBytesIfNecessary(RedisSerializer<Object> serializer, Object value) {
		if (serializer == null && value instanceof byte[]) {
			return (byte[]) value;
		}
		return serializer.serialize(value);
	}

	private abstract class KeyIterator<T> implements Iterator<T> {
		private final Iterator delegate;

		public KeyIterator(final Iterator iterator) {
			this.delegate = iterator;
		}

		@Override
		public boolean hasNext() {
			boolean hasNext = delegate.hasNext();
			if(!hasNext && delegate instanceof Cursor){
				try {
					((Cursor)delegate).close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
			return hasNext;
		}

		@Override
		public void remove() {
			delegate.remove();
		}
	}
}
