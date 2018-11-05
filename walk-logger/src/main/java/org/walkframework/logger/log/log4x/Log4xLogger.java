package org.walkframework.logger.log.log4x;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.walkframework.logger.LoggerType;
import org.walkframework.logger.log.AbstractLogger;

import com.ai.aif.log4x.Log4xClient;
import com.ai.aif.log4x.message.format.Trace;
import com.ai.aif.log4x.message.level.Level;

/**
 * <p>使用log4x实现logger接口</p>
 * <p>使用自定义key实现使用log4x的trace记录各种级别的日志</p>
 * 
 * @author MengQK
 * @version [1.0, 2017年3月28日]
 * @since [2017年3月28日]
 */
public class Log4xLogger extends AbstractLogger {

	/** basic log style, '%time %level %category - %msg\n' */
	private static final String LOG_MSG_FORMATTER = "{} [{}] {} {} - {}\n";
	/** the key in trace data map. */
	private static final String LOG_MSG_KEY = "log4w";

	private static final String LEVEL_ERROR = "ERROR";
	private static final String LEVEL_WARN = "WARN ";
	private static final String LEVEL_INFO = "INFO ";
	private static final String LEVEL_DEBUG = "DEBUG";
	private static final String LEVEL_TRACE = "TRACE";

	private String categoryName;
	// private Class<?> cls;
	private Log4xClient log4x;

	private Level logLevel;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public Log4xLogger(String categoryName) {
		this.categoryName = categoryName;
		log4x = Log4xClient.getInstance();
		setLogLevel();
	}

	public Log4xLogger(Class<?> cls) {
		// this.cls = cls;
		this.categoryName = cls.getName();
		log4x = Log4xClient.getInstance();
		setLogLevel();
	}

	public void setLogLevel() {
		this.logLevel = Log4xConfig.getInstance().getLoggerLevel(this.categoryName);
	}

	@Override
	public String getName() {
		return this.categoryName;
	}

	@Override
	public LoggerType getType() {
		return LoggerType.LOG4X;
	}

	@Override
	public boolean isTraceEnabled() {
		return this.logLevel.toInt() <= Level.TRACE_INT;
	}

	@Override
	public void trace(String msg) {
		if (isTraceEnabled()) {
			addLogInfo(LEVEL_TRACE, msg);
		}
	}

	@Override
	public void trace(String format, Object arg) {
		if (isTraceEnabled()) {
			addLogInfo(LEVEL_TRACE, formatMsg(format, arg));
		}
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		if (isTraceEnabled()) {
			addLogInfo(LEVEL_TRACE, formatMsg(format, arg1, arg2));
		}
	}

	@Override
	public void trace(String format, Object... arguments) {
		if (isTraceEnabled()) {
			addLogInfo(LEVEL_TRACE, formatMsg(format, arguments));
		}
	}

	@Override
	public void trace(String msg, Throwable t) {
		if (isTraceEnabled()) {
			addLogInfo(LEVEL_TRACE, msg, t);
		}
	}

	@Override
	public void trace(Throwable t, String msg, Object arg1) {
		if (isTraceEnabled()) {
			addLogInfo(LEVEL_TRACE, formatMsg(msg, arg1), t);
		}
	}

