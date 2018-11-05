package org.walkframework.shiro.chain;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.apache.shiro.web.filter.mgt.SimpleNamedFilterList;
import org.walkframework.shiro.web.filter.authz.OrAuthorizationHelper;
import org.walkframework.shiro.web.filter.mgt.DynamicDefaultFilterChainManager;
import org.walkframework.shiro.web.filter.mgt.DynamicPathMatchingFilterChainResolver;

/**
 * 初始化过滤器链，程序中可注入此bean进行动态url权限控制
 * 
 * @author shf675
 */
public class DynamicChainManager implements ChainManager {

	private final String PERMS = "orPerms";
	private final String ROLES = "orRoles";

	private DynamicDefaultFilterChainManager filterChainManager;

	public DynamicDefaultFilterChainManager getFilterChainManager() {
		return filterChainManager;
	}

	public void setFilterChainManager(DynamicDefaultFilterChainManager filterChainManager) {
		this.filterChainManager = filterChainManager;
	}

	/**
	 * 获取所有过滤器
	 * 
	 * @return
	 */
	@Override
	public Map<String, Filter> getFilters() {
		return filterChainManager.getFilters();
	}

	/**
	 * 获取所有过滤链(除匿名过滤器(静态资源)外)
	 * 
	 * @return
	 */
	@Override
	public Map<String, NamedFilterList> getFilterChains() {
		return filterChainManager.getFilterChains();
	}

	/**
	 * 获取所有过滤链名字
	 * 
	 * @return
	 */
	@Override
	public Set<String> getChainNames() {
		return filterChainManager.getChainNames();
	}

	/**
	 * 添加角色链
	 * 
	 * @param chainName
	 * @param chainSpecificFilterConfig
	 */
	@Override
	public void addToRolesChain(String chainName, String chainSpecificFilterConfig) {
		filterChainManager.addToChain(chainName, ROLES, chainSpecificFilterConfig);
	}

	/**
	 * 添加权限链
	 * 
	 * @param chainName
	 * @param chainSpecificFilterConfig
	 */
	@Override
	public void addToPermsChain(String chainName, String chainSpecificFilterConfig) {
		filterChainManager.addToChain(chainName, PERMS, chainSpecificFilterConfig);
	}
	
	/**
	 * 添加自定义过滤器链
	 * 
	 * @param chainName
	 * @param filterName
	 * @param chainSpecificFilterConfig
	 */
	@Override
	public void addToFilterChain(String chainName, String filterName, String chainSpecificFilterConfig) {
		filterChainManager.addToChain(chainName, filterName, chainSpecificFilterConfig);
	}

	/**
	 * 添加其他过滤器链（包括自定义）
	 * 
	 * @param chainName
	 * @param filterNames
	 */
	@Override
	public void addToFiltersChain(String chainName, String filterNames) {
		if (StringUtils.hasText(filterNames)) {
			String[] filters = StringUtils.split(filterNames);
			for (String filter : filters) {
				filterChainManager.addToChain(chainName, filter.trim());
			}
		}
	}

	/**
	 * 移除角色链
	 * 
	 * @param chainName
	 * @param chainSpecificFilterConfig
	 */
	@Override
	public void removeRolesChain(String chainName, String chainSpecificFilterConfig) {
		removeFilterChain(chainName, ROLES, chainSpecificFilterConfig);
	}

	/**
	 * 移除权限链
	 * 
	 * @param chainName
	 * @param chainSpecificFilterConfig
	 */
	@Override
	public void removePermsChain(String chainName, String chainSpecificFilterConfig) {
		removeFilterChain(chainName, PERMS, chainSpecificFilterConfig);
	}

	/**
	 * 移除链过滤器
	 * 
	 * @param chainName
	 * @param filterName
	 */
	@Override
	public void removeFilterChain(String chainName, String filterName, String chainSpecificFilterConfig) {
		Set<String> chainConfigSet = new LinkedHashSet<String>();
		Set<String> chainConfigSet2 = new LinkedHashSet<String>();
		Filter filter = filterChainManager.getFilter(filterName);
		NamedFilterList chain = filterChainManager.getChain(chainName);
		if (chain != null) {
			// 移除过滤器中指定的过滤配置
			if (chainSpecificFilterConfig != null) {
				Map<String, Object> appliedPaths = filterChainManager.getAppliedPaths(filter);
				if (appliedPaths != null && appliedPaths.get(chainName) != null) {
					//将老的配置放到新set里，set有排重之功效
					CollectionUtils.addAll(chainConfigSet, (String[]) appliedPaths.get(chainName));
					chainConfigSet2.addAll(chainConfigSet);

					//遍历set找或的配置
					for (String chainConfig : chainConfigSet2) {
						String[] chainSpecificFilterConfigs = StringUtils.split(chainSpecificFilterConfig);
						for (String config : chainSpecificFilterConfigs) {
							if (config.matches(chainConfig)) {
								//如果有或的配置，截断或中的条件
								if (chainConfig.contains(String.valueOf(OrAuthorizationHelper.OR_DELIMITER_CHAR))) {
									//先移除老的
									chainConfigSet.remove(chainConfig);
									//添加删除某或配置后新的
									chainConfigSet.add(removeInOrStr(chainConfig, config));
								} else {
									chainConfigSet.remove(config);
								}
							}
						}
					}
				}

				// 重新注册过滤器
				if (!CollectionUtils.isEmpty(chainConfigSet)) {
					filterChainManager.applyChainConfig(chainName, filter, filterChainManager.getChainConfig(chainConfigSet));
				}
			}

			// 链中移除过滤器
			if (chainSpecificFilterConfig == null || CollectionUtils.isEmpty(chainConfigSet)) {
				if (chain.contains(filter)) {
					chain.remove(filter);
				}
			}

			// 移除子链。
			NamedFilterList chained = removeNamedFilterList(new SimpleNamedFilterList(chain.getName(), chain));
			if (chained.size() == 0) {
				getFilterChains().remove(chain.getName());
			}
		}
	}

	/**
	 * 移除子链
	 * 
	 * @param chain
	 * @return
	 */
	private NamedFilterList removeNamedFilterList(NamedFilterList chain) {
		String chainName = chain.getName();
		DynamicPathMatchingFilterChainResolver chainResolver = filterChainManager.getChainResolver();
		for (String pathPattern : filterChainManager.getChainNames()) {
			if (!chainName.equals(pathPattern) && chainResolver.pathMatches(pathPattern, chainName)) {
				NamedFilterList pathPatternChain = filterChainManager.getChain(pathPattern);
				if (chain.containsAll(pathPatternChain)) {
					chain.removeAll(pathPatternChain);
				}
			}
		}
		return chain;
	}

	/**
	 * 移除以|分隔符的字符串中某个字符串
	 * 
	 * @param orChainConfig
	 * @param chainConfig
	 * @return
	 */
	private String removeInOrStr(String orStr, String str) {
		StringBuilder newOrStr = new StringBuilder();
		String[] orStrs = StringUtils.split(orStr, OrAuthorizationHelper.OR_DELIMITER_CHAR);
		for (String orS : orStrs) {
			if (!orS.equals(str)) {
				newOrStr.append(orS);
				newOrStr.append(OrAuthorizationHelper.OR_DELIMITER_CHAR);
			}
		}
		if (newOrStr.length() > 0) {
			newOrStr.deleteCharAt(newOrStr.length() - 1);
		}
		return newOrStr.toString();
	}
}
