package org.walkframework.cache.annotation;

import org.springframework.cache.interceptor.CacheableOperation;

/**
 * 
 * @author shf675
 * 
 */
public class CustomCacheableOperation extends CacheableOperation {

	public CustomCacheableOperation(Builder b) {
		super(b);
		this.cacheSeconds = b.cacheSeconds;
	}

	private String cacheSeconds;

	public String getCacheSeconds() {
		return cacheSeconds;
	}

	public void setCacheSeconds(String cacheSeconds) {
		this.cacheSeconds = cacheSeconds;
	}

	public static class Builder extends CacheableOperation.Builder {
		private String cacheSeconds;

		public String getCacheSeconds() {
			return cacheSeconds;
		}

		public void setCacheSeconds(String cacheSeconds) {
			this.cacheSeconds = cacheSeconds;
		}

		@Override
		protected StringBuilder getOperationDescription() {
			StringBuilder sb = super.getOperationDescription();
			sb.append(" | cacheSeconds='");
			sb.append(this.cacheSeconds);
			sb.append("'");
			return sb;
		}

		@Override
		public CustomCacheableOperation build() {
			return new CustomCacheableOperation(this);
		}
	}
}
