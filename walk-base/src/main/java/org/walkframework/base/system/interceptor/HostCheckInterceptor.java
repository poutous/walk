package org.walkframework.base.system.interceptor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.walkframework.base.mvc.entity.TdSParam;
import org.walkframework.base.system.annotation.HostCheck;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.base.tools.utils.ParamTranslateUtil;

/**
 * 请求来源IP检查拦截器
 * 
 * @author shf675
 */
public class HostCheckInterceptor extends BaseInterceptor {

	/**
	 * 请求主机校验器拦截
	 * 
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			// host校验开关
			String validateHost = SpringPropertyHolder.getContextProperty("validate.host", "false");
			if ("false".equals(validateHost)) {
				return true;
			}

			// 获取来源IP
			String srcIp = getRequestSrcIp(request);

			// 获取方法指定的校验参数注解
			HandlerMethod method = (HandlerMethod) handler;
			HostCheck hostCheck = method.getMethodAnnotation(HostCheck.class);

			// 根据指定的主机类型
			if (hostCheck != null && !StringUtils.isEmpty(hostCheck.value())) {
				List<TdSParam> list = ParamTranslateUtil.staticlist(hostCheck.value());
				if (list == null) {
					writerRespInfo(response, "-1", "IP allow list without configuration[" + hostCheck.value() + "].");
					return false;
				}
				for (TdSParam param : list) {
					if (srcIp != null && srcIp.equals(param.getDataName())) {
						return true;
					}
				}
				writerRespInfo(response, "-1", "Your IP[" + srcIp + "]is forbidden to access the interface, please contact your system administrator.");
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * 获取请求的来源ip
	 * 
	 * @param request
	 * @return
	 */

	public static String getRequestSrcIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip != null) {
			final int idx = ip.indexOf(',');
			if (idx > -1) {
				ip = ip.substring(0, idx);
			}
		}
		return ip;
	}
}
