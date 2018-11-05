package org.walkframework.base.system.bind.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;


public class BaseMethodArgumentResolver {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected final static Common common = SingletonFactory.getInstance(Common.class);
}
