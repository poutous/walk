package org.walkframework.base.mvc.controller.common;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.shiro.service.JcaptchaImageCaptchaService;

/**
 * 验证码
 *
 */
@RestController
@RequestMapping("/code")
public class JcaptchaController extends BaseController {

	@RequestMapping("/jcaptcha")
	public void jcaptchaCreate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		JcaptchaImageCaptchaService jcaptchaImageCaptchaService = SpringContextHolder.getBean(JcaptchaImageCaptchaService.class);
		jcaptchaImageCaptchaService.generateJcaptcha(request, response);
	}
}