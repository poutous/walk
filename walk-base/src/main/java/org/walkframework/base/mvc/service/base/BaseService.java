package org.walkframework.base.mvc.service.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;


/**
 * 所有的Service必须继承此类
 *
 */
public abstract class BaseService {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected final static Common common = SingletonFactory.getInstance(Common.class);
	
}
