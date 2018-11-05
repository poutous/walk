package org.walkframework.shiro.web.filter.mgt;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.util.PatternMatcher;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 拓展PathMatchingFilterChainResolver
 * 改造点：重写getChain方法
 * @author shf675
 */
public class DynamicPathMatchingFilterChainResolver extends PathMatchingFilterChainResolver {
	private static transient final Logger log = LoggerFactory.getLogger(DynamicPathMatchingFilterChainResolver.class);
	
	public static final String CHAIN_NAMES_PREFIX = "_CHAIN_NAMES_PREFIX";
	
	/**
	 * 改造原有getChain方法，支持多链校验
	 * 
	 * @param request
	 * @param response
	 * @param originalChain
	 */
    public FilterChain getChain(ServletRequest request, ServletResponse response, FilterChain originalChain) {
    	DynamicDefaultFilterChainManager filterChainManager = (DynamicDefaultFilterChainManager)getFilterChainManager();
        if (!filterChainManager.hasChains()) {
            return null;
        }

        String requestURI = getPathWithinApplication(request);
        
        //先匹配匿名过滤器。一般为静态资源
        for (String pathPattern : filterChainManager.getAnonChainNames()) {
            // If the path does match, then pass on to the subclass implementation for specific checks:
            if (pathMatches(pathPattern, requestURI)) {
                if (log.isTraceEnabled()) {
                    log.trace("Matched path pattern [{}] for requestURI [{}]. Utilizing corresponding filter chain...", pathPattern, requestURI);
                }
                return filterChainManager.proxy(originalChain, pathPattern);
            }
        }
        
        //然后过滤其他资源
        List<String> chainNames = new ArrayList<String>();
        //the 'chain names' in this implementation are actually path patterns defined by the user.  We just use them
        //as the chain name for the FilterChainManager's requirements
        for (String pathPattern : filterChainManager.getChainNames()) {

            // If the path does match, then pass on to the subclass implementation for specific checks:
            if (pathMatches(pathPattern, requestURI)) {
            	if (log.isTraceEnabled()) {
                    log.trace("Matched path pattern [{}] for requestURI [{}]. Utilizing corresponding filter chain...", pathPattern, requestURI);
                }
                chainNames.add(pathPattern);
            }
        }

        if(chainNames.size() == 0) {
            return null;
        }

        return filterChainManager.proxy(originalChain, CHAIN_NAMES_PREFIX + chainNames.toString());
    }
    
    public boolean pathMatches(String pattern, String path) {
        PatternMatcher pathMatcher = getPathMatcher();
        return pathMatcher.matches(pattern, path);
    }
}
