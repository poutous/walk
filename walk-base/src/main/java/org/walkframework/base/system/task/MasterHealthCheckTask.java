package org.walkframework.base.system.task;



/**
 * MasterHealthCheckTask
 * 
 * @author shf675
 *
 */
public class MasterHealthCheckTask extends BaseTask {

	//超时时间。默认5秒
	private long timeoutMillis = 5000;

	private MasterFactory masterFactory;

	/**
	 * 健康检查。保证本服务始终为master
	 * 周期检查一次并重新将全局唯一ID置入缓存，同时设置超时时间(timeoutMillis)，即在超时时间过后还未进行检查，则认为本服务放弃master
	 * 
	 * 周期性锁定本服务为master，只要本服务不离线，一直都是master，反之，其他服务接管master
	 * 
	 * 注意：设置的超时时间一定要大于执行周期时间
	 * @return
	 */
	public void doCheck() {
		final MasterFactory masterFactory = getMasterFactory();
		if (masterFactory.isCluster()) {
			//尝试锁定本服务作为master
			masterFactory.tryLockMaster(getTimeoutMillis());
		}
	}

	public long getTimeoutMillis() {
		return timeoutMillis;
	}

	public void setTimeoutMillis(long timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}

	public MasterFactory getMasterFactory() {
		return masterFactory;
	}

	public void setMasterFactory(MasterFactory masterFactory) {
		this.masterFactory = masterFactory;
	}
}
