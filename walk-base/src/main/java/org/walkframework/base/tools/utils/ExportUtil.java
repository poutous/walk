package org.walkframework.base.tools.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.util.StringUtils;
import org.walkframework.base.mvc.entity.TlMExportlog;
import org.walkframework.base.mvc.service.common.LogService;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.constant.CommonConstants;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.data.util.DatasetList;

import com.alibaba.fastjson.JSON;

/**
 * 导出工具
 * 使用方法见exportExample方法
 * 
 */
public abstract class ExportUtil {

	private static Common common = SingletonFactory.getInstance(Common.class);

	public static final String UPLOAD_TYPE_EXPORT = "3";

	public static final String UPLOAD_TYPE_TEMP = "5";

	public static final String DEFAULT_FILE_PATH = "upload";

	/**
	 * 导出示例方法
	 */
	/*private void exportExample() {
	 	//定义表头，支持复杂表头(rowspan、colspan)
		StringBuilder headerJSON = new StringBuilder();
		headerJSON.append("[");
		headerJSON.append("	[");
		headerJSON.append("		{\"field\":\"departId\",\"title\":\"部门Id\",\"rowspan\":2},");
		headerJSON.append("		{\"field\":\"departName\",\"title\":\"部门名称\",\"rowspan\":2},");
		headerJSON.append("		{\"title\":\"其他1\",\"colspan\":2},");
		headerJSON.append("		{\"title\":\"其他2\",\"colspan\":2}");
		headerJSON.append("	],");
		headerJSON.append("	[");
		headerJSON.append("		{\"field\":\"departCode\",\"title\":\"部门编码\"},");
		headerJSON.append("		{\"field\":\"parentDepartId\",\"title\":\"上级部门编码\"},");
		headerJSON.append("		{\"field\":\"departCode\",\"title\":\"部门编码\"},");
		headerJSON.append("		{\"field\":\"parentDepartId\",\"title\":\"上级部门编码\"}");
		headerJSON.append("	]");
		headerJSON.append("]");
		//查出结果集
		List<TdMDepart> dataset = rightService.queryDepartList(param, getPagination(true));
		//开始导出
		ExportUtil.exportExcel(response, dataset, headerJSON, "我的导出");

	}*/

	/**
	 * 导出到excel
	 * 
	 * @param request
	 * @param response
	 * @param dataset
	 * @param xml：xml文件方式
	 * @throws Exception
	 */
	public static String exportExcel(HttpServletRequest request, HttpServletResponse response, List dataset, String xml) throws Exception {
		String filePath = writeZipFileByXml(dataset, xml);
		String fileName = StringUtils.isEmpty(filePath) ? common.getUniqeName() : filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
		
		// 远程下载zip文件
		String fileTimeName = fileName + "_" + common.decodeTimeStr("yyyyMMddHHmmss", common.getSysTime()) + ".zip";
		downFile(response, filePath, fileTimeName);

		File file = new File(filePath);
		File parentFile = file.getParentFile();
		long fileSize = file.length();
		
		// 删除zip
		file.delete();
		
		//删除上级目录
		if(!getUploadPath(UPLOAD_TYPE_EXPORT).equals(parentFile.getPath())){
			parentFile.delete();
		}

		//记录导出日志
		writeTlMExportlog(request, fileSize, filePath);
		return filePath;
	}

