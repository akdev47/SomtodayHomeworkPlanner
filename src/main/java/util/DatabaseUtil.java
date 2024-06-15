package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    public static Connection conn;
    public static String username = "dab_di23242b_168";
    public static String password = "f39egyiyL6ph4m/k";
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            try {
                conn = DriverManager.getConnection(
                        "jdbc:postgresql://bronto.ewi.utwente.nl/" + username + "?currentSchema=topicus6",
                        username, password);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        } catch (ClassNotFoundException e) {
            System.err.println("Error loading driver: " + e);
        }
    }

}
