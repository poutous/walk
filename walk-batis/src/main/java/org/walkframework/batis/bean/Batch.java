package org.walkframework.batis.bean;

import org.apache.ibatis.executor.BatchExecutor;

/**
 * @author shf675
 *
 */
public class Batch {
	
	private BatchExecutor batchExecutor;
	
	private int counter;
	
	public Batch(int counter){
		this.counter = counter;
	}

	public BatchExecutor getBatchExecutor() {
		return batchExecutor;
	}

	public int getCounter() {
		if(counter > 0){
			counter = counter - 1;
		}
		return counter;
	}

	public void setBatchExecutor(BatchExecutor batchExecutor) {
		this.batchExecutor = batchExecutor;
	}
	
}
