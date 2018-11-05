package org.walkframework.base.mvc.controller.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.system.tag.OptionsTag;


/**
 * 下拉列表
 */
@RestController
@RequestMapping("/options")
public class OptionsController extends BaseController{
	
    /**
     * 
     * <p>桥接optionsTag生成HTML</p>
     *
     * @param tagInfo
     * @return
     */
    @RequestMapping(value = "/tag")
    public String tag(OptionsTag tagInfo) {
        return tagInfo.generateOptions();
    }
}
