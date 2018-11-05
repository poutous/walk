package org.walkframework.base.system.task;

import org.walkframework.base.mvc.entity.TfMNotify;

/**
 * 对象通知处理
 * 
 * @author shf675
 *
 */
public interface ObjectHandleNotify {
	TfMNotify handle(TfMNotify notify);
}
