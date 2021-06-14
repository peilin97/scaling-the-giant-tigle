import com.zaxxer.hikari.HikariDataSource;

public class DataSource {

  // NEVER store sensitive information below in plain text!
  private static final String HOST_NAME = System.getProperty("MySQL_IP_ADDRESS");
  private static final String PORT = System.getProperty("MySQL_PORT");
  private static final String DATABASE = "supermarket";
  private static final String USERNAME = System.getProperty("DB_USERNAME");
  private static final String PASSWORD = System.getProperty("DB_PASSWORD");
  private static HikariDataSource ds;

  static {
    ds = new HikariDataSource();
    String url = String
        .format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
    ds.setJdbcUrl(url);
    ds.setUsername(USERNAME);
    ds.setPassword(PASSWORD);
    ds.addDataSourceProperty("cachePrepStmts", "true");
    ds.addDataSourceProperty("prepStmtCacheSize", "250");
    ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
  }

  public static HikariDataSource getDataSource() {
    return ds;
  }
}
