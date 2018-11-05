package org.walkframework.base.tools.i18n;

import java.util.Locale;

import org.springframework.web.servlet.i18n.CookieLocaleResolver;

public class CustomCookieLocaleResolver extends CookieLocaleResolver{
	private Locale defaultLocale;

	public Locale getDefaultLocale() {
		return defaultLocale;
	}

	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}
}
