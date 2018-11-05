package org.walkframework.base.system.tag;

import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.walkframework.base.tools.spring.SpringPropertyHolder;

/**
 * body 标签基类
 * 
 * @author shf675
 *
 */
public abstract class BaseBodyTag extends BodyTagSupport {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected final static String DEFAULT_KEY = "walk-security-key!@#$%^&*";
	
	private String key;
	
	
	/**
	 * 取key
	 * app.properties文件中如果没配securityKey，则取默认
	 * 
	 * @return
	 */
	public String getKey(){
		if(StringUtils.isEmpty(key)){
			key = SpringPropertyHolder.getContextProperty("securityKey", DEFAULT_KEY);
		}
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
