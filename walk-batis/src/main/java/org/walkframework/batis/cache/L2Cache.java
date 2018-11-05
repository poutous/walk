package org.walkframework.batis.cache;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.LoggingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.cache.ICache;

/**
 * 基于walk-cache实现的mybatis二级缓存
 * 
 * 支持记录命中率日志
 * 
 * @author shf675
 * 
 */
public class L2Cache extends LoggingCache {

	private Integer cacheSeconds;

	public L2Cache(String id) {
		super(new InnerL2Cache("SqlData." + id));
	}

	@Override
	public void putObject(Object key, Object object) {
		super.putObject(key, object);
		if (cacheSeconds != null) {
			expire(key, cacheSeconds.longValue());
		}
	}

	public void expire(Object key, long expire) {
		CacheManager.getCache(getId()).expire(key, expire);
	}

	public Integer getCacheSeconds() {
		return cacheSeconds;
	}

	public void setCacheSeconds(Integer cacheSeconds) {
		this.cacheSeconds = cacheSeconds;
	}

	/***********************************************************************************************************
	 * 二级缓存内部类
	 * 
	 * @author shf675
	 *
	 */
	private static class InnerL2Cache implements Cache {
		protected final Logger log = LoggerFactory.getLogger(this.getClass());

		private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

		private String id;

		public InnerL2Cache(String id) {
			if (id == null) {
				throw new IllegalArgumentException("Cache instances require an ID");
			}
			this.id = id;
		}

		@Override
		public void clear() {
			getCache().clear();
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public Object getObject(Object key) {
			Object object = getCache().getValue(key);
			if (log.isDebugEnabled() && object != null) {
				log.debug("Taken sqlData{} from cache [{}] for key [{}]", object, getId(), key.toString().replaceAll("[\\s]+", " "));
			}
			return object;
		}

		@Override
		public ReadWriteLock getReadWriteLock() {
			return readWriteLock;
		}

		@Override
		public int getSize() {
			Long size = getCache().size();
			return size == null ? 0 : size.intValue();
		}

		@Override
		public void putObject(Object key, Object value) {
			getCache().put(key, value);
		}

		@Override
		public Object removeObject(Object key) {
			Object object = getObject(key);
			getCache().evict(key);
			return object;
		}

		public ICache getCache() {
			return CacheManager.getCache(this.id);
		}
	}
}
