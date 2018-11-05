package org.walkframework.batis.holder;

import org.walkframework.batis.bean.Batch;


/**
 * 批量更新所用Holder
 * 
 * @author shf675
 */
public abstract class BatchHolder {
	
	private static final ThreadLocal<Batch> holder = new ThreadLocal<Batch>();
	
	public static void setBatch(Batch batch) {
		holder.set(batch);
    }

    public static Batch getBatch() {
        return holder.get();
    }
    
    public static void clear(){
    	holder.remove();
    }
}
