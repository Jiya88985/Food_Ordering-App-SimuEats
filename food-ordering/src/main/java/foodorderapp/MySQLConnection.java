package foodorderapp;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySQLConnection {
    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        try (InputStream input = MySQLConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new SQLException("Unable to find db.properties");
            }
            props.load(input);
        } catch (IOException e) {
            throw new SQLException("Failed to load database properties", e);
        }
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
