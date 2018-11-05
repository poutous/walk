package org.walkframework.shiro.chain;

import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;

import org.apache.shiro.web.filter.mgt.NamedFilterList;

/**
 * @author shf675
 *
 */
public interface ChainManager {

	/**
	 * 获取所有过滤器
	 * 
	 * @return
	 */
	Map<String, Filter> getFilters();

	/**
	 * 获取所有过滤链(除匿名过滤器(静态资源)外)
	 * 
	 * @return
	 */
	Map<String, NamedFilterList> getFilterChains();

	/**
	 * 获取所有过滤链名字
	 * 
	 * @return
	 */
	Set<String> getChainNames();

	/**
	 * 添加角色链
	 * 
	 * @param chainName
	 * @param chainSpecificFilterConfig
	 */
	void addToRolesChain(String chainName, String chainSpecificFilterConfig);

	/**
	 * 添加权限链
	 * 
	 * @param chainName
	 * @param chainSpecificFilterConfig
	 */
	void addToPermsChain(String chainName, String chainSpecificFilterConfig);

	/**
	 * 添加自定义过滤器链
	 * 
	 * @param chainName
	 * @param filterName
	 * @param chainSpecificFilterConfig
	 */
	void addToFilterChain(String chainName, String filterName, String chainSpecificFilterConfig);
	
	/**
	 * 添加其他过滤器链（包括自定义）
	 * 
	 * @param chainName
	 * @param filterNames
	 */
	void addToFiltersChain(String chainName, String filterNames);

	/**
	 * 移除角色链
	 * 
	 * @param chainName
	 * @param chainSpecificFilterConfig
	 */
	void removeRolesChain(String chainName, String chainSpecificFilterConfig);

	/**
	 * 移除权限链
	 * 
	 * @param chainName
	 * @param chainSpecificFilterConfig
	 */
	void removePermsChain(String chainName, String chainSpecificFilterConfig);

	/**
	 * 移除链过滤器
	 * 
	 * @param chainName
	 * @param filterName
	 */
	void removeFilterChain(String chainName, String filterName, String chainSpecificFilterConfig);

}
