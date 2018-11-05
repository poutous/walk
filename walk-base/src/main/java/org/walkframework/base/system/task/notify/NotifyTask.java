package org.walkframework.base.system.task.notify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.walkframework.base.mvc.entity.TfMNotify;
import org.walkframework.base.mvc.service.common.TaskNotifyService;
import org.walkframework.base.system.constant.NotifyConstants;
import org.walkframework.base.system.task.AbstractOneLoadAndMultiProcessTask;
import org.walkframework.base.system.task.ObjectHandleNotify;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.utils.HttpClientUtil;
import org.walkframework.batis.bean.BatchEachHandler;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.mq.pubsub.IPubSubManager;
import org.walkframework.mq.queue.IQueue;
import org.walkframework.mq.queue.IQueueManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * 通知任务
 * 
 * @author shf675
 * 
 */
public class NotifyTask extends AbstractOneLoadAndMultiProcessTask {
	
	/**
	 * 通知任务队列名称
	 */
	private static final String NOTIFY_TASK_DATA_QUEUE_NAME = "NOTIFY_TASK_DATA_QUEUE";
	
	/**
	 * 单个CPU时线程池中工作线程的数目
	 */
	private static final int POOL_SIZE = 4;
	
	/**
	 * 默认线程数
	 */
	private static final int DEFAULT_THREAD = Runtime.getRuntime().availableProcessors() * POOL_SIZE;

	/**
	 * 默认分页尺寸
	 */
	private static final int DEFAULT_PAGE_SIZE = 2000;

	/**
	 * 默认线程执行超时时间
	 */
	private static final int DEFAULT_AWAIT_TERMINATION_TIMEOUT_SECONDS = 30;
	
	/**
	 * 默认通知过期秒数  一天
	 */
	private static final int DEFAULT_NOTIFY_EXPIRE_SECONDS = 1 * 24 * 60 * 60;
	
	/**
	 * 默认队列最大容量
	 */
	private static final int DEFAULT_QUEUE_MAX_SIZE = 10000;
	
	/**
	 * 队列管理器名称
	 */
	private String queueManagerName;
	
	/**
	 * 发布/订阅管理器名称
	 */
	private String pubSubManagerName;
	
	/**
	 * 通知service
	 */
	private TaskNotifyService taskNotifyService;
	
	/**
	 * 加载数据每次加载梳理
	 */
	private int loadSize = DEFAULT_PAGE_SIZE;
	
	/**
	 * 处理数据启动线程数
	 */
	private int processThread = DEFAULT_THREAD;
	
	/**
	 * 线程执行超时时间
	 */
	private int awaitTerminationTimeoutSeconds = DEFAULT_AWAIT_TERMINATION_TIMEOUT_SECONDS;
	
	/**
	 * 通知过期秒数
	 */
	private long notifyExpireSeconds = DEFAULT_NOTIFY_EXPIRE_SECONDS;
	
	/**
	 * 队列容量
	 */
	private int queueMaxSize = DEFAULT_QUEUE_MAX_SIZE;

	/**
	 * 一点加载数据
	 */
	@Override
	public void oneLoad() {
		IQueue<TfMNotify> taskQueue = getTaskQueue();
		if(taskQueue == null){
			log.warn("queue component not loaded.");
			return;
		}
		
		//检查队列是否已达最大容量
		int size = taskQueue.size();
		if(size >= getQueueMaxSize()){
			log.info("The queue has reached the maximum capacity of the team, no longer accept new request. current capacity:{},limit capacity:{}", size, getQueueMaxSize());
			return;
		}
		
		TfMNotify param = new TfMNotify();
		// 分页分批处理，防止一次获取太多数据造成内存溢出
		int pageSize = getLoadSize();
		int i = 0;
		for (;;) {
			Pagination pagination = new Pagination();
			pagination.setNeedCount(false);
			pagination.setRange(i * pageSize, pageSize);
			PageData<TfMNotify> pageData = taskNotifyService.queryNotifyList(param, pagination);
			List<TfMNotify> subList = pageData.getRows();
			if (subList.size() > 0) {
				
				//1、批量更新状态为处理中
				lockNotifyList(subList);
				
				//2、循环压入队列
				for (TfMNotify tfMNotify : subList) {
					taskQueue.offer(tfMNotify);
				}
				
				if (subList.size() < pageSize) {
					break;
				}
			} else {
				break;
			}
			i++;
		}
	}

