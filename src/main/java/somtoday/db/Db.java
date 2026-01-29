package somtoday.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    private Db() {}

    private static String requireEnv(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing required env var: " + key);
        }
        return v;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC driver not found in WEB-INF/lib", e);
        }

        String host = requireEnv("DB_HOST");
        String port = requireEnv("DB_PORT");
        String name = requireEnv("DB_NAME");
        String user = requireEnv("DB_USER");
        String pass = requireEnv("DB_PASSWORD");

        String url =
                "jdbc:postgresql://" + host + ":" + port + "/" + name +
                        "?sslmode=require";

        return DriverManager.getConnection(url, user, pass);
    }
}
