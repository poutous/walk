package org.walkframework.restful.model.req;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 入参模型基类
 * 
 * @author shf675
 */
public abstract class ReqBody implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected static final SerializerFeature[] serializerFeatures = new SerializerFeature[]{SerializerFeature.WriteDateUseDateFormat, SerializerFeature.DisableCircularReferenceDetect};
	
	@Override
	public String toString() {
		return JSON.toJSONString(this, serializerFeatures);
	}

}
