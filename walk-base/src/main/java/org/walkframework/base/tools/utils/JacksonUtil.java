package org.walkframework.base.tools.utils;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Json处理类
 * 
 * 基于JACKSON的json工具类
 * 
 * JACKSON反序列化时可以调用set方法，在某些情况下是必要的。
 * 
 */
public abstract class JacksonUtil {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	//初始化配置
	static {
		
		//设置日期对象格式化
		objectMapper.setDateFormat(dateFormat);
		
		//设置null值不参加序列化
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		
		/**
        * 序列换成json时,将所有的long变成string
        * 因为js中得数字类型不能包含所有的java long值
		**/
		SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
	}
	
	/**
	 * JSON序列化
	 * 
	 * @param src
	 * @return
	 */
	public static String toJSONString(Object src){
		try {
			return objectMapper.writeValueAsString(src);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * JSON反序列化
	 * 
	 * 调用示例：TdMFile file = parseObject(string, TdMFile.class)
	 * @param <T>
	 * @param json
	 * @param valueType
	 * @return
	 */
	public static <T> T parseObject(String json, Class<T> valueType){
		try {
			return objectMapper.readValue(json, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * 带泛型的JSON反序列化
	 * 
	 * 调用示例：List<TdMFile> list = parseObject(string, new TypeReference<List<TdMFile>>(){})
	 * @param <T>
	 * @param json
	 * @param valueTypeRef
	 * @return
	 */
	public static <T> T parseObject(String json, TypeReference<?> valueTypeRef){
		try {
			return objectMapper.readValue(json, valueTypeRef);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}