	/**
	 * 导出到excel
	 * @param result
	 * @param headerJSON
	 * @throws Exception 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static String exportExcel(HttpServletRequest request, HttpServletResponse response, List dataset, String headerJSON, String fileName) throws Exception {
		String filePath = writeZipFile(dataset, headerJSON, fileName);

		// 远程下载zip文件
		String fileTimeName = fileName + "_" + common.decodeTimeStr("yyyyMMddHHmmss", common.getSysTime()) + ".zip";
		downFile(response, filePath, fileTimeName);

		File file = new File(filePath);
		File parentFile = file.getParentFile();
		long fileSize = file.length();
		
		// 删除zip
		file.delete();
		
		//删除上级目录
		if(!getUploadPath(UPLOAD_TYPE_EXPORT).equals(parentFile.getPath())){
			parentFile.delete();
		}

		//记录导出日志
		writeTlMExportlog(request, fileSize, filePath);
		return filePath;
	}

	/**
	 * 打包并返回文件路径
	 * 
	 * @param dataset
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static String writeZipFileByXml(List dataset, String xml) throws Exception {
		//读取xml文件
		StringBuilder result = new StringBuilder();
		BufferedReader br = null;
		try {
			String exportdirectory = SpringPropertyHolder.getContextProperty("excel.exportdirectory", "exports/");
			br = new BufferedReader(new InputStreamReader(common.getClassResource(exportdirectory + xml).openStream(), "UTF-8"));
			String s = null;
			while ((s = br.readLine()) != null) {
				result.append(s);
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}

		//转换xml文件到json
		JSONObject json = XML.toJSONObject(result.toString());
		JSONObject table = json.getJSONObject("table");
		Object tr = table.get("tr");
		JSONArray josonArray = new JSONArray();
		if (tr instanceof JSONObject) {
			Object th = ((JSONObject) tr).get("th");
			if (th instanceof JSONObject) {
				JSONArray array = new JSONArray();
				array.put(th);
				josonArray.put(array);
			} else if (th instanceof JSONArray) {
				josonArray.put(th);
			}
		} else if (tr instanceof JSONArray) {
			for (Object object : (JSONArray) tr) {
				josonArray.put(((JSONObject) object).getJSONArray("th"));
			}
		}

		//导出
		String headerJSON = josonArray.toString();
		String fileName = table.getString("name");
		return writeZipFile(dataset, headerJSON, fileName);
	}

	/**
	 * 打包并返回文件路径
	 * 
	 * @param dataset
	 * @param headerJSON
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static String writeZipFile(List dataset, String headerJSON, String fileName) throws Exception {
		if (dataset == null)
			return null;
		
		String filePath = getUploadPath(UPLOAD_TYPE_EXPORT) + "/files/" + UUID.randomUUID().toString().replaceAll("-", "");
		File tmpDir = new File(filePath);
		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
		}
		
		fileName = StringUtils.isEmpty(fileName) ? common.getUniqeName() : fileName;
		String zipName = filePath + "/" + fileName + ".zip";
		ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(new File(zipName)));
		
		boolean isSerializable = false;
		if (dataset instanceof DatasetList) {
			DatasetList datasetList = (DatasetList) dataset;
			if (datasetList.isSerializable() && datasetList.isBatchSerializable()) {
				isSerializable = true;

				String originalStorePath = datasetList.getSerializablePath() + "/" + datasetList.getSerializableId();
				File storeDir = new File(originalStorePath);
				if (storeDir.exists() && storeDir.isDirectory()) {
					for (int j = 0; j < datasetList.getBatchPageCount(); j++) {
						File storeFile = new File(originalStorePath + "/" + j);
						if (storeFile.exists()) {
							List storeset = (List) readObject(storeFile);

							//开始写文件
							writeFile(filePath, fileName, headerJSON, storeset, zipout, j);

							//删除临时文件
							storeFile.delete();
						}
					}
					// 删除临时存储目录
					storeDir.delete();
				}
			}
		}
		
		if(!isSerializable){
			//开始写文件
			writeFile(filePath, fileName, headerJSON, dataset, zipout, 0);
		}

		// 关闭zip流
		zipout.flush();
		zipout.close();
		return zipName;
	}

	/**
	 * 记录导出日志
	 * 
	 * @param request
	 */
	public static void writeTlMExportlog(HttpServletRequest request, long fileSize, String filePath) {
		String exportName = request.getParameter(CommonConstants.EXPORT_NAME);
		String exportTotal = request.getParameter("exportTotal") == null ? (String) (request.getAttribute(CommonConstants.EXPORT_TOTAL) == null ? "0" : request.getAttribute(CommonConstants.EXPORT_TOTAL)) : request.getParameter("exportTotal");
		
		TlMExportlog tlMExportlog = new TlMExportlog();
		tlMExportlog.setExportName(StringUtils.isEmpty(exportName) ? "exportfile" : exportName);
		tlMExportlog.setExportMode(CommonConstants.EXPORT_MODE_SYNC);
		tlMExportlog.setExportState(CommonConstants.EXPORT_STATE_SUCCESS);
		tlMExportlog.setReqUri(request.getRequestURI());
		tlMExportlog.setParams(JSON.toJSONString(common.getInParam(request)));
		tlMExportlog.setOperateIp(common.getIpAddr(request));
		tlMExportlog.setTotal(new BigDecimal(exportTotal));
		tlMExportlog.setFileSize(new BigDecimal(fileSize));
		tlMExportlog.setExportPath(filePath);
		LogService logService = SpringContextHolder.getBean(LogService.class);
		logService.insertExportLog(tlMExportlog);
	}

