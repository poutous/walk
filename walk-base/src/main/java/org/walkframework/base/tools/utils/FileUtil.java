package org.walkframework.base.tools.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.tools.excel.ExcelParser;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.data.util.IData;
import org.walkframework.data.util.IDataset;

public abstract class FileUtil {
	protected static Common common = SingletonFactory.getInstance(Common.class);

	protected static Logger log = LoggerFactory.getLogger(FileUtil.class);

	public static final String UPLOAD_TYPE_ATTACH = "1";
	public static final String UPLOAD_TYPE_IMAGE = "2";
	public static final String UPLOAD_TYPE_EXPORT = "3";
	public static final String UPLOAD_TYPE_IMPORT = "4";
	public static final String UPLOAD_TYPE_TEMP = "5";

	public static final String UPLOAD_KIND_USER = "1";
	public static final String UPLOAD_KIND_SYSTEM = "2";

	public static final String FILE_TYPE_JPEG = "JPEG";
	public static final String FILE_TYPE_JPG = "JPG";
	public static final String FILE_TYPE_GIF = "GIF";
	public static final String FILE_TYPE_PNG = "PNG";
	public static final String FILE_TYPE_DOC = "DOC";
	public static final String FILE_TYPE_XLS = "XLS";
	public static final String FILE_TYPE_PPT = "PPT";
	public static final String FILE_TYPE_PDF = "PDF";

	public static final String CONTENT_TYPE_IMAGE_JPEG = "image/jpeg";
	public static final String CONTENT_TYPE_IMAGE_GIF = "image/gif";
	public static final String CONTENT_TYPE_IMAGE_PNG = "image/png";
	public static final String CONTENT_TYPE_WORD = "application/vnd.msword";
	public static final String CONTENT_TYPE_EXCEL = "application/vnd.ms-excel";
	public static final String CONTENT_TYPE_POWERPOINT = "application/vnd.ms-powerpoint";
	public static final String CONTENT_TYPE_PDF = "application/pdf";

	/**
	 * get upload type
	 * @param upload_type
	 * @return String
	 */
	public static String getUploadPath(String upload_type) {
		if (UPLOAD_TYPE_ATTACH.equals(upload_type))
			return "attach";
		if (UPLOAD_TYPE_IMAGE.equals(upload_type))
			return "image";
		if (UPLOAD_TYPE_EXPORT.equals(upload_type))
			return "export";
		if (UPLOAD_TYPE_IMPORT.equals(upload_type))
			return "import";
		if (UPLOAD_TYPE_TEMP.equals(upload_type))
			return "temp";
		return null;
	}

	/**
	 * flush output
	 * @param out
	 * @throws Exception
	 */
	private static void flush(OutputStream out) throws Exception {
		try {
			out.flush();
		} catch (SocketException e) {
		}
	}

	/**
	 * get content type
	 * @param file_type
	 * @return String
	 */
	public static String getContentType(String file_type) {
		String content_type = null;

		if (FILE_TYPE_JPG.equals(file_type) || FILE_TYPE_JPEG.equals(file_type))
			content_type = CONTENT_TYPE_IMAGE_JPEG;
		if (FILE_TYPE_GIF.equals(file_type))
			content_type = CONTENT_TYPE_IMAGE_GIF;
		if (FILE_TYPE_PNG.equals(file_type))
			content_type = CONTENT_TYPE_IMAGE_PNG;

		if (FILE_TYPE_DOC.equals(file_type))
			content_type = CONTENT_TYPE_WORD;
		if (FILE_TYPE_XLS.equals(file_type))
			content_type = CONTENT_TYPE_EXCEL;
		if (FILE_TYPE_PPT.equals(file_type))
			content_type = CONTENT_TYPE_POWERPOINT;

		if (FILE_TYPE_PDF.equals(file_type))
			content_type = CONTENT_TYPE_PDF;

		return content_type;
	}

	/**
	 * get main file name
	 * @param file_name
	 * @return String
	 */
	public static String getMainFileName(String file_name) {
		if (file_name.lastIndexOf(".") == -1)
			return file_name;
		return file_name.substring(0, file_name.lastIndexOf("."));
	}

	/**
	 * get expand file name
	 * @param file_name
	 * @return String
	 */
	public static String getExpandFileName(String file_name) {
		if (file_name.lastIndexOf(".") == -1)
			return null;
		return file_name.substring(file_name.lastIndexOf(".") + 1, file_name.length());
	}

	/**
	 * get file type
	 * @param file_name
	 * @return String
	 */
	public static String getFileType(String file_name) {
		if (file_name.lastIndexOf(".") == -1)
			return null;
		String file_type = file_name.substring(file_name.lastIndexOf(".") + 1, file_name.length());
		return file_type.toUpperCase();
	}

