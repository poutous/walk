package org.walkframework.base.mvc.service.common;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.entity.TdMFile;
import org.walkframework.base.mvc.entity.TdSParam;
import org.walkframework.base.system.constant.CommonConstants;
import org.walkframework.base.system.constant.IntfConstants;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.base.tools.utils.FileUtil;
import org.walkframework.base.tools.utils.IPUtil;
import org.walkframework.base.tools.utils.ParamTranslateUtil;
import org.walkframework.base.tools.utils.TimeTicketUtil;
import org.walkframework.batis.dialect.OracleDialect;
import org.walkframework.data.entity.Conditions;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;

/**
 * 文件服务
 *
 */
@Service("fileService")
public class FileService extends AbstractBaseService {
	
	/**
	 * 线程池，默认大小100
	 */
	private ExecutorService threadPool = Executors.newFixedThreadPool(100);

	/**
	 * 单个上传
	 * 
	 * @param tdMFile
	 * @throws Exception
	 */
	public TdMFile doUpFile(MultipartFile multipartFile) throws Exception {
		List<MultipartFile> multipartFiles = new ArrayList<MultipartFile>();
		multipartFiles.add(multipartFile);
		List<TdMFile> files = doUpFiles(multipartFiles);
		return files.size() > 0 ? files.get(0) : null;
	}

	/**
	 * 批量上传
	 * 
	 * @param tdMFile
	 */
	public List<TdMFile> doUpFiles(List<MultipartFile> multipartFiles) throws Exception {
		List<TdMFile> tdMFiles = new ArrayList<TdMFile>();
		List<File> files = new ArrayList<File>();
		String uploadremote = SpringPropertyHolder.getContextProperty("uploadremote", "false");
		for (MultipartFile multipartFile : multipartFiles) {
			TdMFile tdMFile = new TdMFile();
			String fileId = "";
			if (dao().getDialect() instanceof OracleDialect) {
				//oracle:序列+14位随机数
				fileId = ((BaseSqlSessionDao) dao()).getSequenceL16("SEQ_FILE_ID") + common.getRandomString(14);
			} else {
				//mysql或其他：使用UUID
				fileId = UUID.randomUUID().toString().replaceAll("-", "");
				int len = 40 - fileId.length();
				if (len > 0) {
					fileId += common.getRandomString(len);
				}
			}
			String upPath = SpringPropertyHolder.getContextProperty("uploadpath", "upload/files");
			tdMFile.setFileId(fileId);
			tdMFile.setFileName(multipartFile.getOriginalFilename());
			tdMFile.setFileSize(new BigDecimal(multipartFile.getSize()));
			tdMFile.setFilePath(upPath);
			tdMFile.setFileType("1");
			tdMFile.setFileKind("2");
			tdMFile.setCreaTime(common.getCurrentTime());
			String userId = (String) common.getValueByFieldName(SecurityUtils.getSubject().getPrincipal(), "userId");
			tdMFile.setCreaStaff(userId);
			tdMFiles.add(tdMFile);

			//上传到本机
			File file = FileUtil.uploadFile(multipartFile, upPath, fileId);

			//上传到远端开关
			if ("true".equals(uploadremote)) {
				files.add(file);
			}
		}
		dao().insertBatch(tdMFiles);

		//上传到远端
		if ("true".equals(uploadremote)) {
			uploadRemote(files);
		}
		return tdMFiles;
	}

	/**
	 * 上传到远端
	 * 
	 * @param multipartFile
	 * @param fileId
	 */
	public void uploadRemote(final List<File> files) throws Exception {
		final IData<String, String> params = new DataMap<String, String>();
		//加入时间校验参数
		params.put(IntfConstants.PARAM_GRANT_TICKET, TimeTicketUtil.getTimeTiket());
		//远端主机如果存在文件则不覆盖。远端主机有可能也是本机
		params.put(CommonConstants.FILE_COVER_IF_EXIST, "false");

		//获取本机IP。获取到的IP未必准，如果在虚拟机或docker环境中取到的是虚拟IP
		String localIp = IPUtil.getLocalIP();

		//获取远端地址列表
		List<TdSParam> list = ParamTranslateUtil.staticlist("REMOTE_UPLOAD_ADDR");
		if (list == null || list.size() == 0) {
			log.error("No configured remote upload address [TYPE_ID=REMOTE_UPLOAD_ADDR].");
			return;
		}

		String fileNames = "";
		if (log.isInfoEnabled()) {
			fileNames = getFileNames(files);
		}

		//循环上传
		for (TdSParam tdSParam : list) {
			final String uploadAddr = tdSParam.getDataName();
			//本机不上传。此种判断方式未必准，不过远端上传服务有做判断，如果存在该文件，则不覆盖。
			if (!localIp.equals(new URL(uploadAddr).getHost())) {
				if (log.isInfoEnabled()) {
					log.info("upload files[{}] to remote address:{}", fileNames, uploadAddr);
				}

				//开启新的线程进行上传
				threadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							FileUtil.uploadFileToRemote(uploadAddr, files, params);
						} catch (Exception e) {
							log.error("upload remote error", e);
						}
					}
				});
			}
		}
	}

	/**
	 * 获取文件名列表，逗号分隔
	 * 
	 * @param files
	 * @return
	 */
	private String getFileNames(List<File> files) {
		if (files == null || files.isEmpty()) {
			return "";
		}

		StringBuilder fileNames = new StringBuilder();
		for (File file : files) {
			fileNames.append(file.getName()).append(",");
		}
		fileNames.deleteCharAt(fileNames.length() - 1);
		return fileNames.toString();
	}

	/**
	 * 获取文件信息
	 * 
	 * @param fileId
	 * @return
	 */
	public TdMFile queryFileInfo(String fileId) {
		TdMFile file = new TdMFile();
		file.setFileId(fileId).asCondition();
		return dao().selectOne(file);
	}
	
	/**
	 * 获取文件列表信息
	 * 
	 * @param fileIds
	 * @return
	 */
	public List<TdMFile> queryFileList(Object[] fileIds) {
		if(fileIds == null || fileIds.length == 0){
			common.error("fileIds为空！");
		}
		Conditions conditions = new Conditions(TdMFile.class);
		conditions.andIn(TdMFile.FILE_ID, fileIds);
		return dao().selectList(conditions);
	}
}
