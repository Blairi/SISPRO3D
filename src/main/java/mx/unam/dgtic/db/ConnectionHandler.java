package mx.unam.dgtic.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionHandler {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {

        InputStream input = ConnectionHandler.class.getClassLoader()
                .getResourceAsStream("db.properties");
        Properties props = new Properties();
        try {
            props.load(input);

            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
