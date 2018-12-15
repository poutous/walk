package org.walkframework.batis.tools.dbtobean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.walkframework.data.annotation.Column;
import org.walkframework.data.annotation.Table;
import org.walkframework.data.entity.BaseEntity;
import org.walkframework.data.entity.OperColumn;


public class CreateBeans {
	private static StringBuffer sb = null;
	private static boolean isDateType = false;
	private static boolean isBigDecimal = false;
	private static List<ColumnBeans> lists = null;
	private static CreateBeanConfig appConfig = null;

	/**
	 * 生成javabean
	 * 
	 * @param tableNames
	 * @param appConfig
	 * @throws Exception
	 */
	public static void createJavaBean(String[][] tables, CreateBeanConfig appConfig) throws Exception {
		CreateBeans.appConfig = appConfig;
		Connection connection = null;
		try {
			connection = ConnectionTools.getConnection(appConfig.getDriverClassName(), appConfig.getDburl(), appConfig.getDbusername(), appConfig.getDbpassword());
			for (int i = 0; i < tables.length; i++) {
				run(connection, tables[i][0], tables[i][1], tables[i][2]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			ConnectionTools.closeConnection(null, null, connection);
		}

	}

	/**
	 * 生成javabean
	 * 
	 * @param connection
	 * @param tableName
	 * @param packageName
	 * @param packagePath
	 * @throws Exception
	 */
	private static void run(Connection connection, String tableName, String packageName, String packagePath) throws Exception {
		sb = new StringBuffer();
		isDateType = false;
		isBigDecimal = false;
		lists = new ArrayList<ColumnBeans>();
		createClassMessage(connection, tableName, packageName);
		sb.append("@Table(name=\"" + tableName.toUpperCase() + "\")\r");
		sb.append("public class " + createClassName(tableName) + " extends " + BaseEntity.class.getSimpleName() + " {\r");
		sb.append("\tprivate static final long serialVersionUID = 1L;\r");

		queryFieldName(connection, tableName);

		createFildName();

		createMethod();
		
		createStaticFildName();

		if (appConfig.isOverrideToString()) {
			createToString();
		}
		if (appConfig.isGetField()) {
			createToFiled();
		}
		sb.append("}");
		String msg = sb.toString();
		if (isDateType) {
			msg = msg.replaceAll("#date#", "\rimport java.util.Date;\r");
		} else {
			msg = msg.replaceAll("#date#", "");
		}
		if (isBigDecimal) {
			msg = msg.replaceAll("#bigDecimal#", "\rimport java.math.BigDecimal;\r");
		} else {
			msg = msg.replaceAll("#bigDecimal#", "");
		}

		ExpJavaBean(msg, ".java", "", tableName, packagePath);
		
	}

	/**
	 * 写入生成好的javabean文件
	 * 
	 * @param msg
	 * @param fileType
	 * @param bs
	 * @param tableName
	 * @param packagePath
	 */
	private static void ExpJavaBean(String msg, String fileType, String bs, String tableName, String packagePath) {
		File file = new File(packagePath);
		if ((!file.exists()) && (!file.isDirectory())) {
			file.mkdir();
		}
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		try {
			String fullFileName = file + "\\" + bs + createClassName(tableName) + fileType;
			fos = new FileOutputStream(fullFileName);
			bw = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
			bw.write(msg);
			bw.flush();
			System.out.println("File creation success: " + fullFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			try {
				bw.close();
				fos.close();
			} catch (IOException eo) {
				eo.printStackTrace();
			}

			try {
				bw.close();
				fos.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				bw.close();
				fos.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			try {
				bw.close();
				fos.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} finally {
			try {
				bw.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建get、set方法
	 */
	private static void createMethod() {
		for (ColumnBeans list : lists) {
			sb.append("\tpublic " + list.getType() + " get" + toUpperCaseFirstOne(list.getName()) + "() {\r");
			sb.append("\t\treturn " + list.getName() + ";\r");
			sb.append("\t}\r\r");
			sb.append("\tpublic OperColumn set" + toUpperCaseFirstOne(list.getName()) + "(" + list.getType() + " " + list.getName() + ") {\r");
			sb.append("\t\tthis." + list.getName() + " = " + list.getName() + ";\r");
			sb.append("\t\treturn addOperColumn(" + list.getCName() + ", \"" + list.getName() + "\", " + list.getName() + ", " + list.getType() + ".class);\r");
			sb.append("\t}\r\r");
		}
	}
	
	/**
	 * 创建静态字段
	 */
	private static void createStaticFildName() {
		sb.append("\t//Database field\r");
		for (ColumnBeans list : lists) {
			sb.append("\tpublic static final String " + list.getCName() + " = \"" + list.getCName() + "\";\r");
		}
		sb.append("\t\r");
	}

	/**
	 * 创建字段
	 */
	private static void createFildName() {
		sb.append("\r");
		for (ColumnBeans columnBeans : lists) {
			sb.append("\t/**");
			sb.append("\t\r");
			sb.append("\t *");
			sb.append(" " + columnBeans.getComments());
			sb.append("\t\r");
			sb.append("\t */");
			sb.append("\t\r");
			// sb.append("\t@Column(name=\"" + list.getCName() + "\",
			// type=Types." + JdbcType.forCode(list.getCType()).name() + ",
			// nullable=" + list.isNull() + ", length=" + list.getLeng() +
			// ")\r");
			sb.append("\t@Column(name = " + columnBeans.getCName());
			if(columnBeans.isAutoIncrement()){
				sb.append(", isAutoIncrement = " + columnBeans.isAutoIncrement());
			}
			sb.append(")\r");
			sb.append("\tprivate " + columnBeans.getType() + " " + columnBeans.getName() + ";\r\r");
		}
	}

	public static String getSchema(Connection connection) throws Exception {
		if (isOracle(connection)) {// oracle
			String schema = connection.getMetaData().getUserName();
			if ((schema == null) || (schema.length() == 0)) {
				throw new Exception("Oracle database schema is not allowed to empty.");
			}
			return schema.toUpperCase().toString();
		} else {// 其他 mysql
			return "%";
		}
	}

	/**
	 * 获取表备注信息
	 * 
	 * @param connection
	 * @param tableName
	 * @return
	 */
	private static String queryTableComments(Connection connection, String tableName) {
		ResultSet rs = null;
		try {
			if(isMysql(connection)){
				return getCommentByTableNameFromMysql(connection, tableName);
			}
			rs = connection.getMetaData().getTables(null, getSchema(connection), tableName.toUpperCase(), new String[] { "TABLE" });
			while (rs.next()) {
				String comments = rs.getString("REMARKS");
				if (comments != null){
					return comments;
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			try {
				ConnectionTools.closeConnection(rs, null, null);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * mysql获取表备注
	 * 
	 * @param connection
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public static String getCommentByTableNameFromMysql(Connection connection, String tableName) throws Exception {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + tableName);
		String comment = "";
		if (rs != null && rs.next()) {
			String createDDL = rs.getString(2);
			comment = parse(createDDL);
		}
		rs.close();
		stmt.close();
		return comment;
	}
	
	/**
	 * 返回注释信息
	 * @param all
	 * @return
	 */
	
	public static String parse(String all) {
		String comment = null;
		int index = all.indexOf("COMMENT='");
		if (index < 0) {
			return "";
		}
		comment = all.substring(index + 9);
		comment = comment.substring(0, comment.length() - 1);
		return comment;
	}

	/**
	 * 获取表字段信息
	 * 
	 * @param connection
	 * @param tableName
	 * @throws Exception
	 */
	private static void queryFieldName(Connection connection, String tableName) throws Exception {
		tableName = tableName.toUpperCase();
		String sql = "select * from " + tableName + " where 1 = 0";
		PreparedStatement pre = null;
		
		try {
			Map<String, String> columnsMap = getComments(connection, tableName);
			pre = connection.prepareStatement(sql);
			ResultSetMetaData metaData = pre.executeQuery().getMetaData();
			
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				ColumnBeans columnBeans = new ColumnBeans();
				columnBeans.setClassName(createClassName(tableName));
				columnBeans.setName(formatTableColumnToClassFiled(metaData.getColumnName(i).toUpperCase()));
				columnBeans.setType(formatTableTypeToJavaType(metaData.getColumnType(i), metaData.getScale(i), metaData.getColumnType(i) == 2 ? metaData.getPrecision(i) : metaData.getColumnDisplaySize(i)));
				columnBeans.setCName(metaData.getColumnName(i).toUpperCase());
				columnBeans.setCType(metaData.getColumnType(i));
				columnBeans.setLeng(metaData.getColumnDisplaySize(i));
				columnBeans.setAutoIncrement(metaData.isAutoIncrement(i));
				columnBeans.setComments(columnsMap.get(columnBeans.getCName()));
				lists.add(columnBeans);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				ConnectionTools.closeConnection(null, pre, null);
			} catch (SQLException e) {
				throw e;
			}
		}
	}
	
	/**
	 * 获取表的备注信息
	 * 
	 * @param connection
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	private static Map<String, String> getComments(Connection connection, String tableName) throws Exception {
		tableName = tableName.toUpperCase();
		
		ResultSet rs = null;
		Map<String, String> columnsMap = new HashMap<String, String>();
		try {
			if(isMysql(connection)){
				return getColumnCommentsFromMysql(connection, tableName);
			}
			rs = connection.getMetaData().getColumns(null, getSchema(connection), tableName, "%");
			while (rs.next()) {
				columnsMap.put(rs.getString("COLUMN_NAME"), rs.getString("REMARKS"));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				ConnectionTools.closeConnection(rs, null, null);
			} catch (SQLException e) {
				throw e;
			}
		}
		return columnsMap;
	}
	
	 /**
     * 获取表中字段的所有注释
     * @param tableName
     * @return
     */
    public static Map<String, String> getColumnCommentsFromMysql(Connection connection, String tableName) throws Exception{
    	Map<String, String> columnsMap = new HashMap<String, String>();
    	Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("show full columns from " + tableName);
		while (rs.next()) {
			columnsMap.put(rs.getString("Field").toUpperCase(), rs.getString("Comment"));
		} 
		rs.close();
		stmt.close();
        return columnsMap;
    }
    
	/**
	 * 创建javabean Class信息
	 * 
	 * @param connection
	 * @param tableName
	 * @param packageName
	 */
	private static void createClassMessage(Connection connection, String tableName, String packageName) {
		String tableDesc = queryTableComments(connection, tableName);
		if (tableDesc == null) {
			tableDesc = "";
		}
		if (packageName != null) {
			sb.append("package " + packageName + ";\r");
		}
		sb.append("import " + BaseEntity.class.getName() + ";\r");
		sb.append("import " + OperColumn.class.getName() + ";\r");
		sb.append("import " + Table.class.getName() + ";\r");
		sb.append("import " + Column.class.getName() + ";\r");
		// sb.append("import " + Types.class.getName() + ";\r");
		sb.append("\r");
		sb.append("#date# ");
		sb.append("#bigDecimal# ");
		sb.append("\r");
		sb.append("/**\r");
		sb.append("* @Type " + createClassName(tableName) + "\r");
		sb.append("* @Desc  " + tableDesc + "\r");
		sb.append("* @author " + System.getProperty("user.name") + "\r");
		sb.append("* @date " + getNewTime() + "\r");
		sb.append("* \r");
		sb.append("* 1、本类由工具类DbToEntity自动生成\r");
		sb.append("* 2、数据表新增字段时建议使用DbToEntity工具类重新生成\r");
		sb.append("* 3、不建议直接修改本类，如果想对本类扩展，建议创建子类，在子类里进行扩展，子类的set/get方法无需按照父类的写法，用开发工具直接生成就好\r");
		sb.append("*/\t\r");
	}

	/**
	 * 获取时间
	 * 
	 * @return
	 */
	private static String getNewTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}

	/**
	 * 创建类名
	 * 
	 * @param str
	 * @return
	 */
	private static String createClassName(String str) {
		StringBuffer sc = new StringBuffer();
		if (str == null) {
			throw new RuntimeException("表名称为空！");
		}
		
		String[] className = str.toLowerCase().split("_");
		for (String code : className) {
			sc.append(code.substring(0, 1).toUpperCase() + code.substring(1, code.length()));
		}
		return sc.toString();
	}

	/**
	 * 转换数据库字段类型为java
	 * 参照mybatis-generator-core：org/mybatis/generator/internal/types/JavaTypeResolverDefaultImpl.java
	 * @param code
	 * @return
	 * @throws SQLException
	 */
	private static String formatTableTypeToJavaType(int columnType, int scale, int length) throws SQLException {
		String javaType = "String";
		switch (columnType) {
		case Types.ARRAY:
		case Types.DATALINK:
		case Types.DISTINCT:
		case Types.JAVA_OBJECT:
		case Types.NULL:
		case Types.OTHER:
		case Types.REF:
		case Types.STRUCT:
			javaType = "Object";
			break;
		case Types.CHAR:
		case Types.CLOB:
		case Types.LONGNVARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NCLOB:
		case Types.NVARCHAR:
		case Types.VARCHAR:
			javaType = "String";
			break;
		case Types.DATE:
		case Types.TIME:
		case Types.TIMESTAMP:
			javaType = "Date";
			isDateType = true;
			break;
		case Types.BINARY:
		case Types.BLOB:
		case Types.LONGVARBINARY:
		case Types.VARBINARY:
			javaType = "byte[]";
			break;
		case Types.DOUBLE:
		case Types.FLOAT:
			javaType = "Double";
			break;
		case Types.BIT:
			if(length > 1){
				javaType = "byte[]";
			} else {
				javaType = "Boolean";
			}
			break;
		case Types.BOOLEAN:
			javaType = "Boolean";
			break;
		case Types.BIGINT:
			javaType = "Long";
			break;
		case Types.INTEGER:
			javaType = "Integer";
			break;
		case Types.REAL:
			javaType = "Float";
			break;
		case Types.SMALLINT:
			javaType = "Short";
			break;
		case Types.TINYINT:
			javaType = "Byte";
			break;
		case Types.DECIMAL:
		case Types.NUMERIC:
			if (scale > 0 || length > 18 || appConfig.isForceBigDecimals()) {
				javaType = "BigDecimal";
				isBigDecimal = true;
			} else if (length > 9) {
				javaType = "Long";
			} else if (length > 4) {
				javaType = "Integer";
			} else {
				javaType = "Short";
			}
			break;

		default:
			break;
		}
		return javaType;
	}

	/**
	 * 将数据库字段转换为javabean字段
	 * 
	 * @param code
	 * @return
	 */
	private static String formatTableColumnToClassFiled(String code) {
		try {
			code = code.toLowerCase();
			String[] name = code.split("_");
			String msg = name[0];
			for (int i = 1; i < name.length; i++) {
				msg = msg + name[i].substring(0, 1).toUpperCase() + name[i].substring(1, name[i].length());
			}
			return msg;
		} catch (Exception e) {
			throw new RuntimeException("Create the JavaBeans property name failed, possibly due to the incoming table name error.\r" + e.getMessage());
		}
	}

	/**
	 * 创建toString方法
	 */
	private static void createToString() {
		sb.append("\t//override toString Method \r");
		sb.append("\tpublic String toString() {\r");
		sb.append("\t\tStringBuffer sb = new StringBuffer();\r");
		sb.append("\t\tsb.append(\"{\");\r");
		for (ColumnBeans bean : lists) {
			sb.append("\t\tsb.append(\"" + bean.getName() + "=\"+this.get" + toUpperCaseFirstOne(bean.getName()) + "()+\", \");\r");
		}
		sb.append("\t\tsb.append(\"}\");\r");
		sb.append("\t\treturn sb.toString();\r");
		sb.append("\t}\r");
	}

	/**
	 * 创建getField方法
	 */
	private static void createToFiled() {
		StringBuffer sbb = new StringBuffer();
		for (ColumnBeans bean : lists) {
			sbb.append("\"").append(bean.getName()).append("\",");
		}
		String requ = sbb.toString().substring(0, sbb.toString().length() - 1);
		sb.append("\t//return String[] filed; \r");
		sb.append("\tpublic String[] getField() {\r");
		sb.append("\t\treturn new String[]{" + requ + "};\r");
		sb.append("\t}\r");
	}

	/**
	 * 首字母转大写
	 * 
	 * @param s
	 * @return
	 */
	private static String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
	}
	
	private static boolean isMysql(Connection connection){
		try {
			String connUrl = connection.getMetaData().getURL().toLowerCase();
			return connUrl.startsWith("jdbc:mysql");
		} catch (SQLException e) {
			return false;
		}
    }
    
    private static boolean isOracle(Connection connection){
    	try {
    		String connUrl = connection.getMetaData().getURL().toLowerCase();
    		return connUrl.startsWith("jdbc:oracle");
    	} catch (SQLException e) {
    		return false;
    	}
    }
}
