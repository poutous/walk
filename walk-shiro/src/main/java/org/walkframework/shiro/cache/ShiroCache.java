package org.walkframework.shiro.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.cache.ICache;

/**
 * @author shf675
 *
 * @param <K>
 * @param <V>
 */
public class ShiroCache<K, V> implements Cache<K, V> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private ICache cache;

	/**
	 * 通过一个JedisManager实例构造RedisCache
	 */
	public ShiroCache(ICache cache) {
		if (cache == null) {
			throw new IllegalArgumentException("Cache argument cannot be null.");
		}
		this.cache = cache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(K key) throws CacheException {
		if (log.isTraceEnabled()) {
			log.trace("Getting object from cache [{}] for key [{}]", cache.getName(), key);
		}
		try {
			return this.cache.getValue(key);
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	@Override
	public V put(K key, V value) throws CacheException {
		if (log.isTraceEnabled()) {
			log.trace("Putting object in cache [{}] for key [{}]", cache.getName(), key);
		}
		try {
			V previous = get(key);
			this.cache.put(key, value);
			return previous;
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	@Override
	public V remove(K key) throws CacheException {
		if (log.isTraceEnabled()) {
			log.trace("Removing object from cache [{}] for key [{}]", cache.getName(), key);
		}
		try {
			V previous = get(key);
			this.cache.evict(key);
			return previous;
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	@Override
	public void clear() throws CacheException {
		if (log.isTraceEnabled()) {
			log.trace("Clearing all objects from cache [{}]", cache.getName());
		}
		try {
			this.cache.clear();
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public int size() {
		try {
			Long size = this.cache.size();
			int s = size == null ? 0 : size.intValue();
			if (log.isTraceEnabled()) {
				log.trace("cache [{}] size is {}", cache.getName(), s);
			}
			return s;
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<K> keys() {
		if (log.isTraceEnabled()) {
			log.trace("Getting all keys from cache [{}]", cache.getName());
		}

		try {
			Set<K> keys = new HashSet<K>();
			Iterator<Object> keysIterator = this.cache.keys();
			while (keysIterator.hasNext()) {
				keys.add((K) keysIterator.next());
			}
			return keys;
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<V> values() {
		if (log.isTraceEnabled()) {
			log.trace("Getting all values from cache [{}]", cache.getName());
		}
		try {
			Collection<V> values = new ArrayList<V>();
			Iterator<Object> keysIterator = this.cache.keys();
			while (keysIterator.hasNext()) {
				values.add((V) this.cache.get(keysIterator.next()));
			}
			return values;
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}

	public String toString() {
		return "ShiroCache [" + cache.getName() + "]";
	}
	
	/**
	 * 设置过期时间
	 * 
	 * @param key
	 * @param expireSeconds
	 */
	public V expire(K key, long expireSeconds) throws CacheException {
		if (log.isTraceEnabled()) {
			log.trace("Expireing object seconds[{}] in cache [{}] for key [{}] ", expireSeconds, cache.getName(), key);
		}
		try {
			V previous = get(key);
			this.cache.expire(key, expireSeconds);
			return previous;
		} catch (Throwable t) {
			throw new CacheException(t);
		}
	}
	
	/**
	 * 获取icache
	 * 
	 * @return
	 */
	public ICache getNativeCache(){
		return cache;
	}
}
