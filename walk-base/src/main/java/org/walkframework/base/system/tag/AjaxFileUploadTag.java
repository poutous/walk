package org.walkframework.base.system.tag;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;
import org.walkframework.base.mvc.entity.TdMFile;
import org.walkframework.base.mvc.service.common.FileService;
import org.walkframework.base.tools.spring.SpringContextHolder;

/**
 * ajaxfileupload标签
 * 
 * @author shf675
 * 
 */
public class AjaxFileUploadTag extends BaseTag {

	/** 组件ID */
	private String id;

	/** 限制上传类型列表，多个以竖线分隔 */
	private String types;
	
	/** 限制上传个数 */
	private Integer limit = 1;

	/** 是否允许上传多个 */
	private boolean readonly;

	/** 文件ID列表，可以是字符串，多个以逗号分隔 。可以是List<String> */
	private Object fileIds;

	public void doTag() throws IOException {
		super.doTag();
		outPrint();
	}

	public void outPrint() throws IOException {
		JspWriter out = super.getJspContext().getOut();
		out.print(generateHtml());
	}

	public String generateHtml() {
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"ajaxfileupload\">");
		if(!isReadonly()){
			html.append("	<div class=\"files-btn\">");
			html.append("		<label for=\"" + getId() + "\">选择文件</label>");
			if (!StringUtils.isEmpty(getTypes())) {
				html.append("		<span class=\"fileType\">(文件类型：<span class=\"types\">" + getTypes() + "</span>)</span>");
			}
			html.append("		<input type=\"file\" id=\"" + getId() + "\" limit=\"" + getLimit() + "\" " + (getLimit() > 1 ? "multiple=\"multiple\"" : "") + " onchange=\"$.walk.ajaxFileUpload(this);\"/>");
			html.append("	</div>");
		}
		html.append(generateFilesHtml());
		html.append("</div>");
		return html.toString();
	}

	public String generateFilesHtml() {
		HttpServletRequest request = (HttpServletRequest) ((PageContext) getJspContext()).getRequest();
		List<TdMFile> fileList = getFileList();
		if (CollectionUtils.isEmpty(fileList)) {
			return "";
		}
		StringBuilder files = new StringBuilder();
		files.append("<div class=\"files-list\">");
		for (TdMFile file : fileList) {
			files.append("	<div class=\"file-item\">");
			files.append("		<input type=\"text\" class=\"fileId\" name=\"" + getId() + "_fileId\" value=\"" + file.getFileId() + "\" style=\"display: none\" />");
			files.append("		<img class=\"folder\" src=\"" + request.getContextPath() + "/static/component/resources/scripts/seajs/sea-modules/jquery/ajaxfileupload/1.0.0/images/folder.png\" alt=\"\" />");
			files.append("		<span class=\"file\">");
			files.append("			<a class=\"fileName\" href=\"javascript:void(0)\" title=\"" + file.getFileName() + "\">" + file.getFileName() + "</a>");
			files.append("			<a class=\"fileSize\" href=\"javascript:void(0)\">" + Math.floor(file.getFileSize().intValue() / 1024) + "KB</a>");
			files.append("		</span>");
			files.append("		<img class=\"download\" src=\"" + request.getContextPath() + "/static/component/resources/scripts/seajs/sea-modules/jquery/ajaxfileupload/1.0.0/images/download.png\" alt=\"\" onclick=\"$.walk.ajaxFileUploadDownload('" + file.getFileId() + "')\"/>");
			if(!isReadonly()){
				files.append("		<img class=\"delete\" src=\"" + request.getContextPath() + "/static/component/resources/scripts/seajs/sea-modules/jquery/ajaxfileupload/1.0.0/images/delete.png\" alt=\"\" onclick=\"$.walk.ajaxFileUploadDelete(this)\"/>");
			}
			files.append("	</div>");
		}
		files.append("</div>");
		return files.toString();
	}

	@SuppressWarnings("unchecked")
	private List<TdMFile> getFileList() {
		Object fileIds = getFileIds();
		if (fileIds == null) {
			return null;
		}
		Object[] fileIdArr = null;
		if (fileIds instanceof List) {
			List<String> list = (List<String>) fileIds;
			if (list.size() == 0) {
				return null;
			}
			fileIdArr = list.toArray();
		} else if (fileIds instanceof String) {
			String strs = fileIds.toString();
			if (StringUtils.isEmpty(strs)) {
				return null;
			}
			fileIdArr = StringUtils.trimAllWhitespace(strs).split(",");
		}
		FileService fileService = SpringContextHolder.getBean("fileService", FileService.class);
		return fileService.queryFileList(fileIdArr);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = types;
	}
	
	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Object getFileIds() {
		return fileIds;
	}

	public void setFileIds(Object fileIds) {
		this.fileIds = fileIds;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
}
