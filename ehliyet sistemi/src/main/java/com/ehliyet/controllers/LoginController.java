package com.ehliyet.controllers;

import com.ehliyet.database.DatabaseConnection;
import com.ehliyet.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private RadioButton studentRadio;

    @FXML
    private RadioButton adminRadio;

    @FXML
    private Label messageLabel;

    @FXML
    private Button loginButton;

    private ToggleGroup userTypeGroup;

    @FXML
    public void initialize() {
        userTypeGroup = new ToggleGroup();
        studentRadio.setToggleGroup(userTypeGroup);
        adminRadio.setToggleGroup(userTypeGroup);
        studentRadio.setSelected(true);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Kullanıcı adı ve şifre boş olamaz!");
            return;
        }

        RadioButton selectedRadio = (RadioButton) userTypeGroup.getSelectedToggle();
        String selectedUserType = selectedRadio.getText().toLowerCase();

        if (selectedUserType.equals("öğrenci")) {
            selectedUserType = "student";
        } else if (selectedUserType.equals("yönetici")) {
            selectedUserType = "admin";
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND user_type = ? AND is_active = TRUE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, selectedUserType);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                // BCrypt doğrulama
                if (storedHash != null && storedHash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$")) {
                    BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash);
                    if (result.verified) {
                        loginSuccess(rs);
                    } else {
                        showError("Hatalı şifre!");
                    }
                } else {
                    showError("Geçersiz şifre formatı! Lütfen yöneticinizle iletişime geçin.");
                }
            } else {
                showError("Kullanıcı bulunamadı!");
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Giriş hatası: " + e.getMessage());
        }
    }

    private void loginSuccess(ResultSet rs) {
        try {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setFullName(rs.getString("full_name"));
            user.setUserType(rs.getString("user_type"));
            user.setEmail(rs.getString("email"));

            if (user.isAdmin()) {
                openAdminDashboard(user);
            } else {
                openStudentDashboard(user);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Kullanıcı bilgileri alınamadı!");
        }
    }

    private void openAdminDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Admin Paneli - " + user.getFullName());
            stage.setScene(new Scene(root, 1200, 800));
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Admin paneli açılamadı!");
        }
    }

    private void openStudentDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student_dashboard.fxml"));
            Parent root = loader.load();

            StudentDashboardController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Öğrenci Paneli - " + user.getFullName());
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Öğrenci paneli açılamadı!");
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red;");
    }
}