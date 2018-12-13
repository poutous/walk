package org.walkframework.base.tools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ticket参数工具类
 * 
 */
public class TimeTicketUtil {
	
	protected final static Logger log = LoggerFactory.getLogger(TimeTicketUtil.class);
	
	// 生成的ticket有效时间，默认一小时
	private static final long DEFAULT_TICKET_EXPIRED_TIME = 1 * 60 * 60 * 1000;
	
	//默认加密key
	private static final String DEFAULT_KEY = "!@#$%^&*()_+$WALKING_SECURITY_KEY!!@#$%^&*()_";
	
	// 分隔符
	private static final String SEPARATE = "_";
	
	/**
	 * 获取ticket密文、明文校验参数，以下划线分隔
	 * 格式：密文_明文
	 * @return
	 */
	public static String getTimeTiket() {
		return getTimeTiket(getKey());
	}
	
	/**
	 * 获取ticket密文、明文校验参数，以下划线分隔
	 * 格式：密文_明文
	 * @return
	 */
	public static String getTimeTiket(String key) {
		return getTimeTiket(key, null);
	}
	
	/**
	 * 获取ticket密文、明文校验参数，以下划线分隔
	 * 格式：密文_明文
	 * @return
	 */
	public static String getTimeTiket(String key, String additionalParam) {
		String currentTimeMillis = System.currentTimeMillis() + "";
		String encryptedTicket = "";
		try {
			encryptedTicket = EncryptUtil.encryptByDES(currentTimeMillis, key);
			if(additionalParam != null && !"".equals(additionalParam)){
				additionalParam = EncryptUtil.encryptByDES(additionalParam, key);
				encryptedTicket = additionalParam.concat(SEPARATE).concat(encryptedTicket);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return encryptedTicket.concat(SEPARATE).concat(currentTimeMillis);
	}
	
	/**
	 * 校验时间ticket
	 * 
	 * @param grantTicket
	 * @return
	 */
	public static String[] checkTimeTiket(String grantTicket){
		return checkTimeTiket(getKey(), grantTicket);
	}
	
	/**
	 * 校验时间ticket
	 * 
	 * @param key
	 * @param grantTicket
	 * @return
	 */
	public static String[] checkTimeTiket(String key, String grantTicket){
		return checkTimeTiket(key, grantTicket, null, DEFAULT_TICKET_EXPIRED_TIME);
	}
	
	/**
	 * 校验时间ticket
	 * 
	 * @param key
	 * @param grantTicket
	 * @param additionalParam
	 * @return
	 */
	public static String[] checkTimeTiket(String key, String grantTicket, String additionalParam){
		return checkTimeTiket(key, grantTicket, additionalParam, DEFAULT_TICKET_EXPIRED_TIME);
	}
	
	/**
	 * 校验时间ticket
	 * 
	 * @param key
	 * @param grantTicket
	 * @param additionalParam
	 * @param ticketExpiredMills
	 * @return
	 */
	public static String[] checkTimeTiket(String key, String grantTicket, String additionalParam, long ticketExpiredMills){
		String[] ret = {"0", "校验成功！"};
		if(grantTicket == null || "".equals(grantTicket)){
			ret[0] = "-1";
			ret[1] = "_GRANT_TICKET[" + grantTicket + "]不能为空！";
			return ret;
		}
		String[] ticketArr = grantTicket.split(SEPARATE);
		if(!(ticketArr.length == 2 || ticketArr.length == 3)){
			ret[0] = "-1";
			ret[1] = "_GRANT_TICKET[" + grantTicket + "]不合法！";
			return ret;
		}
		
		//解密
		String decryptedTime = "";
		String decryptedAdditionalParam = "";
		try {
			if(ticketArr.length == 2){
				decryptedTime = EncryptUtil.decryptByDES(ticketArr[0], key);
			}
			if(ticketArr.length == 3){
				decryptedTime = EncryptUtil.decryptByDES(ticketArr[1], key);
				decryptedAdditionalParam = EncryptUtil.decryptByDES(ticketArr[0], key);
			}
		} catch (Exception e) {
			ret[0] = "-1";
			ret[1] = "_GRANT_TICKET[" + grantTicket + "]不合法！";
			return ret;
		}
		
		//校验时间
		String plainTime = ticketArr.length == 2 ? ticketArr[1]:ticketArr[2];
		if (decryptedTime.equals(plainTime)) {
			//校验是否过期
			long nowTime = System.currentTimeMillis();
			long ticketTime = Long.valueOf(plainTime);
			if (nowTime - ticketTime > ticketExpiredMills) {
				ret[0] = "-1";
				ret[1] = "_GRANT_TICKET[" + grantTicket + "]已过期！";
				return ret;
			}
		} else {
			ret[0] = "-1";
			ret[1] = "_GRANT_TICKET[" + grantTicket + "]不合法！";
			return ret;
		}
		
		//校验附加参数
		if(ticketArr.length == 3 && !decryptedAdditionalParam.equals(additionalParam)){
			ret[0] = "-1";
			ret[1] = "_GRANT_TICKET[" + grantTicket + "]不合法！";
			return ret;
		}
		return ret;
	}
	
	/**
	 * 取key
	 * 属性配置文件中如果没配securityKey，则取默认
	 * 
	 * @return
	 */
	public static String getKey(){
		String key = DEFAULT_KEY;
		try {
			key = org.walkframework.base.tools.spring.SpringPropertyHolder.getContextProperty("securityKey");
		} catch (Exception e) {
		}
		return key;
	}
}
