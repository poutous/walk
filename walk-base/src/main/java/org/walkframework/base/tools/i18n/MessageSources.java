package org.walkframework.base.tools.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.walkframework.base.tools.spring.SpringContextHolder;


/**
 * 获取国际化消息
 *
 */
public abstract class MessageSources {
	public static String getMessage(String code) {
		return getMessage(code, "");
	}

	public static String getMessage(String code, String defaultMessage) {
		return getMessage(code, null, defaultMessage);
	}

	public static String getMessage(String code, Object[] paramArray) {
		return getMessage(code, paramArray, "");
	}

	public static String getMessage(String code, Object[] paramArray, String defaultMessage) {
		return getMessage(code, paramArray, defaultMessage, SpringContextHolder.getBean(CustomCookieLocaleResolver.class).getDefaultLocale());
	}

	public static String getMessage(String code, Object[] paramArray, String defaultMessage, Locale locale) {
		MessageSource messageSource = SpringContextHolder.getBean(CustomReloadableResourceBundleMessageSource.class);
		return messageSource.getMessage(code, paramArray, defaultMessage, locale);
	}
}