	/**
	 * 写文件
	 * @param filePath
	 * @param fileName
	 * @param headerJSON
	 * @param dataset
	 * @param zipout
	 * @param index
	 * @throws Exception
	 */
	private static void writeFile(String filePath, String fileName, String headerJSON, List dataset, ZipOutputStream zipout, int index) throws Exception {
		// 1）生成xls文件
		String excelName = "export_" + (index + 1);//中文有乱码，索性就直接用英文名称
		String excelFullPath = filePath + "/" + excelName + ".xls";
		HSSFWorkbook book = new HSSFWorkbook();
		HSSFSheet sheet = book.createSheet(fileName + "_" + (index + 1));
		List ret = writeHeader(book, sheet, headerJSON);
		writeRows(dataset, book, sheet, ret);
		FileOutputStream fout = new FileOutputStream(excelFullPath);
		book.write(fout);
		fout.flush();
		fout.close();

		// 2）将excel打包
		zipout.putNextEntry(new ZipEntry(excelName + ".xls"));
		FileInputStream in = new FileInputStream(excelFullPath);
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int len = -1;
		while ((len = in.read(buffer)) != -1) {
			zipout.write(buffer, 0, len);
		}
		in.close();

		// 3）删除excel文件
		deleteFile(excelFullPath);
	}

	/**
	 * 数据导出到excel 返回excel路径
	 * 
	 * @param result
	 * @param headerJSON
	 * @param filePath
	 * @param index
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static String getExcelFilePath(List result, String headerJSON, String filePath, int index) throws Exception {
		// 生成导出文件的临时路径
		// 如果文件夹不存在，创建文件夹路径
		File tmpDir = new File(filePath);
		if (!tmpDir.exists())
			tmpDir.mkdirs();

		String excelName = "export_" + index;
		String fullName = filePath + "/" + excelName;
		// 生成xls文件
		HSSFWorkbook book = new HSSFWorkbook();
		HSSFSheet sheet = book.createSheet(excelName);
		List ret = writeHeader(book, sheet, headerJSON);
		writeRows(result, book, sheet, ret);
		FileOutputStream fout = new FileOutputStream(fullName);
		book.write(fout);
		fout.flush();
		fout.close();
		return fullName;
	}

	/**
	 * 下载zip文件
	 * 
	 * @param filePath
	 * @param excelFilePaths
	 * @param response
	 * @throws Exception
	 */
	public static void downFile(String filePath, List<String> excelFilePaths, HttpServletResponse response) throws Exception {
		String excelName = "export_" + common.decodeTimeStr("yyyyMMddHHmmss", common.getSysTime());
		String zipName = filePath + "/" + excelName + ".zip";
		ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(zipName));
		for (int i = 0; i < excelFilePaths.size(); i++) {
			String tempExcel = excelFilePaths.get(i);
			// 写入zip文件
			zipout.putNextEntry(new ZipEntry(excelName + "_" + (i + 1) + ".xls"));
			FileInputStream in = new FileInputStream(tempExcel);
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int len = -1;
			while ((len = in.read(buffer)) != -1) {
				zipout.write(buffer, 0, len);
			}
			in.close();

			// 删除临时文件
			deleteFile(tempExcel);
		}

		// 关闭zip流
		try {
			zipout.flush();
			zipout.close();
		} catch (Exception e) {
			throw e;
		}
		// 远程下载zip文件
		downFile(response, zipName, excelName + ".zip");

		// 删除zip
		deleteFile(zipName);

