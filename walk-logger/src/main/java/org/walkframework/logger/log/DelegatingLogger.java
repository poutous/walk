package org.walkframework.logger.log;

import org.walkframework.logger.Logger;
import org.walkframework.logger.LoggerType;

/**
 * <p>代理日志打印服务，用于服务的热切换</p>
 * 
 * @author MengQK
 * @version [1.0, 2017年4月5日]
 * @since [2017年4月5日]
 */
public class DelegatingLogger implements Logger {

	private Logger log;

	public DelegatingLogger(Logger log) {
		this.log = log;
	}

	public void setLogger(Logger log) {
		if (null != log) {
			this.log.info("start change logger {} to new logger {}", this.log, log);
		}
		this.log = log;
	}

	@Override
	public String getName() {
		return this.log.getName();
	}

	@Override
	public LoggerType getType() {
		return this.log.getType();
	}

	@Override
	public boolean isTraceEnabled() {
		return this.log.isTraceEnabled();
	}

	@Override
	public void trace(String msg) {
		this.log.trace(msg);
	}

	@Override
	public void trace(String format, Object arg) {
		this.log.trace(format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		this.log.trace(format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object... arguments) {
		this.log.trace(format, arguments);
	}

	@Override
	public void trace(String msg, Throwable t) {
		this.log.trace(msg, t);
	}

	@Override
	public void trace(Throwable t, String msg, Object arg1) {
		this.log.trace(t, msg, arg1);
	}

	@Override
	public void trace(Throwable t, String msg, Object arg1, Object arg2) {
		this.log.trace(t, msg, arg1, arg2);
	}

	@Override
	public void trace(Throwable t, String msg, Object... arguments) {
		this.log.trace(t, msg, arguments);
	}

	@Override
	public boolean isDebugEnabled() {
		return this.log.isDebugEnabled();
	}

	@Override
	public void debug(String msg) {
		this.log.debug(msg);
	}

	@Override
	public void debug(String format, Object arg) {
		this.log.debug(format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		this.log.debug(format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object... arguments) {
		this.log.debug(format, arguments);
	}

	@Override
	public void debug(String msg, Throwable t) {
		this.log.debug(msg, t);
	}

	@Override
	public void debug(Throwable t, String msg, Object arg1) {
		this.log.debug(t, msg, arg1);
	}

	@Override
	public void debug(Throwable t, String msg, Object arg1, Object arg2) {
		this.log.debug(t, msg, arg1, arg2);
	}

	@Override
	public void debug(Throwable t, String msg, Object... arguments) {
		this.log.debug(t, msg, arguments);
	}

	@Override
	public boolean isInfoEnabled() {
		return this.log.isInfoEnabled();
	}

	@Override
	public void info(String msg) {
		this.log.info(msg);
	}

	@Override
	public void info(String format, Object arg) {
		this.log.info(format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		this.log.info(format, arg1, arg2);
	}

	@Override
	public void info(String format, Object... arguments) {
		this.log.info(format, arguments);
	}

	@Override
	public void info(String msg, Throwable t) {
		this.log.info(msg, t);
	}

	@Override
	public void info(Throwable t, String msg, Object arg1) {
		this.log.info(t, msg, arg1);
	}

	@Override
	public void info(Throwable t, String msg, Object arg1, Object arg2) {
		this.log.info(t, msg, arg1, arg2);
	}

	@Override
	public void info(Throwable t, String msg, Object... arguments) {
		this.log.info(t, msg, arguments);
	}

	@Override
	public boolean isWarnEnabled() {
		return this.log.isWarnEnabled();
	}

	@Override
	public void warn(String msg) {
		this.log.warn(msg);
	}

	@Override
	public void warn(String format, Object arg) {
		this.log.warn(format, arg);
	}

	@Override
	public void warn(String format, Object... arguments) {
		this.log.warn(format, arguments);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		this.log.warn(format, arg1, arg2);
	}

	@Override
	public void warn(String msg, Throwable t) {
		this.log.warn(msg, t);
	}

	@Override
	public void warn(Throwable t, String msg, Object arg1) {
		this.log.warn(t, msg, arg1);
	}

	@Override
	public void warn(Throwable t, String msg, Object arg1, Object arg2) {
		this.log.warn(t, msg, arg1, arg2);
	}

	@Override
	public void warn(Throwable t, String msg, Object... arguments) {
		this.log.warn(t, msg, arguments);
	}

	@Override
	public boolean isErrorEnabled() {
		return this.log.isErrorEnabled();
	}

	@Override
	public void error(String msg) {
		this.log.error(msg);
	}

	@Override
	public void error(String format, Object arg) {
		this.log.error(format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		this.log.error(format, arg1, arg2);
	}

	@Override
	public void error(String format, Object... arguments) {
		this.log.error(format, arguments);
	}

	@Override
	public void error(String msg, Throwable t) {
		this.log.error(msg, t);
	}

	@Override
	public void error(Throwable t, String msg, Object arg1) {
		this.log.error(t, msg, arg1);
	}

	@Override
	public void error(Throwable t, String msg, Object arg1, Object arg2) {
		this.log.error(t, msg, arg1, arg2);
	}

	@Override
	public void error(Throwable t, String msg, Object... arguments) {
		this.log.error(t, msg, arguments);
	}

	@Override
	public Object getCurrentTrace() {
		return this.log.getCurrentTrace();
	}

	@Override
	public <T> T startTrace() {
		return this.log.startTrace();
	}

	@Override
	public <T> T startTrace(String traceId) {
		return this.log.startTrace(traceId);
	}

	@Override
	public void endTrace() {
		this.log.endTrace();
	}

	@Override
	public void endTrace(boolean isSuccess) {
		this.log.endTrace(isSuccess);
	}

	@Override
	public void enterFunction() {
		this.log.enterFunction();
	}

	@Override
	public void enterFunction(String msg) {
		this.log.enterFunction(msg);

	}

	@Override
	public void enterFunction(String msg, Object arg) {
		this.log.enterFunction(msg, arg);

	}

	@Override
	public void enterFunction(String msg, Object arg1, Object arg2) {
		this.log.enterFunction(msg, arg1, arg2);

	}

	@Override
	public void enterFunction(String msg, Object... arguments) {
		this.log.enterFunction(msg, arguments);

	}

	@Override
	public void endFunction(String msg) {
		this.log.endFunction(msg);
	}

	@Override
	public void endFunction() {
		this.log.endFunction();
	}

	@Override
	public void endFunction(String msg, Object arg) {
		this.log.endFunction(msg, arg);
	}

	@Override
	public void endFunction(String msg, Object arg1, Object arg2) {
		this.log.endFunction(msg, arg1, arg2);
	}

	@Override
	public void endFunction(String msg, Object... arguments) {
		this.log.endFunction(msg, arguments);
	}

	@Override
	public void endFunction(Throwable t, String msg) {
		this.log.endFunction(t, msg);
	}

	@Override
	public void endFunction(Throwable t, String msg, Object arg) {
		this.log.endFunction(t, msg, arg);
	}

	@Override
	public void endFunction(Throwable t, String msg, Object arg1, Object arg2) {
		this.log.endFunction(t, msg, arg1, arg2);
	}

	@Override
	public void endFunction(Throwable t, String msg, Object... arguments) {
		this.log.endFunction(t, msg, arguments);
	}

}
