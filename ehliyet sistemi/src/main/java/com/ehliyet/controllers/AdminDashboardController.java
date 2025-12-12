package com.ehliyet.controllers;

import com.ehliyet.dao.ExamDAO;
import com.ehliyet.dao.QuestionDAO;
import com.ehliyet.dao.UserDAO;
import com.ehliyet.models.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab questionTab;

    @FXML
    private Tab examTab;

    @FXML
    private Tab studentTab;

    @FXML
    private Tab reportsTab;

    // Dashboard Statistics Labels
    @FXML
    private Label totalStudentsLabel;

    @FXML
    private Label totalQuestionsLabel;

    @FXML
    private Label totalExamsLabel;

    @FXML
    private Label completedExamsLabel;

    @FXML
    private Label systemInfoLabel;

    @FXML
    private Label dbStatusLabel;

    @FXML
    private Label lastUpdateLabel;

    // Student Management
    @FXML
    private TextField studentSearchField;

    @FXML
    private Label studentCountLabel;

    @FXML
    private TableView<User> studentsTable;

    @FXML
    private TableColumn<User, String> studentIdCol;

    @FXML
    private TableColumn<User, String> studentNoCol;

    @FXML
    private TableColumn<User, String> studentNameCol;

    @FXML
    private TableColumn<User, String> studentTcCol;

    @FXML
    private TableColumn<User, String> studentEmailCol;

    @FXML
    private TableColumn<User, String> studentPhoneCol;

    @FXML
    private TableColumn<User, String> studentStatusCol;

    @FXML
    private TableColumn<User, Void> studentActionsCol;

    // Nested Controllers
    @FXML
    private QuestionManagementController questionManagementController;

    @FXML
    private ExamManagementController examManagementController;

    // Reports
    @FXML
    private TableView<?> recentResultsTable;

    // DAOs
    private UserDAO userDAO;
    private QuestionDAO questionDAO;
    private ExamDAO examDAO;

    // Current User
    private User currentUser;

    // Student list
    private ObservableList<User> allStudents;

    @FXML
    public void initialize() {
        System.out.println("🚀 AdminDashboardController initialize başladı");

        userDAO = new UserDAO();
        questionDAO = new QuestionDAO();
        examDAO = new ExamDAO();
        allStudents = FXCollections.observableArrayList();

        // Setup student table columns
        setupStudentTableColumns();

        System.out.println("✅ AdminDashboardController initialize tamamlandı");
    }

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Hoş Geldin, " + user.getFullName() + "!");
        System.out.println("Admin paneli açıldı: " + user.getFullName());

        // Pass admin to nested controllers
        if (questionManagementController != null) {
            questionManagementController.setCurrentAdmin(user);
        }
        if (examManagementController != null) {
            examManagementController.setCurrentAdmin(user);
        }

        // Load initial data
        Platform.runLater(() -> {
            loadDashboardStatistics();
            loadStudents();
        });
    }

    /**
     * Load dashboard statistics
     */
    private void loadDashboardStatistics() {
        new Thread(() -> {
            try {
                int studentCount = userDAO.getStudentCount();
                int questionCount = questionDAO.getTotalQuestionCount();
                int examCount = examDAO.getAllActiveExams().size();

                Platform.runLater(() -> {
                    if (totalStudentsLabel != null) {
                        totalStudentsLabel.setText(String.valueOf(studentCount));
                    }
                    if (totalQuestionsLabel != null) {
                        totalQuestionsLabel.setText(String.valueOf(questionCount));
                    }
                    if (totalExamsLabel != null) {
                        totalExamsLabel.setText(String.valueOf(examCount));
                    }
                    if (completedExamsLabel != null) {
                        completedExamsLabel.setText("0"); // TODO: Implement completed exam count
                    }
                    if (dbStatusLabel != null) {
                        dbStatusLabel.setText("Veritabanı Durumu: Bağlı ✅");
                    }
                    if (lastUpdateLabel != null) {
                        lastUpdateLabel.setText("Son Güncelleme: " +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (dbStatusLabel != null) {
                        dbStatusLabel.setText("Veritabanı Durumu: Bağlantı Hatası ❌");
                    }
                });
                System.err.println("❌ İstatistikler yüklenirken hata: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Setup student table columns
     */
    private void setupStudentTableColumns() {
        if (studentIdCol != null) {
            studentIdCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getId()))
            );
        }

        if (studentNoCol != null) {
            studentNoCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStudentNo() != null ?
                            cellData.getValue().getStudentNo() : "-")
            );
        }

        if (studentNameCol != null) {
            studentNameCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFullName())
            );
        }

        if (studentTcCol != null) {
            studentTcCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getTcNo() != null ?
                            cellData.getValue().getTcNo() : "-")
            );
        }

        if (studentEmailCol != null) {
            studentEmailCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getEmail() != null ?
                            cellData.getValue().getEmail() : "-")
            );
        }

        if (studentPhoneCol != null) {
            studentPhoneCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPhone() != null ?
                            cellData.getValue().getPhone() : "-")
            );
        }

        if (studentStatusCol != null) {
            studentStatusCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().isActive() ? "✅ Aktif" : "❌ Pasif")
            );
        }

        if (studentActionsCol != null) {
            studentActionsCol.setCellFactory(param -> new TableCell<>() {
                private final Button editBtn = new Button("✏️");
                private final Button deleteBtn = new Button("🗑️");
                private final HBox actionBox = new HBox(5, editBtn, deleteBtn);

                {
                    actionBox.setAlignment(Pos.CENTER);

                    editBtn.setOnAction(event -> {
                        User student = getTableView().getItems().get(getIndex());
                        editStudent(student);
                    });

                    deleteBtn.setOnAction(event -> {
                        User student = getTableView().getItems().get(getIndex());
                        deleteStudent(student);
                    });

                    editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
                    deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : actionBox);
                }
            });
        }
    }

    /**
     * Load students
     */
    private void loadStudents() {
        new Thread(() -> {
            try {
                List<User> students = userDAO.getAllStudents();

                Platform.runLater(() -> {
                    allStudents.setAll(students);
                    if (studentsTable != null) {
                        studentsTable.setItems(allStudents);
                    }
                    if (studentCountLabel != null) {
                        studentCountLabel.setText("Toplam: " + students.size() + " öğrenci");
                    }
                    System.out.println("✅ " + students.size() + " öğrenci yüklendi");
                });

            } catch (Exception e) {
                System.err.println("❌ Öğrenciler yüklenirken hata: " + e.getMessage());
            }
        }).start();
    }

    // ==================== Quick Actions ====================

    @FXML
    private void openQuestionManagement() {
        mainTabPane.getSelectionModel().select(questionTab);
    }

    @FXML
    private void openExamManagement() {
        mainTabPane.getSelectionModel().select(examTab);
    }

    @FXML
    private void openStudentManagement() {
        mainTabPane.getSelectionModel().select(studentTab);
    }

    @FXML
    private void openReports() {
        mainTabPane.getSelectionModel().select(reportsTab);
    }

    // ==================== Student Management ====================

    @FXML
    private void showAddStudentDialog() {
        showStudentDialog(null);
    }

    @FXML
    private void refreshStudents() {
        loadStudents();
        loadDashboardStatistics();
    }

    private void editStudent(User student) {
        showStudentDialog(student);
    }

    private void deleteStudent(User student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Öğrenci Sil");
        alert.setHeaderText("Bu öğrenciyi silmek istediğinize emin misiniz?");
        alert.setContentText("Öğrenci: " + student.getFullName() + "\nNo: " + student.getStudentNo());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.deleteUser(student.getId())) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Başarılı");
                success.setHeaderText(null);
                success.setContentText("Öğrenci başarıyla silindi!");
                success.showAndWait();
                loadStudents();
                loadDashboardStatistics();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Hata");
                error.setHeaderText("Öğrenci silinemedi");
                error.setContentText("Bir hata oluştu. Lütfen tekrar deneyin.");
                error.showAndWait();
            }
        }
    }

    private void showStudentDialog(User student) {
        // Create a dialog for adding/editing students
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(student == null ? "Yeni Öğrenci Ekle" : "Öğrenci Düzenle");
        dialog.setHeaderText(student == null ? "Yeni öğrenci bilgilerini girin" : "Öğrenci bilgilerini düzenleyin");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Kullanıcı adı");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Ad Soyad");
        TextField tcNoField = new TextField();
        tcNoField.setPromptText("TC Kimlik No");
        TextField studentNoField = new TextField();
        studentNoField.setPromptText("Öğrenci No");
        TextField emailField = new TextField();
        emailField.setPromptText("E-posta");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Telefon");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Şifre");

        if (student != null) {
            usernameField.setText(student.getUsername());
            usernameField.setDisable(true);
            fullNameField.setText(student.getFullName());
            tcNoField.setText(student.getTcNo());
            studentNoField.setText(student.getStudentNo());
            emailField.setText(student.getEmail());
            phoneField.setText(student.getPhone());
        }

        grid.add(new Label("Kullanıcı Adı:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Ad Soyad:"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("TC Kimlik No:"), 0, 2);
        grid.add(tcNoField, 1, 2);
        grid.add(new Label("Öğrenci No:"), 0, 3);
        grid.add(studentNoField, 1, 3);
        grid.add(new Label("E-posta:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Telefon:"), 0, 5);
        grid.add(phoneField, 1, 5);
        if (student == null) {
            grid.add(new Label("Şifre:"), 0, 6);
            grid.add(passwordField, 1, 6);
        }

        dialog.getDialogPane().setContent(grid);

        // Handle result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                User newStudent = student != null ? student : new User();
                newStudent.setUsername(usernameField.getText().trim());
                newStudent.setFullName(fullNameField.getText().trim());
                newStudent.setTcNo(tcNoField.getText().trim());
                newStudent.setStudentNo(studentNoField.getText().trim());
                newStudent.setEmail(emailField.getText().trim());
                newStudent.setPhone(phoneField.getText().trim());
                newStudent.setUserType("student");
                newStudent.setActive(true);

                if (student == null) {
                    // New student
                    String password = passwordField.getText();
                    if (password.isEmpty()) {
                        password = "123456"; // Default password
                    }
                    if (userDAO.addUser(newStudent, password)) {
                        return newStudent;
                    }
                } else {
                    // Update existing student
                    if (userDAO.updateUser(newStudent)) {
                        return newStudent;
                    }
                }
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(u -> {
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Başarılı");
            success.setHeaderText(null);
            success.setContentText("Öğrenci başarıyla kaydedildi!");
            success.showAndWait();
            loadStudents();
            loadDashboardStatistics();
        });
    }

    // ==================== Reports ====================

    @FXML
    private void showExamResultsReport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sınav Sonuçları");
        alert.setHeaderText("Sınav Sonuçları Raporu");
        alert.setContentText("Bu özellik henüz geliştirme aşamasındadır.");
        alert.showAndWait();
    }

    @FXML
    private void showStudentPerformanceReport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Öğrenci Performansı");
        alert.setHeaderText("Öğrenci Performans Raporu");
        alert.setContentText("Bu özellik henüz geliştirme aşamasındadır.");
        alert.showAndWait();
    }

    @FXML
    private void showQuestionAnalysisReport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Soru Analizi");
        alert.setHeaderText("Soru Analiz Raporu");
        alert.setContentText("Bu özellik henüz geliştirme aşamasındadır.");
        alert.showAndWait();
    }

    // ==================== Logout ====================

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Çıkış");
        alert.setHeaderText("Çıkış yapmak istediğinize emin misiniz?");
        alert.setContentText("Kaydedilmemiş değişiklikler kaybolabilir.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Open login screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();

                Stage loginStage = new Stage();
                loginStage.setTitle("Ehliyet Sınav Sistemi - Giriş");
                loginStage.setScene(new Scene(root, 500, 600));
                loginStage.setResizable(false);
                loginStage.show();

                // Close current window
                Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
                currentStage.close();

                System.out.println("👋 Admin çıkış yaptı: " + currentUser.getFullName());

            } catch (Exception e) {
                System.err.println("❌ Çıkış yapılırken hata: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}