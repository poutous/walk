package org.walkframework.shiro.util.password;

/**
 * 
 * 散列加密
 * 
 * 特点：返回的长度可控制
 * 
 */
public class Encryptor {

	private static final int DEFAULT_RETLEN = 6;

	/**
	 * 对字符串加密
	 * 
	 * 默认返回长度为6的字符串
	 * 
	 * @param plaintext：原始字符串
	 * @return
	 */
	public static String encrypt(String plaintext) {
		return encrypt(plaintext, DEFAULT_RETLEN);
	}
	
	/**
	 * 对字符串加密
	 * 
	 * @param plaintext：原始字符串
	 * @param salt：盐值
	 * @return
	 */
	public static String encrypt(String plaintext, String salt) {
		return encrypt(plaintext, salt, DEFAULT_RETLEN);
	}

	/**
	 * 对字符串加密
	 * 
	 * @param plaintext：原始字符串
	 * @param retlen：指定返回字符串长度
	 * @return
	 */
	public static String encrypt(String plaintext, long retlen) {
		return encrypt(plaintext, "", retlen);
	}

	/**
	 * 对字符串加密
	 * 
	 * @param plaintext：原始字符串
	 * @param salt：盐值
	 * @param retlen：指定返回字符串长度
	 * @return
	 */
	public static String encrypt(String plaintext, String salt, long retlen) {
		/* 定义初始化变量 */
		long bytes = retlen;
		long randSeed1 = 23456;
		long randSeed2 = 31212;
		long randSeed3 = 1029;
		long modSeed = 32768;
		long mm[] = new long[101];
		char tempChar = '\0';
		char tempChar1 = '\0';
		char tempChar2 = '\0';
		String cmm = "", pwd = "";
		String result = "";

		/* 加密过程 */
		if (plaintext == null || plaintext.length() <= 0) {
			return null;
		}

		pwd = plaintext + salt;

		long longtmp = 0;
		int length = pwd.length();
		for (int i = 1; i <= length; i++) {
			tempChar = pwd.charAt(i - 1);
			longtmp = (long) tempChar;
			randSeed1 = (randSeed1 + longtmp * i) % modSeed;
			randSeed2 = ((randSeed2 + longtmp * (length - i)) % (modSeed / 4)) * 2;
			randSeed3 = ((randSeed3 + longtmp * longtmp) % (modSeed / 4)) * 2 + 1;
		}

		plaintext = pwd;
		if (bytes > 10) {
			bytes = 10;
		}
		length = plaintext.length(); // 输入密码的长度.
		/* 保证输入密码位bytes位 */
		if (length < bytes) {
			for (length = length + 1; length <= bytes; length++) {
				randSeed1 = (randSeed1 * randSeed2 + randSeed3) % modSeed;
				longtmp = randSeed1 % 126;
				if (longtmp < 33) {
					longtmp = 'A' + longtmp;
				}
				plaintext += (char) longtmp;
			}
		} else {
		}

		length = salt.length(); // 第二密参的长度
		/* 保证第二密参的长度也为bytes位 */
		if (length < bytes) {
			for (length = length + 1; length <= bytes; length++) {
				randSeed1 = (randSeed1 * randSeed2 + randSeed3) % modSeed;
				longtmp = randSeed1 % 126;
				if (longtmp < 33) {
					longtmp = 'A' + longtmp;
				}
				salt += (char) longtmp;
			}
		}

		/* mm数组的赋值 */
		longtmp = (randSeed1 * randSeed2 + randSeed3) % modSeed;
		for (int j = 1; j <= bytes; j++) {
			for (int k = 1; k <= bytes; k++) {
				tempChar1 = plaintext.charAt(j - 1);
				tempChar2 = salt.charAt(j - 1);
				longtmp = (longtmp * randSeed1 + tempChar1 * tempChar2 * j) % modSeed;
				if (longtmp >= modSeed / 2) {
					randSeed1 = (randSeed1 * randSeed2 + randSeed3) % modSeed;
					mm[(int) (randSeed1 % (bytes * bytes))] = randSeed1;
				} else {
					randSeed1 = (randSeed1 * (randSeed3 + 1) + randSeed2 + 1) % modSeed;
					mm[(int) (randSeed1 % (bytes * bytes))] = randSeed1;
				}
			}
		}

		for (int k = (int) (bytes * bytes); k >= 1; k--) {
			if (k > 1) {
				mm[k - 1] += (mm[k] / 256);
			}
		}

		for (int k = 1; k <= bytes * bytes; k++) {
			randSeed1 = (randSeed1 * randSeed1) % modSeed;
			if (mm[k] == 0) {
				mm[k] = randSeed1;
			}
		}

		/* 核心加密算法 */
		for (int k = 1; k <= bytes; k++) {
			for (int i = 1; i <= bytes; i++) {
				tempChar1 = plaintext.charAt(i - 1);
				tempChar2 = salt.charAt(k - 1);
				longtmp = (mm[(int) (i + (k - 1) * bytes)] * (int) tempChar2 * (int) tempChar1) % 62;
				if (longtmp < 10) {
					cmm += (char) (longtmp + (int) '0');
				} else {
					if (longtmp < 36) {
						cmm += (char) (longtmp - 10 + (int) 'a');
					} else {
						if (longtmp < 62) {
							cmm += (char) (longtmp - 36 + (int) 'A');
						} else {
							cmm += "_";
						}
					}
				}
			}
		}

		result = cmm.substring(0, (int) retlen);
		return result;
	}
}
