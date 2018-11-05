package org.walkframework.shiro.web.filter.mgt;

import static org.apache.shiro.util.StringUtils.split;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;

import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.PathConfigProcessor;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.apache.shiro.web.filter.mgt.SimpleNamedFilterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;


/**
 * 拓展DefaultFilterChainManager 改造点：重写proxy方法
 * @author shf675
 */
public class DynamicDefaultFilterChainManager extends DefaultFilterChainManager {
	private static transient final Logger log = LoggerFactory.getLogger(DynamicDefaultFilterChainManager.class);
	private Map<String, NamedFilterList> anonFilterChains;
	private Map<String, NamedFilterList> defaultFilterChains;
	private Map<String, NamedFilterList> notDefaultFilterChains;
	private boolean defaultDefinition;
	
	private DynamicPathMatchingFilterChainResolver chainResolver;
	
	public DynamicDefaultFilterChainManager() {
        this.anonFilterChains = new LinkedHashMap<String, NamedFilterList>();
        this.defaultFilterChains = new LinkedHashMap<String, NamedFilterList>();
        this.notDefaultFilterChains = new LinkedHashMap<String, NamedFilterList>();
        addDefaultFilters(false);
    }
	
	public Map<String, NamedFilterList> getAnonFilterChains() {
		return anonFilterChains;
	}

	public void setAnonFilterChains(Map<String, NamedFilterList> anonFilterChains) {
		this.anonFilterChains = anonFilterChains;
	}
	
	/**
	 * 改造原有proxy方法，支持多链校验
	 * 
	 * @param original
	 * @param chainName
	 */
	public FilterChain proxy(FilterChain original, String chainName) {
		NamedFilterList configured = null;
		if (chainName.startsWith(DynamicPathMatchingFilterChainResolver.CHAIN_NAMES_PREFIX)) {
			String[] chainNames = convertToChainNames(chainName);
			for (String cn : chainNames) {
				if (cn != null && !"".equals(cn.trim())) {
					if(configured == null){
						configured = getChain(cn.trim());
					} else {
						NamedFilterList chain = getChain(cn.trim());
						if(chain != null && !configured.containsAll(chain)){
							configured.addAll(chain);
						}
					}
				}
			}
		} else {
			configured = getAnonChain(chainName);
		}
		
		if (configured == null) {
			String msg = "There is no configured chain under the name/key [" + chainName + "].";
			throw new IllegalArgumentException(msg);
		}
		return configured.proxy(original);
	}
	
	public void createChain(String chainName, String chainDefinition, boolean defaultDefinition) {
		this.defaultDefinition = defaultDefinition;
		createChain(chainName, chainDefinition);
    }
	
	@SuppressWarnings("unchecked")
	public void addToChain(String chainName, String filterName, String chainSpecificFilterConfig) {
        if (!StringUtils.hasText(chainName)) {
            throw new IllegalArgumentException("chainName cannot be null or empty.");
        }
        Filter filter = getFilter(filterName);
        if (filter == null) {
            throw new IllegalArgumentException("There is no filter with name '" + filterName +
                    "' to apply to chain [" + chainName + "] in the pool of available Filters.  Ensure a " +
                    "filter with that name/path has first been registered with the addFilter method(s).");
        }
        
        //获取老的配置放到set中，进行排重
        Set<String> chainConfigSet = new LinkedHashSet<String>();
        Map<String, Object> appliedPaths = getAppliedPaths(filter);
        if(appliedPaths != null && appliedPaths.get(chainName) != null){
    		CollectionUtils.addAll(chainConfigSet, (String[])appliedPaths.get(chainName));
    	}
        //添加新配置到set中
        if(chainSpecificFilterConfig != null){
        	String[] chainSpecificFilterConfigs = StringUtils.split(chainSpecificFilterConfig);
        	CollectionUtils.addAll(chainConfigSet, chainSpecificFilterConfigs);
        }
        //过滤器注册配置
        applyChainConfig(chainName, filter, getChainConfig(chainConfigSet));
        
        NamedFilterList chain = ensureChain(chainName, filter);
        //同一过滤器只添加一次即可
    	if(!chain.contains(filter)){
    		chain.add(filter);
    	}
    }
	
