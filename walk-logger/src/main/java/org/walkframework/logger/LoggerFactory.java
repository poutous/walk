/**
 * 
 */
package org.walkframework.logger;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.walkframework.logger.log.AbstractLogger;
import org.walkframework.logger.log.DelegatingLogger;
import org.walkframework.logger.log.log4x.Log4xLogger;
import org.walkframework.logger.log.slf4j.Slf4jLogger;

/**
 * <p>日志实现类的生产工厂</p>
 * <p>区分slf4j和log4x，分别提供logger实现本地打印和远程trace能力</p>
 * 
 * @author MengQK
 * @version [1.0, 2017年3月28日]
 * @since [2017年3月28日]
 */
public class LoggerFactory {

	public static final String LOGGER_CHOOSER = "log4walker.loggerType";
	static final int HAS_INITILIZED = 1;
	static final int NOT_INITILIZED = 0;

	static int INITILIZE_STATE = NOT_INITILIZED;
	static LoggerType loggerType = LoggerType.SLF4J;
	static Map<String, Logger> logContext = new ConcurrentHashMap<String, Logger>();

	/**
	 * <p>提供给外部调用进行初始化的日志创建类型的能力，默认使用performInitOperation获取log4walker.loggerType进行初始化</p>
	 * 
	 * @param loggerTypeStr
	 * @author MengQK
	 * @since [2017年4月1日]
	 */
	public static void setLoggerType(String loggerTypeStr) {
		realSetOperation(loggerTypeStr);
	}

	/**
	 * <p>初始化日志打印工具</p>
	 * 
	 * @param cls
	 * @return
	 * @author  MengQK
	 * @since  [2017年4月1日]
	 */
	public static Logger getLogger(Class<?> cls) {
		performInitOperation();
		return doCreateLogger(cls.getName());
	}

	/**
	 * <p>初始化日志打印工具</p>
	 * 
	 * @param categoryName
	 * @return
	 * @author  MengQK
	 * @since  [2017年4月1日]
	 */
	public static Logger getLogger(String categoryName) {
		performInitOperation();
		return doCreateLogger(categoryName);
	}
	
	/**
	 * <p>获取日志上下文</p>
	 * 
	 * @return
	 * @author  MengQK
	 * @since  [2017年4月7日]
	 */
	public static Map<String, Logger> getLogContext() {
		return logContext;
	}
	
	/**
	 * <p>获取日志类型，目前有slf4j, log4x等</p>
	 * 
	 * @return
	 * @author  MengQK
	 * @since  [2017年4月7日]
	 */
	public static LoggerType getLoggerType() {
		return loggerType;
	}

	private static Logger doCreateLogger(String categoryName) {
		Logger existed = getExisted(categoryName);
		if (null != existed) {
			return existed;
		}
		DelegatingLogger logger = new DelegatingLogger(createLoggerByType(categoryName).setDelegating(true));
		logContext.put(categoryName, logger);
		return logger;
	}
	
	private static AbstractLogger createLoggerByType(String categoryName) {
		switch (loggerType) {
		case LOG4X:
			return new Log4xLogger(categoryName);
		default:
			return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(categoryName));
		}
	}
	
	private static Logger getExisted(String categoryName) {
		return logContext.containsKey(categoryName) ? logContext.get(categoryName) : null;
	}

	private static void performInitOperation() {
		if (INITILIZE_STATE == HAS_INITILIZED) {
			return;
		}
		String loggerTypeStr = System.getProperty(LOGGER_CHOOSER);
		realSetOperation(loggerTypeStr);
	}

	private static void realSetOperation(String loggerTypeStr) {
		if (null == loggerTypeStr || loggerTypeStr.trim().length() == 0) {
			INITILIZE_STATE = HAS_INITILIZED;
			return;
		}

		try {
			LoggerType loggerType = LoggerType.valueOf(loggerTypeStr);
			if (loggerType != LoggerFactory.loggerType) {
				LoggerFactory.loggerType = loggerType;
				changeLoggerType4All();
			}
		} catch (Exception e) {
		}
		INITILIZE_STATE = HAS_INITILIZED;
	}

	private static synchronized void changeLoggerType4All() {
		if (logContext.isEmpty()) {
			return;
		}
		DelegatingLogger logger;
		String category;
		for (Entry<String, Logger> entry : logContext.entrySet()) {
			if (!(entry.getValue() instanceof DelegatingLogger)) {
				break;
			}
			logger = (DelegatingLogger) entry.getValue();
			category = entry.getKey();
			
			logger.setLogger(createLoggerByType(category).setDelegating(true));
		}
	}
}