	/**
	 * get content type by file name
	 * @param file_name
	 * @return String
	 */
	public static String getContentTypeByFileName(String file_name) {
		return getContentType(getFileType(file_name));
	}

	/**
	 * get file name
	 * @param file_name
	 * @return String
	 */
	public static String getFileName(String file_name) {
		file_name = file_name.replaceAll("\\\\", "/");
		int index = file_name.lastIndexOf("/");
		return index == -1 ? file_name : file_name.substring(index + 1);
	}

	/**
	 * get file path
	 * @param file_name
	 * @return String
	 */
	public static String getFilePath(String file_name) {
		file_name = file_name.replaceAll("\\\\", "/");
		int index = file_name.lastIndexOf("/");
		return index == -1 ? null : file_name.substring(0, index);
	}

	/**
	 * get file list
	 * @param path
	 * @return File[]
	 * @throws Exception
	 */
	public static File[] getFileList(String path) throws Exception {
		File file = new File(path);
		return file.exists() ? file.listFiles() : null;
	}

	/**
	 * delete file
	 * @param file
	 * @throws Exception
	 */
	public static void deleteFiles(File file) throws Exception {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] fileList = file.listFiles();
				for (int i = 0; i < fileList.length; i++) {
					deleteFiles(fileList[i]);
				}
			} else {
				file.delete();
			}
			file.delete();
		}
	}

	/**
	 * get output stream
	 * @param response
	 * @param file_name
	 * @return OutputStream
	 * @throws Exception
	 */
	public static OutputStream getOutputStreamByDown(HttpServletResponse response, String file_name) throws Exception {
		//response.reset();
		response.setContentType("application/octet-stream; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + common.encodeCharset(file_name) + "\"");
		return response.getOutputStream();
	}

	/**
	 * get output stream
	 * @param response
	 * @param contenet_type
	 * @return OutputStream
	 * @throws Exception
	 */
	public static OutputStream getOutputStreamByShow(HttpServletResponse response, String contenet_type) throws Exception {
		response.reset();
		response.setContentType(contenet_type);
		/* no cache */
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		return response.getOutputStream();
	}

	/**
	 * write the input stream to the output stream
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	public static void writeInputToOutput(InputStream in, OutputStream out) throws Exception {
		writeInputToOutput(in, out, false);
	}

	/**
	 * write the input stream to the output stream
	 * @param in
	 * @param out
	 * @param persist
	 * @throws Exception
	 */
	public static void writeInputToOutput(InputStream in, OutputStream out, boolean ispersist) throws Exception {
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int len = -1;
		/* if no arrive the end(len is -1) in the data stream then write */
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
			flush(out);
		}
		if (!ispersist) {
			in.close();
			out.close();
		}
	}

	/**
	 * show file
	 * @param response
	 * @param full_name
	 * @param real_name
	 * @throws Exception
	 */
	public static void showFile(HttpServletResponse response, String full_name, String real_name) throws Exception {
		String file_name = real_name == null ? full_name : real_name;

		File file = new File(full_name);
		if (!file.exists())
			common.error("文件 " + file_name + " 未找到!");

		OutputStream out = getOutputStreamByShow(response, getContentTypeByFileName(file_name));
		writeInputToOutput(new FileInputStream(file), out);
	}

	/**
	 * download file
	 * @param response
	 * @param full_name
	 * @throws Exception
	 */
	public static void downFile(HttpServletResponse response, String full_name) throws Exception {
		downFile(response, full_name, null);
	}

	/**
	 * download file
	 * @param response
	 * @param full_name
	 * @param real_name
	 * @throws Exception
	 */
	public static void downFile(HttpServletResponse response, String full_name, String real_name) throws Exception {
		String file_name = real_name == null ? full_name : real_name;

		File file = new File(full_name);
		if (!file.exists()){
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			common.error("File " + full_name + " Not found.");
		}

		OutputStream out = getOutputStreamByDown(response, file_name);
		writeInputToOutput(new FileInputStream(file), out);
	}

	/**
	 * delete file
	 * @param full_name
	 * @return boolean
	 * @throws Exception
	 */
	public static boolean deleteFile(String full_name) throws Exception {
		File file = new File(full_name);
		if (file.exists())
			return file.delete();
		return false;
	}

	/**
	 * delete file
	 * @param file_path
	 * @param file_name
	 * @return boolean
	 * @throws Exception
	 */
	public static boolean deleteFile(String file_path, String file_name) throws Exception {
		File file = new File(file_path, file_name);
		if (file.exists())
			return file.delete();
		return false;
	}

	/**
	 * upload file
	 * @param item:FileItem or IUploadFile
	 * @param file_path
	 * @param file_name
	 * @throws Exception
	 */
	public static File uploadFile(Object item, String file_path, String file_name) throws Exception {
		if (!new File(file_path).isDirectory()) {//目录不存在则创建
			new File(file_path).mkdirs();
		}

		File file = new File(file_path, file_name);
		if (item instanceof MultipartFile) {
			int byteread = 0;
			byte[] buffer = new byte[1444];
			FileOutputStream fs = new FileOutputStream(file);
			InputStream inStream = ((MultipartFile) item).getInputStream();
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			fs.close();
		} else if (item instanceof File) {
			int byteread = 0;
			byte[] buffer = new byte[1444];
			FileOutputStream fs = new FileOutputStream(file);
			InputStream inStream = new FileInputStream((File) item);
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			fs.close();
		}
		return file;
	}
	
	/**
	 * 文件是否存在
	 * 
	 * @param file_path
	 * @param file_name
	 * @return
	 */
	public static boolean existFile(String file_path, String file_name){
		return new File(file_path, file_name).exists();
	}

	/**
	 * 上传文件到远端
	 * 
	 * @param url
	 * @param multipartFiles
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static boolean uploadFileToRemote(String url, List<File> files, IData<String, String> params) throws Exception {
		if (files == null || files.isEmpty()) {
			return false;
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url);
		try {
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

			// 文件
			for (int i = 0; i < files.size(); i++) {
				File file = files.get(i);
				builder.addBinaryBody(file.getName(), file, ContentType.DEFAULT_BINARY, file.getName());
			}

			// 其他参数
			if (params != null && !params.isEmpty()) {
				for (Map.Entry<String, String> entry : params.entrySet()) {
					builder.addTextBody(entry.getKey(), entry.getValue(), ContentType.TEXT_PLAIN);
				}
			}
			httppost.setEntity(builder.build());

			// 开始上传
			String success = httpclient.execute(httppost, new ResponseHandler<String>() {
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					boolean success = false;
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status <= 300) {
						success = true;
					} else {
						HttpEntity entity = response.getEntity();
						String responseBody = entity != null ? EntityUtils.toString(entity, "UTF-8") : null;
						log.error("Unexpected response status: " + status + "\nresponseBody:" + responseBody);
					}
					return success + "";
				}
			});

			return "true".equals(success);
		} finally {
			httpclient.close();
		}
	}

	/**
	 * 
	 * 上传文件到临时目录
	 * @param multipartFile
	 * @return
	 * @throws Exception
	 */
	public static File uploadFileToTemp(Object file, String fileName) throws Exception {
		String upPath = SpringPropertyHolder.getContextProperty("uploadpath", "upload/files") + "/temp/" + common.getUniqeName();
		return uploadFile(file, upPath, fileName);
	}

	/** 
	 * write object
	 * @param file
	 * @param obj
	 * @throws Exception
	 */
	public static void writeObject(File file, Object obj) throws Exception {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(obj);
		out.close();
	}

	/** 
	 * write object
	 * @param file_name
	 * @param obj
	 * @throws Exception
	 */
	public static void writeObject(String file_name, Object obj) throws Exception {
		writeObject(new File(file_name), obj);
	}

	/** 
	 * write object
	 * @param file
	 * @return Object
	 * @throws Exception
	 */
	public static Object readObject(File file) throws Exception {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		Object obj = in.readObject();
		in.close();
		return obj;
	}

	/** 
	 * write object
	 * @param file_name
	 * @return Object
	 * @throws Exception
	 */
	public static Object readObject(String file_name) throws Exception {
		return readObject(new File(file_name));
	}

	/**
	 * 获取上传文件列表
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static List<MultipartFile> getUpFiles(MultipartHttpServletRequest request) {
		List<MultipartFile> files = new ArrayList<MultipartFile>();
		Iterator<String> itr = request.getFileNames();
		while (itr.hasNext()) {
			files.add(request.getFile(itr.next()));
		}
		return files;
	}

	/**
	 * 将文件读入成byte[]字节数组
	 * @param filename 文件名
	 * @return byte[]字节数组
	 * @throws Exception
	 */
	public static long getFileSize(File file) throws Exception {
		FileInputStream stream = new FileInputStream(file);//将文件读入为byte[]
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int n;
		while ((n = stream.read(b)) != -1)
			out.write(b, 0, n);
		stream.close();
		out.close();
		return out.toByteArray().length;
	}

	/**
	 * 获取导入数据集
	 * 
	 * @param multipartFile
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static IDataset getImportDataset(MultipartFile multipartFile, String xml, Class<?> clazz) {
		try {
			if (multipartFile != null && multipartFile.getSize() > 0) {
				IDataset dataset = ExcelParser.importExcel(xml, multipartFile.getInputStream(), clazz)[0];
				if (ExcelParser.verify(dataset)) {//校验通过
					return dataset;
				} else {
					//导出校验失败文件
					ExcelParser.error(dataset, xml);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}