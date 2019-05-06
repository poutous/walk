package org.walkframework.batis.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSession.StrictMap;
import org.mybatis.spring.SqlSessionTemplate;
import org.walkframework.batis.bean.Batch;
import org.walkframework.batis.bean.BatchEachHandler;
import org.walkframework.batis.bean.CacheBoundSql;
import org.walkframework.batis.bean.EntityBoundSql;
import org.walkframework.batis.bean.WrapParameter;
import org.walkframework.batis.constants.EntitySQL;
import org.walkframework.batis.dialect.Dialect;
import org.walkframework.batis.exception.EmptyBatchListException;
import org.walkframework.batis.exception.ExceedsMaxLimitResultsetException;
import org.walkframework.batis.holder.BatchHolder;
import org.walkframework.batis.holder.BoundSqlHolder;
import org.walkframework.batis.holder.ResultMapHolder;
import org.walkframework.batis.tools.util.EntityUtil;
import org.walkframework.batis.tools.util.ResultMapUtil;
import org.walkframework.batis.tools.util.SQlGenerator;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.entity.BaseEntity;
import org.walkframework.data.entity.Conditions;
import org.walkframework.data.entity.Entity;
import org.walkframework.data.entity.EntityHelper;
import org.walkframework.data.util.DatasetList;

/**
 * 基于Mybatis实现的dao工具
 * 
 * @author shf675
 */
public class SqlSessionDao extends AbstractDao {

	private SqlSession sqlSession;

	private Dialect dialect;

	private int exportPageSize;
	
	/**
	 * 随机数范围。mysql使用
	 */
	private int randomRange = DEFAULT_RANDOM_RANGE;
	
	private SQlGenerator sqlGeneration;

	/**
	 * 构造方式注入
	 * 
	 * @param sqlSessionFactory
	 * @param dialect
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public SqlSessionDao(SqlSessionFactory sqlSessionFactory, String dialect) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this(sqlSessionFactory, dialect, DEFAULT_EXPORT_PAGE_SIZE, DEFAULT_RANDOM_RANGE);
	}

	/**
	 * 构造方式注入
	 * 
	 * @param sqlSessionFactory
	 * @param dialect
	 * @param exportPageSize
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public SqlSessionDao(SqlSessionFactory sqlSessionFactory, String dialect, int exportPageSize, int randomRange) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.sqlSession = new SqlSessionTemplate(sqlSessionFactory);
		this.dialect = (Dialect) Class.forName(dialect).newInstance();
		this.exportPageSize = exportPageSize;
		this.randomRange = randomRange;
		this.sqlGeneration = new SQlGenerator();
		
	}

	@Override
	public SqlSession getSqlSession() {
		return this.sqlSession;
	}

	/**
	 * 获取数据库方言
	 * 
	 * @return
	 */
	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public <E> List<E> selectList(String statementId) {
		return this.selectList(statementId, null);
	}

	@Override
	public <E> List<E> selectList(String statementId, Object param) {
		Pagination pagination = null;
		PageData<E> pageData = this.selectList(statementId, param, pagination);
		return pageData.getRows();
	}

	@Override
	public <E> PageData<E> selectList(String statementId, Object param, Pagination pagination) {
		MappedStatement ms = this.sqlSession.getConfiguration().getMappedStatement(statementId);
		return selectList(statementId, getBoundSql(ms, param), param, pagination, null);
	}

	@Override
	public <E> List<E> selectList(Entity entity) {
		PageData<E> pageData = this.selectList(entity, null, null);
		return pageData.getRows();
	}

	@Override
	public <E> List<E> selectList(Entity entity, Integer cacheSeconds) {
		PageData<E> pageData = this.selectList(entity, null, cacheSeconds);
		return pageData.getRows();
	}

	@Override
	public <E> PageData<E> selectList(Entity entity, Pagination pagination) {
		return this.selectList(entity, pagination, null);
	}

