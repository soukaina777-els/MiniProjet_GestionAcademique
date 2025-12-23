package exrc_miniprojet.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gestion_academique?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "soukaina2004@elouansaidi";

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
