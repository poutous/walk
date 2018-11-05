package org.walkframework.console.mvc.controller.session;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.console.mvc.service.session.SessionManagerService;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.InParam;

/**
 * 会话管理
 *
 */
@RestController
@RequestMapping(value = "/console/session")
public class SessionManagerController extends BaseController {

	@Resource(name = "sessionManagerService")
	private SessionManagerService sessionManagerService;

	/**
	 * 进入会话管理界面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/go/sessionManager")
	public ModelAndView cacheManager() {
		return new ModelAndView("session/SessionManager");
	}

	/**
	 * 缓存列表查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/querySessionList")
	public Object querySessionList(InParam<String, Object> inParam, Pagination pagination) {
		return sessionManagerService.querySessionList(inParam, pagination);
	}
	
	/**
	 * 设置会话时长
	 * 
	 * @return
	 */
	@RequestMapping(value = "/setSessionTimeout")
	public Object setSessionTimeout(InParam<String, Object> inParam) {
		sessionManagerService.setSessionTimeout(inParam);
		return message.success("设置会话时长成功！");
	}

	/**
	 * 强制下线
	 * 
	 * @return
	 */
	@RequestMapping(value = "/forceLogout")
	public Object forceLogout(InParam<String, Object> inParam) {
		sessionManagerService.forceLogout(inParam);
		return message.success("下线成功！");
	}
}