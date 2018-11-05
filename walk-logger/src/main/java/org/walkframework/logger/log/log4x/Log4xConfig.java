/**
 * 
 */
package org.walkframework.logger.log.log4x;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.walkframework.logger.Logger;
import org.walkframework.logger.LoggerFactory;
import org.walkframework.logger.LoggerType;

import com.ai.aif.log4x.message.level.Level;

/**
 * <p>log4x 配置监听</p>
 * 
 * @author MengQK
 * @version [1.0, 2017年4月5日]
 * @since [2017年4月5日]
 */
public class Log4xConfig implements Runnable {
	private static String configFile = "log4x.properties";
	private static final String configLevelKey = "log4w.logger.";
	private static Object LOCK = new Object();
	private static Log4xConfig instance;

	/** 日志是否打开 */
	private boolean logEnabled = false;
	/** 日志根级别 */
	private Level rootLevel = Level.OFF;
	/** 日志配置文件监控频率 */
	private long scanPeriod = 0;
	/** 日志级别配置 */
	private Map<String, Level> configLevel = new ConcurrentHashMap<String, Level>();
	private Properties props = new Properties();
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	private Log4xConfig() {
		run();
	}

	private void loadConfigLevel() {
		String value;
		for (String key : props.stringPropertyNames()) {
			if (!key.startsWith(configLevelKey)) {
				continue;
			}
			value = props.getProperty(key);
			configLevel.put(key.substring(configLevelKey.length()), Level.toLevel(value, Level.ERROR));
		}
	}

	/**
	 * <p>加载配置文件，默认log4x.properties</p>
	 * 
	 * @return true when file found and property change happens. false otherwise
	 * @author  MengQK
	 * @since  [2017年4月7日]
	 */
	private boolean loadFromPropertyFile() {
		InputStream inStream = null;
		Properties propsNew = new Properties();
		try {
			inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);

			if (inStream == null) {
				inStream = Log4xConfig.class.getResourceAsStream(configFile);
			}
			if (inStream != null) {
				propsNew.load(inStream);
				return diff(propsNew);
			}
		} catch (IOException e) {
			System.err.println("Error loading log4x config file " + configFile + ", file not found!");
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (Exception e) {
				}
			}
		}
		return false;
	}

	private boolean diff(Properties propsNew) {
		for (Entry<Object, Object> entry : propsNew.entrySet()) {
			if (!entry.getValue().equals(props.get(entry.getKey()))) {
				this.props = propsNew;
				return true;
			}
		}
		return false;
	}

	public static Log4xConfig getInstance() {
		if (null == instance) {
			synchronized (LOCK) {
				if (null == instance) {
					instance = new Log4xConfig();
				}
			}
		}
		return instance;
	}

	public boolean getBoolean(String configVal) {
		return "true".equalsIgnoreCase(configVal);
	}

	public long getLong(String configVal) {
		try {
			return Long.parseLong(configVal);
		} catch (NumberFormatException e) {
			return 0L;
		}
	}

	@Override
	public void run() {
		// first load when scanPeriod == -1, so that once force loading needed.
		// when not first time, reload only when use log4x as logger, branch occurs after a hot switch.
		if (scanPeriod == -1 || LoggerFactory.getLoggerType() == LoggerType.LOG4X) {
			if (loadFromPropertyFile()) {
				logEnabled = getBoolean(props.getProperty("log.enable", "false"));
				rootLevel = Level.toLevel(props.getProperty(configLevelKey + Logger.ROOT_LOGGER_NAME, "ERROR"));
				scanPeriod = getLong(props.getProperty("log4w.scanPeriod", "0"));
				scanPeriod = scanPeriod < 0 ? 0 : scanPeriod; // make me not negative
				if (logEnabled) {
					loadConfigLevel();
				}
			}
		}
		if (scanPeriod > 0) {
			// TODO delete this sysout.
			System.out.println("scan after " + scanPeriod + " second(s).");
			executor.schedule(this, scanPeriod, TimeUnit.SECONDS);
		}
	}

	/**
	 * @return the logEnabled
	 */
	public boolean isLogEnabled() {
		return logEnabled;
	}

	/**
	 * @param logEnabled the logEnabled to set
	 */
	public void setLogEnabled(boolean logEnabled) {
		this.logEnabled = logEnabled;
	}

	/**
	 * @return the rootLevel
	 */
	public Level getRootLevel() {
		return rootLevel;
	}

	/**
	 * @param rootLevel the rootLevel to set
	 */
	public void setRootLevel(Level rootLevel) {
		this.rootLevel = rootLevel;
	}

	/**
	 * @return the configLevel
	 */
	public Map<String, Level> getConfigLevel() {
		return configLevel;
	}

	/**
	 * @param configLevel the configLevel to set
	 */
	public void setConfigLevel(Map<String, Level> configLevel) {
		this.configLevel = configLevel;
	}

	public Level getLoggerLevel(String categoryName) {
		if (!this.logEnabled) {
			return Level.OFF;
		}
		int dotLocation = categoryName.length();
		do {
			categoryName = categoryName.substring(0, dotLocation);
			if (configLevel.containsKey(categoryName)) {
				return configLevel.get(categoryName);
			}
		} while ((dotLocation = categoryName.lastIndexOf('.')) != -1);
		return this.rootLevel;
	}
}
