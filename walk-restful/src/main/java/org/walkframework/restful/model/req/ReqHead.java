package org.walkframework.restful.model.req;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 请求头
 * 
 * @author shf675
 */
public abstract class ReqHead implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected static final SerializerFeature[] serializerFeatures = new SerializerFeature[]{SerializerFeature.WriteDateUseDateFormat, SerializerFeature.DisableCircularReferenceDetect};
	
	@Override
	public String toString() {
		return JSON.toJSONString(this, serializerFeatures);
	}

}
