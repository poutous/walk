package org.walkframework.batis.holder;

import org.walkframework.batis.bean.CacheBoundSql;

/**
 * BoundSql Holder
 * @author shf675
 */
public abstract class BoundSqlHolder {
	
	private static final ThreadLocal<CacheBoundSql> holder = new ThreadLocal<CacheBoundSql>();
	
	public static void set(CacheBoundSql boundSql) {
        if(!boundSql.equals(holder.get())){
        	holder.set(boundSql);
        }
    }

    public static CacheBoundSql get() {
        return holder.get();
    }

    public static void clear() {
    	holder.remove();
    }
}
