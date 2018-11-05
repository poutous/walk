package org.walkframework.batis.tools.dbtobean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;


public class ConnectionTools {
	/**
	 * 获取连接
	 * @param driverName
	 * @param url
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection(String driverClassName, String url, String user, String password) throws Exception {
		Properties props = new Properties();
		props.put("user", user);
		props.put("password", password);
		try {
			Class.forName(driverClassName);
			if(driverClassName.indexOf("oracle") != -1){
				//备注
				//props.put("remarksReporting", "true");
				props.setProperty("remarks", "true");
			}
			return DriverManager.getConnection(url, props);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 关闭连接
	 * @param rs
	 * @param pre
	 * @param conn
	 * @throws SQLException
	 */
	public static void closeConnection(ResultSet rs, PreparedStatement pre, Connection conn) throws SQLException {
		if (rs != null) {
			rs.close();
		}
		if (pre != null) {
			pre.close();
		}
		if ((conn != null) && (!conn.isClosed()))
			conn.close();
	}

}
