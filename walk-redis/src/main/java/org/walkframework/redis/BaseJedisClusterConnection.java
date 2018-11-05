package org.walkframework.redis;

import org.springframework.data.redis.connection.ClusterCommandExecutor;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.ClusterCommandExecutor.ClusterCommandCallback;
import org.springframework.data.redis.connection.ClusterCommandExecutor.MultiKeyClusterCommandCallback;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.connection.jedis.JedisConverters;
import org.springframework.data.redis.connection.jedis.JedisScriptReturnConverter;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * 扩展org.springframework.data.redis.connection.jedis.JedisClusterConnection
 * 
 * @author shf675
 * 
 */
public class BaseJedisClusterConnection extends JedisClusterConnection {

	public BaseJedisClusterConnection(JedisCluster cluster) {
		super(cluster);
	}

	public BaseJedisClusterConnection(JedisCluster cluster, ClusterCommandExecutor executor) {
		super(cluster, executor);
	}
	
	/**
	 * eval命令，JedisClusterConnection不支持此命令，在此扩展，但需传入每个客户端的jedis对象
	 * 
	 * @param client
	 * @param script
	 * @param returnType
	 * @param numKeys
	 * @param keysAndArgs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T eval(Jedis client, byte[] script, ReturnType returnType, int numKeys, byte[]... keysAndArgs) {
		if (isQueueing() || isPipelined()) {
			throw new UnsupportedOperationException();
		}
		try {
			return (T) new JedisScriptReturnConverter(returnType).convert(client.eval(script, JedisConverters.toBytes(numKeys), keysAndArgs));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}
	
	/**
	 * evalSha命令，JedisClusterConnection不支持此命令，在此扩展，但需传入每个客户端的jedis对象
	 * 
	 * @param client
	 * @param scriptSha
	 * @param returnType
	 * @param numKeys
	 * @param keysAndArgs
	 * @return
	 */
	public <T> T evalSha(Jedis client, String scriptSha, ReturnType returnType, int numKeys, byte[]... keysAndArgs) {
		return evalSha(JedisConverters.toBytes(scriptSha), returnType, numKeys, keysAndArgs);
	}

	/**
	 * evalSha命令，JedisClusterConnection不支持此命令，在此扩展，但需传入每个客户端的jedis对象
	 * 
	 * @param client
	 * @param scriptSha
	 * @param returnType
	 * @param numKeys
	 * @param keysAndArgs
	 * @return
	 */
	public <T> T evalSha(Jedis client, byte[] scriptSha, ReturnType returnType, int numKeys, byte[]... keysAndArgs) {
		if (isQueueing() || isPipelined()) {
			throw new UnsupportedOperationException();
		}
		try {
			return (T) new JedisScriptReturnConverter(returnType).convert(client.evalsha(scriptSha, numKeys, keysAndArgs));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/**
	 * {@link Jedis} specific {@link ClusterCommandCallback}.
	 * 
	 * @author Christoph Strobl
	 * @param <T>
	 * @since 1.7
	 */
	public interface JedisClusterCommandCallback<T> extends ClusterCommandCallback<Jedis, T> {
	}

	/**
	 * {@link Jedis} specific {@link MultiKeyClusterCommandCallback}.
	 * 
	 * @author Christoph Strobl
	 * @param <T>
	 * @since 1.7
	 */
	public interface JedisMultiKeyClusterCommandCallback<T> extends MultiKeyClusterCommandCallback<Jedis, T> {
	}

}
