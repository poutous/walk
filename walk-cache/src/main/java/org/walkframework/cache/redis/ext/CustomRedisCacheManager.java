package org.walkframework.cache.redis.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.connection.ClusterCommandExecutor;
import org.springframework.data.redis.connection.DecoratedRedisConnection;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;
import org.walkframework.cache.util.ReflectHelper;
import org.walkframework.redis.BaseJedisClusterConnection;
import org.walkframework.redis.BaseJedisClusterConnection.JedisClusterCommandCallback;

import redis.clients.jedis.Jedis;

/**
 * 扩展org.springframework.data.redis.cache.RedisCacheManager
 * 
 * @author shf675
 *
 */
@SuppressWarnings("unchecked")
public class CustomRedisCacheManager extends RedisCacheManager {
	
	private final RedisSerializer<String> stringSerializer = new StringRedisSerializer();

	private final static StringBuilder LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX = new StringBuilder();

	static {
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" local delimiter = ARGV[1];");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" local keysmatch = '*'..delimiter..'*';");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" local namematch = '(.*)'..string.gsub(delimiter, '[().%+-*?[^$]', '%%%1');");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" local keys = redis.call('KEYS', keysmatch);");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" local names = {};");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" local keysCount = table.getn(keys);");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" if(keysCount > 0) then");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 	for _, key in ipairs(keys) do");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 		local name = key:match(namematch);");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 		local isExist = false;");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 		for k,v in ipairs(names) do");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 			if v == name then");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 				isExist = true;");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 				break;");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 			end;");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 		end;");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 		if not isExist then");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 			table.insert(names, name);");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 		end;");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" 	end;");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" end;");
		LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.append(" return names;");
	}

	private final RedisOperations redisOperations;

	public CustomRedisCacheManager(RedisOperations redisOperations) {
		this(redisOperations, Collections.<String> emptyList());
	}

	public CustomRedisCacheManager(RedisOperations redisOperations, Collection<String> cacheNames) {
		super(redisOperations, cacheNames);
		this.redisOperations = redisOperations;
		setCacheNames(cacheNames);
	}

	/**
	 * 扩展父类loadRemoteCacheKeys方法，使用前缀时取cachenames
	 * 
	 * @return
	 */
	@Override
	protected Set<String> loadRemoteCacheKeys() {
		//如果使用前缀
		if (isUsePrefix()) {
			RedisCachePrefix redisCachePrefix = getCachePrefix();
			final String delimiter;
			if(redisCachePrefix instanceof CustomRedisCachePrefix){
				delimiter = ((CustomRedisCachePrefix)redisCachePrefix).getDelimiter();
			} else {
				delimiter = CustomRedisCachePrefix.DEFAULT_DELIMITER;
			}
			return (Set<String>) redisOperations.execute(new RedisCallback<Set<String>>() {
				@Override
				public Set<String> doInRedis(final RedisConnection connection) throws DataAccessException {
					Set<String> cacheNames = new LinkedHashSet<String>();
					// cluster模式
					if (isClusterConnection(connection)) {
						Collection<List<Object>> namesAllNode = getClusterCommandExecutor(connection).executeCommandOnAllNodes(new JedisClusterCommandCallback<List<Object>>() {
							@Override
							public List<Object> doInCluster(Jedis client) {
								return getNamesBySingleConnection(connection, client, delimiter, true);
							}
						}).resultsAsList();
						if (!CollectionUtils.isEmpty(namesAllNode)) {
							for (List<Object> node : namesAllNode) {
								if (!CollectionUtils.isEmpty(node)) {
									for (Object name : node) {
										addToCacheNames(cacheNames, name);
									}
								}
							}
						}
					} else {
						// 普通模式
						List<Object> allNodeNames = getNamesBySingleConnection(connection, null, delimiter, false);
						if (!CollectionUtils.isEmpty(allNodeNames)) {
							for (Object name : allNodeNames) {
								addToCacheNames(cacheNames, name);
							}
						}
						
					}
					return cacheNames;
				}
			});
		}
		
		//不使用前缀
		return super.loadRemoteCacheKeys();
	}
	
	/**
	 * 将那么假如缓存中
	 * 
	 * @param cacheNames
	 * @param name
	 */
	private void addToCacheNames(Set<String> cacheNames, Object name){
		Object _name = stringSerializer.deserialize((byte[]) name);
		if(_name != null){
			cacheNames.add(_name.toString());
		}
	}
	
	/**
	 * 获取缓存名称
	 * 
	 * @param connection
	 * @param client
	 * @param delimiter
	 * @return
	 */
	private List<Object> getNamesBySingleConnection(RedisConnection connection, Jedis client, String delimiter, boolean isClusterConnection){
		// 使用lua脚本取出总数，避免直接使用keys命令获取总数的性能问题
		byte[] keysByPatternLua = stringSerializer.serialize(LUA_GET_CACHE_NAMES_WHEN_USE_PREFIX.toString());
		byte[] _delimiter = stringSerializer.serialize(delimiter);
		if(isClusterConnection){
			return ((BaseJedisClusterConnection)connection).eval(client, keysByPatternLua, ReturnType.fromJavaType(ArrayList.class), 0, _delimiter);
		}
		return connection.eval(keysByPatternLua, ReturnType.fromJavaType(ArrayList.class), 0, _delimiter);
	}

	public RedisOperations getRedisOperations() {
		return redisOperations;
	}
	
	/**
	 * org.walkframework.cache.redis.RedisCacheDecorator中反射会用到
	 * 
	 * @return
	 */
	@Override
	protected boolean isUsePrefix() {
		return super.isUsePrefix();
	}
	
	/**
	 * org.walkframework.cache.redis.RedisCacheDecorator中反射会用到
	 * 
	 * @return
	 */
	@Override
	protected RedisCachePrefix getCachePrefix() {
		return super.getCachePrefix();
	}
	
	/**
	 * 获取集群执行器
	 * 
	 * @param connection
	 * @return
	 */
	public ClusterCommandExecutor getClusterCommandExecutor(RedisConnection connection) {
		if(isClusterConnection(connection)){
			return (ClusterCommandExecutor) ReflectHelper.getFieldValue(connection, "clusterCommandExecutor");
		}
		return null;
	}
	
	/**
	 * 是否为cluster连接
	 * @param connection
	 * @return
	 */
	public boolean isClusterConnection(RedisConnection connection) {
		while (connection instanceof DecoratedRedisConnection) {
			connection = ((DecoratedRedisConnection) connection).getDelegate();
		}
		return connection instanceof RedisClusterConnection;
	}
	
}
