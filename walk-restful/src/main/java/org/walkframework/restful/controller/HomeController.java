package org.walkframework.restful.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.tools.spring.SpringPropertyHolder;

import springfox.documentation.annotations.ApiIgnore;

/**
 * 欢迎页面
 *
 */
@Controller
@ApiIgnore
public class HomeController extends BaseController {
	
	/**
	 * 欢迎页面
	 * 
	 * @return
	 */
	@ApiIgnore
	@RequestMapping(value = "/")
	public String home() {
		String homeUrl = SpringPropertyHolder.getContextProperty("page.welcome", "/swagger-ui.html");
		return "redirect:" + homeUrl;
	}
}