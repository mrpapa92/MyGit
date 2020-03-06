package watering;

import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

public class DBConnection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(DBConnection.class);
	public Connection conn = null;

	public Connection createDBConnection() throws SQLException {
		String url = "jdbc:mariadb://localhost:3306/";
		String dbName = "wateringdb";
		String driver = "org.mariadb.jdbc.Driver";
		String userName = "root";
		String password = "hondaglx125";
		PoolProperties p = new PoolProperties();
		p.setUrl(url + dbName);
		p.setDriverClassName(driver);
		p.setPassword(password);
		p.setUsername(userName);
		p.setMaxActive(100);
		DataSource datasource = new DataSource();
		datasource.setPoolProperties(p);
		conn = datasource.getConnection();
		log.info("Connected to the database");
		return conn;
	}

	public void closeConnection(Connection conn) throws SQLException {
		conn.close();
		log.info("Disconnected from the database");
	}
}