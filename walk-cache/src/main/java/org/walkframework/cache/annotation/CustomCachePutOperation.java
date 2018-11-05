package org.walkframework.cache.annotation;

import org.springframework.cache.interceptor.CachePutOperation;

/**
 * @author shf675
 * 
 */
public class CustomCachePutOperation extends CachePutOperation {
	public CustomCachePutOperation(Builder b) {
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

	public static class Builder extends CachePutOperation.Builder {
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
		public CustomCachePutOperation build() {
			return new CustomCachePutOperation(this);
		}
	}
}
