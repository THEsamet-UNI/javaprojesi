package com.ehliyet.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/ehliyet_sistemi?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    private static Connection connection = null;

    /**
     * Veritabanı bağlantısını getirir
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Veritabanı bağlantısı başarılı!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ JDBC Driver bulunamadı!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Veritabanı bağlantı hatası!");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Bağlantıyı kapatır
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("✅ Veritabanı bağlantısı kapatıldı.");
                }
                connection = null;
            } catch (SQLException e) {
                System.err.println("❌ Bağlantı kapatma hatası!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Bağlantı testi
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}