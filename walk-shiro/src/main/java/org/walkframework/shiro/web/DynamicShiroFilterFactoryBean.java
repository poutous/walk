package org.walkframework.shiro.web;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.Nameable;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.walkframework.shiro.web.filter.authc.RouteAuthFilter;
import org.walkframework.shiro.web.filter.mgt.DynamicDefaultFilterChainManager;
import org.walkframework.shiro.web.filter.mgt.DynamicPathMatchingFilterChainResolver;
import org.walkframework.shiro.web.servlet.ShiroExtHttpServletResponse;


/**
 * 拓展ShiroFilterFactoryBean
 * 改造点：注入自定义filterChainManager
 * 
 * @author shf675
 */
public class DynamicShiroFilterFactoryBean extends ShiroFilterFactoryBean {
	
	private static transient final Logger log = LoggerFactory.getLogger(DynamicShiroFilterFactoryBean.class);
	
	private static final String DEFAULT_ROUTE_AUTH_FILTER_NAME = "routeAuth";
	
	private DynamicDefaultFilterChainManager filterChainManager;
	
	private String routeAuthFilterName = DEFAULT_ROUTE_AUTH_FILTER_NAME;
	
	private RouteAuthFilter routeAuthFilter;
	

	protected DynamicDefaultFilterChainManager createFilterChainManager() {
		Map<String, Filter> defaultFilters = filterChainManager.getFilters();
		// apply global settings if necessary:
		for (Filter filter : defaultFilters.values()) {
			applyGlobalPropertiesIfNecessary(filter);
		}

		// Apply the acquired and/or configured filters:
		Map<String, Filter> filters = getFilters();
		if (!CollectionUtils.isEmpty(filters)) {
			for (Map.Entry<String, Filter> entry : filters.entrySet()) {
				String name = entry.getKey();
				Filter filter = entry.getValue();
				applyGlobalPropertiesIfNecessary(filter);
				if (filter instanceof Nameable) {
					((Nameable) filter).setName(name);
				}
				// 'init' argument is false, since Spring-configured filters
				// should be initialized
				// in Spring (i.e. 'init-method=blah') or implement
				// InitializingBean:
				filterChainManager.addFilter(name, filter, false);
			}
		}

		// build up the chains:
		Map<String, String> chains = getFilterChainDefinitionMap();
		if (!CollectionUtils.isEmpty(chains)) {
			for (Map.Entry<String, String> entry : chains.entrySet()) {
				String url = entry.getKey();
				String chainDefinition = entry.getValue();
				filterChainManager.createChain(url, chainDefinition, true);
			}
		}

		return filterChainManager;
	}
	
	/**
     * This implementation:
     * <ol>
     * <li>Ensures the required {@link #setSecurityManager(org.apache.shiro.mgt.SecurityManager) securityManager}
     * property has been set</li>
     * <li>{@link #createFilterChainManager() Creates} a {@link FilterChainManager} instance that reflects the
     * configured {@link #setFilters(java.util.Map) filters} and
     * {@link #setFilterChainDefinitionMap(java.util.Map) filter chain definitions}</li>
     * <li>Wraps the FilterChainManager with a suitable
     * {@link org.apache.shiro.web.filter.mgt.FilterChainResolver FilterChainResolver} since the Shiro Filter
     * implementations do not know of {@code FilterChainManager}s</li>
     * <li>Sets both the {@code SecurityManager} and {@code FilterChainResolver} instances on a new Shiro Filter
     * instance and returns that filter instance.</li>
     * </ol>
     *
     * @return a new Shiro Filter reflecting any configured filters and filter chain definitions.
     * @throws Exception if there is a problem creating the AbstractShiroFilter instance.
     */
    protected AbstractShiroFilter createInstance() throws Exception {

        log.debug("Creating Shiro Filter instance.");

        SecurityManager securityManager = getSecurityManager();
        if (securityManager == null) {
            String msg = "SecurityManager property must be set.";
            throw new BeanInitializationException(msg);
        }

        if (!(securityManager instanceof WebSecurityManager)) {
            String msg = "The security manager does not implement the WebSecurityManager interface.";
            throw new BeanInitializationException(msg);
        }

        DynamicDefaultFilterChainManager manager = createFilterChainManager();

        //Expose the constructed FilterChainManager by first wrapping it in a
        // FilterChainResolver implementation. The AbstractShiroFilter implementations
        // do not know about FilterChainManagers - only resolvers:
        DynamicPathMatchingFilterChainResolver chainResolver = new DynamicPathMatchingFilterChainResolver();
        chainResolver.setFilterChainManager(manager);
        
        manager.setChainResolver(chainResolver);
        

        //Now create a concrete ShiroFilter instance and apply the acquired SecurityManager and built
        //FilterChainResolver.  It doesn't matter that the instance is an anonymous inner class
        //here - we're just using it because it is a concrete AbstractShiroFilter instance that accepts
        //injection of the SecurityManager and FilterChainResolver:
        return new SpringShiroFilter((WebSecurityManager) securityManager, chainResolver);
    }
	
