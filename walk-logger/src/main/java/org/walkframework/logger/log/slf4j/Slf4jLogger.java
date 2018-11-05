package org.walkframework.logger.log.slf4j;

import java.util.UUID;

import org.slf4j.MDC;
import org.walkframework.logger.LoggerType;
import org.walkframework.logger.log.AbstractLogger;

/**
 * <p>使用本地logger实现logger接口</p>
 * <p>使用继承于org.slf4j.logger的实现类来实现日志打印，如log4j, logback</p>
 * 
 * @author MengQK
 * @version [1.0, 2017年3月28日]
 * @since [2017年3月28日]
 */
public class Slf4jLogger extends AbstractLogger {

	public static final String TRACE_KEY = "LOG4W_TRACE_ID";
	public static final String TRACE_PREFIX = " - trace=";

	private org.slf4j.Logger log;
	
	public Slf4jLogger(org.slf4j.Logger log) {
		this.log = log;
	}

	@Override
	public String getName() {
		return this.log.getName();
	}

	@Override
	public LoggerType getType() {
		return LoggerType.SLF4J;
	}

	@Override
	public boolean isTraceEnabled() {
		return this.log.isTraceEnabled();
	}

	@Override
	public void trace(String msg) {
		if (isTraceEnabled()) {
			this.log.trace(msg);
		}
	}

	@Override
	public void trace(String format, Object arg) {
		if (isTraceEnabled()) {
			this.log.trace(format, arg);
		}
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		if (isTraceEnabled()) {
			this.log.trace(format, arg1, arg2);
		}
	}

	@Override
	public void trace(String format, Object... arguments) {
		if (isTraceEnabled()) {
			this.log.trace(format, arguments);
		}
	}

	@Override
	public void trace(String msg, Throwable t) {
		if (isTraceEnabled()) {
			this.log.trace(msg, t);
		}
	}

	@Override
	public void trace(Throwable t, String msg, Object arg1) {
		if (isTraceEnabled()) {
			this.log.trace(formatMsg(msg, arg1), t);
		}
	}

