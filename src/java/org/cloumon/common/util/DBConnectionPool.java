package org.cloumon.common.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

public class DBConnectionPool {
  static final String DBCP_DRIVER = "jdbc:apache:commons:dbcp:";
  
  public static void setupPool(String dbName, String driverClassName, 
      String connectURI, Properties prop) throws IOException {
    try {
      Class.forName(driverClassName);

      ObjectPool connectionPool = new GenericObjectPool(null);
      ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, prop);
      PoolableConnectionFactory poolableConnectionFactory = 
        new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
      
      Class.forName("org.apache.commons.dbcp.PoolingDriver");
      PoolingDriver driver = (PoolingDriver) DriverManager.getDriver(DBCP_DRIVER);
      driver.registerPool(dbName, connectionPool);
    } catch (Exception e) {
      throw new IOException(e.getMessage(), e);
    }
  }
  
  public static Connection getConnection(String dbName) throws IOException {
    try {
      return DriverManager.getConnection(DBCP_DRIVER + dbName);
    } catch (SQLException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  public static void close(Connection conn, PreparedStatement stmt) {
    try {
      if (stmt != null) {
        stmt.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      if (conn != null) {
        conn.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}