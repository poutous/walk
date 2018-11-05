package org.walkframework.console.mvc.controller.pubsub;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.console.mvc.service.mq.PubSubManagerService;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.InParam;

/**
 * 发布订阅管理
 *
 */
@RestController
@RequestMapping(value = "/console/pubsub")
public class PubSubManagerController extends BaseController {

	@Resource(name = "pubSubManagerService")
	private PubSubManagerService pubSubManagerService;

	/**
	 * 进入发布订阅管理界面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/go/pubSubManager")
	public ModelAndView pubSubManager() {
		return new ModelAndView("mq/PubSubManager");
	}

	/**
	 * 频道列表查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryChannelList")
	public Object queryChannelList(InParam<String, Object> inParam, Pagination pagination) {
		return pubSubManagerService.queryChannelList(inParam, pagination);
	}
	
	/**
	 * 发布消息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/publishMessage")
	public Object publishMessage(InParam<String, Object> inParam) {
		pubSubManagerService.publishMessage(inParam);
		return message.success("发布消息成功！");
	}
}