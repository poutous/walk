package org.walkframework.base.mvc.controller.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.walkframework.base.mvc.controller.base.BaseController;


/**
 * 应用测试controller
 * 
 */
@RestController
@RequestMapping("/apptest")
public class AppTestController extends BaseController{
	
    /**
     * 测试应用是否可用
     * 
     * 直接返回1
     * 
     * @return
     */
    @RequestMapping(value = "/isAvailableSimple")
    public String isAvailableSimple() {
        return "1";
    }
    
    /**
     * 测试应用是否可用
     * 
     * 要求请求头是json，返回也是json
     * 
     * @return
     */
    @RequestMapping(value = "/isAvailable", produces = "application/json")
    public String isAvailable() {
        return "{\"response\":\"1\"}";
    }
}
