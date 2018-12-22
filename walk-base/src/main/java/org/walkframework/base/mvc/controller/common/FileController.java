package org.walkframework.base.mvc.controller.common;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.mvc.entity.TdMFile;
import org.walkframework.base.mvc.service.common.FileService;
import org.walkframework.base.tools.excel.ExcelParser;
import org.walkframework.base.tools.utils.FileUtil;
import org.walkframework.data.util.IDataset;
import org.walkframework.data.util.InParam;

@RestController
@RequestMapping("/fileserver")
public class FileController extends BaseController{

	@Resource(name="fileService")
	private FileService fileService;

	/**
	 * 上传文件
	 * @param request
	 */
	@RequestMapping(value = "/upload")
	public void upload(MultipartHttpServletRequest request) throws Exception{
		List<MultipartFile> files = FileUtil.getUpFiles(request);
		fileService.doUpFiles(files);
	}

	/**
	 * 根据文件ID下载文件
	 * @param response
	 * @param fileId
	 */
	@RequestMapping(value = "/down/{fileId}")
	public void down(HttpServletResponse response, @PathVariable String fileId) throws Exception{
		TdMFile tdMFile = fileService.queryFileInfo(fileId);
		if(tdMFile == null){
			common.error("Error: file[" + fileId + "]Does not exist.");
		}
		String fullName = tdMFile.getFilePath() + File.separator + tdMFile.getFileId();
		String realName = tdMFile.getFileName();
		FileUtil.downFile(response, fullName, realName);
	}
	
	/**
	 * 根据文件ID显示文件
	 * @param response
	 * @param fileId
	 */
	@RequestMapping(value = "/show/{fileId}")
	public void show(HttpServletResponse response, @PathVariable String fileId) throws Exception{
		TdMFile tdMFile = fileService.queryFileInfo(fileId);
		if(tdMFile == null){
			common.error("Error: file[" + fileId + "]Does not exist.");
		}
		String fullName = tdMFile.getFilePath() + File.separator + tdMFile.getFileId();
		String realName = tdMFile.getFileName();
		FileUtil.showFile(response, fullName, realName);
	}
	
	/**
	 * 下载错误的excel文件
	 * @param response
	 * @param uniqeName
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/downErrorExcel/{uniqeName}")
	public void downErrorExcel(HttpServletRequest request, HttpServletResponse response, @PathVariable String uniqeName){
		Session session = SecurityUtils.getSubject().getSession();
		String dataKey = uniqeName + ExcelParser.EXCEL_UNIQE_DATA;
		String xmlKey = uniqeName + ExcelParser.EXCEL_UNIQE_XML;
		IDataset dataset = (IDataset)session.getAttribute(dataKey);
		if(dataset != null && dataset.size() > 0){
			String xml = (String)session.getAttribute(xmlKey);
			
			//及时清除session内对象
			session.removeAttribute(dataKey);
			session.removeAttribute(xmlKey);
			try {
				ExcelParser.exportErrorExcelFromImport(response, xml, "error_details.xls", dataset);
			} catch (Exception e) {
				common.error(e);
			}
		}
	}
	
	/**
	 * 导出当前页方法
	 * 
	 * @param cycle
	 */
	@RequestMapping(value = "/exportCurrentPage")
	public void exportCurrentPage(HttpServletRequest request, HttpServletResponse response, InParam<String, Object> inParam){
		try {
			String exportName = java.net.URLEncoder.encode(inParam.getString("exportName"), "UTF-8") + ".xls";
			response.setHeader("Content-Type", "application/force-download;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment;filename=" + exportName);
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
			
			StringBuilder sb = new StringBuilder();
			sb.append("<html xmlns:x=\"urn:schemas-microsoft-com:office:excel\">");
			sb.append("<head>");
			sb.append("<!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet>");
			sb.append("<x:Name>" + inParam.getString("exportName") + "</x:Name>");
			sb.append("<x:WorksheetOptions><x:Print><x:ValidPrinterInfo /></x:Print></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml>");
			sb.append("<![endif]-->");
			sb.append("<meta http-equiv=Content-Type content=\"text/html; charset=utf-8\"><style>td{font-family:'宋体';font-size:9pt;}</style>");
			sb.append("</head><body>");
			sb.append(inParam.getString("pData"));
			sb.append("</body></html>");
			PrintWriter pw = response.getWriter();
			pw.print(sb.toString());
			pw.close();
			
			inParam.remove("pData");
			inParam.remove("headerJSON");
		} catch (Exception e) {
			common.error(e);
		}
		
	}
}