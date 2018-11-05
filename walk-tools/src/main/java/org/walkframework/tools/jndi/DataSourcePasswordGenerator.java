package org.walkframework.tools.jndi;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * 加密/解密
 *
 */
public abstract class DataSourcePasswordGenerator {
	
	public static final String DEFAULT_SECURITY_KEY = "!@#%$WALK-SECURITY^&*";
	
	/**
	 * 密码生成入口方法
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if(args.length == 0 || "".equals(args[0].trim())){
			System.out.println("Please enter the passwords of the plaintext!");
			return;
		}
		String pass = args[0];
		String securityKey = args.length > 1 ? ("".equals(args[1].trim()) ? DEFAULT_SECURITY_KEY : args[1].trim()):DEFAULT_SECURITY_KEY;
		System.out.println("{DES}" + encryptByDES(pass, securityKey));
	}

	/**
	 * 加密
	 * 
	 * @param plaintext
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private static String encryptByDES(String plaintext, String securityKey) throws Exception {
		Cipher encryptCipher = Cipher.getInstance("DES");
		encryptCipher.init(1, getDESKey(securityKey.getBytes()));
		byte[] data =  encryptCipher.doFinal(plaintext.getBytes());
		return byteArray2HexStr(data);
	}

	/**
	 * 解密
	 * 首先从系统变量里查找-Dds.security-key配置的密钥，如果找不到则使用默认密钥
	 * 
	 * @param crypttext
	 * @param key
	 * @return
	 * @throws Exception
	 */
	static String decryptByDES(String crypttext) throws Exception {
		String securityKey = System.getProperty("ds.security-key", DEFAULT_SECURITY_KEY);
		byte[] data = hexStr2ByteArray(crypttext);
		Cipher decryptCipher = Cipher.getInstance("DES");
		decryptCipher.init(2, getDESKey(securityKey.getBytes()));
		byte[] result = decryptCipher.doFinal(data);
		return new String(result);
	}

	private static Key getDESKey(byte[] arrBTmp) throws Exception {
		byte[] arrB = new byte[8];
		for (int i = 0; (i < arrBTmp.length) && (i < arrB.length); ++i) {
			arrB[i] = arrBTmp[i];
		}
		Key key = new SecretKeySpec(arrB, "DES");
		return key;
	}

	private static byte[] hexStr2ByteArray(String hexStr) {
		hexStr = hexStr.toUpperCase();
		char[] data = hexStr.toCharArray();
		int len = data.length;

		byte[] out = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			String strTmp = new String(data, i, 2);
			out[(i / 2)] = (byte) Integer.parseInt(strTmp, 16);
		}
		return out;
	}
	
	private static String byteArray2HexStr(byte[] data) {
		char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		int l = data.length;
		char[] out = new char[l << 1];

		int i = 0;
		for (int j = 0; i < l; ++i) {
			out[(j++)] = digits[((0xF0 & data[i]) >>> 4)];
			out[(j++)] = digits[(0xF & data[i])];
		}
		return new String(out);
	}
}