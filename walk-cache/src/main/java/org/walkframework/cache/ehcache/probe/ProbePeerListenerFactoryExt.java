package org.walkframework.cache.ehcache.probe;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerListener;
import net.sf.ehcache.util.PropertyUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ehcachedx.monitor.probe.ProbePeerListenerFactory;
import org.terracotta.ehcachedx.monitor.util.StringUtils;

/**
 * ehcache缓存监控
 * 
 * @author shf675
 *
 */
public class ProbePeerListenerFactoryExt extends ProbePeerListenerFactory {
	protected Logger log = LoggerFactory.getLogger(this.getClass());

	private static CacheManager currCacheManager;
	private static Properties currProperties;
	private static CacheManagerPeerListener currListener;

	@SuppressWarnings("static-access")
	public CacheManagerPeerListener createCachePeerListener(CacheManager cacheManager, Properties properties) {
		this.currCacheManager = cacheManager;
		this.currProperties = properties;
		if (this.currListener == null) {
			startTimer(properties);
		}
		this.currListener = super.createCachePeerListener(cacheManager, properties);
		return this.currListener;
	}

	/**
	 * 开始定时
	 * 
	 * @param properties
	 */
	public void startTimer(Properties properties) {
		try {
			Integer hour = StringUtils.convertToInt(PropertyUtil.extractAndLogProperty("hour", properties));
			Integer minute = StringUtils.convertToInt(PropertyUtil.extractAndLogProperty("minute", properties));
			Integer period = StringUtils.convertToInt(PropertyUtil.extractAndLogProperty("period", properties));
			hour = hour == null ? new Integer(2) : hour;// 默认2点执行
			minute = minute == null ? new Integer(0) : minute;// 默认0分执行
			period = period == null ? new Integer(24 * 60 * 60 * 1000) : period;// 默认执行周期为1天
			Date frstTime = getFirstTime(hour, minute);
			log.info("hour: {}", hour);
			log.info("minute: {}", minute);
			log.info("period: {}ms", period);
			log.info("frstTime: {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(frstTime));
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					restartCacheMonitor();
				}
			}, frstTime, period);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 重启
	 */
	public void restartCacheMonitor() {
		if (currListener != null) {
			currListener.dispose();//停服务
			createCachePeerListener(currCacheManager, currProperties).init();
		} else {
			log.error("CacheMonitor reload failure！");
			throw new RuntimeException("Ehcache is not initialized or not configured cacheMonitor, please check the ehcache.xml file.");
		}
		log.info("CacheMonitor reload success！");
	}

	/**
	 * 获取首次执行事件
	 * @param hour
	 * @param minute
	 * @return
	 */
	private static Date getFirstTime(int hour, int minute) {
		Date currDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(currDate);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		if (cal.getTime().before(currDate)) {
			cal.add(Calendar.DATE, 1);
		}
		return cal.getTime();
	}
}
