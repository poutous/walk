package org.walkframework.console.mvc.service.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import net.sf.ehcache.Ehcache;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.stereotype.Service;
import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;
import org.walkframework.cache.ehcache.EhCacheDecorator;
import org.walkframework.cache.redis.RedisCacheDecorator;
import org.walkframework.cache.util.CollectionHelper;
import org.walkframework.console.mvc.service.base.BaseConsoleService;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;
import org.walkframework.data.util.InParam;
import org.walkframework.shiro.session.BaseSessionDAO;
import org.walkframework.shiro.session.BaseSessionIdGenerator;
import org.walkframework.shiro.util.password.Encryptor;

/**
 * 会话管理
 * 
 * @author shf675
 * 
 */
@Service("sessionManagerService")
public class SessionManagerService extends BaseConsoleService {

	@Resource(name = "${cacheManagerName}")
	private ICacheManager cacheManager;

	@Resource(name = "sessionDAO")
	private BaseSessionDAO sessionDAO;
	
	/**
	 * 会话列表查询
	 * 
	 * @param inParam
	 * @param pagination
	 * @return
	 */
	@SuppressWarnings( { "unchecked", "serial" })
	public PageData<IData<String, Object>> querySessionList(InParam<String, Object> inParam, Pagination pagination) {
		// 分页对象
		PageData<IData<String, Object>> pageData = new PageData<IData<String, Object>>();

		// 设置分页数量
		pageData.setPageSize(pagination.getSize());

		//sessionId
		final String sessionId = inParam.getString("sessionId", "");

		//账号
		final String account = inParam.getString("account", "");

		// 查询条件。sessionId与account都不为空时只以sessionId进行过滤。
		final String qryCond = !"".equals(sessionId) ? sessionId : (!"".equals(account) ? BaseSessionIdGenerator.USER_NAME_PREFIX.concat(Encryptor.encrypt(account, ((BaseSessionIdGenerator)sessionDAO.getSessionIdGenerator()).getSessionIdSalt()).toLowerCase()) : "");

		final String cacheName = sessionDAO.getActiveSessionsCacheName();
		List<IData<String, Object>> elements = new ArrayList<IData<String, Object>>();
		final ICache cache = cacheManager.getICache(cacheName);
		if (cache instanceof RedisCacheDecorator) {
			String keyPattern = "".equals(qryCond) ? "*" : "*" + qryCond + "*";
			RedisCacheDecorator redisCache = (RedisCacheDecorator) cache;
			Iterator<Object> iter = redisCache.keys(pagination.getStart(), pagination.getSize(), "*");
			if (iter != null) {
				int i = 0;
				while (iter.hasNext()) {
					final Object key = iter.next();
					addData(elements, cache, key);
					i++;
				}
				// 设置总数
				pageData.setTotal(redisCache.size(keyPattern));
			}

		} else if (cache instanceof EhCacheDecorator) {
			Collection keys = ((Ehcache) cache.getNativeCache()).getKeys();
			// 查找key
			if (!"".equals(qryCond)) {
				keys = CollectionUtils.select(keys, new Predicate() {
					@Override
					public boolean evaluate(Object key) {
						String keyPattern = String.format("(.*)%s(.*)", qryCond);
						if (key instanceof String && key.toString().matches(keyPattern)) {
							return true;
						}
						return false;
					}
				});
			}
			// 设置总数
			pageData.setTotal(keys.size());

			// 根据分页参数进行分割
			List<Object> sublist = CollectionHelper.subCollection(keys, pagination.getStart(), pagination.getSize());
			int i = 0;
			for (final Object key : sublist) {
				addData(elements, cache, key);
				i++;
			}
		}
		// 设置结果集
		pageData.setRows(elements);
		return pageData;
	}
	
	/**
	 * 强制下线
	 * 
	 * @param inParam
	 * @return
	 */
	public void forceLogout(InParam<String, Object> inParam) {
		String[] sessionIds = inParam.getString("sessionIds", "").split(",");
		if (sessionIds.length == 0) {
			common.error("未选择任何记录！");
		}
		// 循环移除
		Cache<Serializable, Session> sessionCache = sessionDAO.getCacheManager().getCache(sessionDAO.getActiveSessionsCacheName());
		for (String sessionId : sessionIds) {
			Session session = null;
			try {
				session = sessionDAO.readSession(sessionId);
				sessionDAO.delete(session);
			} catch (Exception e) {
				sessionCache.remove(sessionId);
			}
			if (log.isInfoEnabled()) {
				log.info("The account[{}] was forced offline. sessionId:{}", getAccountId(session), sessionId);
			}
		}
	}

	/**
	 * 设置会话时长
	 * 
	 * @param inParam
	 * @return
	 */
	public void setSessionTimeout(InParam<String, Object> inParam) {
		Long timeout = inParam.getLong("timeout");
		String[] sessionIds = inParam.getString("sessionIds", "").split(",");
		if (sessionIds.length == 0) {
			common.error("未选择任何记录！");
		}

		// 循环设置会话时长
		ICache cache = cacheManager.getICache(sessionDAO.getActiveSessionsCacheName());
		for (String sessionId : sessionIds) {
			Session session = sessionDAO.readSession(sessionId);
			session.setTimeout(timeout * 1000);
			cache.expire(sessionId, timeout);
			if (log.isInfoEnabled()) {
				log.info("The account[{}] session time has been reset. sessionId:{}", getAccountId(session), sessionId);
			}
		}
	}
	
	/**
	 * 添加数据
	 * 
	 * @param elements
	 * @param cache
	 * @param key
	 * @param index
	 */
	private void addData(List<IData<String, Object>> elements, ICache cache, Object key){
		Session session = null;
		try {
			session = cache.getValue(key);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		IData<String, Object> data = new DataMap<String, Object>();
		data.put("sessionId", key);
		data.put("sessionTTL", cache.ttl(key));
		if(session != null){
			data.put("account", getAccountId(session));
			data.put("host", session.getHost());
			data.put("lastAccessTime", common.decodeTimestamp("yyyy-MM-dd HH:mm:ss", session.getLastAccessTime()));
			data.put("startTimestamp", common.decodeTimestamp("yyyy-MM-dd HH:mm:ss", session.getStartTimestamp()));
			data.put("timeout", session.getTimeout());
		}
		elements.add(data);
		
	}

	/**
	 * 获取登录账号
	 * 
	 * @param session
	 * @return
	 */
	private String getAccountId(Session session) {
		if(session != null){
			Object principal = session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
			return principal != null ? principal.toString() : "";
		}
		return "";
	}
}
