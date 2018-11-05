package org.walkframework.console.mvc.controller.cache;

import javax.annotation.Resource;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.console.mvc.service.cache.CacheManagerService;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.InParam;

/**
 * 缓存管理
 *
 */
@RestController
@RequestMapping(value = "/console/cache")
public class CacheManagerController extends BaseController {

	@Resource(name = "cacheManagerService")
	private CacheManagerService cacheManagerService;

	/**
	 * 进入缓存管理界面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/go/cacheManager")
	public ModelAndView cacheManager() {
		return new ModelAndView("cache/CacheManager");
	}

	/**
	 * 缓存列表查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryCacheList")
	public Object queryCacheList(InParam<String, Object> inParam, Pagination pagination) {
		return cacheManagerService.queryCacheList(inParam, pagination);
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
	 * 进入缓存元素管理界面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/go/cacheElementManager")
	public ModelAndView cacheElementManager(Model model, InParam<String, Object> inParam) {
		model.addAttribute("cacheName", inParam.getString("cacheName"));
		return new ModelAndView("cache/CacheElementManager");
	}

	/**
	 * 缓存元素列表查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryCacheElementList")
	public Object queryCacheElementList(InParam<String, Object> inParam, Pagination pagination) {
		return cacheManagerService.queryCacheElementList(inParam, pagination);
	}

	/**
	 * 新增元素
	 * 
	 * @return
	 */
	@RequestMapping(value = "/addElement")
	public Object addElement(InParam<String, Object> inParam) {
		cacheManagerService.addElement(inParam);
		return message.success("新增成功！");
	}
	
	/**
	 * 批量删除缓存元素
	 * 
	 * @return
	 */
	@RequestMapping(value = "/removeCacheElement")
	public Object removeCacheElement(InParam<String, Object> inParam) {
		cacheManagerService.removeCacheElement(inParam);
		return message.success("删除成功！");
	}

	/**
	 * 批量设置元素过期时间
	 * 
	 * @return
	 */
	@RequestMapping(value = "/setExpireCacheElement")
	public Object setExpireCacheElement(InParam<String, Object> inParam) {
		cacheManagerService.setExpireCacheElement(inParam);
		return message.success("设置过期时间成功！");
	}
	
	/**
	 * 查看元素值
	 * 
	 * @return
	 */
	@RequestMapping(value = "/viewCacheElementValue")
	public Object viewCacheElementValue(InParam<String, Object> inParam) {
		return cacheManagerService.viewCacheElementValue(inParam);
	}
	
	/**
	 * 保存新值
	 * 
	 * @return
	 */
	@RequestMapping(value = "/saveNewVale")
	public Object saveNewVale(InParam<String, Object> inParam) {
		cacheManagerService.saveNewVale(inParam);
		return message.success("设置新值成功！");
	}
}