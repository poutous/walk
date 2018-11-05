package org.walkframework.redis;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.ClusterCommandExecutor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.ReflectionUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.util.Pool;

/**
 * jedis连接工厂
 * 
 * 重新实现getConnection方法
 * 
 * @author shf675
 *
 */
public class BaseJedisConnectionFactory extends JedisConnectionFactory {
	
	protected final static Logger log = LoggerFactory.getLogger(BaseJedisConnectionFactory.class);
	
	private Pool<Jedis> pool;
	private JedisCluster cluster;
	private ClusterCommandExecutor clusterCommandExecutor;

	@Override
	public RedisConnection getConnection() {
		if (getCluster() != null) {
			return getClusterConnection();
		}
		Jedis jedis = fetchJedisConnector();
		JedisConnection connection = (getUsePool() ? new BaseJedisConnection(jedis, getPool(), getDatabase(), getClientName(), this) : new BaseJedisConnection(jedis, null, getDatabase(), getClientName(), this));
		connection.setConvertPipelineAndTxResults(getConvertPipelineAndTxResults());
		return postProcessConnection(connection);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisConnectionFactory#getClusterConnection()
	 */
	@Override
	public BaseJedisClusterConnection getClusterConnection() {

		if (getCluster() == null) {
			throw new InvalidDataAccessApiUsageException("Cluster is not configured!");
		}
		return new BaseJedisClusterConnection(getCluster(), getClusterCommandExecutor());
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.data.redis.connection.jedis.JedisConnectionFactory#fetchJedisConnector()
	 */
	@Override
	public Jedis fetchJedisConnector() {
		return fetchJedisConnector(true);
	}
	
	/**
	 * 获取jedis对象
	 * 
	 * @param tryFetchFromPool:true，表示尝试从池中获取
	 * @return
	 */
	public Jedis fetchJedisConnector(boolean tryFetchFromPool) {
		try {
			if(tryFetchFromPool){
				return super.fetchJedisConnector();
			}

			Jedis jedis = new Jedis(getShardInfo());
			// force initialization (see Jedis issue #82)
			jedis.connect();

			//potentiallySetClientName(jedis);
			Method method = ReflectionUtils.findMethod(this.getClass().getSuperclass(), "potentiallySetClientName", Jedis.class);
			ReflectionUtils.makeAccessible(method);
			ReflectionUtils.invokeMethod(method, this, jedis);
			return jedis;
		} catch (Exception ex) {
			throw new RedisConnectionFailureException("Cannot get Jedis connection", ex);
		}
	}

	@SuppressWarnings("unchecked")  
	public Pool<Jedis> getPool() {
		if (pool == null) {
			Field field = ReflectionUtils.findField(this.getClass().getSuperclass(), "pool");
			if (field != null) {
				ReflectionUtils.makeAccessible(field);
				try {
					pool = (Pool<Jedis>) field.get(this);
				} catch (IllegalArgumentException e) {
					log.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return pool;
	}
	
	public JedisCluster getCluster() {
		if (cluster == null) {
			Field field = ReflectionUtils.findField(this.getClass().getSuperclass(), "cluster");
			if (field != null) {
				ReflectionUtils.makeAccessible(field);
				try {
					cluster = (JedisCluster) field.get(this);
				} catch (IllegalArgumentException e) {
					log.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return cluster;
	}
	
	public ClusterCommandExecutor getClusterCommandExecutor() {
		if (clusterCommandExecutor == null) {
			Field field = ReflectionUtils.findField(this.getClass().getSuperclass(), "clusterCommandExecutor");
			if (field != null) {
				ReflectionUtils.makeAccessible(field);
				try {
					clusterCommandExecutor = (ClusterCommandExecutor) field.get(this);
				} catch (IllegalArgumentException e) {
					log.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return clusterCommandExecutor;
	}
}
