package org.walkframework.restful.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回报文常量
 * 
 * @author shf675
 * 
 */
public interface RspConstants {
	Integer SUCCESS = 0;
	Integer INTERNAL_ERROR = -1;
	Integer SUBMIT_METHOD_ERROR = -2;
	Integer VALID_ERROR = -3;
	Integer FORMAT_ERROR = -4;
	Integer UNAUTHORIZED_ERROR = -5;
	Integer OTHER_ERROR = -99;

	Map<Integer, String> RSP = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(SUCCESS, "成功");
			put(INTERNAL_ERROR, "内部错误");
			put(SUBMIT_METHOD_ERROR, "提交方式错误");
			put(VALID_ERROR, "请求报文校验错误");
			put(FORMAT_ERROR, "请求报文格式错误");
			put(UNAUTHORIZED_ERROR, "无权限错误");
			put(OTHER_ERROR, "其他错误");
		}
	};
}
