package org.walkframework.base.tools.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.base.tools.utils.EncryptUtil;



/**
 * ticket工具类
 *
 */
public abstract class TicketUtil {
	
	private static Logger log = LoggerFactory.getLogger(TicketUtil.class);
	
	//默认加密key
	private static final String DEFAULT_KEY = "!@#$%^&*()_+$WALKING_SECURITY_KEY!!@&**@#()_";
	
	//时间戳格式
	public static final String TIMESTAMP_FORMAT = "yyyyMMddHH";
	
	//生成ticket间隔符
	private static final String SPACE_CHARACTER = "~";
	
	/**
	 * 加密userId+sessionId+timestamp 生成ticket
	 * @param userId
	 * @return
	 */
	public static String getTicket(String userId, String sessionId, String key) {
		try {
			String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
			return EncryptUtil.encryptByDES(userId + SPACE_CHARACTER + sessionId + SPACE_CHARACTER + timestamp, key);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 生成带ticket的url
	 * @param url
	 * @param userId
	 * @param ticket
	 * @return
	 */
	public static String getTicketUrl(String ticket, String userId, String url) {
		if(url == null || "".equals(url)){
			throw new RuntimeException("Error: incoming URL is empty.");
		}
		return (url.indexOf("?") == -1 ? (url + "?userId=" + userId) : (url + "&userId=" + userId)) + "&ticket=" + ticket ;
	}

	
	/**
	 * 解密ticket 获取userId
	 * @param ticket
	 * @return
	 */
	public static String getUserId(String ticket, String key) {
		return getTicketInfo(ticket, key)[0];
	}
	
	/**
	 * 解密ticket 获取sessionId
	 * @param ticket
	 * @return
	 */
	public static String getSessionId(String ticket, String key) {
		return getTicketInfo(ticket, key)[1];
	}
	
	/**
	 * 解密ticket 获取时间戳
	 * @param ticket
	 * @return
	 */
	public static String getTimestamp(String ticket, String key) {
		return getTicketInfo(ticket, key)[2];
	}
	
	/**
	 * 获取ticket信息
	 * 
	 * @param ticket
	 * @param key
	 * @return
	 */
	private static String[] getTicketInfo(String ticket, String key){
		String[] ticketInfo = null;
		try {
			String deTicket = EncryptUtil.decryptByDES(ticket, key);
			ticketInfo = deTicket.split(SPACE_CHARACTER);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException("Ticket is not legal.", e);
		}
		return ticketInfo;
	}
	
	/**
	 * 获取缓存名称
	 * 
	 * @param userId
	 * @param ticket
	 * @return
	 */
	public static String getCacheName(String userId, String ticket){
		return userId + "@" + ticket;
	}
	
	/**
	 * 取key
	 * 属性配置文件中如果没配securityKey，则取默认
	 * 
	 * @return
	 */
	public static String getKey(){
		return SpringPropertyHolder.getContextProperty("securityKey", DEFAULT_KEY);
	}
	
	/**
	 * 示例
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		//加密/解密key，建议从数据库配置表获取
		String securityKey = "!@#%$WALKING_SECURITY_KEY^&*";
		String userId = "liuqf5";
		String sessionId = "aaaa-bbbb-cccc";
		String url = "http://192.168.22.222:8088/groupmgr/forward/namelist/NameList";
		
		//生成ticket
		String ticket = TicketUtil.getTicket(userId, sessionId, securityKey);
		
		//生成带ticket的url
		String ticketUrl = TicketUtil.getTicketUrl(ticket, userId, url);
		
		System.out.println("ticket: " + ticket);
		System.out.println("ticketUrl: " + ticketUrl);
		
		String tic = "D2E190DACCFF3CB873082C84287494838B579B2C9E5E3B1E8C26583D0A91BDDFEDCF2551F2E6DDB0138E07D94AB428A3599AAA8D7265B8C1E1B75F4DFD6D13359AA7EBA03EEBF2DB628D00BDEFA98EBF865FA7322AD6F6C851D3FB02D10F57C90025F23FBB05D13B";
		System.out.println(EncryptUtil.decryptByDES(tic, securityKey));
	}
}
