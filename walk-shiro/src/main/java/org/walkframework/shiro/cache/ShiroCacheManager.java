package org.walkframework.shiro.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.walkframework.cache.ICacheManager;

/**
 * shiro缓存管理器
 * 
 * @author shf675
 *
 */
public class ShiroCacheManager implements CacheManager, ApplicationContextAware, InitializingBean{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@SuppressWarnings("unchecked")
	private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();
	
	private ApplicationContext applicationContext;// 声明一个静态变量保存
	
	private ICacheManager cacheManager;
	
	private String cacheManagerName;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(this.cacheManager == null && StringUtils.hasText(cacheManagerName)){
			this.cacheManager = (ICacheManager)getApplicationContext().getBean(cacheManagerName);
		}  else {
			if(StringUtils.hasText(cacheManagerName)){
				log.warn("cacheManager already set,cacheManagerName invalid setup!");
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> Cache<K, V> getCache(String name) throws CacheException {
		if (log.isTraceEnabled()) {
            log.trace("Acquiring ShiroCache instance named [{}]", name);
        }
		Cache<K, V> c = caches.get(name);
		if (c == null) {
			c = new ShiroCache<K, V>(this.cacheManager.getICache(name));
			caches.put(name, c);
		}
		return c;
	}
	
	public String getCacheManagerName() {
		return cacheManagerName;
	}

	public void setCacheManagerName(String cacheManagerName) {
		this.cacheManagerName = cacheManagerName;
	}

	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public ICacheManager getCacheManager() {
		return cacheManager;
	}
}
