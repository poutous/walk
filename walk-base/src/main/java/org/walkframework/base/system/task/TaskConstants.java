package org.walkframework.base.system.task;

import java.util.UUID;

/**
 * 常量定义集合
 * 
 */
public interface TaskConstants {
	
	//全局唯一ID
	String UU_ID = UUID.randomUUID().toString();
	
	//健康检查缓存名称
	String TASK_HEALTH_CACHE_NAME = "TASK_HEALTH_CACHE_NAME";
	
	//健康检查缓存key名称
	String TASK_HEALTH_KEY_NAME = "TASK_HEALTH_KEY_NAME";
}
