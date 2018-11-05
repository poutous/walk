package org.walkframework.base.mvc.controller.common;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.mvc.entity.TlMExportlog;
import org.walkframework.base.mvc.service.common.ExportLogService;
import org.walkframework.base.tools.utils.FileUtil;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.InParam;
import org.walkframework.shiro.authc.principal.BasePrincipal;

/**
 * 导出日志
 * 
 * @author shf675
 *
 */
@RestController
@RequestMapping("/common/exportLog")
public class ExportLogController extends BaseController{
	
	@Resource(name="exportLogService")
	private ExportLogService exportLogService;
	
	/**
	 * 进入日志页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/toPage")
	public ModelAndView toPage(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("export/ExportLogList");
		mv.addObject("exportId", request.getParameter("exportId"));
		return mv;
	}
	
	/**
	 * 查询导出日志列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list")
	public Object list(InParam<String, Object> inParam, Pagination pagination) {
		String userId = ((BasePrincipal)SecurityUtils.getSubject().getPrincipal()).getUserId();
		inParam.put("createStaff", StringUtils.isEmpty(userId) ? "NONE" : userId);
		return exportLogService.queryExportList(inParam, pagination);
	}
	
	/**
	 * 下载文件
	 * 
	 * @param response
	 * @param exportId
	 */
	@RequestMapping(value = "/downasynfile/{exportId}")
	public void downasynfile(HttpServletResponse response, @PathVariable String exportId) throws Exception {
		TlMExportlog export = exportLogService.queryExportInfo(exportId);
		if(export == null){
			common.error("Error: export file[" + exportId + "]Does not exist.");
		}
		FileUtil.downFile(response, export.getExportPath(), export.getExportName() + ".zip");
	}
}