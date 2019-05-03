package org.walkframework.base.mvc.controller.common;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.mvc.entity.TdMFile;
import org.walkframework.base.mvc.service.common.FileService;

import com.alibaba.fastjson.JSON;

/**
 * ajax方式上传文件
 *
 */
@RestController
@RequestMapping("/ajaxfileupload")
public class AjaxFileUploadController extends BaseController {
	
	@Resource(name = "fileService")
	private FileService fileService;
	
	/**
	 * 文件上传
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String upload(MultipartHttpServletRequest request) throws Exception {
        List<MultipartFile> multipartFiles = request.getFiles(request.getParameter("fileName"));
        List<TdMFile> files = fileService.doUpFiles(multipartFiles);
        return JSON.toJSONString(files);
 	}
}