package org.walkframework.batis.holder;

import java.util.List;

import org.apache.ibatis.mapping.ResultMap;

/**
 * ResultMap Holder
 * 
 * @author shf675
 */
public abstract class ResultMapHolder {
	
	private static final ThreadLocal<List<ResultMap>> holder = new ThreadLocal<List<ResultMap>>();
	
	public static void set(List<ResultMap> resultMaps) {
        if(!resultMaps.equals(holder.get())){
        	holder.set(resultMaps);
        }
    }

    public static List<ResultMap> get() {
        return holder.get();
    }

    public static void clear() {
    	holder.remove();
    }
}
