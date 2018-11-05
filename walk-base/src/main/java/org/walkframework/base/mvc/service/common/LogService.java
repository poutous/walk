package org.walkframework.base.mvc.service.common;

import java.util.UUID;

import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.entity.TlMExportlog;
import org.walkframework.batis.dialect.OracleDialect;
import org.walkframework.shiro.authc.principal.BasePrincipal;


/**
 * 日志服务
 *
 */
@Service("logService")
public class LogService extends AbstractBaseService {
	
	/**
	 * 记录导出日志
	 * 
	 * @param tdMFile
	 * @throws Exception
	 */
	public boolean insertExportLog(TlMExportlog tlMExportlog){
		BasePrincipal principal = (BasePrincipal)SecurityUtils.getSubject().getPrincipal();
		tlMExportlog.setLogId(getExportId());
		tlMExportlog.setCreateStaff(principal.getUserId());
		tlMExportlog.setCreateTime(common.getCurrentTime());
		tlMExportlog.setFinishTime(common.getCurrentTime());
		try {
			return dao().insert(tlMExportlog) > 0;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}
	
	private String getExportId() {
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
		return fileId;
	}
}
