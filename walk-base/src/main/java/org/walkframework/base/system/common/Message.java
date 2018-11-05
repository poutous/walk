package org.walkframework.base.system.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息返回 通过调用SingletonFactory.getInstance(Message.class)获取实例
 * 
 */
public class Message {
	
	protected static final Logger log = LoggerFactory.getLogger(Message.class);
	
	public static final String JQUERY_FORM_AJAX_REQUEST = "JQUERY_FORM_AJAX_REQUEST";

	/**
	 * 返回成功消息
	 * 
	 * @param text
	 * @return
	 */
	public String success(String text) {
		return message("success", text);
	}

	/**
	 * 返回提示消息
	 * 
	 * @param text
	 * @return
	 */
	public String info(String text) {
		return message("info", text);
	}

	/**
	 * 返回警告消息
	 * 
	 * @param text
	 * @return
	 */
	public String warn(String text) {
		return message("warning", text);
	}

	/**
	 * 返回错误消息
	 * 
	 * @param text
	 * @return
	 */
	public String error(String text) {
		return message("error", text);
	}
	/**
	 * 返回错误消息
	 * 
	 * @param text
	 * @return
	 */
	public String error(String text, Throwable e) {
		log.error(text, e);
		return message("error", text);
	}
	
	/**
	 * 返回消息
	 * 
	 * @param type
	 * @param text
	 * @return
	 */
	private String message(String type, String text) {
		StringBuilder message = new StringBuilder();
		message.append("<div id='_MESSAGE_RESPONSE'>");
		message.append("<div id='type'>");
		message.append(type);
		message.append("</div>");
		message.append("<div id='text'>");
		message.append(text);
		message.append("</div>");
		message.append("</div>");
		return message.toString();
	}
}
