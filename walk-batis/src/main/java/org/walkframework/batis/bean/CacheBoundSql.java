package org.walkframework.batis.bean;

import org.apache.ibatis.mapping.BoundSql;

/**
 * @author shf675
 *
 */
public class CacheBoundSql {

	private BoundSql boundSql;

	private Integer cacheSeconds;

	public CacheBoundSql(BoundSql boundSql, Integer cacheSeconds) {
		this.boundSql = boundSql;
		this.cacheSeconds = cacheSeconds;
	}

	public BoundSql getBoundSql() {
		return boundSql;
	}

	public Integer getCacheSeconds() {
		return cacheSeconds;
	}
}
