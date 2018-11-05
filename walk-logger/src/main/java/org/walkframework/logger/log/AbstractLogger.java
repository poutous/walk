/**
 * 
 */
package org.walkframework.logger.log;

import org.slf4j.helpers.MessageFormatter;
import org.walkframework.logger.Logger;

/**
 * <p>虚拟实现logger，增加通用能力</p>
 * 
 * @author MengQK
 * @version [1.0, 2017年4月1日]
 * @since [2017年4月1日]
 */
public abstract class AbstractLogger implements Logger {

	private static final String TEMPLATE_ENTER_FUNC = "[enterFunction]{}({})";
	private static final String TEMPLATE_END_FUNC = "[endFunction]{}({})";

	/** 是否做日志代理 */
	private boolean isDelegating = false;
	/** 调用者的深度，代理时增加一层 */
	private int invokerDepth = 3;

	/**
	 * <p>格式化日志内容，接受slf4j规定的{}参数，形如"arg1= {}, arg2={} "</p>
	 * 
	 * @param format
	 * @param args
	 * @return
	 * @author MengQK
	 * @since [2017年4月1日]
	 */
	public String formatMsg(String format, Object... args) {
		return MessageFormatter.arrayFormat(format, args).getMessage();
	}

	/**
	 * <p>格式化进入方法的日志内容</p>
	 * 
	 * @param format
	 * @param args
	 * @return
	 * @author MengQK
	 * @since [2017年4月1日]
	 */
	public String formatEnterFuncMsg(String format, Object... args) {
		String newFormatStr = formatMsg(TEMPLATE_ENTER_FUNC, format, getInvokerMethodInfo());
		if (null != args && args.length > 0) {
			return formatMsg(newFormatStr, args);
		}
		return newFormatStr;
	}

	/**
	 * <p>格式化离开方法的日志内容</p>
	 * 
	 * @param format
	 * @param args
	 * @return
	 * @author MengQK
	 * @since [2017年4月1日]
	 */
	public String formatEndFuncMsg(String format, Object... args) {
		String newFormatStr = formatMsg(TEMPLATE_END_FUNC, format, getInvokerMethodInfo());
		if (null != args && args.length > 0) {
			return formatMsg(newFormatStr, args);
		}
		return newFormatStr;
	}

	/**
	 * <p>获取运行时调用logger的方法名和行数</p>
	 * 
	 * @return
	 * @author MengQK
	 * @since [2017年4月1日]
	 */
	public String getInvokerMethodInfo() {
		Throwable t = new Throwable();
		StackTraceElement[] stacks = t.getStackTrace();

		if (stacks.length > this.invokerDepth) {
			return stacks[this.invokerDepth].getMethodName() + ":" + stacks[this.invokerDepth].getLineNumber();
		}
		return "";
	}

	/**
	 * <p>校验入参非空</p>
	 * 
	 * @param inStr
	 * @return
	 * @author MengQK
	 * @since [2017年4月5日]
	 */
	protected boolean isNotEmpty(String inStr) {
		return null != inStr && inStr.length() != 0;
	}

	/**
	 * @return the isDelegating
	 */
	public boolean isDelegating() {
		return isDelegating;
	}

	/**
	 * @param isDelegating the isDelegating to set
	 * @return 
	 */
	public AbstractLogger setDelegating(boolean isDelegating) {
		this.isDelegating = isDelegating;
		this.invokerDepth = getInvokerMethodDepth();
		return this;
	}

	public int getInvokerMethodDepth() {
		return this.isDelegating ? 4 : 3;
	}
}
