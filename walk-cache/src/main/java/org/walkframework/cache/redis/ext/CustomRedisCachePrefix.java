package org.walkframework.cache.redis.ext;

import org.springframework.data.redis.cache.DefaultRedisCachePrefix;

/**
 * 前缀扩展
 * 
 * 扩展org.springframework.data.redis.cache.DefaultRedisCachePrefix
 * @author shf675
 *
 */
public class CustomRedisCachePrefix extends DefaultRedisCachePrefix {
	
	public static final String DEFAULT_DELIMITER = ":";
	
	private String delimiter = DEFAULT_DELIMITER;

	public String getDelimiter() {
		return delimiter;
	}

	public CustomRedisCachePrefix() {
		this(DEFAULT_DELIMITER);
	}

	public CustomRedisCachePrefix(String delimiter) {
		super(delimiter);
		this.delimiter = delimiter;
	}
}
