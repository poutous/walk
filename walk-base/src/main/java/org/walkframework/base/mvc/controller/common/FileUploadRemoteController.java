package org.walkframework.base.mvc.controller.common;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.walkframework.base.mvc.controller.base.BaseController;
import org.walkframework.base.system.annotation.HostCheck;
import org.walkframework.base.system.annotation.TimeTicketCheck;
import org.walkframework.base.system.constant.CommonConstants;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.base.tools.utils.FileUtil;

/**
 * 免登陆方式上传文件
 * 通过直接调用容器的ip及端口进行上传
 * 
 * ！！！！！！注意！！！！！！ ！！！！！！注意！！！！！！ ！！！！！！注意！！！！！！ 
 * 1、在shiro中配置匿名访问。目的是为了免登陆
 * 2、一定要在nginx中配置禁止访问url。目的是为了避免上传漏洞
 * 
 * @author shf675
 * 
 */
@RestController
@RequestMapping("/fileUploadRemoteServer")
public class FileUploadRemoteController extends BaseController {
	
	/**
	 * 免登陆方式上传文件
	 * 
	 * @param request
	 */
	@RequestMapping(value = "/upload")
	@HostCheck("REMOTE_UPLOAD_ALLOW_IP")//来源主机校验
	@TimeTicketCheck//时间参数校验
	public void upload(MultipartHttpServletRequest request) throws Exception {
		//文件列表
		List<MultipartFile> multipartFiles = FileUtil.getUpFiles(request);
		
		//上传路径
		String upPath = SpringPropertyHolder.getContextProperty("uploadpath", "upload/files");
		
		//是否覆盖
		String coverIfExist = request.getParameter(CommonConstants.FILE_COVER_IF_EXIST);
		
		//循环上传
		for (MultipartFile multipartFile : multipartFiles) {
			String fileName = multipartFile.getName();
			if(FileUtil.existFile(upPath, fileName)){
				//如果文件已存在，则不上传。防止虚拟机或docker环境重复上传
				if("true".equals(coverIfExist)){
					FileUtil.uploadFile(multipartFile, upPath, fileName);
					if(log.isInfoEnabled()){
						log.info("uplod file[{}] from remote ip[{}] and cover...", fileName, common.getIpAddr(request));
					}
				}
			} else {
				FileUtil.uploadFile(multipartFile, upPath, fileName);
				if(log.isInfoEnabled()){
					log.info("uplod file[{}] from remote ip[{}]...", fileName, common.getIpAddr(request));
				}
			}
		}
	}
}