package org.walkframework.batis.bean;


/**
 * 批处理遍历时回调
 * 
 * @author shf675
 *
 */
public interface BatchEachHandler<T> {
	
	/**
	 * 遍历时执行此方法，可在此方法内进行特殊处理
	 * 
	 * @param object
	 */
	void onEach(T object);
}
