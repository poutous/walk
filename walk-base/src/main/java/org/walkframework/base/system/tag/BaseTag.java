package org.walkframework.base.system.tag;

import java.io.IOException;

import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;


public class BaseTag extends SimpleTagSupport{
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	protected final static Common common = SingletonFactory.getInstance(Common.class);
	
	public void doTag() throws IOException {
	}
}
