package org.walkframework.base.tools.i18n;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class CustomReloadableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource{
	private String productMode = "true";


	public void setProductMode(String productMode) {
		this.productMode = productMode;
	}
	
	public void setCacheSeconds(int cacheSeconds){
		//开发模式允许国际化文件定时刷新
		if(!"true".equals(this.productMode)){
			super.setCacheSeconds(cacheSeconds);
		}
	}
}
