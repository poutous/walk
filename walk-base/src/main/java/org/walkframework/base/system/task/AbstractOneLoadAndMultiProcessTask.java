package org.walkframework.base.system.task;

/**
 * 一点加载数据，多点处理数据
 * 
 * @author shf675
 *
 */
public abstract class AbstractOneLoadAndMultiProcessTask extends AbstractClusterTask implements OneLoadAndMultiProcessTask {

	@Override
	public void doExecute() {
		//一点加载数据
		oneLoad();
	}

	@Override
	public void doTask() {
		super.doTask();

		//多点处理数据
		multiProcess();
	}
}
