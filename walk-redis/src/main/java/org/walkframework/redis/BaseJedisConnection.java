package org.walkframework.redis;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConverters;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.KeyBoundCursor;
import org.springframework.data.redis.core.ScanCursor;
import org.springframework.data.redis.core.ScanIteration;
import org.springframework.data.redis.core.ScanOptions;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.util.Pool;

/**
 * jedis连接
 * 
 * 重写scan命令(原生返回的key只支持String类型，现修改为可支持任何可序列化的对象)
 * 
 * @author shf675
 * 
 */
public class BaseJedisConnection extends JedisConnection {
	
	private BaseJedisConnectionFactory jedisConnectionFactory;
	
	/**
	 * scan命令专用jedis
	 */
	private Jedis scanJedis;
	
	public BaseJedisConnection(Jedis jedis) {
		super(jedis);
	}

	public BaseJedisConnection(Jedis jedis, Pool<Jedis> pool, int dbIndex) {
		super(jedis, pool, dbIndex);
	}
	
	protected BaseJedisConnection(Jedis jedis, Pool<Jedis> pool, int dbIndex, String clientName, BaseJedisConnectionFactory jedisConnectionFactory) {
		super(jedis, pool, dbIndex, clientName);
		this.jedisConnectionFactory = jedisConnectionFactory;
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.redis.connection.jedis.JedisConnection#scan(long, org.springframework.data.redis.core.ScanOptions)
	 */
	@Override
	public Cursor<byte[]> scan(long cursorId, ScanOptions options) {
		return new ScanCursor<byte[]>(cursorId, options) {
			@Override
			protected ScanIteration<byte[]> doScan(long cursorId, ScanOptions options) {
				if (isQueueing() || isPipelined()) {
					throw new UnsupportedOperationException("'SCAN' cannot be called in pipeline / transaction mode.");
				}
				ScanParams params = JedisConverters.toScanParams(options);
				
				//重写原生的scan方法
				//getScanJedis()
			    ScanResult<byte[]> scanResult = getScanJedis().scan(JedisConverters.toBytes(cursorId), params);
				return new ScanIteration<byte[]>(Long.valueOf(scanResult.getStringCursor()), scanResult.getResult());
			}
			
			protected void doClose() {
				closeScanJedis();
			};

		}.open();
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.data.redis.connection.jedis.JedisConnection#zScan(byte[], java.lang.Long, org.springframework.data.redis.core.ScanOptions)
	 */
	@Override
	public Cursor<Tuple> zScan(byte[] key, Long cursorId, ScanOptions options) {

		return new KeyBoundCursor<Tuple>(key, cursorId, options) {

			@Override
			protected ScanIteration<Tuple> doScan(byte[] key, long cursorId, ScanOptions options) {

				if (isQueueing() || isPipelined()) {
					throw new UnsupportedOperationException("'ZSCAN' cannot be called in pipeline / transaction mode.");
				}

				ScanParams params = JedisConverters.toScanParams(options);
				//getScanJedis()
				ScanResult<redis.clients.jedis.Tuple> result = getScanJedis().zscan(key, JedisConverters.toBytes(cursorId), params);
				return new ScanIteration<RedisZSetCommands.Tuple>(Long.valueOf(result.getStringCursor()),
						JedisConverters.tuplesToTuples().convert(result.getResult()));
			}

			protected void doClose() {
				closeScanJedis();
			};

		}.open();
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.data.redis.connection.jedis.JedisConnection#sScan(byte[], long, org.springframework.data.redis.core.ScanOptions)
	 */
	public Cursor<byte[]> sScan(byte[] key, long cursorId, ScanOptions options) {

		return new KeyBoundCursor<byte[]>(key, cursorId, options) {

			@Override
			protected ScanIteration<byte[]> doScan(byte[] key, long cursorId, ScanOptions options) {

				if (isQueueing() || isPipelined()) {
					throw new UnsupportedOperationException("'SSCAN' cannot be called in pipeline / transaction mode.");
				}

				ScanParams params = JedisConverters.toScanParams(options);
				//getScanJedis()
				redis.clients.jedis.ScanResult<byte[]> result = getScanJedis().sscan(key, JedisConverters.toBytes(cursorId), params);
				return new ScanIteration<byte[]>(Long.valueOf(result.getStringCursor()), result.getResult());
			}

			protected void doClose() {
				closeScanJedis();
			};
		}.open();
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.data.redis.connection.jedis.JedisConnection#hScan(byte[], long, org.springframework.data.redis.core.ScanOptions)
	 */
	public Cursor<Entry<byte[], byte[]>> hScan(byte[] key, long cursorId, ScanOptions options) {

		return new KeyBoundCursor<Map.Entry<byte[], byte[]>>(key, cursorId, options) {

			@Override
			protected ScanIteration<Entry<byte[], byte[]>> doScan(byte[] key, long cursorId, ScanOptions options) {

				if (isQueueing() || isPipelined()) {
					throw new UnsupportedOperationException("'HSCAN' cannot be called in pipeline / transaction mode.");
				}

				ScanParams params = JedisConverters.toScanParams(options);
				//getScanJedis()
				ScanResult<Entry<byte[], byte[]>> result = getScanJedis().hscan(key, JedisConverters.toBytes(cursorId), params);
				return new ScanIteration<Map.Entry<byte[], byte[]>>(Long.valueOf(result.getStringCursor()), result.getResult());
			}

			protected void doClose() {
				closeScanJedis();
			};

		}.open();
	}
	
	/**
	 * scan命令获取一个新的jedis对象，避免造成java.lang.ClassCastException: [B cannot be cast to XXX错误
	 * 
	 * @return
	 */
	private Jedis getScanJedis() {
		if(scanJedis == null){
			scanJedis = jedisConnectionFactory.fetchJedisConnector(false);
		}
		return scanJedis;
	}
	
	/**
	 * 关闭jedis
	 */
	private void closeScanJedis(){
		Exception exc = null;
		try {
			//退出
			scanJedis.quit();
		} catch (Exception ex) {
			exc = ex;
		}
		try {
			//关闭连接
			if(scanJedis.isConnected()){
				scanJedis.disconnect();
			}
			//关闭jedis对象
			scanJedis.close();
		} catch (Exception ex) {
			exc = ex;
		}
		if (exc != null)
			throw convertJedisAccessException(exc);
	}
}
