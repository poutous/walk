package org.walkframework.restful.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文档中的数据变换成JavaFileMeta
 * 
 * @author wangxin
 */
public class DataTransform {

	private ExcelReader excelReader;

	private EachRow rowMap;

	private String srcFileName;

	private Config config;

	public DataTransform(Config config) {
		excelReader = new ExcelReader();
		this.config = config;
	}

	public Map<String, JavaFileMeta> getClassMetas(File file, JavaFileType javaFileTypeEnum) throws Exception {
		Map<String, JavaFileMeta> map = new LinkedHashMap<String, JavaFileMeta>();
		List<Row> rows = excelReader.read(file, javaFileTypeEnum.getSheetAtIndex());
		rowMap = new EachRow().doForEach(rows);
		this.srcFileName = file.getName().replaceAll(".xlsx|.xls", "").replaceAll("[1-9]|\\.|、|\\s", "");
		Map<String, Row> rowByClassNameMap = rowMap.getRowByClassNameMap();
		Set<String> classNames = rowByClassNameMap.keySet();
		for (String className : classNames) {
			JavaFileMeta javaFileMeta = doTransform(javaFileTypeEnum, className);
			if (javaFileMeta != null && javaFileMeta.getFieldMetas() != null && !javaFileMeta.getFieldMetas().isEmpty()) {
				map.put(className, javaFileMeta);
			}
		}
		return map;
	}

	private JavaFileMeta doTransform(JavaFileType javaFileTypeEnum, String className) {
		JavaFileMeta javaFileMeta = new JavaFileMeta(config, javaFileTypeEnum);
		Map<String, Row> rowByClassNameMap = rowMap.getRowByClassNameMap();
		Map<String, Object> rowByNodeNameMap = rowMap.getRowByNodeNameMap();
		Row row = rowByClassNameMap.get(className);
		List<Row> subRows = row.getSubRows();
		List<JavaFieldMeta> fieldMetas = new ArrayList<JavaFieldMeta>();
		javaFileMeta.setSrcFileName(srcFileName);
		javaFileMeta.setDesc(row.getDesc());
		javaFileMeta.setName(className);
		if (subRows == null || subRows.isEmpty())
			return null;
		if (javaFileTypeEnum == JavaFileType.REQ) {
			Row nextReqPaginationNode = null;
			for (int i = 0; i < subRows.size(); i++) {
				Row subRow = subRows.get(i);
				if (subRow == nextReqPaginationNode || (nextReqPaginationNode = nextReqPaginationNode(subRows, i)) != null) {
					javaFileMeta.setSuperClassName(config.getReqPaginationSuperClassName());
					continue;
				}
				fieldMetas.add(rowToFieldMeta(javaFileMeta, subRow, subRow.getRowNum()));
			}
		} else {
			int countComplex = 0;
			for (int i = 0; i < subRows.size();) {
				Row subRow = subRows.get(i);
				String nodeName = subRow.getNodeName();
				// 修改分页rows节点判断逻辑,节点名为rows List类型 是一个父节点(或则说是复杂节点) 且当前的节点是他的父节点
				if ("rows".equals(nodeName) && subRow.getContain().matches("\\*|\\+")) {
					Object obj = rowByNodeNameMap.get("rows");
					Row value = null;
					if (obj instanceof Row) {
						value = (Row) obj;
						value = value.IsComplex() ? value : null;
					} else {
						@SuppressWarnings("unchecked")
						List<Row> rows = (List<Row>) obj;
						for (Row r : rows) {
							if (r.IsComplex() && r.getParentRow() == row) {
								value = r;
								break;
							}
						}
					}
					if (value != null) {
						List<Row> testSubRows = value.getSubRows();
						if (testSubRows != null) {
							subRows = testSubRows;
							i = countComplex = 0;
							javaFileMeta.getImportPackageNames().clear();
							fieldMetas.clear();
							continue;
						}
					}
				}
				JavaFieldMeta fieldMeta = rowToFieldMeta(javaFileMeta, subRow, subRow.getRowNum());
				fieldMetas.add(fieldMeta);
				if (fieldMeta.isComplex()) {
					countComplex++;
				}
				i++;
			}
			javaFileMeta.setRequireTranslate(countComplex > 0);
		}
		javaFileMeta.setFieldMetas(fieldMetas);
		return javaFileMeta;
	}

	/**
	 * 原始Row数据变FieldMeta
	 * 
	 * @param row
	 * @param sheetAtIndex
	 * @return
	 */
	private JavaFieldMeta rowToFieldMeta(JavaFileMeta javaFileMeta, Row row, int position) {
		String nodeName = row.getNodeName();
		JavaFileType javaFileType = javaFileMeta.getJavaFileType();
		boolean isReq = javaFileType == JavaFileType.REQ;
		String contain = row.getContain();
		PkgAndAnt packAndAnt = new PkgAndAnt(row, isReq, config.isForceBigDecimals());
		javaFileMeta.addImportPackageName(packAndAnt.getImportPacks());
		String validatorAnt = packAndAnt.getValidatorAnnotationName();
		boolean isComplex = row.IsComplex();
		String translatorName = null;
		if (isComplex && !isReq) {
			translatorName = addTranslatorjavaFileMeta(javaFileMeta, row);
		}
		return new JavaFieldMeta(nodeName, packAndAnt.getJavaType(), validatorAnt, row.getDesc(), position, contain.equals("1") || contain.equals("+"), isComplex, translatorName);
	}

	private String addTranslatorjavaFileMeta(JavaFileMeta javaFileMeta, Row row) {
		String nodeName = row.getNodeName();
		String translatorName = row.getClassName() + "Translator";
		JavaFileMeta translatorjavaFileMeta = new JavaFileMeta(config, JavaFileType.TRANSLATOR);
		translatorjavaFileMeta.setDesc(javaFileMeta.getName() + " " + nodeName + "属性翻译器");
		translatorjavaFileMeta.setName(translatorName);
		JavaFileBuilder.translatorJavaFileMeta.put(translatorName, translatorjavaFileMeta);
		String packageName = translatorjavaFileMeta.getPackageName();
		if (StringUtil.hasText(packageName)) {
			javaFileMeta.addImportPackageName(packageName + "." + translatorName);
		}
		return translatorName;
	}

	/**
	 * 判断是否是请求分页节点 (逻辑为 ：两节点名为 currPage 、pageSize 并且它们父节点名同名 顺序紧跟 可互换顺序)
	 * 
	 * @param rows
	 * @param index
	 * @return
	 */
	private Row nextReqPaginationNode(List<Row> rows, int index) {
		int size = rows.size();
		Row row = rows.get(index);
		String nodeName = row.getNodeName();
		if (index + 1 > size - 1)
			return null;
		Row nextRow = rows.get(index + 1);
		String nextNodeName = nextRow.getNodeName();
		if (!nextRow.getParentNodeName().equals(row.getParentNodeName()))
			return null;
		if (("currPage".equals(nodeName) && "pageSize".equals(nextNodeName)) || ("pageSize".equals(nodeName) && "currPage".equals(nextNodeName)))
			return nextRow;
		return null;

	}

}