	@Override
	public void trace(Throwable t, String msg, Object arg1, Object arg2) {
		if (isTraceEnabled()) {
			this.log.trace(formatMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void trace(Throwable t, String msg, Object... arguments) {
		if (isTraceEnabled()) {
			this.log.trace(formatMsg(msg, arguments), t);
		}
	}

	@Override
	public boolean isDebugEnabled() {
		return this.log.isDebugEnabled();
	}

	@Override
	public void debug(String msg) {
		if (isDebugEnabled()) {
			this.log.debug(msg);
		}
	}

	@Override
	public void debug(String format, Object arg) {
		if (isDebugEnabled()) {
			this.log.debug(format, arg);
		}
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		if (isDebugEnabled()) {
			this.log.debug(format, arg1, arg2);
		}
	}

	@Override
	public void debug(String format, Object... arguments) {
		if (isDebugEnabled()) {
			this.log.debug(format, arguments);
		}
	}

	@Override
	public void debug(String msg, Throwable t) {
		if (isDebugEnabled()) {
			this.log.debug(msg, t);
		}
	}

	@Override
	public void debug(Throwable t, String msg, Object arg1) {
		if (isDebugEnabled()) {
			this.log.debug(formatMsg(msg, arg1), t);
		}
	}

	@Override
	public void debug(Throwable t, String msg, Object arg1, Object arg2) {
		if (isDebugEnabled()) {
			this.log.debug(formatMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void debug(Throwable t, String msg, Object... arguments) {
		if (isDebugEnabled()) {
			this.log.debug(formatMsg(msg, arguments), t);
		}
	}

	@Override
	public boolean isInfoEnabled() {
		return this.log.isInfoEnabled();
	}

	@Override
	public void info(String msg) {
		if (isInfoEnabled()) {
			this.log.info(msg);
		}
	}

	@Override
	public void info(String format, Object arg) {
		if (isInfoEnabled()) {
			this.log.info(format, arg);
		}
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			this.log.info(format, arg1, arg2);
		}
	}

	@Override
	public void info(String format, Object... arguments) {
		if (isInfoEnabled()) {
			this.log.info(format, arguments);
		}
	}

	@Override
	public void info(String msg, Throwable t) {
		if (isInfoEnabled()) {
			this.log.info(msg, t);
		}
	}

	@Override
	public void info(Throwable t, String msg, Object arg1) {
		if (isInfoEnabled()) {
			this.log.info(formatMsg(msg, arg1), t);
		}
	}

	@Override
	public void info(Throwable t, String msg, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			this.log.info(formatMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void info(Throwable t, String msg, Object... arguments) {
		if (isInfoEnabled()) {
			this.log.info(formatMsg(msg, arguments), t);
		}
	}

	@Override
	public boolean isWarnEnabled() {
		return this.log.isWarnEnabled();
	}

	@Override
	public void warn(String msg) {
		if (isWarnEnabled()) {
			this.log.warn(msg);
		}
	}

	@Override
	public void warn(String format, Object arg) {
		if (isWarnEnabled()) {
			this.log.warn(format, arg);
		}
	}

	@Override
	public void warn(String format, Object... arguments) {
		if (isWarnEnabled()) {
			this.log.warn(format, arguments);
		}
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		if (isWarnEnabled()) {
			this.log.warn(format, arg1, arg2);
		}
	}

	@Override
	public void warn(String msg, Throwable t) {
		if (isWarnEnabled()) {
			this.log.warn(msg, t);
		}
	}

	@Override
	public void warn(Throwable t, String msg, Object arg1) {
		if (isWarnEnabled()) {
			this.log.warn(formatMsg(msg, arg1), t);
		}
	}

	@Override
	public void warn(Throwable t, String msg, Object arg1, Object arg2) {
		if (isWarnEnabled()) {
			this.log.warn(formatMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void warn(Throwable t, String msg, Object... arguments) {
		if (isWarnEnabled()) {
			this.log.warn(formatMsg(msg, arguments), t);
		}
	}

	@Override
	public boolean isErrorEnabled() {
		return this.log.isErrorEnabled();
	}

	@Override
	public void error(String msg) {
		if (isErrorEnabled()) {
			this.log.error(msg);
		}
	}

	@Override
	public void error(String format, Object arg) {
		if (isErrorEnabled()) {
			this.log.error(format, arg);
		}
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		if (isErrorEnabled()) {
			this.log.error(format, arg1, arg2);
		}
	}

	@Override
	public void error(String format, Object... arguments) {
		if (isErrorEnabled()) {
			this.log.error(format, arguments);
		}
	}

	@Override
	public void error(String msg, Throwable t) {
		if (isErrorEnabled()) {
			this.log.error(msg, t);
		}
	}

	@Override
	public void error(Throwable t, String msg, Object arg1) {
		if (isErrorEnabled()) {
			this.log.error(formatMsg(msg, arg1), t);
		}
	}

	@Override
	public void error(Throwable t, String msg, Object arg1, Object arg2) {
		if (isErrorEnabled()) {
			this.log.error(formatMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void error(Throwable t, String msg, Object... arguments) {
		if (isErrorEnabled()) {
			this.log.error(formatMsg(msg, arguments), t);
		}
	}

	@Override
	public Object getCurrentTrace() {
		String trace = MDC.get(TRACE_KEY);
		int tracePrefix = TRACE_PREFIX.length();
		if (null != trace && trace.length() > tracePrefix) {
			return trace.substring(tracePrefix);
		}
		return trace;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String startTrace() {
		return startTrace(generateUUID());
	}

	private String generateUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	@SuppressWarnings("unchecked")
	@Override
	public String startTrace(String traceId) {
		MDC.put(TRACE_KEY, TRACE_PREFIX + traceId);
		info("[TraceStart][{}]", traceId);
		return traceId;
	}

	@Override
	public void endTrace() {
		endTrace(true);
	}

	@Override
	public void endTrace(boolean isSuccess) {
		if (isSuccess) {
			this.log.info("[TraceEnd][{}] with result {}", getCurrentTrace(), isSuccess);
		} else {
			this.log.warn("[TraceEnd][{}] with result {}", getCurrentTrace(), isSuccess);
		}
		MDC.remove(TRACE_KEY);
	}

	@Override
	public void enterFunction() {
		if (isInfoEnabled()) {
			this.log.info(formatEnterFuncMsg(""));
		}
	}

	@Override
	public void enterFunction(String msg) {
		if (isInfoEnabled()) {
			this.log.info(formatEnterFuncMsg(msg));
		}
	}

	@Override
	public void enterFunction(String msg, Object arg) {
		if (isInfoEnabled()) {
			this.log.info(formatEnterFuncMsg(msg), arg);
		}
	}

	@Override
	public void enterFunction(String msg, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			this.log.info(formatEnterFuncMsg(msg), arg1, arg2);
		}
	}

	@Override
	public void enterFunction(String msg, Object... arguments) {
		if (isInfoEnabled()) {
			this.log.info(formatEnterFuncMsg(msg), arguments);
		}
	}

	@Override
	public void endFunction() {
		if (isInfoEnabled()) {
			this.log.info(formatEndFuncMsg(""));
		}
	}

	@Override
	public void endFunction(String msg) {
		if (isInfoEnabled()) {
			this.log.info(formatEndFuncMsg(msg));
		}
	}

	@Override
	public void endFunction(String msg, Object arg) {
		if (isInfoEnabled()) {
			this.log.info(formatEndFuncMsg(msg), arg);
		}
	}

	@Override
	public void endFunction(String msg, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			this.log.info(formatEndFuncMsg(msg), arg1, arg2);
		}
	}

	@Override
	public void endFunction(String msg, Object... arguments) {
		if (isInfoEnabled()) {
			this.log.info(formatEndFuncMsg(msg), arguments);
		}
	}

	@Override
	public void endFunction(Throwable t, String msg) {
		if (isErrorEnabled()) {
			this.log.error(formatEndFuncMsg(msg), t);
		}
	}

	@Override
	public void endFunction(Throwable t, String msg, Object arg) {
		if (isErrorEnabled()) {
			this.log.error(formatEndFuncMsg(msg, arg), t);
		}
	}

	@Override
	public void endFunction(Throwable t, String msg, Object arg1, Object arg2) {
		if (isErrorEnabled()) {
			this.log.error(formatEndFuncMsg(msg, arg1, arg2), t);
		}
	}

	@Override
	public void endFunction(Throwable t, String msg, Object... arguments) {
		if (isErrorEnabled()) {
			this.log.error(formatEndFuncMsg(msg, arguments), t);
		}
	}

}
