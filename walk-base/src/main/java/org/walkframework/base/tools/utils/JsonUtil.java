package org.walkframework.base.tools.utils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;

/**
 * Json处理类
 * 
 */
public abstract class JsonUtil {
	protected static Logger log = LoggerFactory.getLogger(JsonUtil.class);
	
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 往http输出流输出字符串文本
	 * 
	 * @param result :
	 *            dataset
	 */
	public static String toJson(List<?> result){
		return toJson(result, null);
	}

	/**
	 * 往http输出流输出字符串文本(EasyuiGrid专用) 分页专用
	 * 
	 * @param result
	 * @return
	 */
	public static String toJsonByEasyPagination(List<?> result){
		return toJson(result, "pager");
	}

	/**
	 * 往http输出流输出字符串文本
	 * 
	 * @param result :
	 *            dataset
	 * @param actionType :
	 *            为pager时，表示分页，加入total和rows进行json的组装
	 */
	@SuppressWarnings("unchecked")
	private static String toJson(List<?> result, String actionType) {
		if (result != null && result.size() > 0 && result.get(0) instanceof Map) {
			for (int i = 0; i < result.size(); i++) {
				Map data = (Map) result.get(i);
				// 由于框架查出的字段属性默认都设置为大写，但EasyUi内部使用的某些属性是小写，所以要做转换
				if (data.get("STATE") != null) {
					data.put("state", data.get("STATE"));
					data.remove("STATE");
				}
				if (data.get("PARENTID") != null) {
					data.put("parentId", data.get("PARENTID"));
					data.remove("PARENTID");
				}
				if (data.get("ID") != null) {
					data.put("id", data.get("ID"));
					data.remove("ID");
				}
				if (data.get("TEXT") != null) {
					data.put("text", data.get("TEXT"));
					data.remove("TEXT");
				}
				// 其他内部使用的属性需要转换可以继续添加
			}
		}

		String datas = data2JsonString(result);
		return datas;
	}
	
	/**
	 * idata2JsonString
	 * 
	 * @param src
	 * @return
	 */
	public static String data2JsonString(Object src) {
		if (src != null) {
			SerializeConfig mapping = new SerializeConfig();
			//日期转换
			mapping.put(Date.class, new SimpleDateFormatSerializer(DATE_FORMAT));
			mapping.put(Timestamp.class, new SimpleDateFormatSerializer(DATE_FORMAT));
			
			return JSONArray.toJSONString(src, mapping);
		} else {
			return "";
		}
	}
}