package de.uol.swp.server.database;

import com.google.inject.Singleton;
import com.mysql.cj.jdbc.MysqlDataSource;
import de.uol.swp.server.ServerApp;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A DataSource for a Mysql JDBC connection
 *
 * @see MysqlDataSource
 */
@Singleton
public class DataBaseConnection {
    private final MysqlDataSource dataSource;

    /**
     * Constructor
     * Sets the database user
     * Sets the database password
     * Sets the database url
     * The variables that are required for the connection to the database are initialized in the constructor
     */

    public DataBaseConnection() {
        final String databaseuser = "root";
        final String databasepassword = "swp_20I";
        final String url = ServerApp.getDataBaseUrl();

        dataSource = new MysqlDataSource();
        dataSource.setUser(databaseuser);
        dataSource.setPassword(databasepassword);
        dataSource.setUrl(url);
        dataSource.setDatabaseName("catan");
    }

    /**
     * Returns a new {@code Connection} to the sql database
     *
     * @return a new {@code Connection} to the sql database
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
