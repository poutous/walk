package org.walkframework.base.tools.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZipUtil {
	protected final static Logger log = LoggerFactory.getLogger(ZipUtil.class);
	private static final int BUFFEREDSIZE = 1024;

	/**
	 * 压缩zip格式的压缩文件
	 * @param inputFile 需压缩文件
	 * @param out 输出压缩文件
	 * @param base 结束标识
	 * @throws IOException
	 */
	public static boolean doZip(String filesDirPath, String zipFilePath) {
		return doZip(new File(filesDirPath), zipFilePath);
	}

	/**
	 * 压缩zip格式的压缩文件
	 * @param inputFile 需压缩文件
	 * @param out 输出压缩文件
	 * @param base 结束标识
	 * @throws IOException
	 */
	private static boolean doZip(File inputFile, String zipFileName) {
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(zipFileName));
			boolean result = doZip(out, inputFile, "");

			return result;
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			return false;
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				return false;
			}
		}
	}

	/**
	 * 压缩zip格式的压缩文件
	 * @param inputFile 需压缩文件
	 * @param out 输出压缩文件
	 * @param base 结束标识
	 * @throws IOException
	 */
	private static boolean doZip(ZipOutputStream out, File f, String base) {
		try {
			if (f.isDirectory()) {
				File[] fl = f.listFiles();
				out.putNextEntry(new org.apache.tools.zip.ZipEntry(base + "/"));
				base = base.length() == 0 ? "" : base + "/";
				for (int i = 0; i < fl.length; i++) {
					doZip(out, fl[i], base + fl[i].getName());
				}
			} else {
				out.putNextEntry(new org.apache.tools.zip.ZipEntry(base));
				FileInputStream in = new FileInputStream(f);
				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
				in.close();
			}
			return true;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	public static synchronized void unzip(String zipFileName) throws Exception {//解压到当前文件夹
		String outFile = zipFileName.substring(0, zipFileName.lastIndexOf("."));
		unzip(zipFileName, outFile);
	}

	/**
	 * 解压zip格式的压缩文件到指定位置
	 * @param zipFileName 压缩文件
	 * @param extPlace 解压目录,解压出来的目录名
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static synchronized void unzip(String zipFileName, String extPlace) throws Exception {
		try {
			File dirname = new File(extPlace);
			//目录不存在 创建目录
			if (!dirname.isDirectory()) {
				dirname.mkdirs();
			}
			File f = new File(zipFileName);
			ZipFile zipFile = new ZipFile(zipFileName);
			if ((!f.exists()) && (f.length() <= 0)) {
				throw new Exception("To extract the file does not exist.");
			}
			String strPath, gbkPath, strtemp;
			File tempFile = new File(extPlace);//从当前目录开始
			strPath = tempFile.getAbsolutePath();//输出的绝对位置
			Enumeration e = zipFile.getEntries();
			while (e.hasMoreElements()) {
				org.apache.tools.zip.ZipEntry zipEnt = (ZipEntry) e.nextElement();
				gbkPath = zipEnt.getName();
				if (zipEnt.isDirectory()) {
					strtemp = strPath + File.separator + gbkPath;
					File dir = new File(strtemp);
					dir.mkdirs();
					continue;
				} else {
					//读写文件
					InputStream is = zipFile.getInputStream(zipEnt);
					BufferedInputStream bis = new BufferedInputStream(is);
					gbkPath = zipEnt.getName();
					strtemp = strPath + File.separator + gbkPath;

					//建目录
					String strsubdir = gbkPath;
					for (int i = 0; i < strsubdir.length(); i++) {
						if (strsubdir.substring(i, i + 1).equalsIgnoreCase("/")) {
							String temp = strPath + File.separator + strsubdir.substring(0, i);
							File subdir = new File(temp);
							if (!subdir.exists())
								subdir.mkdir();
						}
					}
					FileOutputStream fos = new FileOutputStream(strtemp);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					int len;
					byte[] buff = new byte[BUFFEREDSIZE];
					while ((len = bis.read(buff)) != -1) {
						bos.write(buff, 0, len);
					}
					bos.close();
					fos.close();
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	public static void main(String args[]) throws Exception {
		//unzip("lzma912.zip");
	}
}
