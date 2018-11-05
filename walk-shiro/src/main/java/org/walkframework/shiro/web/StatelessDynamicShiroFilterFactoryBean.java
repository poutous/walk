package org.walkframework.shiro.web;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.walkframework.shiro.web.filter.mgt.DynamicDefaultFilterChainManager;
import org.walkframework.shiro.web.filter.mgt.DynamicPathMatchingFilterChainResolver;
import org.walkframework.shiro.web.servlet.BodyReaderHttpServletRequestWrapper;
import org.walkframework.shiro.web.servlet.ShiroExtHttpServletResponse;


public class StatelessDynamicShiroFilterFactoryBean extends DynamicShiroFilterFactoryBean{
	
	private static transient final Logger log = LoggerFactory.getLogger(StatelessDynamicShiroFilterFactoryBean.class);
	
	
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
        
        @Override
        protected ServletRequest wrapServletRequest(HttpServletRequest orig) {
        	return new BodyReaderHttpServletRequestWrapper(orig);
        }
    }
}
