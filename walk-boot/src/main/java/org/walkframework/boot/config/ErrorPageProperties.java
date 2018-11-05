package org.walkframework.boot.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * 错误页面定义
 * 
 * @author shf675
 * 
 */
@ConfigurationProperties(prefix = "page")
public class ErrorPageProperties {
	
	private static final String DELIMITER = ":";

	private List<String> errorpages = new ArrayList<String>();

	/**
	 * 解析错误页面配置
	 * 
	 * @return
	 */
	public Set<ErrorPage> resolveErrorPages() {
		Set<ErrorPage> pages = new LinkedHashSet<ErrorPage>();
		for (String page : errorpages) {
			page = StringUtils.trimAllWhitespace(page);
			if (!StringUtils.isEmpty(page) && page.contains(DELIMITER)) {
				String status = page.substring(0, page.indexOf(DELIMITER));
				String path = page.substring(page.indexOf(DELIMITER) + 1, page.length());
				pages.add(new ErrorPage(HttpStatus.valueOf(Integer.valueOf(status)), path));
			}
		}
		return pages;
	}

	public void setErrorpages(List<String> errorpages) {
		this.errorpages = errorpages;
	}

	public List<String> getErrorpages() {
		return errorpages;
	}

}
