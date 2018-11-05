package org.walkframework.cache.ehcache;

import java.lang.reflect.Method;
import java.util.Iterator;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.ReflectionUtils;
import org.walkframework.cache.AbstractCache;

/**
 * 基于ehcache实现的缓存
 * 
 * @author shf675
 */
public class EhCacheDecorator extends AbstractCache {

	private Ehcache ehcache;

	public EhCacheDecorator(CacheManager cacheManager, Cache cache) {
		super(cacheManager, cache);
		this.ehcache = (Ehcache) cache.getNativeCache();
	}

	@Override
	public void expire(Object key, long expireSeconds) {
		Element element = ehcache.get(key);
		if (element != null) {
			int seconds = Long.valueOf(expireSeconds).intValue();
			if (seconds > -1) {
				element.setEternal(false);
				element.setTimeToLive(seconds);
				//ehcache高版本中已去除此方法，改用反射方式尝试设置，以此兼容低版本ehcache
				//element.getElementEvictionData().setCreationTime(System.currentTimeMillis());
				trySetElementCreationTime(element);

			} else {
				element.setEternal(true);
			}
		}
	}

	@Override
	public Long ttl(Object key) {
		Element element = ehcache.get(key);
		if (element != null && !element.isExpired()) {
			return element.getExpirationTime() - System.currentTimeMillis();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Object> keys() {
		return ehcache.getKeys().iterator();
	}

	@Override
	public Long size() {
		return new Long(ehcache.getSize());
	}

	private void trySetElementCreationTime(Element element) {
		try {
			Method method = ReflectionUtils.findMethod(element.getClass(), "getElementEvictionData");
			if (method != null) {
				Object elementEvictionData = method.invoke(element);
				if (elementEvictionData != null) {
					ReflectionUtils.findMethod(elementEvictionData.getClass(), "setCreationTime", long.class).invoke(elementEvictionData, System.currentTimeMillis());
				}
			}

		} catch (Exception e) {
		}
	}
}
