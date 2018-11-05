package org.walkframework.base.tools.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 加密/解密
 *
 */
public abstract class EncryptUtil {
	protected final static Logger log = LoggerFactory.getLogger(EncryptUtil.class);
	public static final String SECURITY_KEY = "!@#$%^&*()_+$WALKING_SECURITY_KEY!";
	
	public static void main(String[] args) throws Exception {
		System.out.println(encryptByDES("23u2u302u02u0joaejoo343#$%^&*&^%$#$%^&", SECURITY_KEY));
	}
	
	public static String encryptByMD5(String plaintext) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] data = encryptByMD5(plaintext.getBytes());
		String hexStr = StringUtil.byteArray2HexStr(data);
		return hexStr;
	}

	public static String encryptByMD5(String plaintext, String charset) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] data = encryptByMD5(plaintext.getBytes(charset));
		String hexStr = StringUtil.byteArray2HexStr(data);
		return hexStr;
	}

	public static byte[] encryptByMD5(byte[] plaintext) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(plaintext);
		byte[] result = md.digest();
		return result;
	}

	private static Key getDESKey(byte[] arrBTmp) throws Exception {
		byte[] arrB = new byte[8];
		for (int i = 0; (i < arrBTmp.length) && (i < arrB.length); ++i) {
			arrB[i] = arrBTmp[i];
		}
		Key key = new SecretKeySpec(arrB, "DES");
		return key;
	}

	public static byte[] encryptByDES(byte[] plaintext, String key) throws Exception {
		Cipher encryptCipher = Cipher.getInstance("DES");
		encryptCipher.init(1, getDESKey(key.getBytes()));
		return encryptCipher.doFinal(plaintext);
	}

	public static String encryptByDES(String plaintext, String key) throws Exception {
		byte[] data = encryptByDES(plaintext.getBytes(), key);
		String hexStr = StringUtil.byteArray2HexStr(data);
		return hexStr;
	}

	public static String encryptByDES(String plaintext, String key, String charset) throws Exception {
		byte[] data = encryptByDES(plaintext.getBytes(charset), key);
		String hexStr = StringUtil.byteArray2HexStr(data);
		return hexStr;
	}

	public static byte[] decryptByDES(byte[] crypttext, String key) throws Exception {
		Cipher decryptCipher = Cipher.getInstance("DES");
		decryptCipher.init(2, getDESKey(key.getBytes()));
		return decryptCipher.doFinal(crypttext);
	}

	public static String decryptByDES(String crypttext, String key) throws Exception {
		byte[] data = StringUtil.hexStr2ByteArray(crypttext);
		byte[] result = decryptByDES(data, key);
		return new String(result);
	}

	public static String decryptByDES(String crypttext, String key, String charset) throws Exception {
		byte[] data = StringUtil.hexStr2ByteArray(crypttext);
		byte[] result = decryptByDES(data, key);
		return new String(result, charset);
	}

	public static KeyPair getRSAKeyPair() {
		return getRSAKeyPair(1024);
	}

	public static KeyPair getRSAKeyPair(int length) {
		KeyPairGenerator kpg = null;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage(), e);
		}

		kpg.initialize(length);

		KeyPair keyPair = kpg.genKeyPair();
		return keyPair;
	}

	public static PublicKey getPublicKey(String modulus, String publicExponent) throws Exception {
		//PublicKey publicKey = new RSAPublicKeyImpl(new BigInteger(modulus), new BigInteger(publicExponent));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(publicExponent));  
        PublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);  
		return publicKey;
	}

	public static String encryptByRSA(String plaintext, Key key) throws Exception {
		byte[] data = encryptByRSA(plaintext.getBytes(), key);
		String hexStr = StringUtil.byteArray2HexStr(data);
		return hexStr;
	}

	public static String encryptByRSA(String plaintext, Key key, String charset) throws Exception {
		byte[] data = encryptByRSA(plaintext.getBytes(charset), key);
		String hexStr = StringUtil.byteArray2HexStr(data);
		return hexStr;
	}

	public static byte[] encryptByRSA(byte[] plaintext, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(1, key);

		return cipher.doFinal(plaintext);
	}

	public static String decryptByRSA(String crypttext, Key key) throws Exception {
		return decryptByRSA(crypttext, key, System.getProperty("file.encoding"));
	}

	public static String decryptByRSA(String crypttext, Key key, String charset) throws Exception {
		byte[] data = StringUtil.hexStr2ByteArray(crypttext);
		byte[] result = decryptByRSA(data, key);
		return new String(result, charset);
	}

	public static byte[] decryptByRSA(byte[] crypttext, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(2, key);
		return cipher.doFinal(crypttext);
	}
}

//辅助类
class StringUtil {
	public static String ltrim(String str, String invalidStr) {
		if ((str == null) || (str.length() == 0) || (invalidStr == null) || (invalidStr.length() == 0)) {
			return str;
		}

		int trimLength = 0;

		for (int i = 0; (str != null) && (i < str.length()); ++i) {
			boolean stopFlag = true;
			for (int j = 0; (invalidStr != null) && (j < invalidStr.length()); ++j) {
				if (str.charAt(i) == invalidStr.charAt(j)) {
					++trimLength;
					stopFlag = false;
					break;
				}
			}
			if (stopFlag) {
				break;
			}
		}

		return str.substring(trimLength);
	}

