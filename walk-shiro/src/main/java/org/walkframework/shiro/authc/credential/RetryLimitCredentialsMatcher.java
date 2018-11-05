package org.walkframework.shiro.authc.credential;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.shiro.authc.token.FormToken;

/**
 * 密码重试次数控制
 * 
 * @author shf675
 *
 */
public class RetryLimitCredentialsMatcher extends HashedCredentialsMatcher {

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	private static final String PASSWORD_RETRY_CACHE = "passwordRetryCache";
	
	private int retryNums = 5;//默认重试5次

	//集群中可能会导致出现验证多过retryNums次的现象，因为AtomicInteger只能保证单节点并发    
	private Cache<String, AtomicInteger> passwordRetryCache;

	public RetryLimitCredentialsMatcher(CacheManager cacheManager) {
		passwordRetryCache = cacheManager.getCache(PASSWORD_RETRY_CACHE);
	}

	@Override
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		String username = ((FormToken)token).getUsername();
		//retry count + 1
		AtomicInteger retryCount = passwordRetryCache.get(username);
		if (null == retryCount) {
			retryCount = new AtomicInteger(0);
			passwordRetryCache.put(username, retryCount);
		}
		if (retryCount.incrementAndGet() > retryNums) {
			log.warn("username: {} tried to login more than {} times in period", username, retryNums);
			throw new ExcessiveAttemptsException("username: " + username + " tried to login more than " + retryNums + " times in period");
		}
		boolean matches = super.doCredentialsMatch(token, info);
		if (matches) {
			//clear retry data
			passwordRetryCache.remove(username);
		}
		return matches;
	}

	public void setRetryNums(int retryNums) {
		this.retryNums = retryNums;
	}
}