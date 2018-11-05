package org.walkframework.base.tools.excel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.base.tools.utils.FileUtil;
import org.walkframework.base.tools.utils.ParamTranslateUtil;
import org.walkframework.base.tools.utils.ValidateUtil;
import org.walkframework.data.entity.BaseEntity;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.DatasetList;
import org.walkframework.data.util.IData;
import org.walkframework.data.util.IDataset;

@SuppressWarnings({"unchecked", "unused", "deprecation", "static-access"})
public class ExcelParser {

	public static final int MAX_PAGE_SIZE = 65536;
	public static final String EXCEL_UNIQE_DATA = "_DATA";
	public static final String EXCEL_UNIQE_XML = "_XML";
	
	private static final String IMPORT_RESULT = "_IMPORT_RESULT";
	
	private static final String IMPORT_ERROR = "_IMPORT_ERROR";

	protected static final Common common = SingletonFactory.getInstance(Common.class);

	/**
	 * get sheets
	 * 
	 * @param xml
	 * @return List
	 * @throws Exception
	 */
	
	public static List getSheets(String xml) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(xml);
		Element book = document.getRootElement();
		return book.getChildren();
	}

	/**
	 * get sheets
	 * 
	 * @param path
	 * @param xml
	 * @return List
	 * @throws Exception
	 */
	public static List getSheets(String path, String xml) throws Exception {
		return getSheets(common.getClassResource(path + xml).toString());
	}

	/**
	 * get sheets
	 * 
	 * @param idatasets
	 * @return List
	 * @throws Exception
	 */
	public static List getSheets(IDataset[] isheets) throws Exception {
		Document document = getDocument(isheets);
		return document.getRootElement().getChildren();
	}

	/**
	 * get document
	 * 
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public static Document getDocument(IDataset config) throws Exception {
		return getDocument(new IDataset[] { config });
	}

	/**
	 * get document
	 * 
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public static Document getDocument(IDataset[] isheets) throws Exception {
		return getDocument(isheets, null);
	}

	/**
	 * get document
	 * 
	 * @param isheets
	 * @return Document
	 * @throws Exception
	 */
	public static Document getDocument(IDataset[] isheets, String[] sheetname) throws Exception {
		Element root = new Element("workbook");
		Document document = new Document(root);
		for (int i = 0; i < isheets.length; i++) {
			IDataset isheet = isheets[i];

			Element sheet = new Element("sheet");
			sheet.setAttribute("name", "Sheet" + (i + 1));
			// sheetname为空则用默认
			if (sheetname == null) {
				sheet.setAttribute("desc", "Sheet" + (i + 1));
			} else {
				sheet.setAttribute("desc", (sheetname[i] == null || "".equals(sheetname[i])) ? "Sheet" + (i + 1) : sheetname[i]);
			}
			// sheet.setAttribute("desc", sheetname[i]);
			root.addContent(sheet);

			Element header = new Element("header");
			header.setAttribute("isshow", "true");
			header.setAttribute("height", "300");
			sheet.addContent(header);

			for (int j = 0; j < isheet.size(); j++) {
				IData icell = (IData) isheet.get(j);

				Element cell = new Element("cell");
				String[] icellnames = icell.getNames();
				for (int k = 0; k < icellnames.length; k++) {
					cell.setAttribute(icellnames[k], (String) icell.get(icellnames[k]));
				}
				header.addContent(cell);
			}
		}
		return document;
	}
	
	/**
	 * import excel
	 * 
	 * 默认为Map结构
	 * 
	 * @param bd
	 * @param xml
	 * @param input
	 * @return IDataset[]
	 * @throws Exception
	 */
	public static IDataset[] importExcel(String xml, InputStream input) throws Exception {
		return importExcel(xml, input, DataMap.class);
	}

	/**
	 * import excel
	 * 
	 * 指定内部对象类型
	 * 
	 * @param bd
	 * @param xml
	 * @param input
	 * @return IDataset[]
	 * @throws Exception
	 */
	public static IDataset[] importExcel(String xml, InputStream input, Class<?> clazz) throws Exception {
		List sheets = getSheets(SpringPropertyHolder.getContextProperty("excel.importdirectory","imports/"), xml);
		return importExcel(sheets, input, clazz);
	}

	/**
	 * import excel
	 * 
	 * @param bd
	 * @param isheets
	 * @param input
	 * @return IDataset[]
	 * @throws Exception
	 */
	public static IDataset[] importExcel(IDataset[] isheets, InputStream input, Class<?> clazz) throws Exception {
		List sheets = getSheets(isheets);
		return importExcel(sheets, input, clazz);
	}

	/**
	 * import excel:改造ExcelParser.importExcel
	 * (导入校验，nullable="yes"并且设置了datasrc属性，实际value如果为空则不进行datasrc的校验)
	 * 
	 * @param bd
	 * @param xml
	 * @param input
	 * @return IDataset[]
	 * @throws Exception
	 */
	public static IDataset[] importExcel(List sheets, InputStream input, Class<?> clazz) throws Exception {

		POIFSFileSystem fs = new POIFSFileSystem(input);
		HSSFWorkbook workbook = new HSSFWorkbook(fs);

		IDataset[] datasets = new IDataset[sheets.size()];
		for (int i = 0; i < datasets.length; i++) {
			HSSFSheet worksheet = workbook.getSheetAt(i);
			datasets[i] = new DatasetList();

			Element header = ((Element) sheets.get(i)).getChild("header");
			boolean isshow = Boolean.valueOf(header.getAttributeValue("isshow")).booleanValue();

			List cells = header.getChildren();
			for (int j = 0; j < worksheet.getPhysicalNumberOfRows(); j++) {
				if (j == 0 && isshow)
					continue;

				HSSFRow workrow = worksheet.getRow(j);
				if (workrow == null)
					continue;

				EntityOrMap data = new EntityOrMap(clazz);
				StringBuilder error = new StringBuilder();

				for (short k = 0; k < cells.size(); k++) {

					Element cell = (Element) cells.get(k);
					String cell_name = cell.getAttributeValue("name");
					String cell_format = cell.getAttributeValue("format");
					String ce_type = cell.getAttributeValue("type");
					HSSFCell workcell = workrow.getCell(k);

					if (workcell != null) {
						int cell_type = workcell.getCellType();
						switch (cell_type) {
						case HSSFCell.CELL_TYPE_STRING:
							String cell_value = workcell.getStringCellValue().trim();
							if (!"".equals(cell_value)){
								//如果是日期类型自动转换为日期
								if (ValidateUtil.CELL_TYPE_DATETIME.equals(ce_type)) {
									if(cell_format == null || "".equals(cell_format)){
										cell_format = common.getTimestampFormat(cell_value);
									}
									if(cell_format != null && "".equals(ValidateUtil.checkDate(cell_value, cell_format, ""))){
										data.set(cell_name, common.encodeTimestamp(cell_format, cell_value));
									}
								} else {
									data.set(cell_name, cell_value);
								}
							}
							break;
						case HSSFCell.CELL_TYPE_NUMERIC:
							if (HSSFDateUtil.isCellDateFormatted(workcell)) {
								//如果检测为日期格式
								if (StringUtils.isEmpty(cell_format)) {
									//如未定义默认为年-月-日格式
									cell_format = "yyyy-MM-dd"; 
								}
								Date date = workcell.getDateCellValue();
								data.set(cell_name, DateFormatUtils.format(date, cell_format));
							}else {
								//其余以数字格式处理
								String format = "#.##";
								if (cell_format != null && !"".equals(cell_format)) {
									format = cell_format;
									if (format.indexOf("0") > -1) {
										format = cell_format.replaceAll("0", "#");
									}
								}
								data.set(cell_name, String.valueOf(common.formatDecimal(format, workcell.getNumericCellValue())));
							}
							break;
						case HSSFCell.CELL_TYPE_BOOLEAN:
							data.set(cell_name, String.valueOf(workcell.getBooleanCellValue()));
							break;
						case HSSFCell.CELL_TYPE_BLANK:
							break;
						case HSSFCell.CELL_TYPE_FORMULA:
							data.set(cell_name, String.valueOf(workcell.getCellFormula()));
							break;
						case HSSFCell.CELL_TYPE_ERROR:
							data.set(cell_name, String.valueOf(workcell.getErrorCellValue()));
							break;
						}
					}
					Object cv = data.get(cell_name, "");
					if(cv != null && cv instanceof Date){
						cv = workcell.getStringCellValue().trim();
					}
					error.append(ValidateUtil.verifyCell(cell, String.valueOf(cv)));
					
				}

				//自定义方法校验
				String method = header.getAttributeValue("method");
				if(method != null){
					error.append(ValidateUtil.checkMethod(method, data.getNativeObject()));
				}
				if (data.size() != 0) {
					data.set(IMPORT_RESULT, error.length() == 0);
					data.set(IMPORT_ERROR, error.toString());
					datasets[i].add(data.getNativeObject());
				}
			}
		}
		return datasets;
	}

	/**
	 * import excel
	 * 
	 * @param bd
	 * @param xml
	 * @param excel
	 * @return IDataset[]
	 * @throws Exception
	 */
	public static IDataset[] importExcel(String xml, String excel) throws Exception {
		return importExcel(xml, new FileInputStream(excel));
	}

	/**
	 * import excel
	 * 
	 * @param bd
	 * @param isheets
	 * @param excel
	 * @return IDataset[]
	 * @throws Exception
	 */
	public static IDataset[] importExcel(IDataset[] isheets, String excel, Class<?> clazz) throws Exception {
		return importExcel(isheets, new FileInputStream(excel), clazz);
	}

	public static long getExportMaxSize() throws Exception {
		return Long.valueOf(SpringPropertyHolder.getContextProperty("excel.exportmaxsize","10000"));

	}

	/**
	 * export excel
	 * 
	 * @param bd
	 * @param isheets
	 * @param excel
	 * @param datasets
	 * @throws Exception
	 */
	public static void exportExcel(HttpServletResponse response, IDataset[] isheets, String excel, IDataset[] datasets) throws Exception {
		List sheets = getSheets(isheets);
		exportExcel(response, sheets, excel, datasets);
	}

	/**
	 * export excel
	 * 
	 * @param response
	 * @param xml
	 * @param excel
	 * @param datasets
	 * @throws Exception
	 */
	public static void exportExcel(HttpServletResponse response, String xml, String excel, List list) throws Exception {
		IDataset dataset = new DatasetList();
		dataset.addAll(list);
		exportExcel(response, xml, excel, new IDataset[] { dataset });
	}

	/**
	 * export excel
	 * 
	 * @param response
	 * @param xml
	 * @param excel
	 * @param datasets
	 * @throws Exception
	 */
	public static void exportExcel(HttpServletResponse response, String xml, String excel, IDataset dataset) throws Exception {
		exportExcel(response, xml, excel, new IDataset[] { dataset });
	}

	/**
	 * export excel
	 * 
	 * @param response
	 * @param xml
	 * @param excel
	 * @param datasets
	 * @throws Exception
	 */
	public static void exportExcel(HttpServletResponse response, String xml, String excel, IDataset[] datasets) throws Exception {
		List sheets = getSheets(SpringPropertyHolder.getContextProperty("excel.exportdirectory","exports/"), xml);
		exportExcel(response, sheets, excel, datasets);
	}

	/**
	 * export excel
	 * 
	 * @param bd
	 * @param response
	 * @param sheets
	 * @param real_name
	 * @param datasets
	 * @throws Exception
	 */
	private static void exportExcel(HttpServletResponse response, List sheets, String real_name, IDataset[] datasets) throws Exception {
		String file_path = SpringPropertyHolder.getContextProperty("excel.uploadpath","upload/walk") + "/" + FileUtil.getUploadPath(FileUtil.UPLOAD_TYPE_EXPORT);
		if (!new File(file_path).isDirectory()) {// 目录不存在则创建
			new File(file_path).mkdirs();
		}
		String full_name = file_path + "/" + common.getUniqeName();
		String main_name = real_name.indexOf(".") == -1 ? real_name : real_name.substring(0, real_name.indexOf("."));

		File file = writeExcel(sheets, full_name, real_name, datasets, null);
		FileUtil.downFile(response, full_name, main_name + ".zip");
		file.delete();
	}

	/**
	 * export excel
	 * 
	 * @param response
	 * @param xml
	 * @param excel
	 * @param datasets
	 * @throws Exception
	 */
	public static void exportErrorExcel(HttpServletResponse response, String xml, String filename, IDataset dataset) throws Exception {
		exportErrorExcel(response, xml, filename, dataset, false);
	}
	
	/**
	 * export excel
	 * 
	 * @param response
	 * @param xml
	 * @param excel
	 * @param datasets
	 * @throws Exception
	 */
	public static void exportErrorExcelFromImport(HttpServletResponse response, String xml, String filename, IDataset dataset) throws Exception {
		exportErrorExcelFromImport(response, xml, filename, dataset, false);
	}

	/**
	 * export excel
	 * 
	 * @param response
	 * @param xml
	 * @param excel
	 * @param datasets
	 * @throws Exception
	 */
	public static void exportErrorExcel(HttpServletResponse response, String xml, String filename, IDataset dataset, boolean isfull) throws Exception {
		HttpServletRequest request = common.getContextRequest();
		request.setAttribute("success", "false");

		IDataset errorSheet = ExcelParser.getErrorSheet(SpringPropertyHolder.getContextProperty("excel.exportdirectory","exports/") + xml);
		IDataset errset = new DatasetList();
		if (!isfull) {
			for (int i = 0; i < dataset.size(); i++) {
				MetaObject dataMeta = SystemMetaObject.forObject(dataset.get(i));
				Boolean importResult = Boolean.parseBoolean(String.valueOf(dataMeta.getValue(IMPORT_RESULT)));
				if (!importResult) {
					errset.add(dataset.get(i));
				}
			}
			dataset = errset;
		}
		exportExcelBySheets(response, new IDataset[] { errorSheet }, new IDataset[] { dataset }, null, filename);
	}
	
	/**
	 * export excel
	 * 
	 * @param response
	 * @param xml
	 * @param excel
	 * @param datasets
	 * @throws Exception
	 */
	public static void exportErrorExcelFromImport(HttpServletResponse response, String xml, String filename, IDataset dataset, boolean isfull) throws Exception {
		HttpServletRequest request = common.getContextRequest();
		request.setAttribute("success", "false");
		
		IDataset errorSheet = ExcelParser.getErrorSheet(SpringPropertyHolder.getContextProperty("excel.importdirectory","imports/") + xml);
		IDataset errset = new DatasetList();
		if (!isfull) {
			for (int i = 0; i < dataset.size(); i++) {
				MetaObject dataMeta = SystemMetaObject.forObject(dataset.get(i));
				Boolean importResult = Boolean.parseBoolean(String.valueOf(dataMeta.getValue(IMPORT_RESULT)));
				if (!importResult) {
					errset.add(dataset.get(i));
				}
			}
			dataset = errset;
		}
		exportExcelBySheets(response, new IDataset[] { errorSheet }, new IDataset[] { dataset }, null, filename);
	}

	/**
	 * 直接导出excel
	 * 
	 * @param response
	 * @param xml
	 * @param filename
	 * @param list
	 * @throws Exception
	 */
	public static void exportExcelBySheets(HttpServletResponse response, String xml, String filename, List list) throws Exception {
		int sheetSize = Integer.parseInt(SpringPropertyHolder.getContextProperty("defaultsheetsize", "5000"));
		exportExcelBySheets(response, xml, filename, list, sheetSize);
	}

	/**
	 * 直接导出excel 每个sheet页最大数据量为 sheetSize
	 * 
	 * @param response
	 * @param xml
	 * @param filename
	 * @param list
	 * @param sheetSize
	 * @throws Exception
	 */
	public static void exportExcelBySheets(HttpServletResponse response, String xml, String filename, List list, int sheetSize) throws Exception {
		IDataset dataset = new DatasetList();
		dataset.addAll(list);
		exportExcelBySheets(response, xml, filename, dataset, sheetSize);
	}

	/**
	 * 直接导出excel 每个sheet页最大数据量为 sheetSize
	 * 
	 * @param response
	 * @param xml
	 * @param filename
	 * @param dataset
	 * @param sheetSize
	 * @throws Exception
	 */
	public static void exportExcelBySheets(HttpServletResponse response, String xml, String filename, IDataset dataset, int sheetSize) throws Exception {
		if (dataset != null) {
			List[] list = common.getSubLists(dataset, sheetSize);
			IDataset[] datasets = new IDataset[list.length];
			String[] xmls = new String[list.length];
			String[] sheetnames = new String[list.length];
			for (int i = 0; i < list.length; i++) {
				datasets[i] = new DatasetList();
				datasets[i].addAll(list[i]);
				xmls[i] = xml;
				sheetnames[i] = getSheetNameByFileName(filename);
			}
			exportExcelBySheets(response, xmls, datasets, sheetnames, filename);
		}
	}

	/**
	 * 去掉文件名后缀
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getSheetNameByFileName(String fileName) {
		String sheetName = "";
		if (fileName != null && fileName.indexOf(".") > -1) {
			sheetName = fileName.split("\\.")[0];
		}
		return sheetName;
	}

	/**
	 * 改造方法 直接导出Excle模板，并且支持多个sheet页，自定义每个sheet名字 不直接导出ZIP文件
	 * 
	 * @param bd
	 * @param xmls
	 *            每个sheet的xml配置
	 * @param datasets
	 *            每个sheet数据
	 * @param sheetnames
	 *            定义每个sheet名字 不填则使用默认
	 * @param filename
	 *            文件名称
	 * @throws Exception
	 */
	public static void exportExcelBySheets(HttpServletResponse response, String[] xmls, IDataset[] datasets, String[] sheetnames, String filename) throws Exception {
		exportExcelBySheets(response, getExportSheetsets(xmls), datasets, sheetnames, filename);
	}

	/**
	 * 改造方法 直接导出Excle模板，并且支持多个sheet页，自定义每个sheet名字 不直接导出ZIP文件
	 * 
	 * @param bd
	 * @param sheets
	 *            每个sheet的配置
	 * @param datasets
	 *            每个sheet数据
	 * @param sheetnames
	 *            定义每个sheet名字 不填则使用默认
	 * @param filename
	 *            文件名称
	 * @throws Exception
	 */
	public static void exportExcelBySheets(HttpServletResponse response, IDataset[] sheets, IDataset[] datasets, String[] sheetnames, String filename) throws Exception {
		Document document = getDocument(sheets, sheetnames);
		HSSFWorkbook workbook = getWorkbook(document.getRootElement().getChildren(), datasets, null, null);
		filename = common.encodeCharset(filename);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		response.setContentType(FileUtil.CONTENT_TYPE_EXCEL);
		OutputStream out = response.getOutputStream();
		workbook.write(out);
		out.close();
		out.flush();
	}

	/**
	 * 写Excel 并且支持多个sheet页，自定义每个sheet名字
	 * 
	 * @param bd
	 * @param sheets
	 *            每个sheet的定义
	 * @param datasets
	 *            每个sheet数据
	 * @param sheetnames
	 *            定义每个sheet名字 不填则使用默认
	 * @param full_name
	 *            文件全路径
	 * @throws Exception
	 */
	public static File writeExcelBySheets(IDataset[] sheets, IDataset[] datasets, String[] sheetnames, String full_name) throws Exception {
		Document document = getDocument(sheets, sheetnames);
		HSSFWorkbook workbook = getWorkbook(document.getRootElement().getChildren(), datasets, null, null);
		File file = new File(full_name);
		if (!new File(file.getParent()).isDirectory()) {// 目录不存在则创建
			new File(file.getParent()).mkdirs();
		}

		FileOutputStream out = new FileOutputStream(file);
		workbook.write(out);
		out.close();

		return file;
	}

	/**
	 * 写Excel 并且支持多个sheet页，自定义每个sheet名字
	 * 
	 * @param bd
	 * @param xmls
	 *            每个sheet的xml配置
	 * @param datasets
	 *            每个sheet数据
	 * @param sheetnames
	 *            定义每个sheet名字 不填则使用默认
	 * @param full_name
	 *            文件全路径
	 * @throws Exception
	 */
	public static File writeExcelBySheets(String[] xmls, IDataset[] datasets, String[] sheetnames, String full_name) throws Exception {
		return writeExcelBySheets(getExportSheetsets(xmls), datasets, sheetnames, full_name);
	}

	/**
	 * 写Excel 并且支持多个sheet页，自定义每个sheet名字 返回生成的文件ID
	 * 
	 * @param bd
	 * @param sheets
	 *            每个sheet
	 * @param datasets
	 *            每个sheet数据
	 * @param sheetnames
	 *            定义每个sheet名字 不填则使用默认
	 * @throws Exception
	 */
	public static String writeExcelByImport(IDataset[] sheets, IDataset[] datasets, String[] sheetnames) throws Exception {
		String file_name = common.getUniqeName();
		String file_path = SpringPropertyHolder.getContextProperty("excel.uploadpath","upload/walk") + "/" + FileUtil.getUploadPath(FileUtil.UPLOAD_TYPE_IMPORT);
		if (!new File(file_path).isDirectory()) {// 目录不存在则创建
			new File(file_path).mkdirs();
		}
		String full_name = file_path + "/" + file_name;

		writeExcelBySheets(sheets, datasets, sheetnames, full_name);

		return file_name;
	}

	/**
	 * 写Excel 并且支持多个sheet页，自定义每个sheet名字 返回生成的文件ID
	 * 
	 * @param bd
	 * @param xmls
	 *            每个sheet的xml配置
	 * @param datasets
	 *            每个sheet数据
	 * @param sheetnames
	 *            定义每个sheet名字 不填则使用默认
	 * @throws Exception
	 */
	public static String writeExcelByImport(String[] xmls, IDataset[] datasets, String[] sheetnames) throws Exception {
		return writeExcelByImport(getExportSheetsets(xmls), datasets, sheetnames);
	}

	/**
	 * getSheetsets
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static IDataset[] getExportSheetsets(String[] xmls) throws Exception {
		IDataset[] sheetsets = new IDataset[xmls.length];
		for (int i = 0; i < xmls.length; i++) {
			sheetsets[i] = getXMLDataset(SpringPropertyHolder.getContextProperty("excel.exportdirectory","exports/") + xmls[i]);
		}
		return sheetsets;
	}

	/**
	 * 解析xml
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static List createDocument(String xml) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		String path = common.getClassResource(xml).toString();
		Document document = builder.build(path);
		Element book = document.getRootElement();
		List list = book.getChildren();
		Element element = null;
		for (int i = 0; i < list.size(); i++) {
			element = (Element) list.get(i);
			List list2 = element.getChildren();
			for (int t = 0; t < list2.size(); t++) {
				element = (Element) list2.get(i);
			}
		}
		return element.getChildren();
	}

	/**
	 * 解析xml，返回dataset
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static IDataset getXMLDataset(String xml) throws Exception {
		IDataset datalist = new DatasetList();
		List list = createDocument(xml);
		for (int i = 0; i < list.size(); i++) {
			Element element1 = (Element) list.get(i);
			IData data = new DataMap();
			if (element1.getAttributeValue("name") != null) {
				data.put("name", element1.getAttributeValue("name"));
			}
			if (element1.getAttributeValue("desc") != null) {
				data.put("desc", element1.getAttributeValue("desc"));
			}
			if (element1.getAttributeValue("type") != null) {
				data.put("type", element1.getAttributeValue("type"));
			}
			if (element1.getAttributeValue("align") != null) {
				data.put("align", element1.getAttributeValue("align"));
			} else {
				data.put("align", "1");
			}
			if (element1.getAttributeValue("width") != null) {
				data.put("width", element1.getAttributeValue("width"));
			} else {
				data.put("width", "3000");
			}
			if (element1.getAttributeValue("datasrc") != null) {
				data.put("datasrc", element1.getAttributeValue("datasrc"));
			}
			if (element1.getAttributeValue("dataid") != null) {
				data.put("dataid", element1.getAttributeValue("dataid"));
			}
			if (element1.getAttributeValue("readonly") != null) {
				data.put("readonly", element1.getAttributeValue("readonly"));
			}
			if (element1.getAttributeValue("headercolor") != null) {
				data.put("headercolor", element1.getAttributeValue("headercolor"));
			}
			datalist.add(data);
		}
		return datalist;
	}

	/**
	 * 读取xml配置获取非只读列 readonly为自定义，获取哪些列为非只读(用来导入修改时获取非只读的列)
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getNoReadonlyColumns(String xml) throws Exception {
		StringBuilder col = new StringBuilder();
		List list = createDocument(xml);
		for (int i = 0; i < list.size(); i++) {
			Element element1 = (Element) list.get(i);
			if (element1.getAttributeValue("readonly") == null || "false".equals(element1.getAttributeValue("readonly"))) {
				col.append(element1.getAttributeValue("name")).append(",");
			}
		}
		return "".equals(col.toString()) ? null : col.toString().substring(0, col.toString().length() - 1);
	}

	/**
	 * 返回新增元素的sheet
	 * 
	 * @param datasets
	 * @param name
	 * @param desc
	 * @return
	 * @throws Exception
	 */
	public static IDataset getSheet(IDataset sheet, IData data, int index) throws Exception {
		sheet.add(index, data);
		return sheet;
	}

	/**
	 * 返回对比sheet
	 * 
	 * @param datasets
	 * @param name
	 * @param desc
	 * @return
	 * @throws Exception
	 */
	public static IDataset getCompareSheet(String xml) throws Exception {
		IDataset orgSheet = getXMLDataset(xml);
		IData data1 = new DataMap();
		data1.put("name", "B_A_COMPARE");
		data1.put("desc", "前后对比");
		data1.put("type", "1");
		data1.put("align", "1");
		data1.put("width", "4000");
		return getSheet(orgSheet, data1, 0);
	}

	/**
	 * 返回错误sheet
	 * 
	 * @param datasets
	 * @param name
	 * @param desc
	 * @return
	 * @throws Exception
	 */
	public static IDataset getErrorSheet(String xml) throws Exception {
		IDataset errorSheet = getXMLDataset(xml);
		IData data1 = new DataMap();
		data1.put("name", IMPORT_ERROR);
		data1.put("desc", "导入错误");
		data1.put("type", "1");
		data1.put("align", "1");
		data1.put("width", "10000");
		return getSheet(errorSheet, data1, errorSheet.size());
	}

	/**
	 * 移除元素，返回新的sheet
	 * 
	 * @param xml
	 * @param elements
	 *            :逗号分隔
	 * @return
	 * @throws Exception
	 */
	public static IDataset removeElementSheet(String xml, String elements) throws Exception {
		IDataset sheet = getXMLDataset(xml);
		if (elements == null || "".equals(elements)) {
			return sheet;
		}
		String[] eles = elements.split(",");
		for (int i = 0; i < sheet.size(); i++) {
			IData data = (IData) sheet.get(i);
			for (int j = 0; j < eles.length; j++) {
				if (eles[j].equals(data.getString("name"))) {
					sheet.remove(data);
				}
			}
		}
		return sheet;
	}

	/**
	 * 获取动态列导出配置
	 * 
	 * @param cols
	 * @param de
	 * @return
	 * @throws Exception
	 */
	public static Document getDynExportDocument(List cols, DynExportConfig de) throws Exception {
		IDataset datalist = new DatasetList();
		for (int i = 0; i < cols.size(); i++) {
			IData col = (IData) cols.get(i);
			IData data = new DataMap();
			if (!"".equals(dealNullValue(de.getName()))) {
				data.put("name", col.getString(de.getName().toUpperCase(), de.getNameDefaultValue()));
			} else if ("".equals(dealNullValue(de.getName())) && !"".equals(de.getNameDefaultValue())) {
				data.put("name", de.getNameDefaultValue());
			}

			if (!"".equals(dealNullValue(de.getDesc()))) {
				data.put("desc", col.getString(de.getDesc().toUpperCase(), de.getDescDefaultValue()));
			} else if ("".equals(dealNullValue(de.getDesc())) && !"".equals(de.getDescDefaultValue())) {
				data.put("desc", de.getDescDefaultValue());
			}

			if (!"".equals(dealNullValue(de.getType()))) {
				data.put("type", getColType(col.getString(de.getType().toUpperCase(), de.getTypeDefaultValue())));
			} else if ("".equals(dealNullValue(de.getType())) && !"".equals(de.getTypeDefaultValue())) {
				data.put("type", getColType(de.getTypeDefaultValue()));
			}

			if (!"".equals(dealNullValue(de.getAlign()))) {
				data.put("align", col.getString(de.getAlign().toUpperCase(), de.getAlignDefaultValue()));
			} else if ("".equals(dealNullValue(de.getAlign())) && !"".equals(de.getAlignDefaultValue())) {
				data.put("align", de.getAlignDefaultValue());
			}

			if (!"".equals(dealNullValue(de.getWidth()))) {
				data.put("width", col.getString(de.getWidth().toUpperCase(), de.getWidthDefaultValue()));
			} else if ("".equals(dealNullValue(de.getWidth())) && !"".equals(de.getWidthDefaultValue())) {
				data.put("width", de.getWidthDefaultValue());
			}

			if (!"".equals(dealNullValue(de.getScale()))) {
				data.put("scale", col.getString(de.getScale().toUpperCase(), de.getScaleDefaultValue()));
			} else if ("".equals(dealNullValue(de.getScale())) && !"".equals(de.getScaleDefaultValue())) {
				data.put("scale", de.getScaleDefaultValue());
			}

			if (!"".equals(dealNullValue(de.getDatasrc()))) {
				data.put("datasrc", col.getString(de.getDatasrc().toUpperCase(), de.getDatasrcDefaultValue()));
			} else if ("".equals(dealNullValue(de.getDatasrc())) && !"".equals(de.getDatasrcDefaultValue())) {
				data.put("datasrc", de.getDatasrcDefaultValue());
			}

			if (!"".equals(dealNullValue(de.getDataid()))) {
				data.put("dataid", col.getString(de.getDataid().toUpperCase(), de.getDataidDefaultValue()));
			} else if ("".equals(dealNullValue(de.getDataid())) && !"".equals(de.getDataidDefaultValue())) {
				data.put("dataid", de.getDataidDefaultValue());
			}

			if (!"".equals(dealNullValue(de.getHeadercolor()))) {
				data.put("headercolor", col.getString(de.getHeadercolor().toUpperCase(), de.getHeadercolorDefaultValue()));
			} else if ("".equals(dealNullValue(de.getHeadercolor())) && !"".equals(de.getHeadercolorDefaultValue())) {
				data.put("headercolor", de.getHeadercolorDefaultValue());
			}

			datalist.add(data);
		}
		return getDocument(datalist);
	}

	/**
	 * 获取动态列导入配置
	 * 
	 * @param cols
	 * @param di
	 * @return
	 * @throws Exception
	 */
	public static Document getDynImportDocument(List cols, DynImportConfig di) throws Exception {
		IDataset datalist = new DatasetList();
		for (int i = 0; i < cols.size(); i++) {
			IData col = (IData) cols.get(i);
			IData data = new DataMap();
			if (!"".equals(dealNullValue(di.getName()))) {
				data.put("name", col.getString(di.getName().toUpperCase(), di.getNameDefaultValue()));
			} else if ("".equals(dealNullValue(di.getName())) && !"".equals(di.getNameDefaultValue())) {
				data.put("name", di.getNameDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getType()))) {
				data.put("type", getColType(col.getString(di.getType().toUpperCase(), di.getTypeDefaultValue())));
			} else if ("".equals(dealNullValue(di.getType())) && !"".equals(di.getTypeDefaultValue())) {
				data.put("type", getColType(di.getTypeDefaultValue()));
			}

			if (!"".equals(dealNullValue(di.getDesc()))) {
				data.put("desc", col.getString(di.getDesc().toUpperCase(), di.getDescDefaultValue()));
			} else if ("".equals(dealNullValue(di.getDesc())) && !"".equals(di.getDescDefaultValue())) {
				data.put("desc", di.getDescDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getNullable()))) {
				data.put("nullable", col.getString(di.getNullable().toUpperCase(), di.getNullableDefaultValue()));
			} else if ("".equals(dealNullValue(di.getNullable())) && !"".equals(di.getNullableDefaultValue())) {
				data.put("nullable", di.getNullableDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getEqusize()))) {
				data.put("equsize", col.getString(di.getEqusize().toUpperCase(), di.getEqusizeDefaultValue()));
			} else if ("".equals(dealNullValue(di.getEqusize())) && !"".equals(di.getEqusizeDefaultValue())) {
				data.put("equsize", di.getEqusizeDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getMinsize()))) {
				data.put("minsize", col.getString(di.getMinsize().toUpperCase(), di.getMinsizeDefaultValue()));
			} else if ("".equals(dealNullValue(di.getMinsize())) && !"".equals(di.getMinsizeDefaultValue())) {
				data.put("minsize", di.getMinsizeDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getMaxsize()))) {
				data.put("maxsize", col.getString(di.getMaxsize().toUpperCase(), di.getMaxsizeDefaultValue()));
			} else if ("".equals(dealNullValue(di.getMaxsize())) && !"".equals(di.getMaxsizeDefaultValue())) {
				data.put("maxsize", di.getMaxsizeDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getMinvalue()))) {
				data.put("minvalue", col.getString(di.getMinvalue().toUpperCase(), di.getMinvalueDefaultValue()));
			} else if ("".equals(dealNullValue(di.getMinvalue())) && !"".equals(di.getMinvalueDefaultValue())) {
				data.put("minvalue", di.getMinvalueDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getMaxvalue()))) {
				data.put("maxvalue", col.getString(di.getMaxvalue().toUpperCase(), di.getMaxvalueDefaultValue()));
			} else if ("".equals(dealNullValue(di.getMaxvalue())) && !"".equals(di.getMaxvalueDefaultValue())) {
				data.put("maxvalue", di.getMaxvalueDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getFormat()))) {
				data.put("format", col.getString(di.getFormat().toUpperCase(), di.getFormatDefaultValue()));
			} else if ("".equals(dealNullValue(di.getFormat())) && !"".equals(di.getFormatDefaultValue())) {
				data.put("format", di.getFormatDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getDatasrc()))) {
				data.put("datasrc", col.getString(di.getDatasrc().toUpperCase(), di.getDatasrcDefaultValue()));
			} else if ("".equals(dealNullValue(di.getDatasrc())) && !"".equals(di.getDatasrcDefaultValue())) {
				data.put("datasrc", di.getDatasrcDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getRegex()))) {
				data.put("regex", col.getString(di.getRegex().toUpperCase(), di.getRegexDefaultValue()));
			} else if ("".equals(dealNullValue(di.getRegex())) && !"".equals(di.getRegexDefaultValue())) {
				data.put("regex", di.getRegexDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getRegexDesc()))) {
				data.put("regexdesc", col.getString(di.getRegexDesc().toUpperCase(), di.getRegexDescDefaultValue()));
			} else if ("".equals(dealNullValue(di.getRegexDesc())) && !"".equals(di.getRegexDescDefaultValue())) {
				data.put("regexdesc", di.getRegexDescDefaultValue());
			}

			if (!"".equals(dealNullValue(di.getReadonly()))) {
				data.put("readonly", col.getString(di.getReadonly().toUpperCase(), di.getReadonlyDefaultValue()));
			} else if ("".equals(dealNullValue(di.getReadonly())) && !"".equals(di.getReadonlyDefaultValue())) {
				data.put("readonly", di.getReadonlyDefaultValue());
			}

			datalist.add(data);
		}

		return getDocument(datalist);
	}

	/**
	 * 获取列类型
	 * 
	 * @param typeValue
	 * @return
	 * @throws Exception
	 */
	public static String getColType(String typeValue) throws Exception {
		String colType = ValidateUtil.CELL_TYPE_STRING;// 默认是字符串
		if ("NUMBER".equalsIgnoreCase(typeValue) || "LONG".equalsIgnoreCase(typeValue) || "NUM".equalsIgnoreCase(typeValue)) {
			colType = ValidateUtil.CELL_TYPE_NUMERIC;
		}
		if ("DATE".equalsIgnoreCase(typeValue)) {
			colType = ValidateUtil.CELL_TYPE_DATETIME;
		}
		return colType;
	}

	/**
	 * 处理null值，返回空字符串
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String dealNullValue(String value) throws Exception {
		return value == null ? "" : value;
	}

	/**
	 * write excel
	 * 
	 * @param bd
	 * @param xml
	 * @param full_name
	 * @param datasets
	 * @return File
	 * @throws Exception
	 */
	public static File writeExcel(String xml, String full_name, IDataset[] datasets) throws Exception {
		return writeExcel(xml, full_name, null, datasets);
	}

	/**
	 * write excel
	 * 
	 * @param bd
	 * @param isheets
	 * @param full_name
	 * @param datasets
	 * @return File
	 * @throws Exception
	 */
	public static File writeExcel(IDataset[] isheets, String full_name, IDataset[] datasets) throws Exception {
		return writeExcel(isheets, full_name, null, datasets);
	}

	/**
	 * write excel
	 * 
	 * @param bd
	 * @param xml
	 * @param full_name
	 * @param real_name
	 * @param datasets
	 * @return File
	 * @throws Exception
	 */
	public static File writeExcel(String xml, String full_name, String real_name, IDataset[] datasets) throws Exception {
		return writeExcel(xml, full_name, real_name, datasets, null);
	}

	/**
	 * write excel
	 * 
	 * @param bd
	 * @param isheets
	 * @param full_name
	 * @param real_name
	 * @param datasets
	 * @return File
	 * @throws Exception
	 */
	public static File writeExcel(IDataset[] isheets, String full_name, String real_name, IDataset[] datasets) throws Exception {
		return writeExcel(isheets, full_name, real_name, datasets, null);
	}

	/**
	 * write excel
	 * 
	 * @param bd
	 * @param sheets
	 * @param full_name
	 * @param real_name
	 * @param datasets
	 * @param sheets_cells
	 * @return File
	 * @throws Exception
	 */
	public static File writeExcel(String xml, String full_name, String real_name, IDataset[] datasets, List[] sheets_cells) throws Exception {
		List sheets = getSheets(SpringPropertyHolder.getContextProperty("excel.exportdirectory","exports/"), xml);
		return writeExcel(sheets, full_name, real_name, datasets, sheets_cells);
	}

	/**
	 * write excel
	 * 
	 * @param bd
	 * @param usheets
	 * @param full_name
	 * @param real_name
	 * @param datasets
	 * @param sheets_cells
	 * @return File
	 * @throws Exception
	 */
	public static File writeExcel(IDataset[] isheets, String full_name, String real_name, IDataset[] datasets, List[] sheets_cells) throws Exception {
		List sheets = getSheets(isheets);
		return writeExcel(sheets, full_name, real_name, datasets, sheets_cells);
	}

	/**
	 * write excel
	 * 
	 * @param bd
	 * @param sheets
	 * @param full_name
	 * @param real_name
	 * @param datasets
	 * @param sheets_cells
	 * @return File
	 * @throws Exception
	 */
	private static File writeExcel(List sheets, String full_name, String real_name, IDataset[] datasets, List[] sheets_cells) throws Exception {
		String file_path = SpringPropertyHolder.getContextProperty("excel.uploadpath","upload/walk") + "/" + FileUtil.getUploadPath(FileUtil.UPLOAD_TYPE_EXPORT);
		if (!new File(file_path).isDirectory()) {// 目录不存在则创建
			new File(file_path).mkdirs();
		}
		if (real_name == null)
			real_name = FileUtil.getFileName(full_name);
		String main_name = real_name.indexOf(".") == -1 ? real_name : real_name.substring(0, real_name.indexOf("."));
		File file = new File(full_name);

		OutputStream out = new FileOutputStream(file);
		ZipOutputStream zipout = new ZipOutputStream(out);

		for (int i = 0; i < datasets.length; i++) {
			IDataset dataset = datasets[i];
			if (dataset.isSerializable() && dataset.isBatchSerializable()) {
				String store_path = dataset.getSerializablePath() + "/" + dataset.getSerializableId();
				File store_dir = new File(store_path);
				if (store_dir.exists() && store_dir.isDirectory()) {
					for (int j = 0; j < dataset.getBatchPageCount(); j++) {
						File store_file = new File(store_path + "/" + j);
						if (store_file.exists()) {
							IDataset storeset = (IDataset) FileUtil.readObject(store_file);
							String file_realname = store_path + "/" + main_name + "_" + (i + 1) + "_" + (j + 1) + ".xls";

							File real_file = writeExcelBySingle(file_realname, sheets, new IDataset[] { storeset }, new IDataset[] { dataset }, sheets_cells);
							zipout.putNextEntry(new ZipEntry(real_file.getName()));
							writeFile(real_file, zipout);

							store_file.delete();
							real_file.delete();
						}
					}
					store_dir.delete();
				}
			} else {
				String file_realname = file_path + "/" + "single" + common.getUniqeName();

				File real_file = writeExcelBySingle(file_realname, sheets, new IDataset[] { dataset }, null, sheets_cells);
				zipout.putNextEntry(new ZipEntry(main_name + "_" + (i + 1) + "_" + 1 + ".xls"));
				writeFile(real_file, zipout);

				real_file.delete();
			}
		}
		zipout.close();

		return file;
	}

	/**
	 * writer excel by import
	 * 
	 * @param bd
	 * @param xml
	 * @param datasets
	 * @return String
	 * @throws Exception
	 */
	public static String writeExcelByImport(String xml, IDataset[] datasets) throws Exception {
		String file_name = common.getUniqeName();
		String file_path = SpringPropertyHolder.getContextProperty("excel.uploadpath","upload/walk") + "/" + FileUtil.getUploadPath(FileUtil.UPLOAD_TYPE_IMPORT);
		if (!new File(file_path).isDirectory()) {// 目录不存在则创建
			new File(file_path).mkdirs();
		}
		String full_name = file_path + "/" + file_name;

		List sheets = getSheets(SpringPropertyHolder.getContextProperty("excel.exportdirectory","exports/"), xml);
		writeExcelBySingle(full_name, sheets, datasets, null, null);

		return file_name;
	}

	/**
	 * get work book
	 * 
	 * @param bd
	 * @param xml
	 * @param datasets
	 * @param orgdatasets
	 * @param sheets_cells
	 * @return HSSFWorkbook
	 * @throws Exception
	 */
	private static HSSFWorkbook getWorkbook(List sheets, IDataset[] datasets, IDataset[] orgdatasets, List[] sheets_cells) throws Exception {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFDataFormat format = workbook.createDataFormat();

		List allsheets = new ArrayList();
		List alldatasets = new ArrayList();

		for (int i = 0; i < datasets.length; i++) {
			int sheetCount = (int) Math.ceil((double) datasets[i].size() / (double) MAX_PAGE_SIZE);
			IDataset dataset = datasets[i];
			IDataset orgdataset = orgdatasets == null ? dataset : orgdatasets[i];
			for (int j = 0; j < sheetCount; j++) {
				Map allsheet = new HashMap();
				allsheet.put("SHEET_DATA", sheets.get(i));
				// allsheet.put("TRANSACT_KEYS", orgdataset.getTransactKeys());
				if (sheets_cells != null) {
					allsheet.put("SHEET_CELLS", sheets_cells[i]);
				}
				allsheets.add(allsheet);
				alldatasets.add(new DatasetList(dataset.subList(j * MAX_PAGE_SIZE, (j + 1) * MAX_PAGE_SIZE > dataset.size() ? dataset.size() : (j + 1) * MAX_PAGE_SIZE)));
			}
		}

		Map headerColor = new HashMap();
		for (int i = 0; i < allsheets.size(); i++) {
			Map allsheet = (Map) allsheets.get(i);
			Element sheet = (Element) allsheet.get("SHEET_DATA");
			List sheet_cells = (List) allsheet.get("SHEET_CELLS");
			// Map transactKeys = (Map) allsheet.get("TRANSACT_KEYS");

			String sheet_desc = sheet.getAttributeValue("desc");

			HSSFSheet worksheet = workbook.createSheet();
			workbook.setSheetName(i, sheet_desc + "_" + (i + 1));

			int rows = 0;

			Element header = sheet.getChild("header");
			boolean isshow = Boolean.valueOf(header.getAttributeValue("isshow")).booleanValue();
			if (isshow) {
				HSSFRow rowH = worksheet.createRow(rows++);
				rowH.setHeight(Short.parseShort(header.getAttributeValue("height")));

				short indexH = 0;
				List cells = header.getChildren();
				for (short h = 0; h < cells.size(); h++) {
					Element cell = (Element) cells.get(h);
					String cell_name = cell.getAttributeValue("name");
					if (sheet_cells != null && !sheet_cells.contains(cell_name)) {
						continue;
					}
					String cell_desc = cell.getAttributeValue("desc");
					String cell_width = cell.getAttributeValue("width");

					HSSFCell cellH = rowH.createCell(indexH);
					cellH.setCellValue(cell_desc);
					worksheet.setColumnWidth(indexH, Short.parseShort(cell_width));
					cellH.setCellStyle(getHeaderColor(workbook, cell.getAttributeValue("headercolor"), headerColor));

					indexH++;
				}
			}

			HSSFFont font = workbook.createFont();
			font.setColor(HSSFColor.BLACK.index);

			// List styles = new ArrayList();

			// 设置行间隔颜色
			Map styles = new HashMap();
			// 淡蓝色
			HSSFCellStyle paleBlue = workbook.createCellStyle();
			paleBlue.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			paleBlue.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
			paleBlue.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			// 白色
			HSSFCellStyle white = workbook.createCellStyle();
			white.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			white.setFillForegroundColor(HSSFColor.WHITE.index);
			white.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			styles.put("PALE_BLUE", paleBlue);
			styles.put("WHITE", white);

			List cells = header.getChildren();
			IDataset dataset = (IDataset) alldatasets.get(i);
			for (int j = 0; j < dataset.size(); j++) {
				HSSFRow workrow = worksheet.createRow(rows++);

				// 设置行间隔颜色
				HSSFCellStyle style = (HSSFCellStyle) styles.get(rows % 2 == 0 ? "PALE_BLUE" : "WHITE");

				short indexH = 0;
				for (short h = 0; h < cells.size(); h++) {
					Element cell = (Element) cells.get(h);
					String cell_name = cell.getAttributeValue("name");
					if (sheet_cells != null && !sheet_cells.contains(cell_name)) {
						continue;
					}

					String cell_type = cell.getAttributeValue("type");
					String cell_align = cell.getAttributeValue("align");
					String cell_scale = cell.getAttributeValue("scale");
					String cell_format = cell.getAttributeValue("format");
					String cell_datasrc = cell.getAttributeValue("datasrc");
					String cell_dataid = cell.getAttributeValue("dataid");

					/*
					 * if (transactKeys != null &&
					 * transactKeys.containsKey(cell_name)) { String
					 * transactValue = (String) transactKeys.get(cell_name);
					 * String[] transactParams = transactValue.split(";");
					 * cell_datasrc = transactParams[0]; cell_dataid =
					 * transactParams[1]; }
					 */
					String cell_value = null;
					MetaObject dataMeta = SystemMetaObject.forObject(dataset.get(j));
					Object val = dataMeta.getValue(cell_dataid == null ? cell_name : cell_dataid);
					if (val != null) {
						if (val instanceof Date) {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							cell_value = sdf.format(val);
						} else {
							cell_value = val.toString();
						}
					}
					if (cell_datasrc != null) {
						cell_value = ParamTranslateUtil.getTranslateValue(cell_value, cell_datasrc);
						if (cell_value == null)
							cell_value = "";
					}

					HSSFCell workcell = workrow.createCell(indexH);

					/*
					 * if (j == 0) {
					 * style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
					 * 
					 * styles.add(style); } else { style = (HSSFCellStyle)
					 * styles.get(indexH); }
					 */
					if (cell_align != null) {
						style.setAlignment(Short.parseShort(cell_align));
					}
					if (cell_value != null) {
						if (ValidateUtil.CELL_TYPE_STRING.equals(cell_type)) {
							workcell.setCellValue(cell_value);
						}
						if (ValidateUtil.CELL_TYPE_NUMERIC.equals(cell_type)) {
							if (cell_format != null)
								style.setDataFormat(format.getFormat(cell_format));
							try {
								workcell.setCellValue(cell_scale == null ? Double.parseDouble(cell_value) : Double.parseDouble(cell_value) / Double.parseDouble(cell_scale));
							} catch (Exception e) {
								workcell.setCellValue(cell_value);
							}
						}
						if (ValidateUtil.CELL_TYPE_DATETIME.equals(cell_type)) {
							try {
								
								workcell.setCellValue(cell_format == null ? cell_value : common.decodeTimeStr(cell_format, cell_value));
							} catch (Exception e) {
								workcell.setCellValue(cell_value);
							}
						}
					}
					workcell.setCellStyle(style);
					indexH++;
				}
			}
		}
		return workbook;
	}

	/**
	 * 获取单元格背景颜色
	 * 
	 * @param workbook
	 * @param font
	 * @return
	 * @throws Exception
	 */
	public static HSSFCellStyle getHeaderColor(HSSFWorkbook workbook, String color, Map headerColor) throws Exception {
		color = (color == null || "".equals(color)) ? "LIGHT_BLUE" : color;
		if (headerColor.get("HEADERCOLOR_" + color) != null) {
			return (HSSFCellStyle) headerColor.get("HEADERCOLOR_" + color);
		}
		HSSFFont fontH = workbook.createFont();
		fontH.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontH.setColor(HSSFColor.WHITE.index);

		HSSFCellStyle styleH = workbook.createCellStyle();
		styleH.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		styleH.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		styleH.setFont(fontH);
		styleH.setFillForegroundColor(getColor(color));
		styleH.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleH.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleH.setBottomBorderColor(HSSFColor.WHITE.index);
		styleH.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleH.setLeftBorderColor(HSSFColor.WHITE.index);
		styleH.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleH.setRightBorderColor(HSSFColor.WHITE.index);
		styleH.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleH.setTopBorderColor(HSSFColor.WHITE.index);

		headerColor.put("HEADERCOLOR_" + color, styleH);
		return styleH;
	}

	/**
	 * 获取颜色 颜色索引：http://blog.csdn.net/edisonln3604/article/details/4406547
	 * 
	 * @param color
	 * @return
	 * @throws Exception
	 */
	public static short getColor(String color) throws Exception {
		short ffc = HSSFColor.LIGHT_BLUE.index;
		if (color != null && !"".equals(color)) {
			if ("AQUA".equalsIgnoreCase(color)) {
				ffc = HSSFColor.AQUA.index;
			} else if ("BLACK".equalsIgnoreCase(color)) {
				ffc = HSSFColor.BLACK.index;
			} else if ("BLUE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.BLUE.index;
			} else if ("BLUE_GREY".equalsIgnoreCase(color)) {
				ffc = HSSFColor.BLUE_GREY.index;
			} else if ("BRIGHT_GREEN".equalsIgnoreCase(color)) {
				ffc = HSSFColor.BRIGHT_GREEN.index;
			} else if ("BROWN".equalsIgnoreCase(color)) {
				ffc = HSSFColor.BROWN.index;
			} else if ("CORAL".equalsIgnoreCase(color)) {
				ffc = HSSFColor.CORAL.index;
			} else if ("CORNFLOWER_BLUE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.CORNFLOWER_BLUE.index;
			} else if ("DARK_BLUE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.DARK_BLUE.index;
			} else if ("DARK_GREEN".equalsIgnoreCase(color)) {
				ffc = HSSFColor.DARK_GREEN.index;
			} else if ("DARK_RED".equalsIgnoreCase(color)) {
				ffc = HSSFColor.DARK_RED.index;
			} else if ("DARK_TEAL".equalsIgnoreCase(color)) {
				ffc = HSSFColor.DARK_TEAL.index;
			} else if ("DARK_YELLOW".equalsIgnoreCase(color)) {
				ffc = HSSFColor.DARK_YELLOW.index;
			} else if ("GOLD".equalsIgnoreCase(color)) {
				ffc = HSSFColor.GOLD.index;
			} else if ("GREEN".equalsIgnoreCase(color)) {
				ffc = HSSFColor.GREEN.index;
			} else if ("GREY_25_PERCENT".equalsIgnoreCase(color)) {
				ffc = HSSFColor.GREY_25_PERCENT.index;
			} else if ("GREY_40_PERCENT".equalsIgnoreCase(color)) {
				ffc = HSSFColor.GREY_40_PERCENT.index;
			} else if ("GREY_50_PERCENT".equalsIgnoreCase(color)) {
				ffc = HSSFColor.GREY_50_PERCENT.index;
			} else if ("GREY_80_PERCENT".equalsIgnoreCase(color)) {
				ffc = HSSFColor.GREY_80_PERCENT.index;
			} else if ("INDIGO".equalsIgnoreCase(color)) {
				ffc = HSSFColor.INDIGO.index;
			} else if ("LAVENDER".equalsIgnoreCase(color)) {
				ffc = HSSFColor.LAVENDER.index;
			} else if ("LEMON_CHIFFON".equalsIgnoreCase(color)) {
				ffc = HSSFColor.LEMON_CHIFFON.index;
			} else if ("LIGHT_BLUE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.LIGHT_BLUE.index;
			} else if ("LIGHT_CORNFLOWER_BLUE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.LIGHT_CORNFLOWER_BLUE.index;
			} else if ("LIGHT_GREEN".equalsIgnoreCase(color)) {
				ffc = HSSFColor.LIGHT_GREEN.index;
			} else if ("LIGHT_ORANGE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.LIGHT_ORANGE.index;
			} else if ("LIGHT_TURQUOISE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.LIGHT_TURQUOISE.index;
			} else if ("LIGHT_YELLOW".equalsIgnoreCase(color)) {
				ffc = HSSFColor.LIGHT_YELLOW.index;
			} else if ("LIME".equalsIgnoreCase(color)) {
				ffc = HSSFColor.LIME.index;
			} else if ("OLIVE_GREEN".equalsIgnoreCase(color)) {
				ffc = HSSFColor.OLIVE_GREEN.index;
			} else if ("ORANGE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.ORANGE.index;
			} else if ("ORCHID".equalsIgnoreCase(color)) {
				ffc = HSSFColor.ORCHID.index;
			} else if ("PALE_BLUE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.PALE_BLUE.index;
			} else if ("PINK".equalsIgnoreCase(color)) {
				ffc = HSSFColor.PINK.index;
			} else if ("PLUM".equalsIgnoreCase(color)) {
				ffc = HSSFColor.PLUM.index;
			} else if ("RED".equalsIgnoreCase(color)) {
				ffc = HSSFColor.RED.index;
			} else if ("ROSE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.ROSE.index;
			} else if ("ROYAL_BLUE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.ROYAL_BLUE.index;
			} else if ("SEA_GREEN".equalsIgnoreCase(color)) {
				ffc = HSSFColor.SEA_GREEN.index;
			} else if ("SKY_BLUE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.SKY_BLUE.index;
			} else if ("TAN".equalsIgnoreCase(color)) {
				ffc = HSSFColor.TAN.index;
			} else if ("TEAL".equalsIgnoreCase(color)) {
				ffc = HSSFColor.TEAL.index;
			} else if ("TURQUOISE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.TURQUOISE.index;
			} else if ("VIOLET".equalsIgnoreCase(color)) {
				ffc = HSSFColor.VIOLET.index;
			} else if ("WHITE".equalsIgnoreCase(color)) {
				ffc = HSSFColor.WHITE.index;
			} else if ("YELLOW".equalsIgnoreCase(color)) {
				ffc = HSSFColor.YELLOW.index;
			}
		}
		return ffc;
	}

	/**
	 * write excel by single
	 * 
	 * @param bd
	 * @param full_name
	 * @param sheets
	 * @param excel
	 * @param datasets
	 * @param orgdatasets
	 * @param sheets_cells
	 * @return File
	 * @throws Exception
	 */
	private static File writeExcelBySingle(String full_name, List sheets, IDataset[] datasets, IDataset[] orgdatasets, List[] sheets_cells) throws Exception {
		HSSFWorkbook workbook = getWorkbook(sheets, datasets, orgdatasets, sheets_cells);

		File file = new File(full_name);

		FileOutputStream out = new FileOutputStream(file);
		workbook.write(out);
		out.close();

		return file;
	}

	/**
	 * write file
	 * 
	 * @param file
	 * @param out
	 * @param full_name
	 * @param real_name
	 * @param xml
	 * @param dataset
	 * @return File
	 * @throws Exception
	 */
	private static void writeFile(File file, OutputStream zipout) throws Exception {
		FileInputStream in = new FileInputStream(file);
		FileUtil.writeInputToOutput(in, zipout, true);
		in.close();
	}

	/**
	 * write text
	 * 
	 * @param bd
	 * @param xml
	 * @param full_name
	 * @param datasets
	 * @return File
	 * @throws Exception
	 */
	private static File writeText(String xml, String full_name, IDataset[] datasets) throws Exception {
		File file = new File(full_name);

		FileOutputStream out = new FileOutputStream(file);
		writeContentByText(out, common.getClassResource(SpringPropertyHolder.getContextProperty("excel.exportdirectory","exports/") + xml).toString(), datasets);
		out.close();

		return file;
	}

	/**
	 * export text
	 * 
	 * @param response
	 * @param xml
	 * @param file_name
	 * @param datasets
	 * @throws Exception
	 */
	private static void exportText(HttpServletResponse response, String xml, String file_name, IDataset[] datasets) throws Exception {
		String file_path = SpringPropertyHolder.getContextProperty("excel.uploadpath","upload/walk") + "/" + FileUtil.getUploadPath(FileUtil.UPLOAD_TYPE_TEMP);
		if (!new File(file_path).isDirectory()) {// 目录不存在则创建
			new File(file_path).mkdirs();
		}
		String full_name = file_path + "/" + common.getUniqeName();

		File file = writeText(full_name, xml, datasets);

		OutputStream out = FileUtil.getOutputStreamByDown(response, file_name);
		FileUtil.writeInputToOutput(new FileInputStream(file), out);

		file.delete();
	}

	/**
	 * write content by text
	 * 
	 * @param bd
	 * @param out
	 * @param xml
	 * @param datasets
	 * @throws Exception
	 */
	private static void writeContentByText(OutputStream out, String xml, IDataset[] datasets) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

		List sheets = getSheets(xml);

		for (int i = 0; i < sheets.size(); i++) {
			Element sheet = (Element) sheets.get(i);
			Element header = sheet.getChild("header");
			boolean isshow = Boolean.valueOf(header.getAttributeValue("isshow")).booleanValue();
			if (isshow) {
				List cells = header.getChildren();
				for (short h = 0; h < cells.size(); h++) {
					Element cell = (Element) cells.get(h);
					String cell_desc = cell.getAttributeValue("desc");

					writer.write(cell_desc);
					if (h != cells.size() - 1)
						writer.write(",");
				}
				writer.newLine();
			}

			List cells = header.getChildren();
			for (int j = 0; j < datasets[i].size(); j++) {
				IData data = (IData) datasets[i].get(j);

				for (short h = 0; h < cells.size(); h++) {
					Element cell = (Element) cells.get(h);
					String cell_name = cell.getAttributeValue("name");
					String cell_type = cell.getAttributeValue("type");
					String cell_scale = cell.getAttributeValue("scale");
					String cell_format = cell.getAttributeValue("format");
					String cell_datasrc = cell.getAttributeValue("datasrc");
					String cell_value = (String) data.get(cell_name, "");

					if (cell_datasrc != null) {
						cell_value = ParamTranslateUtil.getTranslateValue(cell_value, cell_datasrc);
						if (cell_value == null)
							cell_value = "";
					}

					if (ValidateUtil.CELL_TYPE_STRING.equals(cell_type)) {
						writer.write(cell_value);
					}
					if (ValidateUtil.CELL_TYPE_NUMERIC.equals(cell_type)) {
						writer.write(String.valueOf(cell_scale == null ? Double.parseDouble(cell_value) : Double.parseDouble(cell_value) / Double.parseDouble(cell_scale)));
					}
					if (ValidateUtil.CELL_TYPE_DATETIME.equals(cell_type)) {
						writer.write(cell_format == null ? cell_value : common.decodeTimeStr(cell_format, cell_value));
					}

					if (h != cells.size() - 1)
						writer.write(",");
				}

				writer.newLine();
			}
		}

		writer.close();
	}

	/**
	 * 校验导入
	 * 
	 * @param dataset
	 * @return
	 */
	public static boolean verify(IDataset dataset) {
		boolean verify = true;
		try {
			for (int i = 0; i < dataset.size(); i++) {
				MetaObject dataMeta = SystemMetaObject.forObject(dataset.get(i));
				Boolean importResult = Boolean.parseBoolean(String.valueOf(dataMeta.getValue(IMPORT_RESULT)));
				if (!importResult) {
					verify = false;
					break;
				}
			}
		} catch (Exception e) {
			verify = false;
		}
		return verify;
	}
	
	/**
	 * 导入校验失败
	 * @param dataset
	 * @param xml
	 * @throws Exception
	 */
	public static void error(List dataset, String xml) throws Exception {
		String uniqeName = common.getUniqeName();
		Session session = SecurityUtils.getSubject().getSession();
		session.setAttribute(uniqeName + EXCEL_UNIQE_DATA, dataset);
		session.setAttribute(uniqeName + EXCEL_UNIQE_XML, xml);
		String contextPath = common.getContextRequest().getContextPath();
		common.error("Check failure：<a href=\"" + contextPath + "/fileserver/downErrorExcel/" + uniqeName + "\" onclick=\"$('.panel-tool-close').click();\" target=\"dialogframe\">下载查看详情</a>");
	}
	/* ------------------------------- trash end ------------------------------- */
}