package org.walkframework.batis.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.walkframework.batis.bean.BatchEachHandler;
import org.walkframework.batis.dialect.Dialect;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.entity.BaseEntity;
import org.walkframework.data.entity.Entity;


/**
 * 基于Mybatis实现的dao工具
 * 
 * @author shf675
 *
 */
public interface Dao {
	
	/**
	 * 获取sqlSession对象
	 * 
	 * @return
	 */
	SqlSession getSqlSession();
	
	/**
	 * 获取数据库方言
	 * @return
	 */
	Dialect getDialect();
	
	/**
	 * 查询列表，无参
	 * 
	 * @param <E>
	 * @param statementId
	 * @return
	 */
	<E> List<E> selectList(String statementId);
	
	/**
	 * 查询列表
	 * 
	 * @param <E>
	 * @param statementId
	 * @param param
	 * @return
	 */
	<E> List<E> selectList(String statementId, Object param);

	/**
	 * 查询列表，有分页参数
	 * 
	 * @param <E>
	 * @param statementId
	 * @param param
	 * @param pagination
	 * @return
	 */
	<E> PageData<E> selectList(String statementId, Object param, Pagination pagination);
	
	/**
	 * 单表查询列表
	 * 
	 * @param <E>
	 * @param entity
	 * @return
	 */
	<E> List<E> selectList(Entity entity);
	
	/**
	 * 单表查询列表
	 * 
	 * @param <E>
	 * @param entity
	 * @param cacheSeconds
	 * @return
	 */
	<E> List<E> selectList(Entity entity, Integer cacheSeconds);
	
	/**
	 * 单表查询列表，有分页参数
	 * 
	 * @param <E>
	 * @param entity
	 * @param param
	 * @param pagination
	 * @return
	 */
	<E> PageData<E> selectList(Entity entity, Pagination pagination);
	
	/**
	 * 单表查询列表，有分页参数
	 * 
	 * @param <E>
	 * @param entity
	 * @param param
	 * @param pagination
	 * @param cacheSeconds
	 * @return
	 */
	<E> PageData<E> selectList(Entity entity, Pagination pagination, Integer cacheSeconds);
	
	/**
	 * 查询一个对象 无参
	 * 
	 * @param <T>
	 * @param statementId
	 * @return
	 */
	<T> T selectOne(String statementId);

	/**
	 * 查询一个对象
	 * 
	 * @param <T>
	 * @param statementId
	 * @param param
	 * @return
	 */
	<T> T selectOne(String statementId, Object param);
	
	/**
	 * 查询一个对象
	 * 
	 * @param <T>
	 * @param entity
	 * @return
	 */
	<T> T selectOne(Entity entity);
	
	/**
	 * 查询一个对象
	 * 
	 * @param <T>
	 * @param entity
	 * @param cacheSeconds
	 * @return
	 */
	<T> T selectOne(Entity entity, Integer cacheSeconds);

	/**
	 * 获取总数，无参
	 * @param statementId
	 * @return
	 */
	Long selectCount(String statementId);
	
	/**
	 * 获取总数
	 * @param statementId
	 * @param param
	 * @return
	 */
	Long selectCount(String statementId, Object param);
	
	/**
	 * 获取总数
	 * @param entity
	 * @return
	 */
	Long selectCount(Entity entity);
	
	/**
	 * 获取总数
	 * @param entity
	 * @param cacheSeconds
	 * @return
	 */
	Long selectCount(Entity entity, Integer cacheSeconds);
	
	
	/**
	 * 根据语句插入数据
	 * 
	 * @param statementId
	 * @return
	 */
	int insert(String statementId);

	/**
	 * 根据语句插入数据
	 * 
	 * @param statementId
	 * @param param
	 * @return
	 */
	int insert(String statementId, Object param);
	
	/**
	 * 插入一个对象
	 * 
	 * @param entity
	 * @return
	 */
	int insert(BaseEntity entity);
	
	/**
	 * 批量插入对象
	 * 
	 * @param list
	 * @return
	 */
	void insertBatch(List<? extends BaseEntity> list);
	
	/**
	 * 批量插入对象
	 * 
	 * @param list
	 * @param batchEachHandler
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	void insertBatch(List<? extends BaseEntity> list, BatchEachHandler batchEachHandler);

	/**
	 * 根据语句更新数据
	 * 
	 * @param statementId
	 * @return
	 */
	int update(String statementId);

	/**
	 * 根据语句更新数据
	 * 
	 * @param statementId
	 * @param param
	 * @return
	 */
	int update(String statementId, Object param);
	
	/**
	 * 更新一个对象
	 * 
	 * @param entity
	 * @return
	 */
	int update(BaseEntity entity);
	
	/**
	 * 批量更新对象
	 * 
	 * @param list
	 * @param conditionColumns
	 * @return
	 */
	void updateBatch(List<? extends BaseEntity> list, String... conditionColumns);
	
	/**
	 * 批量更新对象
	 * 
	 * @param list
	 * @param batchEachHandler
	 * @param conditionColumns
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	void updateBatch(List<? extends BaseEntity> list, BatchEachHandler batchEachHandler, String... conditionColumns);

	/**
	 * 根据语句删除数据
	 * 
	 * @param statementId
	 * @return
	 */
	int delete(String statementId);
	
	/**
	 * 根据语句删除数据
	 * 
	 * @param statementId
	 * @param param
	 * @return
	 */
	int delete(String statementId, Object param);
	
	
	/**
	 * 删除一个对象
	 * 
	 * @param entity
	 * @return
	 */
	int delete(Entity entity);
	
	/**
	 * 批量删除对象
	 * 
	 * @param list
	 * @param conditionColumns
	 * @return
	 */
	void deleteBatch(List<? extends BaseEntity> list, String... conditionColumns);
	
	/**
	 * 批量删除对象
	 * 
	 * @param list
	 * @param batchEachHandler
	 * @param conditionColumns
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	void deleteBatch(List<? extends BaseEntity> list, BatchEachHandler batchEachHandler, String... conditionColumns);
	
	/**
	 * 保存一个对象，有更新，无插入
	 * 注意：因为要先做一次查询判断数据是否存在，然后再进行insert或update，因此效率不高，慎用！！！
	 * 
	 * @param entity
	 * @return boolean
	 */
	int save(BaseEntity entity);
	
	/**
	 * 根据语句批量执行语句
	 * 
	 * @param statement
	 * @param param
	 * @return
	 */
	void executeBatch(String statementId, List<?> list);
	
	/**
	 * 根据语句批量执行语句
	 * 
	 * @param statement
	 * @param param
	 * @param batchEachHandler
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	void executeBatch(String statementId, List<?> list, BatchEachHandler batchEachHandler);
	
}