	@Override
	public <E> PageData<E> selectList(Entity entity, Pagination pagination, Integer cacheSeconds) {
		if(entity instanceof Conditions){
			Conditions conditions = (Conditions)entity;
			return this.selectListBySql(EntityHelper.getConditionsSql(conditions), new WrapParameter(EntityHelper.getConditionsParameters(conditions), EntityHelper.getEntityClazz(entity)), pagination, cacheSeconds);
		}
		
		String statementId = EntitySQL.SELECT;
		MappedStatement ms = this.sqlSession.getConfiguration().getMappedStatement(statementId);
		BoundSql originalBoundSql = getBoundSql(ms, entity);

		EntityBoundSql entityBoundSql = sqlGeneration.generateSelectSql(ms.getConfiguration(), entity);
		MetaObject boundSqlMeta = SystemMetaObject.forObject(originalBoundSql);
		boundSqlMeta.setValue("sql", entityBoundSql.getSql());
		boundSqlMeta.setValue("parameterMappings", entityBoundSql.getParameterMappings());
		return this.selectList(statementId, originalBoundSql, entity, pagination, cacheSeconds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T selectOne(String statementId) {
		return (T) this.selectOne(statementId, null);
	}

	@Override
	public <T> T selectOne(String statementId, Object param) {
		List<T> list = this.selectList(statementId, param, RowBounds.DEFAULT);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T selectOne(Entity entity) {
		return (T) selectOne(entity, null);
	}

	@Override
	public <T> T selectOne(Entity entity, Integer cacheSeconds) {
		List<T> list = this.<T> selectList(entity, cacheSeconds);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
		} else {
			return null;
		}
	}

	@Override
	public Long selectCount(String statementId) {
		return this.selectCount(statementId, null);
	}

	@Override
	public Long selectCount(String statementId, Object param) {
		MappedStatement ms = this.sqlSession.getConfiguration().getMappedStatement(statementId);
		BoundSql boundSql = getBoundSql(ms, param);
		return selectCount(statementId, boundSql, param, false, null);
	}

	@Override
	public Long selectCount(Entity entity) {
		return this.selectCount(entity, null);
	}

	@Override
	public Long selectCount(Entity entity, Integer cacheSeconds) {
		if(entity instanceof Conditions){
			Conditions conditions = (Conditions)entity;
			return this.selectCountBySql(EntityHelper.getConditionsSql(conditions), EntityHelper.getConditionsParameters(conditions), cacheSeconds);
		}
		String statementId = EntitySQL.SELECT;
		MappedStatement ms = this.sqlSession.getConfiguration().getMappedStatement(statementId);
		BoundSql originalBoundSql = getBoundSql(ms, entity);
		EntityBoundSql entityBoundSql = sqlGeneration.generateSelectSql(ms.getConfiguration(), entity);
		MetaObject boundSqlMeta = SystemMetaObject.forObject(originalBoundSql);
		boundSqlMeta.setValue("sql", entityBoundSql.getSql());
		boundSqlMeta.setValue("parameterMappings", entityBoundSql.getParameterMappings());
		return this.selectCount(statementId, originalBoundSql, entity, false, cacheSeconds);
	}

	@Override
	public int insert(String statementId) {
		return this.insert(statementId, null);
	}

	@Override
	public int insert(String statementId, Object param) {
		return this.sqlSession.insert(statementId, param);
	}

	@Override
	public int insert(BaseEntity entity) {
		int rows = updateEntity(SqlCommandType.INSERT, entity);
		
		//处理mysql的自增字段
		EntityUtil.handleAutoIncrement(entity, this);
		
		return rows;
	}

	@Override
	public void insertBatch(List<? extends BaseEntity> list) {
		insertBatch(list, null);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void insertBatch(List<? extends BaseEntity> list, BatchEachHandler batchEachHandler) {
		executeBatch(SqlCommandType.INSERT, list, batchEachHandler);
	}

	@Override
	public int update(String statementId) {
		return this.update(statementId, null);
	}

	@Override
	public int update(String statementId, Object param) {
		return this.sqlSession.update(statementId, param);
	}

	@Override
	public int update(BaseEntity entity) {
		return updateEntity(SqlCommandType.UPDATE, entity);
	}

	@Override
	public void updateBatch(List<? extends BaseEntity> list, String... conditionColumns) {
		updateBatch(list, null, conditionColumns);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void updateBatch(List<? extends BaseEntity> list, BatchEachHandler batchEachHandler, String... conditionColumns) {
		executeBatch(SqlCommandType.UPDATE, list, batchEachHandler, conditionColumns);
	}

	@Override
	public int delete(String statementId) {
		return this.delete(statementId, null);
	}

	@Override
	public int delete(String statementId, Object param) {
		return this.sqlSession.delete(statementId, param);
	}

	@Override
	public int delete(Entity entity) {
		return updateEntity(SqlCommandType.DELETE, entity);
	}

	@Override
	public void deleteBatch(List<? extends BaseEntity> list, String... conditionColumns) {
		deleteBatch(list, null, conditionColumns);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void deleteBatch(List<? extends BaseEntity> list, BatchEachHandler batchEachHandler, String... conditionColumns) {
		executeBatch(SqlCommandType.DELETE, list, batchEachHandler, conditionColumns);
	}

	@Override
	public int save(BaseEntity entity) {
		Entity data = this.selectOne(entity);
		if (data != null) {
			return this.update(entity);
		}
		return this.insert(entity);
	}

	/**
	 * 根据语句批量更新
	 * mybatis在执行批量更新时xml中只能用update节点，为了避免开发人员不知道这种情况造成的疑惑，在这修改成使用DaoCommonMapper.update
	 * mybatis的defaultExecutorType设置为非BATCH模式进行的批量更新
	 * WalkbatisPlugin拦截器进行拦截，针对这种批量更新做特殊处理 java.sql的批量更新模式无法返回影响行数，所以直接以void处理
	 * 
	 * @param statementId
	 * @param list
	 * @return
	 */
	@Override
	public void executeBatch(String statementId, List<?> list) {
		executeBatch(statementId, list, null);
	}
	
	/**
	 * 根据语句批量更新
	 * mybatis在执行批量更新时xml中只能用update节点，为了避免开发人员不知道这种情况造成的疑惑，在这修改成使用DaoCommonMapper.update
	 * mybatis的defaultExecutorType设置为非BATCH模式进行的批量更新
	 * WalkbatisPlugin拦截器进行拦截，针对这种批量更新做特殊处理 java.sql的批量更新模式无法返回影响行数，所以直接以void处理
	 * 
	 * @param statementId
	 * @param list
	 * @param batchEachHandler
	 * @return
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void executeBatch(String statementId, List<?> list, BatchEachHandler batchEachHandler) {
		if (list == null || list.size() == 0) {
			throw new EmptyBatchListException();
		}
		try {
			if (!ExecutorType.BATCH.equals(this.sqlSession.getConfiguration().getDefaultExecutorType())) {
				// 置入线程
				BatchHolder.setBatch(new Batch(list.size()));
			}

			String updateStatementId = EntitySQL.UPDATE;
			for (Object object : list) {
				
				//用户自定义处理循环内容
				if(batchEachHandler != null){
					batchEachHandler.onEach(object);
				}
				
				MappedStatement originalMs = this.sqlSession.getConfiguration().getMappedStatement(statementId);
				BoundSql originalBoundSql = getBoundSql(originalMs, object);
				MappedStatement updateMs = this.sqlSession.getConfiguration().getMappedStatement(updateStatementId);
				BoundSql updateBoundSql = getBoundSql(updateMs, object);

				// 修改sql和parameterMappings
				MetaObject boundSqlMeta = SystemMetaObject.forObject(updateBoundSql);
				boundSqlMeta.setValue("sql", originalBoundSql.getSql());
				boundSqlMeta.setValue("parameterMappings", originalBoundSql.getParameterMappings());

				// 将boundSql置入线程
				BoundSqlHolder.set(new CacheBoundSql(updateBoundSql, null));

				this.update(updateStatementId, object);

				// 执行完将boundSql从线程中移除
				BoundSqlHolder.clear();
			}

		} finally {
			// 清理线程
			if (!ExecutorType.BATCH.equals(this.sqlSession.getConfiguration().getDefaultExecutorType())) {
				// 从线程中移除
				BatchHolder.clear();
			}

			// boundSql从线程中移除
			BoundSqlHolder.clear();
		}
	}
	
	/** *****************************************分割线************************************************************************** */
	/**
	 * 分页查询
	 * 
	 * @param <E>
	 * @param statementId
	 * @param originalBoundSql
	 * @param param
	 * @param pagination
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <E> PageData<E> selectList(String statementId, BoundSql originalBoundSql, Object param, Pagination pagination, Integer cacheSeconds) {
		PageData<E> pageData = new PageData<E>();
		try {

			// 将boundSql置入线程
			BoundSqlHolder.set(new CacheBoundSql(originalBoundSql, cacheSeconds));

			// 执行count SQL
			Long count = 0L;
			
			//调用存储过程有可能使用select方法调用
			if(originalBoundSql.getSql() != null && !originalBoundSql.getSql().startsWith("{")){
				// 分页对象分析。从配置读取maxLimitResultset配置，避免因sql写法有误或条件设置不合理导致查询出大量结果集造成的内存溢出。
				Properties properties = this.sqlSession.getConfiguration().getVariables();
				String maxLimitResultset = properties == null ? "" : properties.getProperty("maxLimitResultset", "");
				if (pagination == null) {
					if (!"".equals(maxLimitResultset)) {
						pagination = new Pagination();
						pagination.setRange(0, Integer.parseInt(maxLimitResultset));
						pagination.setCurrPage(1);
						pagination.setNeedCount(false);
					} else {
						List<E> list = this.selectList(statementId, param, RowBounds.DEFAULT);
						pageData.setRows(list);
						pageData.setTotal(list.size());
						return pageData;
					}
				} else {
					// 检查分页数量是否超出最大限制结果集设置
					if (!"".equals(maxLimitResultset) && pagination.getSize() > Integer.parseInt(maxLimitResultset)) {
						throw new ExceedsMaxLimitResultsetException(Integer.parseInt(maxLimitResultset), pagination.getSize());
					}
				}
			}

			// 判断是否为导出
			if (pagination.isBatch()) {
				DatasetList<E> list = export(statementId, originalBoundSql, param, this.exportPageSize);
				pageData.setRows(list);
				pageData.setTotal(list.count());
				return pageData;
			}

			// 判断是否需要count
			if (pagination.isNeedCount()) {
				count = this.selectCount(statementId, originalBoundSql, param, true, cacheSeconds);
				// 1、如果记录数为0则不进行分页查询。
				// 2、当前页大于总页数则不进行分页查询。
				if (count == 0 || pagination.getCurrPage() > ((count + pagination.getSize() - 1) / pagination.getSize())) {
					pageData.setRows(Collections.EMPTY_LIST);
					pageData.setTotal(count);
					return pageData;
				}
			}

			// 分页SQL条件：1、不需要count时，通常是导出时用；2、需要count时，并且count大于pageSize
			MetaObject originalBoundSqlMeta = SystemMetaObject.forObject(originalBoundSql);
			String originalSql = originalBoundSql.getSql();
			boolean isPagination = false;
			if (!pagination.isNeedCount() || count > pagination.getSize()) {
				isPagination = true;
				// 设置分页SQL
				String pagingSql = this.dialect.getPagingSql(originalSql, pagination.getStart(), pagination.getStart() + pagination.getSize());
				originalBoundSqlMeta.setValue("sql", pagingSql);
			}

			// 执行SQL
			List<E> list = this.selectList(statementId, param, new RowBounds(0, pagination.getSize()));

			// 设置返回分页数据
			pageData.setRows(list);
			pageData.setTotal(count);
			pageData.setCurrPage(pagination.getCurrPage());
			pageData.setPageSize(pagination.getSize());

			// 如果执行了分页SQL，将原始sql设置回去
			if (isPagination) {
				originalBoundSqlMeta.setValue("sql", originalSql);
			}
		} finally {
			// 清空当前线程
			BoundSqlHolder.clear();
		}

		return pageData;
	}

	/**
	 * list查询
	 * 
	 * @param <E>
	 * @param statementId
	 * @param param
	 * @param rowBounds
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <E> List<E> selectList(String statementId, Object param, RowBounds rowBounds) {
		List<E> list = null;
		try {
			MappedStatement ms = this.sqlSession.getConfiguration().getMappedStatement(statementId);
			// 修改原始resultMaps
			if (EntitySQL.SELECT.equals(statementId) && param != null) {
				Class<? extends BaseEntity> entityType = null;
				if(param instanceof WrapParameter){
					WrapParameter wp = (WrapParameter)param;
					entityType = wp.getEntityType();
					param = wp.getParameterObject();
				} else {
					entityType = EntityHelper.getEntityClazz((Entity) param);
				}
				ResultMapUtil.resolveResultMapByEntity(ms, entityType);
			} else {
				ResultMapUtil.resolveResultMap(ms);
			}

			list = this.sqlSession.selectList(statementId, param, rowBounds);
		} finally {
			// 将resultMap从线程中移除
			ResultMapHolder.clear();
		}
		return list == null ? Collections.EMPTY_LIST : list;
	}
	
	/**
	 * 通过sql查询
	 * 
	 * protected：限制业务侧直接使用sql方式进行查询
	 * 
	 * @param <E>
	 * @param sql
	 * @param wrapParameter
	 * @param pagination
	 * @param cacheSeconds
	 * @return
	 */
	protected <E> PageData<E> selectListBySql(String sql, WrapParameter wrapParameter, Pagination pagination, Integer cacheSeconds) {
		BoundSql originalBoundSql = handleSql(sql, wrapParameter);
		return this.selectList(EntitySQL.SELECT, originalBoundSql, wrapParameter, pagination, cacheSeconds);
	}
	
	/**
	 * 通过sql获取总数
	 * 
	 * protected：限制业务侧直接使用sql方式进行查询
	 * 
	 * @param <E>
	 * @param sql
	 * @param param
	 * @return
	 */
	protected Long selectCountBySql(String sql, Object param, Integer cacheSeconds) {
		Long count = 0L;
		try {
			sql = this.dialect.getCountSql(sql);
			BoundSql originalBoundSql = handleSql(sql, param);
			
			BoundSqlHolder.set(new CacheBoundSql(originalBoundSql, cacheSeconds));
			
			List<Map<String, Object>> list = this.sqlSession.selectList(EntitySQL.SELECT, param, RowBounds.DEFAULT);
			count = Long.valueOf(list.get(0).get("CNT") + "");
		} finally {
			BoundSqlHolder.clear();
		}
		return count == null ? 0 : count;
	}
	
	/**
	 * 获取总数
	 * 
	 * @param sqlSession
	 * @param dialect
	 * @param originalBoundSql
	 * @param param
	 * @return
	 */
	protected Long selectCount(String statementId, BoundSql originalBoundSql, Object param, boolean isFromPagination, Integer cacheSeconds) {
		Long count = 0L;
		try {
			// 原始sql
			String originalSql = originalBoundSql.getSql();

			// 设置count SQL
			MetaObject originalBoundSqlMeta = SystemMetaObject.forObject(originalBoundSql);
			originalBoundSqlMeta.setValue("sql", dialect.getCountSql(originalSql));

			// 设置新的resultMaps
			ResultMapUtil.resolveResultMapCount(this.sqlSession.getConfiguration().getMappedStatement(statementId));

			// 将boundSql置入线程
			if (!isFromPagination) {
				BoundSqlHolder.set(new CacheBoundSql(originalBoundSql, cacheSeconds));
			}

			// 执行SQL
			List<Long> list = this.selectList(statementId, param, RowBounds.DEFAULT);
			count = list.get(0);

			if (isFromPagination) {
				// 将原始sql设置回去
				originalBoundSqlMeta.setValue("sql", originalSql);
			}
		} finally {
			// 将boundSql从线程中移除
			if (!isFromPagination) {
				BoundSqlHolder.clear();
			}
		}

		return count == null ? 0 : count;
	}
	
	/**
	 * 批量更新 mybatis的defaultExecutorType设置为非BATCH模式进行的批量更新
	 * WalkbatisPlugin拦截器进行拦截，针对这种批量更新做特殊处理 java.sql的批量更新模式无法返回影响行数，所以直接以void处理
	 * 
	 * @param sqlCommandType
	 * @param list
	 * @param batchEachHandler
	 * @param conditionColumns
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void executeBatch(SqlCommandType sqlCommandType, List<? extends BaseEntity> list, BatchEachHandler batchEachHandler, String... conditionColumns) {
		if (list == null || list.size() == 0) {
			throw new EmptyBatchListException();
		}
		try {
			if (!ExecutorType.BATCH.equals(this.sqlSession.getConfiguration().getDefaultExecutorType())) {
				// 置入线程
				BatchHolder.setBatch(new Batch(list.size()));
			}
			
			for (BaseEntity entity : list) {
				
				//用户自定义处理循环内容
				if(batchEachHandler != null){
					batchEachHandler.onEach(entity);
				}
				
				// 处理操作列
				EntityUtil.handleOperColumn(entity, conditionColumns);
				
				// updateEntity
				updateEntity(sqlCommandType, entity);
			}
			
		} finally {
			if (!ExecutorType.BATCH.equals(this.sqlSession.getConfiguration().getDefaultExecutorType())) {
				// 从线程中移除
				BatchHolder.clear();
			}
		}
	}

	/**
	 * entity的增、删、改
	 * 
	 * @param sqlCommandType
	 * @param entity
	 * @return
	 */
	protected int updateEntity(SqlCommandType sqlCommandType, Entity entity) {
		String statementId = EntitySQL.UPDATE;
		MappedStatement ms = this.sqlSession.getConfiguration().getMappedStatement(statementId);
		BoundSql originalBoundSql = getBoundSql(ms, entity);

		EntityBoundSql entityBoundSql = null;
		if (SqlCommandType.INSERT.equals(sqlCommandType)) {
			entityBoundSql = sqlGeneration.generateInsertSql(ms.getConfiguration(), entity);
		} else if (SqlCommandType.UPDATE.equals(sqlCommandType)) {
			entityBoundSql = sqlGeneration.generateUpdateSql(ms.getConfiguration(), entity);
		} else if (SqlCommandType.DELETE.equals(sqlCommandType)) {
			entityBoundSql = sqlGeneration.generateDeleteSql(ms.getConfiguration(), entity);
		}

		// 修改原始sql和parameterMappings
		MetaObject boundSqlMeta = SystemMetaObject.forObject(originalBoundSql);
		boundSqlMeta.setValue("sql", entityBoundSql.getSql());
		boundSqlMeta.setValue("parameterMappings", entityBoundSql.getParameterMappings());

		int rows = 0;
		try {
			// 将boundSql置入线程
			BoundSqlHolder.set(new CacheBoundSql(originalBoundSql, null));
			if (entity instanceof BaseEntity) {
				rows = this.update(statementId, entity);
			} else {
				rows = this.update(statementId);
			}
		} finally {
			// 执行完将boundSql从线程中移除
			BoundSqlHolder.clear();
		}
		return rows;
	}
	
	/**
	 * 获取BoundSql
	 * 
	 * @param ms
	 * @param parameterObject
	 * @return
	 */
	private BoundSql getBoundSql(MappedStatement ms, Object parameterObject) {
		if(parameterObject != null && parameterObject instanceof WrapParameter){
			parameterObject = ((WrapParameter)parameterObject).getParameterObject();
		}
		return ms.getBoundSql(wrapCollection(parameterObject));
	}

	/**
	 * 处理集合类型参数
	 * 
	 * @param object
	 * @return
	 */
	private Object wrapCollection(final Object object) {
		if (object instanceof Collection) {
			StrictMap<Object> map = new StrictMap<Object>();
			map.put("collection", object);
			if (object instanceof List) {
				map.put("list", object);
			}
			return map;
		} else if (object != null && object.getClass().isArray()) {
			StrictMap<Object> map = new StrictMap<Object>();
			map.put("array", object);
			return map;
		}
		return object;
	}
	
	/**
	 * 处理sql
	 * 
	 * @param sql
	 * @param param
	 * @return
	 */
	private BoundSql handleSql(String sql, Object param){
		if(param instanceof WrapParameter){
			param = ((WrapParameter)param).getParameterObject();
		}
		Configuration configuration = getSqlSession().getConfiguration();
		DynamicSqlSource sqlSource = new DynamicSqlSource(getSqlSession().getConfiguration(), new StaticTextSqlNode(sql.toString()));
		BoundSql boundSql = sqlSource.getBoundSql(param);
		
		String statementId = EntitySQL.SELECT;
		MappedStatement ms = configuration.getMappedStatement(statementId);
		BoundSql originalBoundSql = ms.getBoundSql(param);
		MetaObject originalBoundMeta = SystemMetaObject.forObject(originalBoundSql);
		originalBoundMeta.setValue("sql", boundSql.getSql());
		originalBoundMeta.setValue("parameterMappings", boundSql.getParameterMappings());
		return originalBoundSql;
	}

	public int getRandomRange() {
		return randomRange;
	}
}
