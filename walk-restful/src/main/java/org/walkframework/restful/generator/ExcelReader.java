package org.walkframework.restful.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.walkframework.restful.exception.ExcelReadException;

/**
 * @author wangxin
 * 
 */
public class ExcelReader {

	public ExcelReader() {
	}

	/**
	 * Excel解析的入口方法
	 * 
	 * @param classNamesAndNodeNames
	 * @param dataList
	 */
	public List<Row> read(File file, int sheetAtIndex) {
		try {
			List<Row> dataList = new ArrayList<Row>();
			if (file.getName().toLowerCase().endsWith(".xlsx")) {
				readExcel2007(file, sheetAtIndex, dataList);
			} else {
				readExcel2003(file, sheetAtIndex, dataList);
			}
			famterDeal(dataList);
			return dataList;
		} catch (IllegalArgumentException e) {
			String message = e.getMessage();
			if (message.indexOf("out of range") != -1 && message.indexOf("Sheet index") != -1) {
				throw new ExcelReadException("Sheet不存在");
			} else {
				throw new ExcelReadException(e);
			}
		} catch (FileNotFoundException e) {
			throw new ExcelReadException(file.getPath() + "文件不存在", e);
		} catch (IOException e) {
			throw new ExcelReadException(e);
		}
	}

	/**
	 * 读取Excel原数据
	 * 
	 * @param classNamesAndNodeNames
	 *            类名和复杂节点名
	 * @param dataList
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void readExcel2003(File file, int sheetIndex, List<Row> dataList) throws FileNotFoundException, IOException {
		HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));
		HSSFSheet sheet = wb.getSheetAt(sheetIndex);
		int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
		for (int rowNum = sheet.getFirstRowNum() + Constants.BEGIN_ROW_NUM - 1, rowCount = Constants.BEGIN_ROW_NUM - 1; rowCount++ < physicalNumberOfRows; rowNum++) {
			HSSFRow row = sheet.getRow(rowNum);
			if (row != null) {
				Row rowData = new Row(rowCount, cell2003ToStr(row, Constants.NODE_NAME), cell2003ToStr(row, Constants.PARENT_NODE_NAME), cell2003ToStr(row, Constants.CONTAIN), cell2003ToStr(row, Constants.TYPE), cell2003ToStr(row, Constants.LENGTH), cell2003ToStr(row, Constants.DESC), cell2003ToStr(row, Constants.CLASS_NAME));
				dataList.add(rowData);
			}
		}
	}

	/**
	 * 读取Excel元数据
	 * 
	 * @param classNamesAndNodeNames
	 * @param dataList
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void readExcel2007(File file, int sheetIndex, List<Row> dataList) throws FileNotFoundException, IOException {
		XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
		XSSFSheet sheet = wb.getSheetAt(sheetIndex);
		int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
		for (int rowNum = sheet.getFirstRowNum() + Constants.BEGIN_ROW_NUM - 1, rowCount = Constants.BEGIN_ROW_NUM - 1; rowCount++ < physicalNumberOfRows; rowNum++) {
			XSSFRow row = sheet.getRow(rowNum);
			if (row != null) {
				Row rowEntry = new Row(rowCount, cell2007ToStr(row, Constants.NODE_NAME), cell2007ToStr(row, Constants.PARENT_NODE_NAME), cell2007ToStr(row, Constants.CONTAIN), cell2007ToStr(row, Constants.TYPE), cell2007ToStr(row, Constants.LENGTH), cell2007ToStr(row, Constants.DESC), cell2007ToStr(row, Constants.CLASS_NAME));
				dataList.add(rowEntry);
			}
		}
	}

	/**
	 * 单元格数据合并格式化处理（根据节点为空白字符来合并数据）
	 * 
	 * @param dataList
	 */
	private void famterDeal(List<Row> dataList) {
		int len = dataList.size();
		for (int i = 0; i < len;) {
			Row rowEntry = dataList.get(i);
			if ("".equals(rowEntry.getNodeName())) {
				Row target = dataList.get(i - 1);
				try {
					// 合并数据
					MergeData(target, rowEntry);
					// 移除该行数据
					dataList.remove(i);
					// 重置长度
					len = dataList.size();
				} catch (Exception e) {
					throw new ExcelReadException("合并数据时出错", e);
				}
			} else {
				i++;
			}
		}
	}

	/**
	 * 合并行数据
	 * 
	 * @param target
	 * @param rowEntry
	 * @throws Exception
	 */
	private void MergeData(Row target, Row rowEntry) throws Exception {
		Field[] fields = Row.class.getDeclaredFields();
		for (Field field : fields) {
			if (field.getType() == String.class) {
				field.setAccessible(true);
				String targetString = String.valueOf(field.get(target)) + String.valueOf(field.get(rowEntry));
				field.set(target, targetString);
			}
		}
	}

	/**
	 * 单元格的数据类型全部转化为String
	 * 
	 * @param row
	 * @param index
	 * @return
	 */
	@SuppressWarnings("all")
	private String cell2003ToStr(HSSFRow row, int index) {
		HSSFCell cell = row.getCell(index);
		if (cell != null) {
			if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
				return (cell.getNumericCellValue() + "").split("\\.")[0];
			}
			if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
				return "";
			}
			// 对于文档中的 换行("\n") 和 "\"都要予以替换 避免错误 (其实在这里可以开放一个批量替换文档中错误的接口)
			return cell.toString().trim().replace("\n", " ").replace("\\", "\\\\").replace("\"", "\\\"");
		}
		return "";
	}

	/**
	 * 单元格的数据类型全部转化为String
	 * 
	 * @param row
	 * @param index
	 * @return
	 */
	@SuppressWarnings("all")
	private String cell2007ToStr(XSSFRow row, int index) {
		XSSFCell cell = row.getCell(index);
		if (cell != null) {
			if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
				return (cell.getNumericCellValue() + "").split("\\.")[0];
			}
			if (cell.getCellType() == XSSFCell.CELL_TYPE_BLANK) {
				return "";
			}
			// 对于文档中的 换行("\n") 和 "\"都要予以替换 避免错误
			return cell.toString().trim().replace("\n", " ").replace("\\", "\\\\").replace("\"", "\\\"");
		}
		return "";
	}

	private abstract class Constants {
		public static final int NODE_NAME = 0, // 节点名称
				PARENT_NODE_NAME = 1, // 父节点名称
				CONTAIN = 2, // 约束
				TYPE = 3, // 类型
				LENGTH = 4, // 长度
				DESC = 5, // 说明
				CLASS_NAME = 6; // 类名

		public static final int BEGIN_ROW_NUM = 2; // 文档读取的起始行数 (通常第一行为表格的头)
	}

}
