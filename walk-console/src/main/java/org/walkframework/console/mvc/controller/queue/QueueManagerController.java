package org.walkframework.console.mvc.controller.queue;

import javax.annotation.Resource;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.console.mvc.service.mq.QueueManagerService;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.InParam;

/**
 * 队列管理
 *
 */
@RestController
@RequestMapping(value = "/console/queue")
public class QueueManagerController extends BaseController {

	@Resource(name = "queueManagerService")
	private QueueManagerService queueManagerService;

	/**
	 * 进入队列管理界面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/go/queueManager")
	public ModelAndView queueManager() {
		return new ModelAndView("mq/QueueManager");
	}

	/**
	 * 队列列表查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryQueueList")
	public Object queryQueueList(InParam<String, Object> inParam, Pagination pagination) {
		return queueManagerService.queryQueueList(inParam, pagination);
	}
	
	/**
	 * 队列清空
	 * 
	 * @return
	 */
	@RequestMapping(value = "/clearQueue")
	public Object clearQueue(InParam<String, Object> inParam) {
		queueManagerService.clearQueue(inParam);
		return message.success("清空成功！");
	}
	
	/**
	 * 进入队列元素管理界面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/go/queueElementManager")
	public ModelAndView queueElementManager(Model model, InParam<String, Object> inParam) {
		model.addAttribute("queueName", inParam.getString("queueName"));
		return new ModelAndView("mq/QueueElementManager");
	}
	
	/**
	 * 队列元素列表查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryQueueElementList")
	public Object queryQueueElementList(InParam<String, Object> inParam, Pagination pagination) {
		return queueManagerService.queryQueueElementList(inParam, pagination);
	}
	
	/**
	 * 新元素入队
	 * 
	 * @return
	 */
	@RequestMapping(value = "/offerQueueElement")
	public Object offerQueueElement(InParam<String, Object> inParam) {
		queueManagerService.offerQueueElement(inParam);
		return message.success("新元素入队成功！");
	}
	
	/**
	 * 批量删除队列元素
	 * 
	 * @return
	 */
	@RequestMapping(value = "/removeQueueElement")
	public Object removeQueueElement(InParam<String, Object> inParam) {
		queueManagerService.removeQueueElement(inParam);
		return message.success("移除队列元素成功！");
	}

	/**
	 * 查看队列元素值
	 * 
	 * @return
	 */
	@RequestMapping(value = "/viewQueueElementValue")
	public Object viewQueueElementValue(InParam<String, Object> inParam) {
		return queueManagerService.viewQueueElementValue(inParam);
	}
}