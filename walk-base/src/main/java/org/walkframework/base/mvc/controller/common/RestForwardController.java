package org.walkframework.base.mvc.controller.common;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.mvc.controller.base.BaseController;



/**
 * Rest风格转向某页面。 由于禁止了直接访问jsp页面，所以采用rest风格直接转向某页面
 * 使用方法： 页面路径为 example/FusionchartExample，访问http://xxx.xxx.xxx.xxx:xxxx/walk/forward/example/FusionchartExample将会直接转向FusionchartExample页面
 * 默认支持十级目录，如十级还不够用，可再依次重载forward方法
 *
 */
@RestController
@RequestMapping("/forward")
public class RestForwardController extends BaseController{
	
	/**
	 * 根目录下页面
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{pageName}")
	public ModelAndView forward(@PathVariable String pageName, HttpServletRequest request) {
		ModelAndView model = new ModelAndView(pageName);
		forwardExtendHandle(model, request);
		return model;
	}

	/**
	 * 一级目录下页面
	 * @param path
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{path1}/{pageName}")
	public ModelAndView forward(
			@PathVariable String path1, 
			@PathVariable String pageName,
			HttpServletRequest request) {
		ModelAndView model = new ModelAndView(path1 + "/" + pageName);
		forwardExtendHandle(model, request);
		return model;
	}
	
	/**
	 * 二级目录下页面
	 * @param path1
	 * @param path2
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{path1}/{path2}/{pageName}")
	public ModelAndView forward(
			@PathVariable String path1, 
			@PathVariable String path2, 
			@PathVariable String pageName,
			HttpServletRequest request) {
		ModelAndView model = new ModelAndView(path1 + "/" + path2 + "/" + pageName);
		forwardExtendHandle(model, request);
		return model;
	}
	
	/**
	 * 三级目录下页面
	 * @param path1
	 * @param path2
	 * @param path3
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{path1}/{path2}/{path3}/{pageName}")
	public ModelAndView forward(
			@PathVariable String path1, 
			@PathVariable String path2, 
			@PathVariable String path3, 
			@PathVariable String pageName,
			HttpServletRequest request) {
		ModelAndView model = new ModelAndView(path1 + "/" + path2 + "/" + path3 + "/" + pageName);
		forwardExtendHandle(model, request);
		return model;
	}

	/**
	 * 四级目录下页面
	 * @param path1
	 * @param path2
	 * @param path3
	 * @param path4
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{path1}/{path2}/{path3}/{path4}/{pageName}")
	public ModelAndView forward(
			@PathVariable String path1, 
			@PathVariable String path2, 
			@PathVariable String path3, 
			@PathVariable String path4, 
			@PathVariable String pageName,
			HttpServletRequest request) {
		ModelAndView model = new ModelAndView(path1 + "/" + path2 + "/" + path3 + "/" + path4 + "/" + pageName);
		forwardExtendHandle(model, request);
		return model;
	}
	
	/**
	 * 
	 * 五级目录下页面
	 * @param path1
	 * @param path2
	 * @param path3
	 * @param path4
	 * @param path5
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{path1}/{path2}/{path3}/{path4}/{path5}/{pageName}")
	public ModelAndView forward(
			@PathVariable String path1, 
			@PathVariable String path2, 
			@PathVariable String path3, 
			@PathVariable String path4, 
			@PathVariable String path5, 
			@PathVariable String pageName,
			HttpServletRequest request) {
		ModelAndView model = new ModelAndView(path1 + "/" + path2 + "/" + path3 + "/" + path4 + "/" + path5 + "/" + pageName);
		forwardExtendHandle(model, request);
		return model;
	}
	
	/**
	 * 
	 * 六级目录下页面
	 * @param path1
	 * @param path2
	 * @param path3
	 * @param path4
	 * @param path5
	 * @param path6
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{path1}/{path2}/{path3}/{path4}/{path5}/{path6}/{pageName}")
	public ModelAndView forward(
			@PathVariable String path1, 
			@PathVariable String path2, 
			@PathVariable String path3, 
			@PathVariable String path4, 
			@PathVariable String path5, 
			@PathVariable String path6, 
			@PathVariable String pageName,
			HttpServletRequest request) {
		ModelAndView model = new ModelAndView(path1 + "/" + path2 + "/" + path3 + "/" + path4 + "/" + path5 + "/" + path6 + "/" + pageName);
		forwardExtendHandle(model, request);
		return model;
	}
	
	/**
	 * 
	 * 七级目录下页面
	 * @param path1
	 * @param path2
	 * @param path3
	 * @param path4
	 * @param path5
	 * @param path6
	 * @param path7
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{path1}/{path2}/{path3}/{path4}/{path5}/{path6}/{path7}/{pageName}")
	public ModelAndView forward(
			@PathVariable String path1, 
			@PathVariable String path2, 
			@PathVariable String path3, 
			@PathVariable String path4, 
			@PathVariable String path5, 
			@PathVariable String path6, 
			@PathVariable String path7, 
			@PathVariable String pageName,
			HttpServletRequest request) {
		ModelAndView model = new ModelAndView(path1 + "/" + path2 + "/" + path3 + "/" + path4 + "/" + path5 + "/" + path6 + "/" + path7 + "/" + pageName);
		forwardExtendHandle(model, request);
		return model;
	}
	
	/**
	 * 
	 * 八级目录下页面
	 * @param path1
	 * @param path2
	 * @param path3
	 * @param path4
	 * @param path5
	 * @param path6
	 * @param path7
	 * @param path8
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{path1}/{path2}/{path3}/{path4}/{path5}/{path6}/{path7}/{path8}/{pageName}")
	public ModelAndView forward(
			@PathVariable String path1, 
			@PathVariable String path2, 
			@PathVariable String path3, 
			@PathVariable String path4, 
			@PathVariable String path5, 
			@PathVariable String path6, 
			@PathVariable String path7, 
			@PathVariable String path8, 
			@PathVariable String pageName,
			HttpServletRequest request) {
		ModelAndView model = new ModelAndView(path1 + "/" + path2 + "/" + path3 + "/" + path4 + "/" + path5 + "/" + path6 + "/" + path7 + "/" + path8 + "/" + pageName);
		forwardExtendHandle(model, request);
		return model;
	}
	
	/**
	 * 
	 * 九级目录下页面
	 * @param path1
	 * @param path2
	 * @param path3
	 * @param path4
	 * @param path5
	 * @param path6
	 * @param path7
	 * @param path8
	 * @param path9
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{path1}/{path2}/{path3}/{path4}/{path5}/{path6}/{path7}/{path8}/{path9}/{pageName}")
	public ModelAndView forward(
			@PathVariable String path1, 
			@PathVariable String path2, 
			@PathVariable String path3, 
			@PathVariable String path4, 
			@PathVariable String path5, 
			@PathVariable String path6, 
			@PathVariable String path7, 
			@PathVariable String path8, 
			@PathVariable String path9, 
			@PathVariable String pageName,
			HttpServletRequest request) {
		ModelAndView model = new ModelAndView(path1 + "/" + path2 + "/" + path3 + "/" + path4 + "/" + path5 + "/" + path6 + "/" + path7 + "/" + path8 + "/" + path9 + "/" + pageName);
		forwardExtendHandle(model, request);
		return model;
	}
	
	/**
	 * 
	 * 十级目录下页面
	 * @param path1
	 * @param path2
	 * @param path3
	 * @param path4
	 * @param path5
	 * @param path6
	 * @param path7
	 * @param path8
	 * @param path9
	 * @param path10
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "/{path1}/{path2}/{path3}/{path4}/{path5}/{path6}/{path7}/{path8}/{path9}/{path10}/{pageName}")
	public ModelAndView forward(
			@PathVariable String path1, 
			@PathVariable String path2, 
			@PathVariable String path3, 
			@PathVariable String path4, 
			@PathVariable String path5, 
			@PathVariable String path6, 
			@PathVariable String path7, 
			@PathVariable String path8, 
			@PathVariable String path9, 
			@PathVariable String path10, 
			@PathVariable String pageName,
			HttpServletRequest request) {
		ModelAndView model = new ModelAndView(path1 + "/" + path2 + "/" + path3 + "/" + path4 + "/" + path5 + "/" + path6 + "/" + path7 + "/" + path8 + "/" + path9 + "/" + path10 + "/" + pageName);
		forwardExtendHandle(model, request);
		return model;
	}
	
	/**
	 * 页面转向时统一需要执行其他操作的入口方法
	 * @param request
	 */
	private void forwardExtendHandle(ModelAndView model, HttpServletRequest request){
		//传递url参数
		transferUrlParam(model, request);
		
	}
	
	/**
	 * 传递url参数
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	private void transferUrlParam(ModelAndView model, HttpServletRequest request){
		Enumeration<String> fields = request.getParameterNames();
		while (fields.hasMoreElements()) {
			String field = fields.nextElement();
			String[] values = request.getParameterValues(field);
			if (values.length > 1) {
				model.addObject(field, values);
			} else {
				model.addObject(field, values[0]);
			}
		}
	}
}
