package org.walkframework.shiro.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.util.StringUtils;
import org.walkframework.shiro.bean.UrlFilter;
import org.walkframework.shiro.chain.ChainManager;

/**
 * URL权限加载器
 * 
 * @author shf675
 * 
 */
public abstract class AbstractUrlFilterLoader implements UrlFilterLoader {

	private ChainManager chainManager;

	public void setChainManager(ChainManager chainManager) {
		this.chainManager = chainManager;
	}
	
	public ChainManager getChainManager() {
		return chainManager;
	}

	/**
	 * 单个注册url权限校验
	 * 
	 * @param urlFilters
	 */
	@Override
	public void addUrlFilter(UrlFilter urlFilter) {
		List<UrlFilter> urlFilters = new ArrayList<UrlFilter>();
		urlFilters.add(urlFilter);
		addUrlFilters(urlFilters);
	}

	/**
	 * 单个移除已注册的chain
	 * 
	 * @param urlFilters
	 */
	@Override
	public void removeUrlFilter(UrlFilter urlFilter) {
		List<UrlFilter> urlFilters = new ArrayList<UrlFilter>();
		urlFilters.add(urlFilter);
		removeUrlFilters(urlFilters);
	}

	/**
	 * 批量注册url权限校验
	 * 
	 * @param urlFilters
	 */
	@Override
	public void addUrlFilters(Collection<UrlFilter> urlFilters) {
		for (UrlFilter urlFilter : urlFilters) {
			String url = urlFilter.getUrlPath();
			// 注册roles chain
			if (!StringUtils.isEmpty(urlFilter.getRoleCodes())) {
				chainManager.addToRolesChain(url, urlFilter.getRoleCodes());
			}
			// 注册perms chain
			if (!StringUtils.isEmpty(urlFilter.getRightCodes())) {
				chainManager.addToPermsChain(url, urlFilter.getRightCodes());
			}
			// 注册其他 chain
			if (!StringUtils.isEmpty(urlFilter.getFilters())) {
				chainManager.addToFiltersChain(url, urlFilter.getFilters());
			}
		}
	}

	/**
	 * 批量移除已注册的chain
	 * 
	 * @param urlFilters
	 */
	@Override
	public void removeUrlFilters(Collection<UrlFilter> urlFilters) {
		for (UrlFilter urlFilter : urlFilters) {
			String url = urlFilter.getUrlPath();
			// 移除roles chain
			if (!StringUtils.isEmpty(urlFilter.getRoleCodes())) {
				chainManager.removeRolesChain(url, urlFilter.getRoleCodes());
			}
			// 移除perms chain
			if (!StringUtils.isEmpty(urlFilter.getRightCodes())) {
				chainManager.removePermsChain(url, urlFilter.getRightCodes());
			}
			// 移除其他 chain
			if (!StringUtils.isEmpty(urlFilter.getFilters())) {
				String[] filterNames = urlFilter.getFilters().split(",");
				for (String filterName : filterNames) {
					if (filterName != null && !"".equals(filterName.trim())) {
						chainManager.removeFilterChain(url, filterName, null);
					}
				}
			}
		}
	}
}