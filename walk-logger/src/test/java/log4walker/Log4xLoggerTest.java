package log4walker;

import junit.framework.TestCase;

import org.walkframework.logger.Logger;
import org.walkframework.logger.LoggerFactory;
import org.walkframework.logger.LoggerType;

import com.ai.aif.log4x.Log4xClient;
import com.ai.aif.log4x.message.format.Trace;

public class Log4xLoggerTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty(LoggerFactory.LOGGER_CHOOSER, LoggerType.LOG4X.toString());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		System.clearProperty(LoggerFactory.LOGGER_CHOOSER);
	}

	/**
	 * <p>log4w用法</p>
	 * 
	 * @author  MengQK
	 * @since  [2017年4月11日]
	 */
	public void testStartToEnd() {
		Logger log = LoggerFactory.getLogger(Log4xLoggerTest.class);
		Trace trace = log.startTrace();
		//Trace trace = (Trace) log.getCurrentTrace();
		trace.setServiceName("wocrm_ITestSV_testlog4w");
		log.info("hello world~~");
		System.out.println(trace);
		log.endTrace();
		waitQueueHandler();
	}

	/**
	 * <p>原生用法</p>
	 * 
	 * @throws InterruptedException
	 * @author  MengQK
	 * @since  [2017年4月11日]
	 */
	public void testOrgin() throws InterruptedException {
		Log4xClient log4x = Log4xClient.getInstance();
		Trace trace = log4x.getTrace();
		trace.setServiceName("wocrm_ITestSV_testorgin");

		log4x.startTrace(trace);
		System.out.println(trace);
		Thread.sleep(100L);
		long t1 = System.currentTimeMillis();
		log4x.finishTrace(true);
		System.out.println("cost:: " + (System.currentTimeMillis() - t1) + "ms");
		waitQueueHandler();
	}

	/**
	 * <p>等待队列处理，使消息真正发送到kafka</p>
	 * 
	 * @author  MengQK
	 * @since  [2017年4月11日]
	 */
	public void waitQueueHandler() {
		int loop = 10;
		try {
			while (loop < 50) {
				Thread.sleep(100);
				loop++;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
