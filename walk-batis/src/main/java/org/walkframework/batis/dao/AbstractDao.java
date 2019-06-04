package org.walkframework.batis.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.mapping.BoundSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.batis.exception.ExportException;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.util.DatasetList;

/**
 * dao工具抽象类
 * 
 * @author shf675
 */
public abstract class AbstractDao implements Dao {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected final static int DEFAULT_EXPORT_PAGE_SIZE = 2000;
	
	protected final static int DEFAULT_BATCH_SIZE = 100;
	
	protected final static int DEFAULT_RANDOM_RANGE = 1000;
	
	protected final static String SERIALIZABLE_PATH = "export/temp/batis/serializable";
	
	/**
	 * 导出
	 * 
	 * @param statementId
	 * @param param
	 * @return
	 */
	protected <E> DatasetList<E> export(String statementId, BoundSql originalBoundSql, Object param, int exportPageSize){
		DatasetList<E> dataset = new DatasetList<E>();
		dataset.setSerializable(true);
		dataset.setBatchSerializable(true);
		dataset.setSerializablePath(SERIALIZABLE_PATH);
		dataset.setSerializableId(UUID.randomUUID().toString());
		File file = new File(dataset.getSerializablePath() + "/" + dataset.getSerializableId());
		if (!file.exists()){
			file.mkdirs();
		}

		int i = 0;
		int pageCount = 0;
		int total = 0;
		for (;;) {
			Pagination pagination = new Pagination();
			pagination.setNeedCount(false);
			pagination.setRange(i * exportPageSize, exportPageSize);
//			PageData<E> pageData = selectList(statementId, param, pagination);
			PageData<E> pageData = selectList(statementId, originalBoundSql, param, pagination, null);
			
			List<E> subset = pageData.getRows();
			if (subset.size() > 0) {
				total += subset.size();
				pageCount++;
				try {
					String fileName = dataset.getSerializablePath() + "/" + dataset.getSerializableId() + "/" + i;
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(fileName)));
					out.writeObject(subset);
					out.close();
				} catch (Exception e) {
					throw new ExportException(e);
				}

				if (subset.size() < exportPageSize) {
					break;
				}
			} else {
				break;
			}
			i++;
		}
		dataset.setBatchPageCount(pageCount);
		dataset.setBatchPageSize(exportPageSize);
		dataset.setCount(total);
		return dataset;
	}
	
	protected abstract <E> PageData<E> selectList(String statementId, BoundSql originalBoundSql, Object param, Pagination pagination, Integer cacheSeconds);
}
