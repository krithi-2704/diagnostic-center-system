package com.medicare.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Connects to your active local SQL Server instance (SQLEXPRESS) with medicare_user
    private static final String URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=DiagnosticDB;user=medicare_user;password=MediCare@2026;encrypt=false;trustServerCertificate=true;";
    private static final String FALLBACK_URL = "jdbc:sqlserver://127.0.0.1;instanceName=SQLEXPRESS;databaseName=DiagnosticDB;user=medicare_user;password=MediCare@2026;encrypt=false;trustServerCertificate=true;";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Microsoft JDBC Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            return DriverManager.getConnection(FALLBACK_URL);
        }
    }
}