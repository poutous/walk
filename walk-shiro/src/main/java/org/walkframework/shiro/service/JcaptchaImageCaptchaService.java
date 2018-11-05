package org.walkframework.shiro.service;


import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import com.octo.captcha.engine.CaptchaEngine;
import com.octo.captcha.service.captchastore.CaptchaStore;
import com.octo.captcha.service.multitype.GenericManageableCaptchaService;

/**
 * 验证码服务
 *
 */
public class JcaptchaImageCaptchaService extends GenericManageableCaptchaService {
	
	private String verifyCodeName = "verifyCode";
	
	public JcaptchaImageCaptchaService(CaptchaStore captchaStore, CaptchaEngine captchaEngine, int minGuarantedStorageDelayInSeconds, int maxCaptchaStoreSize, int captchaStoreLoadBeforeGarbageCollection) {
		super(captchaStore, captchaEngine, minGuarantedStorageDelayInSeconds, maxCaptchaStoreSize, captchaStoreLoadBeforeGarbageCollection);
	}

	//产生验证码
	public void generateJcaptcha(HttpServletRequest request, HttpServletResponse response){
		ServletOutputStream out = null;
		try {
			response.setDateHeader("Expires", 0L);
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
			response.addHeader("Cache-Control", "post-check=0, pre-check=0");
			response.setHeader("Pragma", "no-cache");
			response.setContentType("image/jpeg");
			
			BufferedImage bufferedImage = getImageChallengeForID(getNewSessionId());
			out = response.getOutputStream();
			ImageIO.write(bufferedImage, "jpg", out);
			if(out != null){
				out.flush();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if(out != null){
					out.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * 获取新sessionId
	 * 
	 * @return
	 */
	private String getNewSessionId() {
		Subject subject = SecurityUtils.getSubject();
		Session session = subject.getSession();
		if(session != null) {
			session.stop();
		}
		return subject.getSession(true).getId().toString();
	}

	//校验验证码, 并删除已生成的验证码
	public boolean validateJcaptcha(HttpServletRequest request) {
		if (request.getSession(false) == null)
			return false;
		if (request.getParameter(getVerifyCodeName()) == null) {
			return false;
		}
		return validateResponseForID(request.getSession().getId(), request.getParameter(getVerifyCodeName())).booleanValue();
	}
	
	public String getVerifyCodeName() {
		return verifyCodeName;
	}

	public void setVerifyCodeName(String verifyCodeName) {
		this.verifyCodeName = verifyCodeName;
	}
}