package org.walkframework.base.mvc.service.common;

import org.springframework.stereotype.Service;
import org.walkframework.base.mvc.entity.TlMExportlog;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.IData;

/**
 * 导出日志服务
 *
 */
@Service("exportLogService")
public class ExportLogService extends AbstractBaseService {
	
	/**
	 * 查询导出日志列表
	 * 
	 * @param param
	 * @param pagination
	 * @return
	 */
	public PageData<TlMExportlog> queryExportList(IData<String, Object> param, Pagination pagination){
		return dao().selectList("CommonSQL.queryExportList", param, pagination);
    	
	}
	/**
	 * 获取导出文件信息
	 * 
	 * @param exportId
	 * @return
	 */
	public TlMExportlog queryExportInfo(String exportId) {
		TlMExportlog export = new TlMExportlog();
		export.setLogId(exportId).asCondition();
		return dao().selectOne(export);
	}
}
