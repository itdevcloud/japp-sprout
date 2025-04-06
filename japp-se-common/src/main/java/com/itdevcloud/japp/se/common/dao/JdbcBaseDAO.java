package com.itdevcloud.japp.se.common.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.util.StringUtil;
import com.itdevcloud.japp.se.common.vo.AttributeVO;
import com.itdevcloud.japp.se.common.vo.BaseVO;

public abstract class JdbcBaseDAO {
	private static final Logger logger = Logger.getLogger(JdbcBaseDAO.class.getName());
	//private static final IaaConfigManager configManager = IaaConfigManager.getIntance();

	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	private String dbURL;
	private String dbUser;
	private String dbPassword;

	public void jdbcInit(String dbURL, String dbUser, String dbPassword) {
		logger.info("jdbcInit() ... ...");
		this.dbURL = dbURL;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
	}

	protected Connection getConnection() {

		try {
			logger.finer("getConnection() from jdbc driver... dbUser=" + this.dbUser + ", dbUrl=" + this.dbURL);
			Properties prop = new Properties();
			prop.put("user", this.dbUser);
			prop.put("password", this.dbPassword);
			Connection conn = DriverManager.getConnection(this.dbURL, prop);
			logger.finer("getConnection() from jdbc driver end. ");
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	protected void closeConnection(Connection conn, PreparedStatement stmt, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			logger.warning(e.getMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.warning(e.getMessage());
				}
			}

		}
	}

	
	protected void setVoDatesFromResultSet(ResultSet rs, int startPosition, BaseVO vo) {
		try {
			java.sql.Timestamp tmpTS = rs.getTimestamp(startPosition);
			vo.setEffectiveDate(tmpTS == null ? null : new Date(tmpTS.getTime()));
			tmpTS = rs.getTimestamp(startPosition+1);
			vo.setExpiryDate(tmpTS == null ? null : new Date(tmpTS.getTime()));
			tmpTS = rs.getTimestamp(startPosition+2);
			vo.setCreatedDate(tmpTS == null ? null : new Date(tmpTS.getTime()));
			vo.setCreatedBy(rs.getString(startPosition+3));
			tmpTS = rs.getTimestamp(startPosition+4);
			vo.setUpdatedDate(tmpTS == null ? null : new Date(tmpTS.getTime()));
			vo.setUpdatedBy(rs.getString(startPosition+5));
		} catch (Throwable t) {
			logger.severe("Fail to setVoDatesFromResultSet() , exception " + t);
			throw new RuntimeException(t);
		} 

	}
	protected void setIaaAttributeFromResultSet(ResultSet rs, int startPosition, long mainEntityId, AttributeVO vo) {
		try {
			vo.setMainEntityId(mainEntityId);
			vo.setPk(rs.getLong(startPosition));
			vo.setTypeId(rs.getLong(startPosition+1));
			vo.setTypeCode(rs.getString(startPosition+2));
			vo.setSequence(rs.getInt(startPosition+3));
			vo.setValue(rs.getString(startPosition+4));
			vo.setDisplayOrder(rs.getInt(startPosition+5));
			vo.setDescription(rs.getString(startPosition+6));
			String str = rs.getString(startPosition+7);
			if(StringUtil.isEmptyOrNull(str) || !str.trim().equalsIgnoreCase("Y")) {
				vo.setRequireUnique(false);
			}else {
				vo.setRequireUnique(true);
			}
			
			setVoDatesFromResultSet(rs, startPosition+8, vo);
			
		} catch (Throwable t) {
			logger.severe("Fail to setIaaAttributeFromResultSet() , exception " + t);
			throw new RuntimeException(t);
		} 

	}

	
}
