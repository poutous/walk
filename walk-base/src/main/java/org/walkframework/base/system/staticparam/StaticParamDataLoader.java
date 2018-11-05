package org.walkframework.base.system.staticparam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.CollectionUtils;
import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;
import org.walkframework.cache.redis.RedisCacheDecorator;
import org.walkframework.cache.util.ReflectHelper;

/**
 * 静态参数缓存加载器
 * 
 * 如果是在redis集群环境中，可保证集群中只有一个节点加载缓存，避免多点加载问题
 * 
 * @author shf675
 * 
 */
public class StaticParamDataLoader implements BeanNameAware, SmartLifecycle {

	private final static Logger log = LoggerFactory.getLogger(StaticParamDataLoader.class);

	/**
	 * 静态参数加载器bean缓存名称
	 */
	private static final String STATIC_PARAM_LOADER_BEAN_CACHE_NAME = "___STATIC_PARAM_LOADER_BEAN";

	/**
	 * 缓存管理器
	 */
	private ICacheManager cacheManager;

	/**
	 * 是否每次启动时都加载。如果为true：则每次重新启动时都会强制重新加载。如果为false：工程启动时，当发现本加载器已在之前加载过则放弃重新加载。
	 */
	private boolean forceLoad;

	/**
	 * 是否异步加载。true：开另外的线程进行加载，不影响工程启动速度
	 */
	private boolean asynLoad;

	/**
	 * 延迟加载秒数，默认为30秒
	 */
	private long delaySeconds = 30;

	/**
	 * 是否启用自动刷新
	 */
	private boolean autoRefresh;

	/**
	 * 自动刷新周期秒数，默认8小时
	 */
	private long autoRefreshSeconds = 28800;

	/**
	 * 静态参数列表
	 */
	private List<StaticParamLoadManager> staticParams;

	private Map<String, StaticParamLoadManager> staticParamsCaches = new HashMap<String, StaticParamLoadManager>();

	/**
	 * 当前beanId
	 */
	private String beanId;

	@Override
	public void setBeanName(String name) {
		this.beanId = name;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void start() {
		if (isAutoRefresh()) {
			//1、延迟加载，避免工程启动被阻断
			//2、设置自动刷新周期
			new Timer().schedule(new TimerTask() {
				public void run() {
					load();
				}
			}, getDelaySeconds() * 1000, getAutoRefreshSeconds() * 1000);
		} else {
			//1、延迟加载，避免工程启动被阻断
			new Timer().schedule(new TimerTask() {
				public void run() {
					load();
				}
			}, getDelaySeconds() * 1000);
		}
	}

	/**
	 * 加载静态参数表数据
	 * 
	 * init-method指定
	 * 
	 */
	public void load() {
		try {
			if (CollectionUtils.isEmpty(getStaticParams())) {
				log.warn("property staticParams not configured, No parameters are loaded...");
				return;
			}

			// 是否加载分别有如下条件：
			// 1、设置每次启动强制加载时进行加载
			// 2、未加载过进行加载
			if (isForceLoad() || (!isForceLoad() && !hasLoad())) {
				if (isAsynLoad()) {
					// 开启新的线程进行加载
					asynLoad();
				} else {
					startLoad();
				}
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 异步加载
	 * 
	 */
	public void asynLoad() {
		// 开启新的线程进行加载
		new Thread() {
			@Override
			public void run() {
				try {
					startLoad();
				} catch (Exception e) {
					log.error("load error", e);
				}
			}
		}.start();
	}

	/**
	 * 开始加载
	 */
	public void startLoad() {
		for (StaticParamLoadManager staticParamLoadManager : getStaticParams()) {
			//如果静态参数加载管理器未设置缓存管理器，则默认使用加载器的缓存管理器
			if (staticParamLoadManager.getCacheManager() == null) {
				staticParamLoadManager.setCacheManager(getCacheManager());
			}
			staticParamsCaches.put(staticParamLoadManager.getBeanId(), staticParamLoadManager);

			//开始加载
			staticParamLoadManager.loadAllByFile();
		}

		// 设置已经加载过
		hasLoad();
	}

	/**
	 * 是否已加载过
	 * 
	 * 当缓存使用redis时，使用分布式锁，即使用redis时本执行方法才有意义
	 * 
	 * @return
	 */
	@SuppressWarnings( { "unchecked" })
	public boolean hasLoad() {
		final ICache lockCache = getLockCache();
		// 当缓存使用redis时，使用分布式锁判断是否已加载
		if (lockCache instanceof RedisCacheDecorator) {
			final RedisOperations redisOperations = (RedisOperations) lockCache.getNativeCache();
			return (Boolean) redisOperations.execute(new RedisCallback<Boolean>() {
				public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
					String lockKey = getLockKey();
					String lockValue = UUID.randomUUID().toString();
					byte[] keyBytes = (byte[]) ReflectHelper.invokeMethod(lockCache, lockCache.getClass(), "computeKey", new Object[] { lockKey }, new Class[] { Object.class });
					boolean successSet = connection.setNX(keyBytes, redisOperations.getValueSerializer().serialize(lockValue));
					// 如果设置成功并且启用了自动刷新，设置过期时间
					if (successSet && isAutoRefresh()) {
						connection.expire(keyBytes, getAutoRefreshSeconds() - 10);
					}
					return !successSet;
				}
			});
		}
		return false;
	}

	/**
	 * 获取锁的cache对象
	 * 
	 * @return
	 */
	private ICache getLockCache() {
		return getCacheManager().getICache(STATIC_PARAM_LOADER_BEAN_CACHE_NAME);
	}

	/**
	 * 获取锁的key值
	 * 
	 * @return
	 */
	public String getLockKey() {
		return "lock:" + getBeanId();
	}

	public String getBeanId() {
		return beanId;
	}

	public boolean isForceLoad() {
		return forceLoad;
	}

	public void setForceLoad(boolean forceLoad) {
		this.forceLoad = forceLoad;
	}

	public boolean isAsynLoad() {
		return asynLoad;
	}

	public void setAsynLoad(boolean asynLoad) {
		this.asynLoad = asynLoad;
	}

	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}

	public long getAutoRefreshSeconds() {
		return autoRefreshSeconds;
	}

	public void setAutoRefreshSeconds(long autoRefreshSeconds) {
		this.autoRefreshSeconds = autoRefreshSeconds;
	}

	public long getDelaySeconds() {
		return delaySeconds;
	}

	public void setDelaySeconds(long delaySeconds) {
		this.delaySeconds = delaySeconds;
	}

	public ICacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public List<StaticParamLoadManager> getStaticParams() {
		return staticParams;
	}

	public void setStaticParams(List<StaticParamLoadManager> staticParams) {
		this.staticParams = staticParams;
	}

	public Map<String, StaticParamLoadManager> getStaticParamsCaches() {
		return staticParamsCaches;
	}

	@Override
	public void stop(Runnable callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop() {

	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

}
