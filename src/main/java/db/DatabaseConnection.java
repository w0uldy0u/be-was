package db;

import java.sql.*;

public class DatabaseConnection {
    private static final String JDBC_URL = "jdbc:h2:./data/was_db;AUTO_SERVER=TRUE";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";

    static {
        try {
            Class.forName("org.h2.Driver");
            initializeTables();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    private static void initializeTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    profile_image VARCHAR(255)
                )
            """);

            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image VARCHAR(255)");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sessions (
                    sid VARCHAR(255) PRIMARY KEY,
                    user_id VARCHAR(255) NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS articles (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    author_id VARCHAR(255) NOT NULL,
                    content TEXT NOT NULL,
                    image VARCHAR(255),
                    FOREIGN KEY (author_id) REFERENCES users(user_id)
                )
            """);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database tables", e);
        }
    }
}
