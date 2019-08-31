package org.walkframework.mq.queue.redis;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.walkframework.mq.queue.IQueue;



/**
 * 基于Redis实现的消息队列
 * 
 * 分布式队列，web应用重启不会清空队列，建议在生产使用
 * 
 * @author shf675
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class RedisQueue<E> extends AbstractQueue<E> implements IQueue<E>{
	
	public static final String QUEUE_SUFFIX = "~queues";

	//队列名
	private String queueName;
	
	//队列名字节
	private byte[] queueNameBytes;
	
	private RedisOperations redisOperations;
	
	public RedisQueue(String queueName, RedisOperations redisOperations, RedisSerializer<String> stringSerializer){
		this.queueName = queueName;
		this.redisOperations = redisOperations;
		
		queueName += QUEUE_SUFFIX;//统一在redis里给队列加~queues后缀，标识以~queues为后缀的是队列
		this.queueNameBytes = redisOperations.getKeySerializer().serialize(queueName);
	}

	/**
	 * 入队 如果队列已满，则返回false
	 * 
	 */
	@Override
	public boolean offer(E e) {
		final byte[] valueBytes = serialize(e instanceof ValueWrapper ? e : new ValueWrapper(e));
		return (Boolean) redisOperations.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				connection.lPush(queueNameBytes, valueBytes);
				return true;
			}
		});
	}
	
	/**
	 * 出队 如果队列为空，则返回null
	 * 
	 */
	@Override
	public E poll() {
		return (E) redisOperations.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] bs = connection.rPop(queueNameBytes);
				Object o = deserialize(bs);
				return o instanceof ValueWrapper ? ((ValueWrapper)o).get() : o;
			}
		});
	}
	
	/**
	 * 返回队列头部的元素 如果队列为空，则返回null
	 * 
	 */
	@Override
	public E peek() {
		return (E) redisOperations.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] bs = connection.lIndex(queueNameBytes, size() - 1);
				Object o = deserialize(bs);
				return o instanceof ValueWrapper ? ((ValueWrapper)o).get() : o;
			}
		});
	}
	
	/**
	 * 移除队列指定元素
	 * 
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean remove(final Object o) {
		if(o instanceof ValueWrapper) {
			return (Boolean) redisOperations.execute(new RedisCallback<Boolean>() {
				public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
					Long nums = connection.lRem(queueNameBytes, 1, serialize(o));
					return nums.longValue() > 0;
				}
			});
		} else {
			RedisQueue.QueueIterator iterator = (RedisQueue.QueueIterator)iterator();
			while (iterator.hasNext()) {
				ValueWrapper valueWrapper = iterator.nextValueWrapper();   
				if(Arrays.equals(serialize(valueWrapper.get()), serialize(o))) {
					return remove(valueWrapper);
				}
			}
		}
		return false;
	}
	
	/**
	 * 从队列中删除在values中的元素
	 */
	@Override
	public boolean removeAll(Collection<?> values) {
		boolean modified = false;
		for (Object value : values) {
			if(remove(value) == true){
				modified = true;
			}
		}
		return modified;
	}
	
	/**
	 * 从队列中删除不在values中的元素
	 * 
	 * 为提高效率，采用先删后重新入队的方式
	 */
	@Override
	public boolean retainAll(Collection<?> values) {
		boolean modified = false;
		
		Collection<Object> exists = new ArrayList<Object>();
		
		//1、将存在的元素从队列中删除，并缓存
		for (Object value : values) {
			if(remove(value) == true){
				exists.add(value);
				modified = true;
			}
		}
		
		//2、清空队列
		clear();
		
		//3、将缓存的数据重新入队
		for (Object exist : exists) {
			offer((E)exist);
		}
		return modified;
	}
	
	
	/**
	 * 是否包含某元素
	 * 
	 */
	@Override
	public boolean contains(Object o) {
		Iterator e = iterator();
		if (o == null) {
			return super.contains(o);
		} else {
			while (e.hasNext()) {
				if (Arrays.equals(serialize(o), serialize(e.next()))) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 获取队列大小
	 * 
	 */
	@Override
	public int size() {
		return (Integer) redisOperations.execute(new RedisCallback<Integer>() {
			public Integer doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.lLen(queueNameBytes).intValue();
			}
		});
	}
	
	/**
	 * 迭代器
	 * 
	 */
	@Override
	public Iterator<E> iterator() {
		return (Iterator<E>) redisOperations.execute(new RedisCallback<Iterator<E>>() {
			public Iterator<E> doInRedis(final RedisConnection connection) throws DataAccessException {
				List list = connection.lRange(queueNameBytes, 0, -1);
				return new QueueIterator<E>(list.iterator());
			}
		});
	}
	
	/**
	 * 迭代器
	 * 
	 * 可指定范围
	 */
	@Override
	public Iterator<E> iterator(final int start, final int size) {
		return (Iterator<E>) redisOperations.execute(new RedisCallback<Iterator<E>>() {
			public Iterator<E> doInRedis(RedisConnection connection) throws DataAccessException {
				List list = connection.lRange(queueNameBytes, start, start + size - 1);
				return new QueueIterator<E>(list.iterator());
			}
		});
	}
	
	/**
	 * 获取队列名
	 * 
	 */
	@Override
	public String getQueueName() {
		return this.queueName;
	}
	
	private byte[] serialize(Object value) {
		if (redisOperations.getValueSerializer() == null && value instanceof byte[]) {
			return (byte[]) value;
		}
		return redisOperations.getValueSerializer().serialize(value);
	}
	
	private Object deserialize(byte[] bytes) {
		if (redisOperations.getValueSerializer() == null) {
			return bytes;
		}
		return redisOperations.getValueSerializer().deserialize(bytes);
	}
	
	public class QueueIterator<E> implements Iterator<E> {
		private final Iterator<E> delegate;

		public QueueIterator(Iterator<E> iterator) {
			this.delegate = iterator;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}
		
		@Override
		public E next() {
			Object o = deserialize((byte[]) delegate.next());
			return (E) (o instanceof ValueWrapper ? ((ValueWrapper)o).get() : o);
		}
		
		public ValueWrapper nextValueWrapper() {
			return (ValueWrapper)deserialize((byte[]) delegate.next());
		}

		@Override
		public void remove() {
			delegate.remove();
		}
	}
}