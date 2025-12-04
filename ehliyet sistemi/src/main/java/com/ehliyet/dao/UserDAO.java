package com.ehliyet.dao;

import com.ehliyet.database.DatabaseConnection;
import com.ehliyet.models.User;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Yeni kullanıcı ekler
     */
    public boolean addUser(User user, String password) {
        String sql = "INSERT INTO users (tc_no, username, password_hash, full_name, user_type, " +
                "student_no, phone, email, birth_date, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Şifreyi hash'le
            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

            stmt.setString(1, user.getTcNo());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getUserType());
            stmt.setString(6, user.getStudentNo());
            stmt.setString(7, user.getPhone());
            stmt.setString(8, user.getEmail());
            stmt.setDate(9, user.getBirthDate() != null ? Date.valueOf(user.getBirthDate()) : null);
            stmt.setBoolean(10, user.isActive());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
                System.out.println("✅ Kullanıcı eklendi: " + user.getUsername());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı eklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Kullanıcı günceller
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET tc_no=?, full_name=?, user_type=?, student_no=?, " +
                "phone=?, email=?, birth_date=?, is_active=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getTcNo());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getUserType());
            stmt.setString(4, user.getStudentNo());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getEmail());
            stmt.setDate(7, user.getBirthDate() != null ? Date.valueOf(user.getBirthDate()) : null);
            stmt.setBoolean(8, user.isActive());
            stmt.setInt(9, user.getId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✅ Kullanıcı güncellendi: " + user.getUsername());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı güncellenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Şifre değiştirir
     */
    public boolean changePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());

            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✅ Şifre değiştirildi: UserID=" + userId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Şifre değiştirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Kullanıcı siler (soft delete)
     */
    public boolean deleteUser(int userId) {
        String sql = "UPDATE users SET is_active=FALSE WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Kullanıcı silindi: ID=" + userId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı silinirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ID'ye göre kullanıcı getirir
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Username'e göre kullanıcı getirir
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tüm öğrencileri getirir
     */
    public List<User> getAllStudents() {
        String sql = "SELECT * FROM users WHERE user_type='student' AND is_active=TRUE " +
                "ORDER BY full_name";
        return getUsers(sql);
    }

    /**
     * Tüm adminleri getirir
     */
    public List<User> getAllAdmins() {
        String sql = "SELECT * FROM users WHERE user_type='admin' AND is_active=TRUE " +
                "ORDER BY full_name";
        return getUsers(sql);
    }

    /**
     * Toplam kullanıcı sayısı
     */
    public int getTotalUserCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active=TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Kullanıcı sayısı alınırken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Öğrenci sayısı
     */
    public int getStudentCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE user_type='student' AND is_active=TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Öğrenci sayısı alınırken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Helper: SQL query ile kullanıcıları getirir
     */
    private List<User> getUsers(String sql) {
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Kullanıcılar getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Helper: ResultSet'ten User objesi oluşturur
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setTcNo(rs.getString("tc_no"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setUserType(rs.getString("user_type"));
        user.setStudentNo(rs.getString("student_no"));
        user.setPhone(rs.getString("phone"));
        user.setEmail(rs.getString("email"));

        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) {
            user.setBirthDate(birthDate.toLocalDate());
        }

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            user.setCreatedDate(createdTimestamp.toLocalDateTime());
        }

        user.setActive(rs.getBoolean("is_active"));
        return user;
    }
}
