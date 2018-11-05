package org.walkframework.batis.plugins;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.batis.bean.Batch;
import org.walkframework.batis.bean.CacheBoundSql;
import org.walkframework.batis.cache.L2Cache;
import org.walkframework.batis.holder.BatchHolder;
import org.walkframework.batis.holder.BoundSqlHolder;
import org.walkframework.batis.holder.ResultMapHolder;

/**
 * walkbatis拦截器
 * 
 * @author shf675
 *
 */
@Intercepts( { 
	@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }), 
	@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }), 
	@Signature(type = StatementHandler.class, method = "getBoundSql", args = {}), 
	@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}), 
	@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = { Statement.class }) })
@SuppressWarnings("unchecked")
public class WalkbatisPlugin implements Interceptor {

	protected final Logger log = LoggerFactory.getLogger(WalkbatisPlugin.class);

	public final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 拦截类型设置
	 * @param target
	 * @return
	 */
	@Override
	public Object plugin(Object target) {
		if (target instanceof Executor || target instanceof StatementHandler || target instanceof ResultSetHandler) {
			return Plugin.wrap(target, this);
		}
		return target;
	}

	/**
	 * 拦截处理
	 * 
	 * @param target
	 * @return
	 */
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		long beginTime = System.currentTimeMillis();

		Object target = invocation.getTarget();
		Method method = invocation.getMethod();
		if (target instanceof Executor) {
			if ("query".equals(method.getName())) {
				Executor executor = (Executor) target;
				MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
				Object parameter = invocation.getArgs()[1];
				RowBounds rowBounds = (RowBounds) invocation.getArgs()[2];
				ResultHandler resultHandler = (ResultHandler) invocation.getArgs()[3];

				//更改取boundSql方式
				BoundSql boundSql;
				Integer cacheSeconds = null;
				CacheBoundSql cacheBoundSql = BoundSqlHolder.get();
				if (cacheBoundSql != null && cacheBoundSql.getBoundSql() != null) {
					boundSql = cacheBoundSql.getBoundSql();
					cacheSeconds = cacheBoundSql.getCacheSeconds();
				} else {
					boundSql = ms.getBoundSql(parameter);
				}

				//缓存处理。针对的是根据实体类做查询的语句
				CacheKey key = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
				Object result = null;
				if (cacheSeconds != null && CachingExecutor.class.equals(executor.getClass())) {
					MetaObject executorMeta = SystemMetaObject.forObject(executor);
					TransactionalCacheManager tcm = (TransactionalCacheManager) executorMeta.getValue("tcm");
					Executor delegate = (Executor) executorMeta.getValue("delegate");

					//键值加入缓存时间
					key.update(cacheSeconds.intValue());
					result = queryFromCacheByEntity(ms, parameter, rowBounds, resultHandler, key, boundSql, executor, delegate, tcm, cacheSeconds);
				} else {
					result = executor.query(ms, parameter, rowBounds, resultHandler, key, boundSql);
				}

				//打印sql执行时间
				printSqlExecuteTime(beginTime, boundSql, parameter, ms);
				return result;
			} else if ("update".equals(method.getName())) {
				MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
				Object parameter = invocation.getArgs()[1];
				Executor originalExecutor = (Executor) target;

				Integer rows = 0;
				Batch batch = BatchHolder.getBatch();
				if (batch == null || parameter instanceof List || originalExecutor instanceof BatchExecutor) {
					//原始
					rows = (Integer) invocation.proceed();
				} else {
					BatchExecutor batchExecutor = batch.getBatchExecutor();
					if (batch.getBatchExecutor() == null) {
						batchExecutor = new BatchExecutor(ms.getConfiguration(), originalExecutor.getTransaction());
						batch.setBatchExecutor(batchExecutor);
					}
					rows = batchExecutor.update(ms, parameter);
					if (batch.getCounter() == 0) {//计数器，每次获取减一
						batchExecutor.commit(false);//false表示不做事务提交，最后由原始executor负责事务提交
					}
				}

				BoundSql boundSql;
				CacheBoundSql cacheBoundSql = BoundSqlHolder.get();
				if (cacheBoundSql != null && cacheBoundSql.getBoundSql() != null) {
					boundSql = cacheBoundSql.getBoundSql();
				} else {
					boundSql = ms.getBoundSql(parameter);
				}

				//打印sql执行时间
				printSqlExecuteTime(beginTime, boundSql, parameter, ms);
				return rows;
			}
		} else if (target instanceof StatementHandler) {
			//更新时，修改绑定sql
			MetaObject shMeta = SystemMetaObject.forObject(target);
			MappedStatement mappedStatement = (MappedStatement) shMeta.getValue("delegate.mappedStatement");
			SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
			if (isUpdate(sqlCommandType)) {
				Executor executor = (Executor) shMeta.getValue("delegate.executor");
				//update: SimpleExecutor使用，在执行prepare方法之前做处理
				if ("prepare".equals(method.getName()) && executor instanceof SimpleExecutor) {
					handleStatementHandler(shMeta);
					return invocation.proceed();
				}
				//update: ReuseExecutor、BatchExecutor使用。处理getBoundSql方法，直接返回新的boundSql
				else if ("getBoundSql".equals(method.getName())) {
					BoundSql boundSql = handleStatementHandler(shMeta);
					if (boundSql != null) {
						return boundSql;
					}
				}
			}
			return invocation.proceed();
		} else if (target instanceof ResultSetHandler) {
			return handleResultSets(invocation);
		}
		return invocation.proceed();
	}

	/**
	 * 处理语句
	 * 
	 * @param shMeta
	 * @return
	 */
	private BoundSql handleStatementHandler(MetaObject shMeta) {
		CacheBoundSql cacheBoundSql = BoundSqlHolder.get();
		if (cacheBoundSql != null && cacheBoundSql.getBoundSql() != null) {
			Configuration configuration = (Configuration) shMeta.getValue("delegate.configuration");
			MappedStatement mappedStatement = (MappedStatement) shMeta.getValue("delegate.mappedStatement");
			//修改一些信息
			BoundSql boundSql = cacheBoundSql.getBoundSql();
			shMeta.setValue("delegate.boundSql", boundSql);
			shMeta.setValue("delegate.parameterHandler", configuration.newParameterHandler(mappedStatement, boundSql.getParameterObject(), boundSql));
			return boundSql;
		}
		return null;
	}

	private boolean isUpdate(SqlCommandType sqlCommandType) {
		return sqlCommandType == SqlCommandType.UPDATE || sqlCommandType == SqlCommandType.INSERT || sqlCommandType == SqlCommandType.DELETE;
	}

	/**
	 * 根据实体类查询的语句从缓存中取数据
	 * 
	 * @param <E>
	 * @param ms
	 * @param parameterObject
	 * @param rowBounds
	 * @param resultHandler
	 * @param key
	 * @param boundSql
	 * @param target
	 * @param delegate
	 * @param tcm
	 * @param cacheSeconds
	 * @return
	 * @throws Exception
	 */
	private <E> List<E> queryFromCacheByEntity(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql, Executor target, Executor delegate, TransactionalCacheManager tcm, Integer cacheSeconds) throws Exception {
		Cache cache = ms.getCache();
		if (cache != null) {
			invoke(target, "flushCacheIfRequired", new Object[] { ms }, new Class[] { ms.getClass() });
			if (resultHandler == null) {
				invoke(target, "ensureNoOutParams", new Object[] { ms, parameterObject, boundSql }, new Class[] { ms.getClass(), Object.class, boundSql.getClass() });
				List<E> list = (List<E>) tcm.getObject(cache, key);
				if (list == null) {
					list = delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
					tcm.putObject(cache, key, list); // issue #578 and #116

					//先提交
					target.commit(true);

					//设置过期时间
					if (cache.getClass().equals(L2Cache.class)) {
						((L2Cache) cache).expire(key, cacheSeconds.longValue());
					}
				}
				return list;
			}
		}
		return delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
	}

	/**
	 * hack原handleResultSets方法 修改resultMaps获取方式，其他按原方法执行
	 * 
	 * @param invocation
	 * @return
	 * @throws Throwable
	 */
	private Object handleResultSets(Invocation invocation) throws Throwable {
		// 获取原始对象一些信息
		Object target = invocation.getTarget();
		MetaObject resultSetHandlerMeta = SystemMetaObject.forObject(target);
		Configuration configuration = (Configuration) resultSetHandlerMeta.getValue("configuration");
		Map<String, ResultMapping> nextResultMaps = (Map<String, ResultMapping>) resultSetHandlerMeta.getValue("nextResultMaps");
		MappedStatement mappedStatement = (MappedStatement) resultSetHandlerMeta.getValue("mappedStatement");

		// 执行原逻辑
		ErrorContext.instance().activity("handling results").object(mappedStatement.getId());
		Statement stmt = (Statement) invocation.getArgs()[0];
		final List<Object> multipleResults = new ArrayList<Object>();
		int resultSetCount = 0;
		Object rsw = invoke(target, "getFirstResultSet", new Object[] { stmt }, new Class[] { Statement.class });

		//修改原resultMaps获取方式
		List<ResultMap> resultMaps = ResultMapHolder.get();
		if (resultMaps == null) {
			resultMaps = mappedStatement.getResultMaps();
		}
		int resultMapCount = resultMaps.size();
		invoke(target, "validateResultMapsCount", new Object[] { rsw, resultMapCount }, new Class[] { rsw.getClass(), int.class });
		while (rsw != null && resultMapCount > resultSetCount) {
			ResultMap resultMap = resultMaps.get(resultSetCount);
			invoke(target, "handleResultSet", new Object[] { rsw, resultMap, multipleResults, null }, new Class[] { rsw.getClass(), ResultMap.class, List.class, ResultMapping.class });
			rsw = invoke(target, "getNextResultSet", new Object[] { stmt }, new Class[] { Statement.class });
			invoke(target, "cleanUpAfterHandlingResultSet", null, null);
			resultSetCount++;
		}

		String[] resultSets = mappedStatement.getResultSets();// 待修改
		if (resultSets != null) {
			while (rsw != null && resultSetCount < resultSets.length) {
				ResultMapping parentMapping = nextResultMaps.get(resultSets[resultSetCount]);
				if (parentMapping != null) {
					String nestedResultMapId = parentMapping.getNestedResultMapId();
					ResultMap resultMap = configuration.getResultMap(nestedResultMapId);
					invoke(target, "handleResultSet", new Object[] { rsw, resultMap, null, parentMapping }, new Class[] { rsw.getClass(), ResultMap.class, List.class, ResultMapping.class });
				}
				rsw = invoke(target, "getNextResultSet", new Object[] { stmt }, new Class[] { Statement.class });
				invoke(target, "cleanUpAfterHandlingResultSet", null, null);
				resultSetCount++;
			}
		}
		return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
	}

	@Override
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub
	}

	/**
	 * 反射执行方法
	 * @param target
	 * @param methodName
	 * @param params
	 * @param paramsClass
	 * @return
	 * @throws Exception
	 */
	private static Object invoke(Object target, String methodName, Object[] params, Class<?>[] paramsClass) throws Exception {
		Method method = target.getClass().getDeclaredMethod(methodName, paramsClass);
		method.setAccessible(true);
		return method.invoke(target, params);
	}

	/**
	 * 打印sql执行时间
	 * 
	 * @param beginTime
	 * @param sql
	 */
	private void printSqlExecuteTime(long beginTime, BoundSql boundSql, Object parameterObject, MappedStatement ms) {
		//打印绑定后的SQL会损失一些性能，因此只在日志级别为debug或以下才打印绑定参数后的SQL
		Log statementLog = ms.getStatementLog();
		if (statementLog.isDebugEnabled()) {
			String sql = getSql(boundSql, parameterObject, ms.getConfiguration());
			statementLog.debug("==>  " + sql);
			statementLog.debug("<==  SQL execute time: " + (double) (System.currentTimeMillis() - beginTime) / (double) 1000 + "s");
		}

		//本地开发时直接将绑定参数后的SQL打印到控制台
		String isHotDeploy = ms.getConfiguration().getVariables().getProperty("isHotDeploy");
		if ("true".equals(isHotDeploy)) {
			String sql = getSql(boundSql, parameterObject, ms.getConfiguration());
			double executeTime = (double) (System.currentTimeMillis() - beginTime) / (double) 1000;
			System.out.println("[" + executeTime + "s]" + ms.getId() + " ==> " + sql);
		}
	}

	/**
	 * 获取绑定参数后的sql
	 *  
	 * @param boundSql
	 * @param parameterObject
	 * @param configuration
	 * @return
	 */
	private String getSql(BoundSql boundSql, Object parameterObject, Configuration configuration) {
		String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
		try {
			List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
			TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
			if (parameterMappings != null) {
				for (int i = 0; i < parameterMappings.size(); i++) {
					ParameterMapping parameterMapping = parameterMappings.get(i);
					if (parameterMapping.getMode() != ParameterMode.OUT) {
						Object value;
						String propertyName = parameterMapping.getProperty();
						if (boundSql.hasAdditionalParameter(propertyName)) {
							value = boundSql.getAdditionalParameter(propertyName);
						} else if (parameterObject == null) {
							value = null;
						} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
							value = parameterObject;
						} else {
							MetaObject metaObject = configuration.newMetaObject(parameterObject);
							value = metaObject.getValue(propertyName);
						}
						sql = replacePlaceholder(sql, value);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return sql;
	}

	/**
	 * 问号替换成属性值
	 * @param sql
	 * @param propertyValue
	 * @return
	 */
	private String replacePlaceholder(String sql, Object propertyValue) {
		String result;
		if (propertyValue != null) {
			if (propertyValue instanceof String) {
				result = "'" + propertyValue + "'";
			} else if (propertyValue instanceof Date) {
				result = "'" + new SimpleDateFormat(DEFAULT_PATTERN, Locale.getDefault()).format(propertyValue) + "'";
			} else {
				result = propertyValue.toString();
			}
		} else {
			result = "null";
		}
		return sql.replaceFirst("\\?", result);
	}

}
