package org.walkframework.shiro.bean;

import java.io.Serializable;

public class UrlFilter implements Serializable {

	private static final long serialVersionUID = -5805055925384376558L;

	/**	
	 *URL编码
	 */
	private String urlCode;

	/**	
	 *URL访问路径	
	 */
	private String urlPath;

	/**	
	 *角色编码。多个以逗号分隔	
	 */
	private String roleCodes;

	/**	
	 *权限编码。多个以逗号分隔	
	 */
	private String rightCodes;

	/**	
	 * 其他过滤器
	 */
	private String filters;

	public String getUrlCode() {
		return urlCode;
	}

	public void setUrlCode(String urlCode) {
		this.urlCode = urlCode;
	}

	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}

	public String getRoleCodes() {
		return roleCodes;
	}

	public void setRoleCodes(String roleCodes) {
		this.roleCodes = roleCodes;
	}

	public String getRightCodes() {
		return rightCodes;
	}

	public void setRightCodes(String rightCodes) {
		this.rightCodes = rightCodes;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}
}
