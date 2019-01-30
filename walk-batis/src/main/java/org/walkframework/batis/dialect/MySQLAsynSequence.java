package org.walkframework.batis.dialect;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.ibatis.logging.LogFactory;
import org.walkframework.batis.dao.Dao;
import org.walkframework.batis.dao.SqlSessionDao;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;

/**
 * mysql异步方式获取序列
 * 
 * 原因：mysql获取序列逻辑是先插入自增表再取值，有插入操作，如果事物为只读则会报错
 * 
 * @author shf675
 * 
 */
public class MySQLAsynSequence {

	private final CountDownLatch countDownLatch = new CountDownLatch(1);
	
	private static final Random random = new Random();

	private String value;
	
	public String getSequenceValue(final Dao dao, final String sequence) {
		// 1、获取数据
		new Thread() {
			@Override
			public void run() {
				// 获取序列并设置结果
				value = getSequence(dao, sequence);

				// 3、获取完毕结束主线程等待
				countDownLatch.countDown();
			}
		}.start();

		// 2、主线程等待
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			LogFactory.getLog(MySQLAsynSequence.class.getSimpleName() + ".getSequenceValue").error(e.getMessage(), e);
		}
		return value;
	}
	
	private String getSequence(Dao dao, String sequence) {
		IData<String, Object> param = new DataMap<String, Object>();
		param.put("seq_name", sequence);
		
		//1、向序列表中插入数据
		dao.insert("CommonSQL.selectSequence_mysql", param);
		
		//2、清空序列表：为提高性能，不需要每次都清空序列表。在一定范围内取随机数，取到1时就删。
		if(random.nextInt(((SqlSessionDao)dao).getRandomRange()) == 1){
			dao.delete("CommonSQL.clearSeqTable", param);
		}
		
		//3、获取序列值
		return param.getString("id");
	}
}
