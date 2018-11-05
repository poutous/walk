package org.walkframework.base.system.tag.resources;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang3.StringUtils;
import org.walkframework.base.system.tag.BaseTag;
import org.walkframework.base.tools.spring.SpringPropertyHolder;

/**
 * 资源标签基类
 * 
 * @author shf675
 * 
 */
public abstract class BaseResourcesTag extends BaseTag {

	/**
	 * 静态资源本地缓存
	 */
	private final static Map<String, String> resourcesCache = new ConcurrentHashMap<String, String>();

	/**
	 * 版本号
	 */
	private String version;

	/**
	 * 是否自动拼context，默认true
	 */
	private boolean autoContext = true;
	
	/**
	 * 扩展属性
	 */
	private String extendProps;

	public void doTag() throws IOException {
		super.doTag();

		// 生成标签
		generateTag();
	}

	// 生成标签
	public abstract void generateTag() throws IOException;

	/**
	 * 插入属性
	 * 
	 * @param properties
	 * @param property
	 * @param value
	 */
	protected void appendIfNotEmpty(StringBuilder properties, String property, String value) {
		if (StringUtils.isNotBlank(value)) {
			properties.append(property).append("=").append("\"").append(value).append("\" ");
		}
	}

	/**
	 * 处理属性值
	 * 
	 * @param value
	 * @param defaultValue
	 */
	protected String getValue(String value, String defaultValue) {
		return StringUtils.isBlank(value) ? defaultValue : value;
	}

	/**
	 * 处理url版本号问题
	 * 
	 * @return
	 */
	protected String handleUrlVersion(String url) {
		String version = getVersion();
		if (StringUtils.isNotBlank(version)) {
			if (url.indexOf("?") > -1) {
				url += "&_v=" + version;
			} else {
				url += "?_v=" + version;
			}
		}
		
		//绝对路径时自动拼装context
		if(url.startsWith("/")){
			// 自动拼装context
			if (autoContext) {
				HttpServletRequest request = (HttpServletRequest) ((PageContext) getJspContext()).getRequest();
				url = request.getContextPath() + url;
			}
		}
		return url;
	}
	
	/**
	 * 获取key
	 * 
	 * @param resourceType
	 * @param url
	 * @return
	 */
	protected String getResourceKey(String resourceType, String url) {
		return new StringBuilder().append(resourceType).append("_").append(url).append("_").append(getVersion()).toString();
	}

	/**
	 * 获取资源缓存对象
	 * 
	 * @return
	 */
	public static Map<String, String> getResourcesCache() {
		return resourcesCache;
	}
	
	/**
	 * 获取版本号
	 * 
	 * @return
	 */
	public String getVersion() {
		String force = SpringPropertyHolder.getContextProperty("resources.global.force");
		//1、强制使用全局版本号时直接返回
		//2、标签未设置版本号时使用全局版本号
		if("true".equals(force) || StringUtils.isBlank(version)){
			String vname = SpringPropertyHolder.getContextProperty("resources.global.vname");
			return (String)getJspContext().getAttribute(vname);
		}
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isAutoContext() {
		return autoContext;
	}

	public void setAutoContext(boolean autoContext) {
		this.autoContext = autoContext;
	}
	
	public String getExtendProps() {
		return extendProps;
	}

	public void setExtendProps(String extendProps) {
		this.extendProps = extendProps;
	}
}