		// 删除临时目录
		deleteFile(filePath);
	}

	/**
	 * 毫秒数转换为相对天数
	 * 
	 * @param mss
	 * @return
	 */
	public static long getDays(long mss) {
		long days = mss / (1000 * 60 * 60 * 24);
		return days;
	}

	/**
	 * 往excel文档中写入常规数据
	 * 
	 * @param result
	 * @param book
	 * @param sheet
	 * @param ret
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	@SuppressWarnings( { "unchecked", "deprecation", "static-access" })
	public static void writeRows(List result, HSSFWorkbook book, HSSFSheet sheet, List ret) throws Exception {
		HSSFCellStyle style = book.createCellStyle();
		HSSFFont font = book.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 9);
		style.setFont(font);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setWrapText(false);// 设置不自动换行
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

		short rowStart = Short.parseShort(getString(ret.get(0)));
		List fields = (List) ret.get(1);
		List colX = (List) ret.get(2);
		List frzCol = (List) ret.get(3);

		for (int i = 0; i < result.size(); i++) {
			Object rowData = result.get(i);
			HSSFRow row = sheet.createRow(rowStart + i);
			for (short j = 0; j < fields.size(); j++) {
				HSSFCell cell = row.createCell(Short.parseShort(getString(colX.get(j))));
				String fieldName = fields.get(j) == null ? "" : fields.get(j).toString();
				String text = "";
				if (rowData instanceof Map) {
					text = getStringValue(((Map) rowData).get(fieldName));
				} else {
					text = getStringValue(common.getValueByFieldName(rowData, fieldName));
				}
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);

				// 如果为活动列正常数据，且值为空时，显示默认字符
				if (!frzCol.contains(fieldName) && (null == text || "".equals(text) || text.length() == 0 || "null".equals(text)))
					text = "";

				HSSFRichTextString rText = new HSSFRichTextString(text);
				cell.setCellStyle(style);
				cell.setCellValue(rText);

				// 自适应宽度
				// sheet.autoSizeColumn(j);//太耗时 注掉
				sheet.setColumnWidth(j, (short) 4000);
			}
		}

		// 对冻结列自适应宽度
		for (short j = 0; j < frzCol.size(); j++) {
			sheet.autoSizeColumn(j);
		}

		mergeTDs(sheet, frzCol, rowStart);// 合并冻结列单元格
	}

	/**
	 * 获取字符串值
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	private static String getStringValue(Object obj) throws Exception {
		if (obj == null)
			return "";
		if (obj instanceof Date) {
			return common.decodeTimestamp("yyyy-MM-dd HH:mm:ss", (Date) obj);
		}
		return String.valueOf(obj);
	}

	/**
	 * 合并冻结列的单元格
	 * 
	 * @param sheet
	 * @param frzCol
	 * @param rowStart
	 */
	@SuppressWarnings( { "unchecked", "deprecation" })
	public static void mergeTDs(HSSFSheet sheet, List frzCol, short rowStart) {
		List merges = new ArrayList();// 定义合并对象数组
		// 最后一列无需合并，如果需要合并，也是因为统计结果冗余。同理，仅有一个冻结列时，没有合并的必要
		List frzTmp = new ArrayList();// 联合主键的键数组
		for (short j = 0; j < frzCol.size() - 1; j++) {
			int count = 0;// 连续需要合并的单元格数记录值
			int idx = rowStart;// 新一轮合并的起始下标
			String oldValue = "";// 比对变量定义，作用域在内部循环之外
			frzTmp.add(frzCol.get(j));
			for (int i = rowStart; i < sheet.getLastRowNum(); i++) {
				String currValue = "";// 以字符串描述联合主键
				for (int k = 0; k < frzTmp.size(); k++) {
					int xIdx = frzCol.indexOf(frzTmp.get(k));
					String value = sheet.getRow(i).getCell(xIdx).getRichStringCellValue().getString();
					currValue = currValue + ":" + value;
				}

				if (i == rowStart) {
					oldValue = currValue;// 从第二列开始，需要同时比对上一级（左侧）单元格数值
					count++;
					continue;// 第一个单元格无需比对，仅需记录，循环跳出继续
				}

				if (oldValue.equals(currValue)) {
					count++;
				}

				// 当前数值与上一个被比对记录无差异的数值 不相同，或者已经到了数据记录集合的最后一条时
				if (!oldValue.equals(currValue) || i == sheet.getLastRowNum() - 1) {
					// 在迭代获得单元格需要合并的数量大于1个时，产生一个merge对象，其中idx为记录起始变化时的单元格下标
					if (count > 1) {
						Region region = new Region(idx, j, idx + count - 1, j);
						merges.add(region);
					}
					// 完成添加后，将参考比对的相关变量重置
					oldValue = currValue;
					count = 1;
					idx = i;
				}
			}

		}

		for (int i = 0; i < merges.size(); i++) {
			sheet.addMergedRegion((Region) merges.get(i));
		}
	}

	/**
	 * 往excel文档写入表头
	 * 
	 * @param book
	 * @param sheet
	 * @param headerJSON
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings( { "unchecked", "deprecation" })
	public static List writeHeader(HSSFWorkbook book, HSSFSheet sheet, String headerJSON) throws Exception {
		HSSFCellStyle style = book.createCellStyle();
		HSSFFont font = book.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 9);
		style.setFont(font);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setWrapText(false);// 设置不自动换行
		style.setFillForegroundColor(HSSFColor.YELLOW.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

		com.alibaba.fastjson.JSONArray arr = com.alibaba.fastjson.JSONArray.parseArray(headerJSON);
		short x = 0;
		short y = 0;
		List rowspanCells = new ArrayList();// 记录被跨行的单元格
		List fields = new ArrayList();// 叶子节点数组
		List colX = new ArrayList();// 叶子节点对应的X轴坐标
		List frzCol = new ArrayList();
		for (int i = 0; i < arr.size(); i++) {
			com.alibaba.fastjson.JSONArray bbb = arr.getJSONArray(i);
			HSSFRow row = sheet.createRow(y);
			for (int j = 0; j < bbb.size(); j++) {
				com.alibaba.fastjson.JSONObject obj = bbb.getJSONObject(j);
				short cspan = 1;
				short rspan = 1;
				if (obj.get("freeze") != null && obj.getBoolean("freeze")) {//暂时支持的不好，待完善
					frzCol.add(obj.getString("field"));
				}
				if (obj.get("colspan") != null)
					cspan = obj.getShortValue("colspan");
				if (obj.get("rowspan") != null)
					rspan = obj.getShortValue("rowspan");
				if (obj.get("title") != null) {
					String title = obj.getString("title");
					// sheet.setColumnWidth(j, title.toString().length() *
					// 512);//宽度自适应
					// 跳过所有垂直跨度
					while (rowspanCells.contains(x + "," + y)) {
						x++;
					}
					HSSFCell cell = row.createCell(x);
					cell.setCellStyle(style);
					cell.setCellValue(new HSSFRichTextString(title));

					if (obj.get("field") != null) {
						fields.add(obj.getString("field"));
						colX.add(x + "");
					}

					if (cspan > 1) {
						Region r0 = new Region(y, x, y, (short) (x + cspan - 1));
						sheet.addMergedRegion(r0);
						fillEmptyBorderLine(sheet, r0, style);
						x += cspan - 1;
					}
					if (rspan > 1) {
						short yEnd = (short) (y + rspan - 1);
						Region r1 = new Region(y, x, yEnd, x);
						sheet.addMergedRegion(r1); // 记录被占用的单元格
						fillEmptyBorderLine(sheet, r1, style);
						while (yEnd > y) {
							rowspanCells.add(x + "," + yEnd);
							yEnd--;
						}
					}
					x++;
				}
			}
			y++;
			x = 0;
		}

		// 冻结表头和冻结列
		sheet.createFreezePane(frzCol.size(), y);
		// 回传参数构造
		List ret = new ArrayList();
		ret.add(y + "");// 列头跨行
		ret.add(fields);// 字段名
		ret.add(colX);// 字段名对应的列数信息
		ret.add(frzCol);// 冻结列数组
		return ret;
	}

	/**
	 * 解决合并单元格后边框线没有的问题
	 * 
	 * @param sheet
	 * @param region
	 * @param style
	 */
	@SuppressWarnings("deprecation")
	public static void fillEmptyBorderLine(HSSFSheet sheet, Region region, HSSFCellStyle style) {
		int xBegin = region.getColumnFrom();
		int xEnd = region.getColumnTo() + 1;
		int yBegin = region.getRowFrom();
		int yEnd = region.getRowTo() + 1;
		for (int xx = xBegin; xx < xEnd; xx++) {
			for (int yy = yBegin; yy < yEnd; yy++) {
				HSSFRow _row = null == sheet.getRow(yy) ? sheet.createRow(yy) : sheet.getRow(yy);
				HSSFCell _cell = null == _row.getCell(xx) ? _row.createCell((short) (xx)) : _row.getCell(xx);
				_cell.setCellStyle(style);
			}
		}
	}

	public static String getString(Object value) {
		return value == null ? "" : value.toString();
	}

	/**
	 * get upload type
	 * @param upload_type
	 * @return String
	 */
	public static String getUploadPath(String upload_type) {
		if (UPLOAD_TYPE_EXPORT.equals(upload_type))
			return "export";
		if (UPLOAD_TYPE_TEMP.equals(upload_type))
			return "temp";
		return null;
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
	 * download file
	 * @param response
	 * @param full_name
	 * @param real_name
	 * @throws Exception
	 */
	public static void downFile(HttpServletResponse response, String full_name, String real_name) throws Exception {
		String file_name = real_name == null ? full_name : real_name;

		File file = new File(full_name);
		if (!file.exists())
			common.error("文件 " + full_name + " 未找到!");

		response.setContentType("application/octet-stream; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + common.encodeCharset(file_name) + "\"");
		OutputStream out = response.getOutputStream();
		writeInputToOutput(new FileInputStream(file), out, false);
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
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
			try {
				out.flush();
			} catch (SocketException e) {
			}
		}
		if (!ispersist) {
			in.close();
			out.close();
		}
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
}
