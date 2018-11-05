package org.walkframework.base.system.localcache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用LinkedHashMap实现的简单本地缓存。用于一些无法做集群的对象，看具体情况使用。
 * LinkedHashMap自身已经实现了顺序存储，默认情况下是按照元素的添加顺序存储，也可以启用按照访问顺序存储，
 * 即最近读取的数据放在最前面，最早读取的数据放在最后面，然后它还有一个判断是否删除最老数据的方法，默认是返回false
 * 即不删除数据，我们使用LinkedHashMap实现LRU缓存的方法就是对LinkedHashMap实现简单的扩展
 * 重写removeEldestEntry方法判断是否删除
 * @param <K>
 * @param <V>
 */
public class LocalLRUCache<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 1L;
	
	private static final int defaultCacheSize = 2000;
	private int cacheSize = 0;

	public LocalLRUCache() {
		super((int) Math.ceil(defaultCacheSize / 0.75f) + 1, 0.75f, true);
		this.cacheSize = defaultCacheSize;
	}
	
	public LocalLRUCache(int cacheSize) {
		super((int) Math.ceil(cacheSize / 0.75f) + 1, 0.75f, true);
		this.cacheSize = cacheSize;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > LocalLRUCache.this.cacheSize;
	}
}