package org.walkframework.base.mvc.controller.base;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.common.Message;
import org.walkframework.base.system.editor.DateEditor;
import org.walkframework.base.system.factory.SingletonFactory;


/**
 * 所有的Controller必须继承此类
 *
 */
public abstract class BaseController {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected final static Common common = SingletonFactory.getInstance(Common.class); 
	
	protected final static Message message = SingletonFactory.getInstance(Message.class);
	
	/**
	 * 做一些初始化工作
	 * 
	 * @param request
	 * @param response
	 */
	@ModelAttribute
	public void init(HttpServletRequest request, HttpServletResponse response) {
		//暂未任何处理...
	}
	
	/**
	 * 注册属性绑定
	 * @param request
	 * @param binder
	 */
	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder){
		
		//设置需要包裹的元素个数，默认为256
		//spring从4.3.5开始，必须将此方法置于其他方法之前
		binder.setAutoGrowCollectionLimit(Integer.MAX_VALUE);
		
	    //对于需要转换为Date类型的属性，使用DateEditor进行处理
	    binder.registerCustomEditor(Date.class, new DateEditor());
	    
	}
	
	/*
	//容器初始化bean时操作
	@PostConstruct
	public void init() {
	}
	
	//容器销毁时操作
	@PreDestroy  
	public void  dostory(){  
	}
	*/
}