	public void applyChainConfig(String chainName, Filter filter, String chainSpecificFilterConfig) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to apply path [{}] to filter [{}] with config [{}]", chainName, filter, chainSpecificFilterConfig);
        }
        if (filter instanceof PathConfigProcessor) {
        	Map<String, Object> appliedPaths = getAppliedPaths(filter);
			Object paths = appliedPaths.get(chainName);
			//新加入的放最前
			if (paths == null) {
				Map<String, Object> newAppliedPaths = new LinkedHashMap<String, Object>();
				String[] values = null;
				if (chainSpecificFilterConfig != null) {
					values = split(chainSpecificFilterConfig);
				}
				newAppliedPaths.put(chainName, values);
				newAppliedPaths.putAll(appliedPaths);
				appliedPaths.clear();
				appliedPaths.putAll(newAppliedPaths);
			} else {
				((PathConfigProcessor) filter).processPathConfig(chainName, chainSpecificFilterConfig);
			}
        } else {
            if (StringUtils.hasText(chainSpecificFilterConfig)) {
                //they specified a filter configuration, but the Filter doesn't implement PathConfigProcessor
                //this is an erroneous config:
                String msg = "chainSpecificFilterConfig was specified, but the underlying " +
                        "Filter instance is not an 'instanceof' " +
                        PathConfigProcessor.class.getName() + ".  This is required if the filter is to accept " +
                        "chain-specific configuration.";
                throw new ConfigurationException(msg);
            }
        }
    }
	
	protected NamedFilterList ensureChain(String chainName, Filter filter) {
        NamedFilterList chain = getChain(chainName);
        if (chain == null) {
            chain = new SimpleNamedFilterList(chainName);
            //匿名过滤器。不需要登录即可访问；一般用于静态资源过滤；
            if(filter instanceof AnonymousFilter){
            	this.anonFilterChains.put(chainName, chain);
            } else {
            	//spring文件里默认配置
            	if(defaultDefinition){
            		this.getFilterChains().put(chainName, chain);
            		this.defaultFilterChains.put(chainName, chain);
            		this.defaultDefinition = false;
            	} 
            	//后动态添加
            	else {
            		this.notDefaultFilterChains.put(chainName, chain);
            		this.getFilterChains().clear();
            		this.getFilterChains().putAll(this.notDefaultFilterChains);
            		this.getFilterChains().putAll(this.defaultFilterChains);
            	}
            }
            
        }
        return chain;
    }

	private String[] convertToChainNames(String chainName) {
		return chainName.substring(DynamicPathMatchingFilterChainResolver.CHAIN_NAMES_PREFIX.length() + 1, chainName.length() - 1).split(",");
	}
	
	public NamedFilterList getAnonChain(String chainName) {
        return this.getAnonFilterChains().get(chainName);
    }
	
	@SuppressWarnings("unchecked")
	public Set<String> getAnonChainNames() {
        return this.getAnonFilterChains() != null ? this.getAnonFilterChains().keySet() : Collections.EMPTY_SET;
    }

	public DynamicPathMatchingFilterChainResolver getChainResolver() {
		return chainResolver;
	}

	public void setChainResolver(DynamicPathMatchingFilterChainResolver chainResolver) {
		this.chainResolver = chainResolver;
	}
	
	/**
	 * 获取老的配置信息
	 * 
	 * @param filter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getAppliedPaths(Filter filter){
		Map<String, Object> appliedPaths = null;
        Field field = ReflectionUtils.findField(filter.getClass(), "appliedPaths");
        if(field != null){
        	field.setAccessible(true);
        	appliedPaths = (Map<String, Object>)ReflectionUtils.getField(field, filter);
        }
        return appliedPaths;
	}
	
	/**
	 * 获取配置
	 * 
	 * @param chainConfigSet
	 * @return
	 */
	public String getChainConfig(Set<String> chainConfigSet){
		StringBuilder configs = new StringBuilder();
		int i = 0;
        for (String config : chainConfigSet) {
        	configs.append(config.trim());
        	if(i < chainConfigSet.size() - 1){
        		configs.append(StringUtils.DEFAULT_DELIMITER_CHAR);
        	}
        	i++;
		}
        return configs.toString();
	}
	
	protected void addDefaultFilters(boolean init) {
        for (ExtendDefaultFilter defaultFilter : ExtendDefaultFilter.values()) {
            addFilter(defaultFilter.name(), defaultFilter.newInstance(), init, false);
        }
    }
}
