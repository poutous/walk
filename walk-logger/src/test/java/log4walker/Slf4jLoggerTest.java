package log4walker;

import org.walkframework.logger.Logger;
import org.walkframework.logger.LoggerFactory;
import org.walkframework.logger.LoggerType;

import junit.framework.TestCase;

public class Slf4jLoggerTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty(LoggerFactory.LOGGER_CHOOSER, LoggerType.SLF4J.toString());
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		System.clearProperty(LoggerFactory.LOGGER_CHOOSER);
	}

	public void testBasicUsage() {
		Logger log = LoggerFactory.getLogger(Slf4jLoggerTest.class);
		log.enterFunction();
		log.info("the basic log info usage here for param1:[{}], param2:{}", "param1 value", "param2 value...");
		log.endFunction("i'm done...");
	}

	public void testTraceIdUsage() {
		Logger log = LoggerFactory.getLogger(Slf4jLoggerTest.class);
		String traceId = log.startTrace("201712011100223333");
		log.info("this msg should show the traceId param {}.", traceId);
		log.endTrace();
	}
	
	public void testUUIDTraceIdUsage() {
		Logger log = LoggerFactory.getLogger(Slf4jLoggerTest.class);
		String traceId = log.startTrace();
		log.info("this msg should show the UUID traceId param {}.", traceId);
		log.endTrace();
	}

}