	@Override
	public void trace(Throwable t, String msg, Object arg1, Object arg2) {
		if (isTraceEnabled()) {
			addLogInfo(LEVEL_TRACE, formatMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void trace(Throwable t, String msg, Object... arguments) {
		if (isTraceEnabled()) {
			addLogInfo(LEVEL_TRACE, formatMsg(msg, arguments), t);
		}
	}

	@Override
	public boolean isDebugEnabled() {
		return this.logLevel.toInt() <= Level.DEBUG_INT;
	}

	@Override
	public void debug(String msg) {
		if (isDebugEnabled()) {
			addLogInfo(LEVEL_DEBUG, msg);
		}

	}

	@Override
	public void debug(String format, Object arg) {
		if (isDebugEnabled()) {
			addLogInfo(LEVEL_DEBUG, formatMsg(format, arg));
		}

	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		if (isDebugEnabled()) {
			addLogInfo(LEVEL_DEBUG, formatMsg(format, arg1, arg2));
		}

	}

	@Override
	public void debug(String format, Object... arguments) {
		if (isDebugEnabled()) {
			addLogInfo(LEVEL_DEBUG, formatMsg(format, arguments));
		}

	}

	@Override
	public void debug(String msg, Throwable t) {
		if (isDebugEnabled()) {
			addLogInfo(LEVEL_DEBUG, msg, t);
		}

	}

	@Override
	public void debug(Throwable t, String msg, Object arg1) {
		if (isDebugEnabled()) {
			addLogInfo(LEVEL_DEBUG, formatMsg(msg, arg1), t);
		}
	}

	@Override
	public void debug(Throwable t, String msg, Object arg1, Object arg2) {
		if (isDebugEnabled()) {
			addLogInfo(LEVEL_DEBUG, formatMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void debug(Throwable t, String msg, Object... arguments) {
		if (isDebugEnabled()) {
			addLogInfo(LEVEL_DEBUG, formatMsg(msg, arguments), t);
		}
	}

	@Override
	public boolean isInfoEnabled() {
		return this.logLevel.toInt() <= Level.INFO_INT;
	}

	@Override
	public void info(String msg) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, msg);
		}

	}

	@Override
	public void info(String format, Object arg) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatMsg(format, arg));
		}
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatMsg(format, arg1, arg2));
		}
	}

	@Override
	public void info(String format, Object... arguments) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatMsg(format, arguments));
		}
	}

	@Override
	public void info(String msg, Throwable t) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, msg, t);
		}
	}

	@Override
	public void info(Throwable t, String msg, Object arg1) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatMsg(msg, arg1), t);
		}
	}

	@Override
	public void info(Throwable t, String msg, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void info(Throwable t, String msg, Object... arguments) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatMsg(msg, arguments), t);
		}
	}

	@Override
	public boolean isWarnEnabled() {
		return this.logLevel.toInt() <= Level.WARN_INT;
	}

	@Override
	public void warn(String msg) {
		if (isWarnEnabled()) {
			addLogInfo(LEVEL_WARN, msg);
		}
	}

	@Override
	public void warn(String format, Object arg) {
		if (isWarnEnabled()) {
			addLogInfo(LEVEL_WARN, formatMsg(format, arg));
		}
	}

	@Override
	public void warn(String format, Object... arguments) {
		if (isWarnEnabled()) {
			addLogInfo(LEVEL_WARN, formatMsg(format, arguments));
		}
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		if (isWarnEnabled()) {
			addLogInfo(LEVEL_WARN, formatMsg(format, arg1, arg2));
		}
	}

	@Override
	public void warn(String msg, Throwable t) {
		if (isWarnEnabled()) {
			addLogInfo(LEVEL_WARN, msg, t);
		}
	}

	@Override
	public void warn(Throwable t, String msg, Object arg1) {
		if (isWarnEnabled()) {
			addLogInfo(LEVEL_WARN, formatMsg(msg, arg1), t);
		}
	}

	@Override
	public void warn(Throwable t, String msg, Object arg1, Object arg2) {
		if (isWarnEnabled()) {
			addLogInfo(LEVEL_WARN, formatMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void warn(Throwable t, String msg, Object... arguments) {
		if (isWarnEnabled()) {
			addLogInfo(LEVEL_WARN, formatMsg(msg, arguments), t);
		}
	}

	@Override
	public boolean isErrorEnabled() {
		return this.logLevel.toInt() <= Level.ERROR_INT;
	}

	@Override
	public void error(String msg) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, msg);
		}
	}

	@Override
	public void error(String format, Object arg) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, formatMsg(format, arg));
		}
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, formatMsg(format, arg1, arg2));
		}
	}

	@Override
	public void error(String format, Object... arguments) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, formatMsg(format, arguments));
		}
	}

	@Override
	public void error(String msg, Throwable t) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, msg, t);
		}
	}

	@Override
	public void error(Throwable t, String msg, Object arg1) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, formatMsg(msg, arg1), t);
		}
	}

	@Override
	public void error(Throwable t, String msg, Object arg1, Object arg2) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, formatMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void error(Throwable t, String msg, Object... arguments) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, formatMsg(msg, arguments), t);
		}
	}

	@Override
	public Object getCurrentTrace() {
		return log4x.getCurrentTrace();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Trace startTrace() {
		return startTrace(null);
	}

	/**
	 * 此处traceId需要为traceContext，用于开启子trace
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Trace startTrace(String traceContext) {
		Trace trace = log4x.getTrace();
		if (isNotEmpty(traceContext)) {
			trace.transToSubTrace(traceContext);
		}
		log4x.startTrace(trace);
		return trace;
	}

	@Override
	public void endTrace() {
		endTrace(true);
	}

	@Override
	public void endTrace(boolean isSuccess) {
		log4x.finishTrace(isSuccess);
	}

	@Override
	public void enterFunction() {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatEnterFuncMsg(""));
		}
	}

	@Override
	public void enterFunction(String msg) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatEnterFuncMsg(msg));
		}
	}

	@Override
	public void enterFunction(String msg, Object arg) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatEnterFuncMsg(msg, arg));
		}
	}

	@Override
	public void enterFunction(String msg, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatEnterFuncMsg(msg, arg1, arg2));
		}
	}

	@Override
	public void enterFunction(String msg, Object... arguments) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatEnterFuncMsg(msg, arguments));
		}
	}

	@Override
	public void endFunction() {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatEndFuncMsg(""));
		}
	}

	@Override
	public void endFunction(String msg) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatEndFuncMsg(msg));
		}
	}

	@Override
	public void endFunction(String msg, Object arg) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatEndFuncMsg(msg, arg));
		}
	}

	@Override
	public void endFunction(String msg, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatEndFuncMsg(msg, arg1, arg2));
		}
	}

	@Override
	public void endFunction(String msg, Object... arguments) {
		if (isInfoEnabled()) {
			addLogInfo(LEVEL_INFO, formatEndFuncMsg(msg, arguments));
		}
	}

	@Override
	public void endFunction(Throwable t, String msg) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, formatEndFuncMsg(msg), t);
		}
	}

	@Override
	public void endFunction(Throwable t, String msg, Object arg) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, formatEndFuncMsg(msg, arg), t);
		}
	}

	@Override
	public void endFunction(Throwable t, String msg, Object arg1, Object arg2) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, formatEndFuncMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void endFunction(Throwable t, String msg, Object... arguments) {
		if (isErrorEnabled()) {
			addLogInfo(LEVEL_ERROR, formatEndFuncMsg(msg, arguments), t);
		}
	}

	public String getTimestamp() {
		return sdf.format(new Date());
	}

	/**
	 * <p>增加格式化的日志信息</p>
	 * 
	 * @param lvlStr
	 * @param msg
	 * @author MengQK
	 * @since [2017年4月7日]
	 */
	public void addLogInfo(String lvlStr, String msg) {
		addLogInfo(lvlStr, msg, null);
	}

	/**
	 * <p>增加格式化的日志信息</p>
	 * 
	 * @param lvlStr
	 * @param msg
	 * @param t
	 * @author MengQK
	 * @since [2017年4月7日]
	 */
	public void addLogInfo(String lvlStr, String msg, Throwable t) {
		String fnMsg = formatMsg(LOG_MSG_FORMATTER, getTimestamp(), Thread.currentThread().getName(), lvlStr, this.categoryName,
				msg);
		Trace trace = (Trace) getCurrentTrace();
		if (null != t) {
			trace.addException(t);
			fnMsg += getThrowablePrinting(t);
		}
		trace.getDataMap().put(LOG_MSG_KEY,
				trace.getDataMap().containsKey(LOG_MSG_KEY) ? trace.getDataMap().get(LOG_MSG_KEY) + fnMsg : fnMsg);
	}

	public String getThrowablePrinting(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			t.printStackTrace(pw);
			pw.flush();
		} catch (RuntimeException ignored) {
		} finally {
			pw.close();
		}
		return sw.toString();
	}
}
