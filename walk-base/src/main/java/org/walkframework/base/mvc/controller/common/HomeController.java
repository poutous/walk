package org.walkframework.base.mvc.controller.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.tools.spring.SpringPropertyHolder;

/**
 * 欢迎页面
 *
 */
@RestController
public class HomeController extends BaseController {
	
	/**
	 * 欢迎页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/")
	public ModelAndView home() {
		String homeUrl = SpringPropertyHolder.getContextProperty("page.welcome", "common/sidebar/Sidebar");
		return new ModelAndView(homeUrl);
	}
}