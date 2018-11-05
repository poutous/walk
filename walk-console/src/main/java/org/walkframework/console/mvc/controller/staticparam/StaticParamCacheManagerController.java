package org.walkframework.console.mvc.controller.staticparam;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.console.mvc.service.cache.CacheManagerService;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.InParam;

/**
 * 静态参数缓存管理
 *
 */
@RestController
@RequestMapping(value = "/console/staticparam")
public class StaticParamCacheManagerController extends BaseController {

	@Resource(name = "cacheManagerService")
	private CacheManagerService cacheManagerService;

	/**
	 * 进入静态参数表缓存管理界面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/go/staticParamCacheManager")
	public ModelAndView staticParamCacheManager() {
		return new ModelAndView("cache/StaticParamCacheManager");
	}

	/**
	 * 缓存列表查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryStaticParamCacheList")
	public Object queryStaticParamCacheList(InParam<String, Object> inParam, Pagination pagination) {
		return cacheManagerService.queryStaticParamCacheList(inParam, pagination);
	}
	
	/**
	 * 缓存批量清空
	 * 
	 * @return
	 */
	@RequestMapping(value = "/clearCache")
	public Object clearCache(InParam<String, Object> inParam) {
		cacheManagerService.clearCache(inParam);
		return message.success("清空成功！");
	}
	
	/**
	 * 缓存批量清空
	 * 
	 * @return
	 */
	@RequestMapping(value = "/reloadStaticParam")
	public Object reloadStaticParam(InParam<String, Object> inParam) {
		cacheManagerService.reloadStaticParam(inParam);
		return message.success("重新加载成功！");
	}
}