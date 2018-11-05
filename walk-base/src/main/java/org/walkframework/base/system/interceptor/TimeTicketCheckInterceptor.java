package org.walkframework.base.system.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.walkframework.base.system.annotation.TimeTicketCheck;
import org.walkframework.base.system.constant.IntfConstants;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.base.tools.utils.TimeTicketUtil;

/**
 * 时间ticket检查拦截器
 * 
 * @author shf675
 */
public class TimeTicketCheckInterceptor extends BaseInterceptor {

	/**
	 * 时间ticket检查拦截
	 * 
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			//校验时间ticket开关
			String timeTicket = SpringPropertyHolder.getContextProperty("validate.timeTicket", "false");
			if ("false".equals(timeTicket)) {
				return true;
			}

			//获取方法指定的校验参数注解
			HandlerMethod method = (HandlerMethod) handler;
			TimeTicketCheck paramCheck = method.getMethodAnnotation(TimeTicketCheck.class);
			if (paramCheck != null) {
				//校验ticket
				String grantTicket = request.getParameter(IntfConstants.PARAM_GRANT_TICKET);
				String[] ret = TimeTicketUtil.checkTimeTiket(grantTicket);
				if (!"0".equals(ret[0])) {
					writerRespInfo(response, ret[0], ret[1]);
					return false;
				}
			}
		}
		return true;
	}
}
