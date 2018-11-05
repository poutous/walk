package org.walkframework.shiro.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.collections.IteratorUtils;
import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;

import com.octo.captcha.Captcha;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.captchastore.CaptchaAndLocale;
import com.octo.captcha.service.captchastore.CaptchaStore;

/**
 * 验证码存储
 * 
 * @author shf675
 *
 */
public class CacheCaptchaStore implements CaptchaStore {

	public static String SESSIONCAPTCHA = "session_captcha";

	public static String CACHE_NAME = "JCAPTCHA_IMAGE_CACHE";
	
	private int minGuarantedStorageDelayInSeconds;

	/**
	 * 缓存管理器
	 */
	private ICacheManager cacheManager;

	/**
	 * 获取锁的cache对象
	 * 
	 * @return
	 */
	private ICache getCache() {
		return getCacheManager().getICache(CACHE_NAME);
	}

	/**
	 * Check if a captcha is stored for this id
	 * 
	 * @return true if a captcha for this id is stored, false otherwise
	 */
	public boolean hasCaptcha(String id) {
		return getCaptcha(id) != null;
	}

	/**
	 * Store the captcha with the provided id as key. The key is assumed to be
	 * unique, so if the same key is used twice to store a captcha, the store
	 * will return an exception
	 * 
	 * @param id
	 *            the key
	 * @param captcha
	 *            the captcha
	 * 
	 * @throws CaptchaServiceException
	 *             if the captcha already exists, or if an error occurs during
	 *             storing routine.
	 */
	public void storeCaptcha(String id, Captcha captcha) throws CaptchaServiceException {
		storeCaptcha(id, captcha, null);
	}

	/**
	 * Store the captcha with the provided id as key. The key is assumed to be
	 * unique, so if the same key is used twice to store a captcha, the store
	 * will return an exception
	 * 
	 * @param id
	 *            the key
	 * @param captcha
	 *            the captcha
	 * @param locale
	 *            the locale used that triggers the captcha generation
	 * @throws com.octo.captcha.service.CaptchaServiceException
	 *             if the captcha already exists, or if an error occurs during
	 *             storing routine.
	 */
	public void storeCaptcha(String id, Captcha captcha, Locale locale) throws CaptchaServiceException {
		getCache().put(SESSIONCAPTCHA + id, new CaptchaAndLocale(captcha, locale));
		getCache().expire(SESSIONCAPTCHA + id, minGuarantedStorageDelayInSeconds);
	}

	/**
	 * Retrieve the captcha for this key from the store.
	 * 
	 * @return the captcha for this id
	 * 
	 * @throws CaptchaServiceException
	 *             if a captcha for this key is not found or if an error occurs
	 *             during retrieving routine.
	 */
	public Captcha getCaptcha(String id) throws CaptchaServiceException {
		Object captchaAndLocale = getCache().getValue(SESSIONCAPTCHA + id);
		return captchaAndLocale != null ? ((CaptchaAndLocale) captchaAndLocale).getCaptcha() : null;
	}

	/**
	 * Retrieve the locale for this key from the store.
	 * 
	 * @return the locale for this id, null if not found
	 * @throws com.octo.captcha.service.CaptchaServiceException
	 *             if an error occurs during retrieving routine.
	 */
	public Locale getLocale(String id) throws CaptchaServiceException {
		Object captchaAndLocale = getCache().getValue(SESSIONCAPTCHA + id);
		return captchaAndLocale != null ? ((CaptchaAndLocale) captchaAndLocale).getLocale() : null;
	}

	/**
	 * Remove the captcha with the provided id as key.
	 * 
	 * @param id
	 *            the key
	 * 
	 * @return true if found, false otherwise
	 * 
	 * @throws CaptchaServiceException
	 *             if an error occurs during remove routine
	 */
	public boolean removeCaptcha(String id) {
		if (getCache().getValue(SESSIONCAPTCHA + id) != null) {
			getCache().evict(SESSIONCAPTCHA + id);
			return true;
		}
		return false;
	}

	/**
	 * get the size of this store
	 */
	public int getSize() {
		Long size = getCache().size();
		return size == null ? 0 : size.intValue();
	}

	/**
	 * Return all the contained keys
	 */
	@SuppressWarnings("unchecked")
	public Collection getKeys() {
		Iterator<Object> iterator = getCache().keys();
		return iterator == null ? null : IteratorUtils.toList(iterator);
	}

	/**
	 * Empty the store
	 */
	public void empty() {
		getCache().clear();
	}

	public void cleanAndShutdown() {
		getCache().clear();
	}

	public void initAndStart() {
		// TODO Auto-generated method stub

	}

	public ICacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public int getMinGuarantedStorageDelayInSeconds() {
		return minGuarantedStorageDelayInSeconds;
	}

	public void setMinGuarantedStorageDelayInSeconds(int minGuarantedStorageDelayInSeconds) {
		this.minGuarantedStorageDelayInSeconds = minGuarantedStorageDelayInSeconds;
	}
}
