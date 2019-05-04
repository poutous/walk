package org.walkframework.activiti.mvc.controller.process;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ActivitiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.activiti.mvc.entity.ActUdWorkorder;
import org.walkframework.activiti.mvc.service.process.ActProcessConfigService;
import org.walkframework.activiti.mvc.service.process.ActWorkOrderService;
import org.walkframework.activiti.system.constant.ProcessConstants;
import org.walkframework.activiti.system.process.NodeConfigEntity;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.tools.spring.SpringPropertyHolder;

/**
 * 流程管理Controller
 * 
 * @author shf675
 *
 */
@RestController
@RequestMapping("/act/process")
public class ActProcessController extends BaseController {
	
	private static final String ORDER_ID_VARIABLE = "\\{orderId\\}";
	
	@Resource(name = "actWorkOrderService")
	private ActWorkOrderService actWorkOrderService;
	
	@Resource(name = "actProcessConfigService")
	private ActProcessConfigService actProcessConfigService;
	
	/**
	 * 工单处理
	 * 
	 * @param inParam
	 * @return
	 */
	@RequestMapping(value = "deal/{orderId}")
	public ModelAndView deal(@PathVariable String orderId, HttpServletRequest request) {
		String page = SpringPropertyHolder.getContextProperty("custom.activiti.process.page", "act/process/ProcessFrame");
		ModelAndView mv = new ModelAndView(page);
		ActUdWorkorder orderInfo = actWorkOrderService.queryWorkOrderById(orderId);
		if(orderInfo == null){
			throw new ActivitiException("工单[" + orderId + "]不存在！");
		}
		if(StringUtils.isEmpty(orderInfo.getProcInstId())){
			throw new ActivitiException("工单[" + orderId + "]未启动流程，不能处理！");
		}
		if(ProcessConstants.END_NODE_STATE.equals(orderInfo.getProcState())){
			throw new ActivitiException("工单[" + orderId + "]已结束流程，不能处理！");
		}
		
		NodeConfigEntity nodeInfo = actProcessConfigService.getCurrTaskNodeConfig(orderInfo.getProcInstId());
		String pageUrl = nodeInfo.getPageUrl();
		if(StringUtils.isNotEmpty(pageUrl) && pageUrl.contains(ORDER_ID_VARIABLE.replaceAll("\\\\", ""))){
			pageUrl = pageUrl.replaceAll(ORDER_ID_VARIABLE, orderId);
			nodeInfo.setPageUrl(pageUrl.startsWith("/") ? pageUrl.substring(1):pageUrl);
		} else {
			nodeInfo.setPageUrl("404");
		}
		mv.addObject("nodeInfo", nodeInfo);
		mv.addObject("orderInfo", orderInfo);
		
		//缩放
		String zoom = request.getParameter("zoom");
		if(StringUtils.isNotEmpty(zoom)){
			mv.addObject("zoom", zoom);
		}
		//高度
		String height = request.getParameter("height");
		mv.addObject("height", StringUtils.isEmpty(height) ? "170" : height);
		return mv;
	}
}