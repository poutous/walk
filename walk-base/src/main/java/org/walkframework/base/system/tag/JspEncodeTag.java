package org.walkframework.base.system.tag;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.util.CyptoUtil;


/**
 * jsp内容加密
 * 
 * @author shf675
 *
 */
public class JspEncodeTag extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	
	private boolean encode = true;

	public int doEndTag() {
		try {
			if (encode) {
				pageContext.getOut().print(CyptoUtil.encode(getKey(), StringUtils.trim(getBodyContent().getString())));
			} else {
				getBodyContent().writeOut(getBodyContent().getEnclosingWriter());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return EVAL_PAGE;
	}

	public boolean isEncode() {
		return encode;
	}

	public void setEncode(boolean encode) {
		this.encode = encode;
	}
}
