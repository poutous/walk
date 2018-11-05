package org.walkframework.base.system.task;

/**
 * 一点加载数据，多点处理数据
 * 
 * @author shf675
 *
 */
public interface OneLoadAndMultiProcessTask {
	/**
	 * 一点加载数据
	 */
	void oneLoad();
	
	/**
	 * 多点处理数据
	 */
	void multiProcess();
}