	public static String rtrim(String str, String invalidStr) {
		if ((str == null) || (str.length() == 0) || (invalidStr == null) || (invalidStr.length() == 0)) {
			return str;
		}

		int trimLength = 0;

		for (int i = str.length() - 1; i >= 0; --i) {
			boolean stopFlag = true;
			for (int j = 0; (invalidStr != null) && (j < invalidStr.length()); ++j) {
				if (str.charAt(i) == invalidStr.charAt(j)) {
					++trimLength;
					stopFlag = false;
					break;
				}
			}
			if (stopFlag) {
				break;
			}
		}

		return str.substring(0, str.length() - trimLength);
	}

	public static String trim(String str, String invalidStr) {
		return ltrim(rtrim(str, invalidStr), invalidStr);
	}

	public static String ltrim(String str) {
		if ((str == null) || (str.length() == 0)) {
			return str;
		}

		int beginIndex = 0;
		while ((beginIndex < str.length()) && (str.charAt(beginIndex) <= ' ')) {
			++beginIndex;
		}
		return str.substring(beginIndex);
	}

	public static String rtrim(String str) {
		if ((str == null) || (str.length() == 0)) {
			return str;
		}

		int endIndex = str.length();
		while ((endIndex > 0) && (str.charAt(endIndex - 1) <= ' ')) {
			--endIndex;
		}
		return str.substring(0, endIndex);
	}

	public static String trim(String str) {
		return ltrim(rtrim(str));
	}

	public static boolean isEmpty(String str) {
		return ((str == null) || (str.length() == 0));
	}

	public static boolean isNumber(String str) {
		return ((str != null) && (((str.matches("[+-]?[0-9]+\\.?[0-9]*")) || (str.matches("[+-]?[0-9]*\\.?[0-9]+")))));
	}

	public static boolean isInteger(String str) {
		return ((str != null) && (str.matches("[+-]?[0-9]+")));
	}

	public static boolean isNaturalNumber(String str) {
		return ((str != null) && (str.matches("+?[0-9]+")));
	}

	public static boolean isBoolean(String str) {
		return ((str != null) && ((("true".equals(str)) || ("false".equals(str)))));
	}

	public static boolean isLetters(String str) {
		return ((str != null) && (str.matches("[a-zA-z]+")));
	}

	public static boolean contains(String str, String value) {
		if ((str == null) || (value == null)) {
			return false;
		}

		if ("".equals(value)) {
			return false;
		}

		int index = str.indexOf(value);

		return (index != -1);
	}

	public static String encodeString(String str, String destCharset) throws Exception {
		if (str == null) {
			return null;
		}

		return new String(str.getBytes(), destCharset);
	}

	public static String encodeString(String str, String srcCharset, String destCharset) throws Exception {
		if (str == null) {
			return null;
		}

		return new String(str.getBytes(srcCharset), destCharset);
	}

	public static String camel2Underline(String str) {
		if ((str == null) || ("".equals(str))) {
			return null;
		}
		String result = "";

		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);

			if ((c >= 'A') && (c <= 'Z')) {
				result = result + "_" + c;
			} else {
				result = result + c;
			}
		}
		return result.toUpperCase();
	}

	public static String string2Underline(String str, int[] splitIndexes) {
		if ((str == null) || ("".equals(str)) || (str.length() < splitIndexes.length)) {
			return null;
		}
		String result = "";

		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			boolean split = false;

			for (int j = 0; j < splitIndexes.length; ++j) {
				if (i == splitIndexes[j]) {
					split = true;
					break;
				}
			}

			if (split) {
				result = result + "_" + c;
			} else {
				result = result + c;
			}
		}
		return result.toUpperCase();
	}

	public static String underline2Camel(String str) {
		if ((str == null) || ("".equals(str))) {
			return null;
		}
		str = str.toLowerCase();

		String[] values = str.split("_");
		String result = "";

		for (int i = 0; i < values.length; ++i) {
			if (i == 0) {
				result = result + values[i];
			} else {
				result = result + values[i].substring(0, 1).toUpperCase() + values[i].substring(1);
			}
		}
		return result;
	}

	public static String hexStr2String(String hexStr) throws UnsupportedEncodingException {
		byte[] out = hexStr2ByteArray(hexStr);
		return new String(out);
	}

	public static String hexStr2String(String hexStr, String charset) throws UnsupportedEncodingException {
		byte[] out = hexStr2ByteArray(hexStr);
		return new String(out, charset);
	}

	public static byte[] hexStr2ByteArray(String hexStr) {
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

	public static String string2HexStr(String str) throws UnsupportedEncodingException {
		byte[] data = str.getBytes();
		return byteArray2HexStr(data);
	}

	public static String string2HexStr(String str, String charset) throws UnsupportedEncodingException {
		byte[] data = str.getBytes(charset);
		return byteArray2HexStr(data);
	}
	
	public static String byteArray2HexStr(byte[] data) {
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