	/**
	 * 多点处理数据
	 */
	@Override
	public void multiProcess() {
		IQueue<TfMNotify> taskQueue = getTaskQueue();
		if(taskQueue == null){
			log.warn("queue component not loaded.");
			return;
		}
		
		//检查队列是否已达最大容量
		int size = taskQueue.size();
		if(size == 0){
			log.info("The queue is empty.");
			return;
		}
		
		//从队列中取出数据
		List<TfMNotify> notifyList = new ArrayList<TfMNotify>();
		for (int i = 0; i < getProcessThread(); i++) {
			TfMNotify notify = taskQueue.poll();
			if(notify != null){
				notifyList.add(notify);
			}
		}
		if(notifyList.size() == 0){
			return ;
		}
		
		int urlModeNums = 0;
		
		// 利用线程池处理
		ExecutorService pool = Executors.newFixedThreadPool(notifyList.size());
		List<TfMNotify> resultList = new ArrayList<TfMNotify>();
		try {

			//处理
			urlModeNums += handleNotifyList(notifyList, resultList, pool);
		} finally {
			//如果存在url通知模式，执行完毕关闭线程池。
			if (urlModeNums > 0 && !pool.isShutdown()) {
				try {
					//关闭线程池
					pool.shutdown();

					//等待关闭线程池，该方法是阻塞的，设置超时时间，过了超时时间将自动关闭。
					if (!pool.isTerminated()) {
						pool.awaitTermination(getAwaitTerminationTimeoutSeconds(), TimeUnit.MILLISECONDS);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		//批量更新通知结果状态
		if (!CollectionUtils.isEmpty(resultList)) {
			updateNotifyResult(resultList);
		}
		
	}

	/**
	 * 处理通知列表
	 * 
	 * @param notifyList
	 */
	protected int handleNotifyList(List<TfMNotify> notifyList, List<TfMNotify> resultList, ExecutorService pool) {
		int urlModeNums = 0;
		for (final TfMNotify notify : notifyList) {

			//如果通知信息超过指定时间则不执行通知处理。
			long differMillis = common.getCurrentTime().getTime() - notify.getNotifyPlanTime().getTime();
			if (differMillis > this.notifyExpireSeconds * 1000) {
				// 失败记录
				notify.setNotifyState("2");
				notify.setErrorInfo("The notification message has expired! notify expire seconds is " + this.notifyExpireSeconds + "s");
				notify.setRetryLimitNums(0);
				resultList.add(notify);
				break;
			}
			try {
				if (!StringUtils.isBlank(notify.getObjectHandleClass())) {
					ObjectHandleNotify handler = getBean(notify.getObjectHandleClass());
					
					log.info("notifyId[{}] custom handle class is {}", notify.getNotifyId(), notify.getObjectHandleClass());
					// 自定义处理
					resultList.add(handler.handle(notify));
					break;
				}

				// 消息通知模式
				if (NotifyConstants.NOTIFY_MODE_MQ.equals(notify.getNotifyMode())) {
					mqModeHandle(notify, resultList);
				}

				// url通知模式
				else if (NotifyConstants.NOTIFY_MODE_URL.equals(notify.getNotifyMode())) {
					urlModeNums++;
					urlModeHandle(notify, resultList, pool);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				// 失败记录
				notify.setNotifyState("2");
				notify.setErrorInfo(common.getStackTrace(e));
				resultList.add(notify);
			}
		}
		return urlModeNums;
	}

	/**
	 * 消息模式通知
	 * 
	 * @param notify
	 * @param resultList
	 */
	protected void mqModeHandle(TfMNotify notify, List<TfMNotify> resultList) {
		Assert.isTrue(isUrlMode(notify.getNotifyAddr()), "URL is not allowed in mq mode! notifyId:" + notify.getNotifyId());

		// 发布消息
		getPubSubManager().publish(notify.getNotifyContent(), notify.getNotifyAddr());

		// 处理结果
		notify.setNotifyState("1");
		notify.setNotifyFinishTime(common.getCurrentTime());
		resultList.add(notify);
	}

	/**
	 * url模式通知
	 * 
	 * @param notify
	 * @param resultList
	 * @throws InterruptedException 
	 */
	protected void urlModeHandle(final TfMNotify notify, final List<TfMNotify> resultList, ExecutorService pool) throws Exception {
		Assert.isTrue(isUrlMode(notify.getNotifyAddr()), "URL format error! notifyId:" + notify.getNotifyId());

		// 线程池异步执行
		pool.execute(new Runnable() {
			@Override
			public void run() {
				log.info("start notify[{}] addr:{}", notify.getNotifyId(), notify.getNotifyAddr());
				try {
					if(!StringUtils.isBlank(notify.getUrlHandleClass())){//自定义url处理类方式
						URLNotifyHandleAdapter adapter = getBean(notify.getUrlHandleClass());
						
						//自定义处理器开始处理。
						NotifyReq req = new NotifyReq();
						BeanUtils.copyProperties(notify, req);
						NotifyRsp notifyRsp = adapter.handle(req);
						Assert.notNull(notifyRsp, notify.getUrlHandleClass().concat(".handle方法返回为空。"));
						
						//设置返回
						notify.setRspContent(notifyRsp.getRspContent());
						notify.setRspState(notifyRsp.getRspState());
						notify.setRspTime(common.getCurrentTime());
						notify.setNotifyState("1".equals(notifyRsp.getRspState()) ? "1":"2");
					} else {//默认处理方式
						
						// 开始请求
						String responseBody = HttpClientUtil.post(notify.getNotifyAddr(), handleReqParam(notify.getNotifyContent()));
						
						// 反馈类型。0：不等待反馈；1：等待反馈。当为等待时，接收方收到通知后需在通知url中反馈处理结果
						if ("1".equals(notify.getRspType())) {
							notify.setRspContent(responseBody);
							notify.setRspTime(common.getCurrentTime());
							if (isSuccess(responseBody)) {
								notify.setNotifyState("1");
								notify.setRspState("1");
							} else {
								notify.setNotifyState("2");
								notify.setRspState("0");
							}
						} else {
							notify.setNotifyState("1");
						}
					}
					notify.setNotifyFinishTime(common.getCurrentTime());
					resultList.add(notify);
				} catch (Exception e) {
					log.error(e.getMessage(), e);

					// 失败记录
					notify.setNotifyState("2");
					notify.setErrorInfo(common.getStackTrace(e));
					resultList.add(notify);
				}
				log.info("end notify[{}] addr:{}", notify.getNotifyId(), notify.getNotifyAddr());
			}
		});
	}

	/**
	 * 批量更新为处理中
	 * 
	 * @param notifyList
	 */
	protected void lockNotifyList(List<TfMNotify> notifyList) {
		// 批量更新状态
		taskNotifyService.updateNotifyList(notifyList, new BatchEachHandler<TfMNotify>() {
			@Override
			public void onEach(TfMNotify notify) {
				notify.setNotifyId(notify.getNotifyId()).asCondition();
				notify.setNotifyState("9");// 处理中
				notify.setRetryCount(notify.getRetryCount() == 0 ? 1 : notify.getRetryCount() + 1);
				notify.setEnqueueTime(common.getCurrentTime());
			}
		}, false);
	}

	/**
	 * 批量更新通知结果状态
	 * 
	 * @param notifyList
	 */
	protected void updateNotifyResult(List<TfMNotify> resultList) {
		taskNotifyService.updateNotifyList(resultList, new BatchEachHandler<TfMNotify>() {
			@Override
			public void onEach(TfMNotify notify) {
				notify.setNotifyId(notify.getNotifyId()).asCondition();
			}
		}, true);
	}

	/**
	 * 获取处理类
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getBean(String className) {
		Class<T> clazz = null;
		try {
			clazz = (Class<T>)Class.forName(className);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return SpringContextHolder.getBean(clazz);
	}

	/**
	 * 是否为url通知模式
	 * 
	 * @param addr
	 * @return
	 */
	protected boolean isUrlMode(String addr) {
		return addr.toLowerCase().startsWith("http://") || addr.toLowerCase().startsWith("https://");
	}

	/**
	 * 处理请求参数
	 * 
	 * @param notifyContent
	 * @return
	 */
	protected Object handleReqParam(String notifyContent) {
		if (StringUtils.isEmpty(notifyContent)) {
			return notifyContent;
		}
		
		//尝试转为json
		try {
			return JSON.parse(notifyContent);
		} catch (JSONException e) {
		}
		return notifyContent;
	}

	/**
	 * 反馈是否处理成功
	 * 
	 * @param responseBody
	 * @return
	 */
	private boolean isSuccess(String responseBody) {
		try {
			JSONObject json = JSON.parseObject(responseBody);
			return "0".equals(json.getString("rspCode"));
		} catch (Exception e) {
			return "0".equals(StringUtils.trim(responseBody));
		}
	}

	public String getPubSubManagerName() {
		return pubSubManagerName;
	}

	public void setPubSubManagerName(String pubSubManagerName) {
		this.pubSubManagerName = pubSubManagerName;
	}

	public IPubSubManager getPubSubManager() {
		return SpringContextHolder.getBean(getPubSubManagerName(), IPubSubManager.class);
	}

	public TaskNotifyService getTaskNotifyService() {
		return taskNotifyService;
	}

	public void setTaskNotifyService(TaskNotifyService taskNotifyService) {
		this.taskNotifyService = taskNotifyService;
	}

	@SuppressWarnings("unchecked")
	public IQueue<TfMNotify> getTaskQueue() {
		IQueueManager queueManager = null;
		try {
			queueManager = SpringContextHolder.getBean(getQueueManagerName(), IQueueManager.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if(queueManager == null){
			return null;
		}
		return queueManager.getQueue(NOTIFY_TASK_DATA_QUEUE_NAME);
	}

	public int getProcessThread() {
		return processThread;
	}

	public void setProcessThread(int processThread) {
		this.processThread = processThread;
	}

	public int getLoadSize() {
		return loadSize;
	}

	public void setLoadSize(int loadSize) {
		this.loadSize = loadSize;
	}

	public String getQueueManagerName() {
		return queueManagerName;
	}

	public void setQueueManagerName(String queueManagerName) {
		this.queueManagerName = queueManagerName;
	}
	
	public long getNotifyExpireSeconds() {
		return notifyExpireSeconds;
	}

	public void setNotifyExpireSeconds(long notifyExpireSeconds) {
		this.notifyExpireSeconds = notifyExpireSeconds;
	}

	public int getAwaitTerminationTimeoutSeconds() {
		return awaitTerminationTimeoutSeconds;
	}

	public void setAwaitTerminationTimeoutSeconds(int awaitTerminationTimeoutSeconds) {
		this.awaitTerminationTimeoutSeconds = awaitTerminationTimeoutSeconds;
	}
	
	public int getQueueMaxSize() {
		return queueMaxSize;
	}

	public void setQueueMaxSize(int queueMaxSize) {
		this.queueMaxSize = queueMaxSize;
	}
	
}
