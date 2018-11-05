package org.walkframework.base.system.serializer;

import java.io.Serializable;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * 自定义序列化/反序列化工具
 * Thrift/Google Protocol Buffer/hessian
 *
 */
public class XSerializer implements RedisSerializer<Serializable> {

	@Override
	public Serializable deserialize(byte[] paramArrayOfByte) throws SerializationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] serialize(Serializable paramT) throws SerializationException {
		// TODO Auto-generated method stub
		return null;
	}

}
