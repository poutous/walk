package org.walkframework.base.system.http;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.http.annotation.ThreadSafe;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieIdentityComparator;
import org.apache.http.impl.client.BasicCookieStore;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;

/**
 * httpclient CookieStore
 * 
 * 支持集群的CookieStore
 *
 */
@ThreadSafe
public class ClusterBasicCookieStore extends BasicCookieStore{
	
	private static final long serialVersionUID = 1L;
	
	private final String CACHE_HTTP_CLIENT_COOKIES = "CACHE_HTTP_CLIENT_COOKIES";
	
	private String cookieStoreKey;
	
	private long expire;
	
	public ClusterBasicCookieStore(String cookieStoreKey, long expire){
		this.cookieStoreKey = cookieStoreKey;
	}
	
    /**
     * Adds an {@link Cookie HTTP cookie}, replacing any existing equivalent cookies.
     * If the given cookie has already expired it will not be added, but existing
     * values will still be removed.
     *
     * @param cookie the {@link Cookie cookie} to be added
     *
     * @see #addCookies(Cookie[])
     *
     */
    public synchronized void addCookie(final Cookie cookie) {
        if (cookie != null) {
            // first remove any old cookie that is equivalent
        	TreeSet<Cookie> cookies = getCookiesFromCache();
            cookies.remove(cookie);
            if (!cookie.isExpired(new Date())) {
                cookies.add(cookie);
            }
            setCookiesToCache(cookies);
        }
    }
    
    /**
     * Returns an immutable array of {@link Cookie cookies} that this HTTP
     * state currently contains.
     *
     * @return an array of {@link Cookie cookies}.
     */
    public synchronized List<Cookie> getCookies() {
        //create defensive copy so it won't be concurrently modified
        return new ArrayList<Cookie>(getCookiesFromCache());
    }

    /**
     * Removes all of {@link Cookie cookies} in this HTTP state
     * that have expired by the specified {@link java.util.Date date}.
     *
     * @return true if any cookies were purged.
     *
     * @see Cookie#isExpired(Date)
     */
    public synchronized boolean clearExpired(final Date date) {
        if (date == null) {
            return false;
        }
        TreeSet<Cookie> cookies = getCookiesFromCache();
        boolean removed = false;
        for (final Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
            if (it.next().isExpired(date)) {
                it.remove();
                removed = true;
            }
        }
        setCookiesToCache(cookies);
        return removed;
    }

    /**
     * Clears all cookies.
     */
    public synchronized void clear() {
    	getCookiesFromCache().clear();
    	setCookiesToCache(null);
    }

    @Override
    public synchronized String toString() {
        return getCookiesFromCache().toString();
    }
    
	@SuppressWarnings("unchecked")
	private synchronized TreeSet<Cookie> getCookiesFromCache() {
		Object o = getCache().getValue(cookieStoreKey);
		if(o == null){
			TreeSet<Cookie> cookies = new TreeSet<Cookie>(new CookieIdentityComparator());
			setCookiesToCache(cookies);
			return cookies;
		}
		return (TreeSet<Cookie>) o;
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void setCookiesToCache(TreeSet<Cookie> cookies) {
		getCache().put(cookieStoreKey, cookies);
		if(this.expire > 0L){
			getCache().expire(cookieStoreKey, this.expire);
		}
	}

	public ICache getCache() {
		return SpringContextHolder.getBean(SpringPropertyHolder.getContextProperty("cacheManagerName", "springCacheManager"), ICacheManager.class).getICache(CACHE_HTTP_CLIENT_COOKIES);
	}
}
