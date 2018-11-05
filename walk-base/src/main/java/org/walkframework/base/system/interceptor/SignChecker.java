package org.walkframework.base.system.interceptor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.walkframework.base.mvc.entity.TdMAppCfg;
import org.walkframework.base.mvc.service.common.AbstractBaseService;
import org.walkframework.base.system.exception.SignCheckException;
import org.walkframework.cache.ICache;
import org.walkframework.cache.redis.RedisCacheDecorator;
import org.walkframework.cache.util.ReflectHelper;
import org.walkframework.shiro.cache.ShiroCacheManager;
import org.walkframework.shiro.util.ISignChecker;

/**
 * 签名校验拦截器
 * 
 */
public class SignChecker extends AbstractBaseService implements ApplicationContextAware, ISignChecker {

	/**
	 * 示例：300000为5分钟 即以当前时间为准，向前5分钟，向后5分钟，此时间范围内生成的时间戳有效
	 * 
	 */
	private static final long DEFAULT_TIME_DIFF_MILLIS = 300000;

	private static final String APP_CACHE_NAME = "APP_CFG_CACHE_NAME";

	private static final String SIGN_COUNT_CACHE_NAME = "SIGN_COUNT_CACHE_NAME";

	private ApplicationContext applicationContext;// 声明一个静态变量保存

	private ShiroCacheManager cacheManager;
	
	private StringRedisSerializer serializer = new StringRedisSerializer();
	
	/**
	 * 获取app配置信息
	 * 
	 * @param appId
	 * @return
	 */
	@SuppressWarnings("serial")
	protected TdMAppCfg getAppCfg(final String appId){
		return dao().selectOne(new TdMAppCfg(){{
			setAppId(appId).asCondition();
		}});
	}

	@SuppressWarnings( { "serial", "unchecked" })
	@Override
	public TdMAppCfg check(final String appId, final String timestamp, final String sign) {
		// 缓存处理
		ICache appCache = cacheManager.getCacheManager().getICache(APP_CACHE_NAME);
		TdMAppCfg appCfg = appCache.getValue(appId);
		if (appCfg == null) {
			appCfg = getAppCfg(appId);

			// appId是否存在
			if (appCfg == null) {
				throw new SignCheckException(String.format("appId[%s]不存在，请联系服务提供方。", appId));
			}
			appCache.put(appId, appCfg);
		}

		// appId状态检查
		if ("0".equals(appCfg.getAppState())) {
			throw new SignCheckException(String.format("appId[%s]已失效，请联系服务提供方。", appId));
		}
		if ("4".equals(appCfg.getAppState())) {
			throw new SignCheckException(String.format("appId[%s]已锁定，请联系服务提供方。", appId));
		}

		// appId是否过期
		Date currentTime = new Timestamp(System.currentTimeMillis());
		if (!(currentTime.after(appCfg.getStartDate()) && currentTime.before(appCfg.getEndDate()))) {
			throw new SignCheckException(String.format("签名校验错误：appId[%s]已过期，请联系服务提供方。startDate:%s endDate:%s", appId, decodeTimestamp("yyyy-MM-dd HH:mm:ss", appCfg.getStartDate()), decodeTimestamp("yyyy-MM-dd HH:mm:ss", appCfg.getEndDate())));
		}

		//如果程序校验签名...
		if ("1".equals(appCfg.getSignCheck())) {
			// 1、时间戳校验
			Long timestampMillis = 0L;
			try {
				timestampMillis = Long.parseLong(timestamp);
			} catch (NumberFormatException e) {
				throw new SignCheckException(String.format("时间戳[%s]非法", timestamp));
			}
			final long timeDiffMillis = Long.parseLong(applicationContext.getEnvironment().getProperty("validate.timeDiffMillis", DEFAULT_TIME_DIFF_MILLIS + ""));
			long beforeTime = System.currentTimeMillis() - timeDiffMillis;
			long afterTime = System.currentTimeMillis() + timeDiffMillis;
			if (!(timestampMillis > beforeTime && timestampMillis < afterTime)) {
				throw new SignCheckException(String.format("时间戳[%s]已过期", timestamp));
			}

			// 2、签名有效性校验
			String appKey = appCfg.getAppKey();
			String genSign = DigestUtils.sha256Hex(appId + timestamp + appKey);
			if (!genSign.equals(sign)) {
				throw new SignCheckException(String.format("无效的签名[%s]", sign));
			}

			// 3、签名使用次数校验
			// 使用redis缓存时可实现分布式环境中的次数校验
			if (appCache instanceof RedisCacheDecorator) {
				final RedisCacheDecorator redisCache = (RedisCacheDecorator) appCache;
				final RedisOperations redisOperations = (RedisOperations) redisCache.getNativeCache();
				Long count = (Long) redisOperations.execute(new RedisCallback<Long>() {
					public Long doInRedis(RedisConnection connection) throws DataAccessException {
						String incrKey = "incr:signfreq:" + sign;
						byte[] keyBytes = (byte[]) ReflectHelper.invokeMethod(redisCache, redisCache.getClass(), "computeKey", new Object[] { incrKey }, new Class[] { Object.class });
						Boolean nx = connection.setNX(keyBytes, serializer.serialize("0"));
						if (nx) {
							//设置过期时间，防止内存过载
							connection.expire(keyBytes, timeDiffMillis / 1000);
						}
						return connection.incr(keyBytes);
					}
				});
				if (count > appCfg.getSignFreq()) {
					throw new SignCheckException(String.format("签名[%s]已超过使用次数[%s]", sign, appCfg.getSignFreq()));
				}
			} else {
				// 未使用redis做缓存时，仅为单机部署情况下有效
				ICache countCache = cacheManager.getCacheManager().getICache(SIGN_COUNT_CACHE_NAME);
				AtomicInteger count = countCache.getValue(sign);
				if (count == null) {
					count = new AtomicInteger(0);
					countCache.put(sign, count);
				}
				if (count.incrementAndGet() > appCfg.getSignFreq().longValue()) {
					throw new SignCheckException(String.format("签名[%s]已超过使用次数限制[%s]", sign, appCfg.getSignFreq()));
				}
			}
		}
		return appCfg;
	}

	public String decodeTimestamp(String format, Date time) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(time);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public ShiroCacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(ShiroCacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}