package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Database configurations based on user input
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
    private static final String USER = "system";
    private static final String PASSWORD = "root";

    /**
     * Optional static block to ensure the driver is loaded.
     * With newer JDBC drivers it's usually automatic, but good to have for older/specific versions.
     */
    static {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found. Ensure ojdbc11.jar is in the classpath.");
            e.printStackTrace();
        }
    }

    /**
     * Get a connection to the Oracle Database.
     * @return Connection object
     * @throws SQLException Context-specific SQL errors
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /**
     * Test logic to verify connection.
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Conecction successful to Oracle Database!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
