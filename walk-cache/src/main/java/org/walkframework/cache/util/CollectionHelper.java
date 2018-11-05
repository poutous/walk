package org.walkframework.cache.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 集合工具类
 * 
 * @author shf675
 *
 */
public abstract class CollectionHelper {
	
	/**
	 * 分割集合，返回list
	 * 
	 * @param <E>
	 * @param collection
	 * @param start
	 * @param size
	 * @param total
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E> List<E> subCollection(Collection<E> collection, int start, int size) {
		if (collection == null || collection.isEmpty() || size <= 0) {
			return Collections.EMPTY_LIST;
		}
		int total = collection.size();
		start = start <= 0 ? 0 : start;
		if(start == 0 && size >= total){
			return new ArrayList<E>(collection);
		}
		int end = start + size;
		end = end >= total ? total : end;
		return new ArrayList<E>(collection).subList(start, end);
	}
}
