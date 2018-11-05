package org.walkframework.data.bean;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class Pagination implements Serializable {
	
	protected static final SerializerFeature[] serializerFeatures = new SerializerFeature[]{SerializerFeature.WriteDateUseDateFormat};
	
	private static final long serialVersionUID = 1L;
	
	public static final int MAX_PAGE_SIZE = 2000;
	public static final int MAX_RECODE_SIZE = Integer.MAX_VALUE;
	private boolean batch;
	private boolean range;
	private boolean needCount = true;
	private boolean onlyCount = false;
	private int start;
	private int size;
	private int count;
	private int currPage;
	
    /**
     * construct function
     */
    public Pagination(){
    	range = true;
    }
    /**
     * construct function
     * @param batch
     * @throws Exception
     */
    public Pagination(boolean batch){
    	setBatch(batch);
    }
    
	/**
	 * set batch
	 * @param batch
	 * @throws Exception
	 */
	public Pagination setBatch(boolean batch){
		this.batch = batch;
		if (batch) range = true;
		return this;
	}
	
	/**
	 * set range
	 * @param start
	 * @param count
	 */
	public Pagination setRange(int start, int size) {
		range = true;
		this.start = start;
		this.size = size;
		return this;
	}
	
	/**
	 * is batch
	 * @return boolean
	 */
	public boolean isBatch() {
		return batch;
	}
	
	/**
	 * is range
	 * @return boolean
	 */
	public boolean isRange() {
		return range;
	}

	/**
	 * is need count
	 * @return boolean
	 */
	public boolean isNeedCount() {
		return needCount;
	}
	
	/**
	 * set need count
	 * @param needCount
	 */
	public Pagination setNeedCount(boolean needCount) {
		this.needCount = needCount;
		return this;
	}
	
	/**
	 * is count
	 * @return int
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * set count
	 * @param count
	 */
	public Pagination setCount(int count) {
		this.count = count;
		return this;
	}
	
	/**
	 * get start
	 * @return int
	 */
	public int getStart() {
		return start;
	}
	
	/**
	 * get size
	 * @return int
	 */
	public int getSize() {
		return size;
	}

	/**
	 * get curr page
	 * @return int
	 */
	public int getCurrPage() {
		return currPage;
	}
	
	/**
	 * get curr page
	 * @param currPage
	 */
	public Pagination setCurrPage(int currPage) {
		this.currPage = currPage;
		return this;
	}

	/**
	 * @return onlyCount
	 */
	public boolean isOnlyCount() {
		return onlyCount;
	}

	/**
	 * @param onlyCount 要设置的 onlyCount
	 */
	public Pagination setOnlyCount(boolean onlyCount) {
		this.onlyCount = onlyCount;
		return this;
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(this, serializerFeatures);
	}
	
}