	private void applyLoginUrlIfNecessary(Filter filter) {
		String loginUrl = getLoginUrl();
		if (StringUtils.hasText(loginUrl) && (filter instanceof AccessControlFilter)) {
			AccessControlFilter acFilter = (AccessControlFilter) filter;
			// only apply the login url if they haven't explicitly configured
			// one already:
			String existingLoginUrl = acFilter.getLoginUrl();
			if (AccessControlFilter.DEFAULT_LOGIN_URL.equals(existingLoginUrl)) {
				acFilter.setLoginUrl(loginUrl);
			}
		}
	}

	private void applySuccessUrlIfNecessary(Filter filter) {
		String successUrl = getSuccessUrl();
		if (StringUtils.hasText(successUrl) && (filter instanceof AuthenticationFilter)) {
			AuthenticationFilter authcFilter = (AuthenticationFilter) filter;
			// only apply the successUrl if they haven't explicitly configured
			// one already:
			String existingSuccessUrl = authcFilter.getSuccessUrl();
			if (AuthenticationFilter.DEFAULT_SUCCESS_URL.equals(existingSuccessUrl)) {
				authcFilter.setSuccessUrl(successUrl);
			}
		}
	}

	private void applyUnauthorizedUrlIfNecessary(Filter filter) {
		String unauthorizedUrl = getUnauthorizedUrl();
		if (StringUtils.hasText(unauthorizedUrl) && (filter instanceof AuthorizationFilter)) {
			AuthorizationFilter authzFilter = (AuthorizationFilter) filter;
			// only apply the unauthorizedUrl if they haven't explicitly
			// configured one already:
			String existingUnauthorizedUrl = authzFilter.getUnauthorizedUrl();
			if (existingUnauthorizedUrl == null) {
				authzFilter.setUnauthorizedUrl(unauthorizedUrl);
			}
		}
	}

	private void applyGlobalPropertiesIfNecessary(Filter filter) {
		applyLoginUrlIfNecessary(filter);
		applySuccessUrlIfNecessary(filter);
		applyUnauthorizedUrlIfNecessary(filter);
	}
	
	/**
     * Ordinarily the {@code AbstractShiroFilter} must be subclassed to additionally perform configuration
     * and initialization behavior.  Because this {@code FactoryBean} implementation manually builds the
     * {@link AbstractShiroFilter}'s
     * {@link AbstractShiroFilter#setSecurityManager(org.apache.shiro.web.mgt.WebSecurityManager) securityManager} and
     * {@link AbstractShiroFilter#setFilterChainResolver(org.apache.shiro.web.filter.mgt.FilterChainResolver) filterChainResolver}
     * properties, the only thing left to do is set those properties explicitly.  We do that in a simple
     * concrete subclass in the constructor.
     */
    private static final class SpringShiroFilter extends AbstractShiroFilter {

        protected SpringShiroFilter(WebSecurityManager webSecurityManager, FilterChainResolver resolver) {
            super();
            if (webSecurityManager == null) {
                throw new IllegalArgumentException("WebSecurityManager property cannot be null.");
            }
            setSecurityManager(webSecurityManager);
            if (resolver != null) {
                setFilterChainResolver(resolver);
            }
        }
        
        @Override
        protected ServletResponse wrapServletResponse(HttpServletResponse orig, ShiroHttpServletRequest request) {
        	return new ShiroExtHttpServletResponse(orig, getServletContext(), request);
        }
    }
    
    @Override
    public Map<String, Filter> getFilters() {
    	RouteAuthFilter routeAuthFilter = getRouteAuthFilter();
    	if(routeAuthFilter != null){
    		Map<String, Filter> filters = new LinkedHashMap<String, Filter>();
    		filters.put("routeAuth", getRouteAuthFilter());
    		filters.putAll(getRouteAuthFilter().getFilters());
    		return filters;
    	}
    	return super.getFilters();
    }
    
    public DynamicDefaultFilterChainManager getFilterChainManager() {
		return filterChainManager;
	}

	public void setFilterChainManager(DynamicDefaultFilterChainManager filterChainManager) {
		this.filterChainManager = filterChainManager;
	}
	
	public RouteAuthFilter getRouteAuthFilter() {
		return routeAuthFilter;
	}

	public void setRouteAuthFilter(RouteAuthFilter routeAuthFilter) {
		this.routeAuthFilter = routeAuthFilter;
	}

	public String getRouteAuthFilterName() {
		return routeAuthFilterName;
	}

	public void setRouteAuthFilterName(String routeAuthFilterName) {
		this.routeAuthFilterName = routeAuthFilterName;
	}
}
