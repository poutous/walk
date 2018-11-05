package org.walkframework.restful.security.filter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import org.walkframework.base.system.exception.SignCheckException;
import org.walkframework.restful.constant.RspConstants;
import org.walkframework.restful.model.rsp.RspInfo;
import org.walkframework.restful.security.principal.StatelessPrincipal;
import org.walkframework.restful.security.token.StatelessToken;
import org.walkframework.shiro.web.filter.authz.OrAuthorizationHelper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 无状态认证过滤器
 * 
 * @author shf675
 * 
 */
public class StatelessAuthcFilter extends AccessControlFilter {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private boolean validateSign;
	
	private boolean validatePermission;
	
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		return false;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		// 关闭签名校验时直接通过
		if (!validateSign) {
			return true;
		}

		boolean isAccessAllowed = true;
		Exception ex = null;
		try {
			JSONObject reqInfo = getReqInfo(request, response);
			if (reqInfo != null) {
				JSONObject reqHead = reqInfo.getJSONObject("reqHead");
				String appId = reqHead.getString("appId");
				String timestamp = reqHead.getString("timestamp");
				String sign = reqHead.getString("sign");
				if (!StringUtils.hasText(appId) || !StringUtils.hasText(timestamp) || !StringUtils.hasText(sign)) {
					throw new SignCheckException("appId or timestamp or sign is empty!");
				}

				// 1、委托给Realm进行签名合法性校验
				getSubject(request, response).login(new StatelessToken(appId, timestamp, sign));

				// 2、权限校验
				if(validatePermission){
					StatelessPrincipal principal = (StatelessPrincipal)getSubject(request, response).getPrincipal();
					if(principal.isUrlCheck()){
						checkPermissions(request, response, mappedValue);
					}
				}
			}
		} catch (Exception e) {
			isAccessAllowed = false;
			ex = e;
		}

		if (!isAccessAllowed) {
			log.error(ex.getMessage(), ex);

			Integer errorCode = RspConstants.VALID_ERROR;
			if (ex instanceof UnauthorizedException) {
				errorCode = RspConstants.UNAUTHORIZED_ERROR;
			}
			String errorMsg = RspConstants.RSP.get(errorCode) + (ex.getMessage() == null ? "" : "：" + ex.getMessage());

			//写入浏览器
			writeInternal(getRspInfo(errorCode, errorMsg), response);
		}
		return isAccessAllowed;
	}

	/**
	 * 权限校验
	 * 
	 * @param request
	 * @param response
	 * @param mappedValue
	 * @return
	 */
	protected void checkPermissions(ServletRequest request, ServletResponse response, Object mappedValue) {
		if (mappedValue instanceof String[] && ((String[]) mappedValue).length > 0) {
			String[] perms = (String[]) mappedValue;
			Subject subject = getSubject(request, response);
			
			boolean isAccessAllowed = OrAuthorizationHelper.orAccessAllowed(subject, perms);
			if(!isAccessAllowed){
				if (perms.length == 1) {
					isAccessAllowed = subject.isPermitted(perms[0]);
				} else {
					isAccessAllowed = subject.isPermittedAll(perms);
				}
			}
			
			if (!isAccessAllowed) {
				throw new UnauthorizedException("Sorry, you have no permission to call the interface!");
			}
		}
	}

	/**
	 * 向浏览器里写入信息
	 * 
	 * @param text
	 * @param response
	 * @throws IOException
	 */
	protected void writeInternal(String text, ServletResponse response) throws IOException {
		response.setContentType("application/json; charset=" + response.getCharacterEncoding());
		StreamUtils.copy(text, Charset.forName(response.getCharacterEncoding()), response.getOutputStream());
	}

	/**
	 * 获取请求报文
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected JSONObject getReqInfo(ServletRequest request, ServletResponse response) {
		String jsonString = getJSONStringFromJsonContentType(request, response);
		if (StringUtils.hasText(jsonString)) {
			return JSONObject.parseObject(jsonString);
		}
		return null;
	}

	/**
	 * 从流中获取请求报文
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public String getJSONStringFromJsonContentType(ServletRequest request, ServletResponse response) {
		String jsonString = null;
		InputStream in = null;
		try {
			in = request.getInputStream();
			jsonString = IOUtils.toString(in, response.getCharacterEncoding());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return jsonString;
	}

	/**
	 * 返回信息
	 * 
	 * @param respDesc
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private String getRspInfo(Integer rspCode, String rspDesc) {
		RspInfo<?> rspInfo = new RspInfo();
		rspInfo.setRspCode(rspCode);
		rspInfo.setRspDesc(rspDesc);
		return JSON.toJSONString(rspInfo, SerializerFeature.WriteMapNullValue);
	}
	
	public boolean isValidateSign() {
		return validateSign;
	}

	public void setValidateSign(boolean validateSign) {
		this.validateSign = validateSign;
	}
	
	public boolean isValidatePermission() {
		return validatePermission;
	}

	public void setValidatePermission(boolean validatePermission) {
		this.validatePermission = validatePermission;
	}
}
