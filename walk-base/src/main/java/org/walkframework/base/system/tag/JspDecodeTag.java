package org.walkframework.base.system.tag;

import org.jasig.cas.client.util.CyptoUtil;

/**
 * jsp内容加密
 * 
 * @author shf675
 *
 */
public class JspDecodeTag extends BaseBodyTag {
	private static final long serialVersionUID = 1L;

	public int doEndTag() {
		try {
			String encodedBodyContent = getBodyContent().getString() == null ? "" : getBodyContent().getString().trim();
			pageContext.getOut().print(CyptoUtil.decode(getKey(), encodedBodyContent));
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return EVAL_PAGE;
	}

}
