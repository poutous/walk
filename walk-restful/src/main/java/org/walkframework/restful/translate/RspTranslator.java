package org.walkframework.restful.translate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.system.translate.AbstractTranslator;
import org.walkframework.restful.model.rsp.RspData;

/**
 * 返回报文翻译器基类
 * 
 * @author shf675
 *
 */
public abstract class RspTranslator extends AbstractTranslator {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected final static Common common = SingletonFactory.getInstance(Common.class);

	@Override
	public <T> T translate(Object sourceObject, String translatedField) {
		RspData rspData = (RspData) sourceObject;
		Object so = rspData.gainPropertyTranslatorSourceObject(translatedField);
		if(so == null) {
			return null;
		}
		return translate(so);
	}
}