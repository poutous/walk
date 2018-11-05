package org.walkframework.shiro.service;


import java.util.Collection;

import org.walkframework.shiro.bean.UrlFilter;


/**
 * URL权限加载器
 * 
 * @author shf675
 */
public interface UrlFilterLoader{
	
	/**
	 * 加载url权限数据
	 * 
	 * @param tdMUrls
	 */
	void loadUrlFiltersData();
	
	/**
	 * 单个注册url权限校验
	 * 
	 * @param urlChains
	 */
	void addUrlFilter(UrlFilter urlChain);

	/**
	 * 单个移除已注册的chain
	 * 
	 * @param urlChains
	 */
	void removeUrlFilter(UrlFilter urlChain);

	/**
	 * 批量注册url权限校验
	 * 
	 * @param urlChains
	 */
	void addUrlFilters(Collection<UrlFilter> urlChains);

	/**
	 * 批量移除已注册的chain
	 * 
	 * @param urlChains
	 */
	void removeUrlFilters(Collection<UrlFilter> urlChains);
}
