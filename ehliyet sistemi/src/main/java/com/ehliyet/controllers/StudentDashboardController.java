package com.ehliyet.controllers;

import com.ehliyet.dao.ExamDAO;
import com.ehliyet.dao.ExamResultDAO;
import com.ehliyet.dao.QuestionDAO;
import com.ehliyet.models.Exam;
import com.ehliyet.models.ExamResult;
import com.ehliyet.models.Question;
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
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class StudentDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab examsTab;

    @FXML
    private Tab resultsTab;

    @FXML
    private Tab practiceTab;

    // Dashboard Labels
    @FXML
    private Label nameLabel;

    @FXML
    private Label studentNoLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label completedExamsLabel;

    @FXML
    private Label passedExamsLabel;

    @FXML
    private Label averageScoreLabel;

    @FXML
    private Label availableExamsLabel;

    // Available Exams Table
    @FXML
    private TableView<Exam> availableExamsTable;

    @FXML
    private TableColumn<Exam, String> examCodeCol;

    @FXML
    private TableColumn<Exam, String> examNameCol;

    @FXML
    private TableColumn<Exam, String> examQuestionsCol;

    @FXML
    private TableColumn<Exam, String> examDurationCol;

    @FXML
    private TableColumn<Exam, String> examPassingCol;

    @FXML
    private TableColumn<Exam, String> examStatusCol;

    @FXML
    private TableColumn<Exam, Void> examActionsCol;

    // Results Table
    @FXML
    private TableView<ExamResult> resultsTable;

    @FXML
    private TableColumn<ExamResult, String> resultDateCol;

    @FXML
    private TableColumn<ExamResult, String> resultExamCol;

    @FXML
    private TableColumn<ExamResult, String> resultScoreCol;

    @FXML
    private TableColumn<ExamResult, String> resultCorrectCol;

    @FXML
    private TableColumn<ExamResult, String> resultWrongCol;

    @FXML
    private TableColumn<ExamResult, String> resultEmptyCol;

    @FXML
    private TableColumn<ExamResult, String> resultPassCol;

    @FXML
    private TableColumn<ExamResult, String> resultDurationCol;

    @FXML
    private TableColumn<ExamResult, Void> resultDetailsCol;

    // DAOs
    private ExamDAO examDAO;
    private ExamResultDAO resultDAO;
    private QuestionDAO questionDAO;

    // Current User
    private User currentUser;

    // Data
    private ObservableList<Exam> availableExams;
    private ObservableList<ExamResult> examResults;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("🚀 StudentDashboardController initialize başladı");

        examDAO = new ExamDAO();
        resultDAO = new ExamResultDAO();
        questionDAO = new QuestionDAO();
        availableExams = FXCollections.observableArrayList();
        examResults = FXCollections.observableArrayList();

        setupExamsTableColumns();
        setupResultsTableColumns();

        System.out.println("✅ StudentDashboardController initialize tamamlandı");
    }

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Hoş Geldin, " + user.getFullName() + "!");
        System.out.println("Öğrenci paneli açıldı: " + user.getFullName());

        // Update student info
        Platform.runLater(() -> {
            if (nameLabel != null) nameLabel.setText(user.getFullName());
            if (studentNoLabel != null) studentNoLabel.setText(user.getStudentNo() != null ? user.getStudentNo() : "-");
            if (emailLabel != null) emailLabel.setText(user.getEmail() != null ? user.getEmail() : "-");

            loadDashboardStatistics();
            loadAvailableExams();
            loadResults();
        });
    }

    /**
     * Load dashboard statistics
     */
    private void loadDashboardStatistics() {
        new Thread(() -> {
            try {
                List<ExamResult> results = resultDAO.getResultsByStudentId(currentUser.getId());
                List<Exam> exams = examDAO.getAvailableExamsForStudent();

                long completed = results.stream().filter(r -> "completed".equals(r.getStatus())).count();
                long passed = results.stream().filter(r -> r.isPassed()).count();
                double avgScore = results.stream()
                        .filter(r -> "completed".equals(r.getStatus()))
                        .mapToInt(ExamResult::getScore)
                        .average()
                        .orElse(0.0);

                Platform.runLater(() -> {
                    if (completedExamsLabel != null) completedExamsLabel.setText(String.valueOf(completed));
                    if (passedExamsLabel != null) passedExamsLabel.setText(String.valueOf(passed));
                    if (averageScoreLabel != null) averageScoreLabel.setText(String.format("%.0f", avgScore));
                    if (availableExamsLabel != null) availableExamsLabel.setText(String.valueOf(exams.size()));
                });

            } catch (Exception e) {
                System.err.println("❌ İstatistikler yüklenirken hata: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Setup exams table columns
     */
    private void setupExamsTableColumns() {
        if (examCodeCol != null) {
            examCodeCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getExamCode())
            );
        }

        if (examNameCol != null) {
            examNameCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getExamName())
            );
        }

        if (examQuestionsCol != null) {
            examQuestionsCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getTotalQuestions()))
            );
        }

        if (examDurationCol != null) {
            examDurationCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getDurationMinutes() + " dk")
            );
        }

        if (examPassingCol != null) {
            examPassingCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPassingScore() + " puan")
            );
        }

        if (examStatusCol != null) {
            examStatusCol.setCellValueFactory(cellData -> {
                // TODO: Check if student has already taken this exam
                return new SimpleStringProperty("Girilmedi");
            });
        }

        if (examActionsCol != null) {
            examActionsCol.setCellFactory(param -> new TableCell<>() {
                private final Button startBtn = new Button("🎯 Sınava Gir");

                {
                    startBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                    startBtn.setOnAction(event -> {
                        Exam exam = getTableView().getItems().get(getIndex());
                        startExam(exam);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : startBtn);
                }
            });
        }
    }

    /**
     * Setup results table columns
     */
    private void setupResultsTableColumns() {
        if (resultDateCol != null) {
            resultDateCol.setCellValueFactory(cellData -> {
                if (cellData.getValue().getStartTime() != null) {
                    return new SimpleStringProperty(cellData.getValue().getStartTime().format(dateFormatter));
                }
                return new SimpleStringProperty("-");
            });
        }

        if (resultExamCol != null) {
            resultExamCol.setCellValueFactory(cellData -> {
                if (cellData.getValue().getExam() != null) {
                    return new SimpleStringProperty(cellData.getValue().getExam().getExamName());
                }
                return new SimpleStringProperty("-");
            });
        }

        if (resultScoreCol != null) {
            resultScoreCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getScore()))
            );
        }

        if (resultCorrectCol != null) {
            resultCorrectCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getCorrectAnswers()))
            );
        }

        if (resultWrongCol != null) {
            resultWrongCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getWrongAnswers()))
            );
        }

        if (resultEmptyCol != null) {
            resultEmptyCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getEmptyAnswers()))
            );
        }

        if (resultPassCol != null) {
            resultPassCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().isPassed() ? "✅ Geçti" : "❌ Kaldı")
            );
        }

        if (resultDurationCol != null) {
            resultDurationCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getDurationInMinutes() + " dk")
            );
        }

        if (resultDetailsCol != null) {
            resultDetailsCol.setCellFactory(param -> new TableCell<>() {
                private final Button detailsBtn = new Button("📋 Detay");

                {
                    detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                    detailsBtn.setOnAction(event -> {
                        ExamResult result = getTableView().getItems().get(getIndex());
                        showResultDetails(result);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : detailsBtn);
                }
            });
        }
    }

    /**
     * Load available exams
     */
    private void loadAvailableExams() {
        new Thread(() -> {
            try {
                List<Exam> exams = examDAO.getAvailableExamsForStudent();

                Platform.runLater(() -> {
                    availableExams.setAll(exams);
                    if (availableExamsTable != null) {
                        availableExamsTable.setItems(availableExams);
                    }
                    System.out.println("✅ " + exams.size() + " sınav yüklendi");
                });

            } catch (Exception e) {
                System.err.println("❌ Sınavlar yüklenirken hata: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Load exam results
     */
    private void loadResults() {
        new Thread(() -> {
            try {
                List<ExamResult> results = resultDAO.getResultsByStudentId(currentUser.getId());

                Platform.runLater(() -> {
                    examResults.setAll(results);
                    if (resultsTable != null) {
                        resultsTable.setItems(examResults);
                    }
                    System.out.println("✅ " + results.size() + " sonuç yüklendi");
                });

            } catch (Exception e) {
                System.err.println("❌ Sonuçlar yüklenirken hata: " + e.getMessage());
            }
        }).start();
    }

    // ==================== Quick Actions ====================

    @FXML
    private void openAvailableExams() {
        mainTabPane.getSelectionModel().select(examsTab);
    }

    @FXML
    private void openMyResults() {
        mainTabPane.getSelectionModel().select(resultsTab);
    }

    @FXML
    private void openPracticeMode() {
        mainTabPane.getSelectionModel().select(practiceTab);
    }

    // ==================== Exam Actions ====================

    @FXML
    private void refreshExams() {
        loadAvailableExams();
        loadDashboardStatistics();
    }

    @FXML
    private void refreshResults() {
        loadResults();
        loadDashboardStatistics();
    }

    private void startExam(Exam exam) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sınava Başla");
        alert.setHeaderText(exam.getExamName());
        alert.setContentText("Bu sınava başlamak istediğinize emin misiniz?\n\n" +
                "Soru Sayısı: " + exam.getTotalQuestions() + "\n" +
                "Süre: " + exam.getDurationMinutes() + " dakika\n" +
                "Geçme Puanı: " + exam.getPassingScore() + "\n\n" +
                "Sınav başladıktan sonra geri dönüş yapılamaz!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // TODO: Implement exam taking screen
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Sınav Başlıyor");
            info.setHeaderText(null);
            info.setContentText("Sınav ekranı henüz geliştirme aşamasındadır.\n\n" +
                    "Yakında bu özellik aktif olacaktır!");
            info.showAndWait();
        }
    }

    private void showResultDetails(ExamResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sınav Sonucu Detayı");
        alert.setHeaderText(result.getExam() != null ? result.getExam().getExamName() : "Sınav Sonucu");

        String content = "Tarih: " + (result.getStartTime() != null ? result.getStartTime().format(dateFormatter) : "-") + "\n\n" +
                "Puan: " + result.getScore() + "\n" +
                "Doğru: " + result.getCorrectAnswers() + "\n" +
                "Yanlış: " + result.getWrongAnswers() + "\n" +
                "Boş: " + result.getEmptyAnswers() + "\n\n" +
                "Süre: " + result.getDurationInMinutes() + " dakika\n" +
                "Sonuç: " + (result.isPassed() ? "✅ BAŞARILI" : "❌ BAŞARISIZ");

        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==================== Practice Mode ====================

    @FXML
    private void startTrafficPractice() {
        startPractice("trafik_kuralları", "Trafik Kuralları");
    }

    @FXML
    private void startMotorPractice() {
        startPractice("motor", "Motor ve Araç Tekniği");
    }

    @FXML
    private void startFirstAidPractice() {
        startPractice("ilkyardım", "İlk Yardım");
    }

    @FXML
    private void startSignsPractice() {
        startPractice("işaretler", "Trafik İşaretleri");
    }

    @FXML
    private void startRandomPractice() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Çalışma Modu");
        info.setHeaderText("Rastgele Sorular");
        info.setContentText("Çalışma modu henüz geliştirme aşamasındadır.\n\n" +
                "Yakında bu özellik aktif olacaktır!");
        info.showAndWait();
    }

    private void startPractice(String category, String categoryName) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Çalışma Modu");
        info.setHeaderText(categoryName + " Pratik");
        info.setContentText("Çalışma modu henüz geliştirme aşamasındadır.\n\n" +
                "Yakında bu özellik aktif olacaktır!");
        info.showAndWait();
    }

    // ==================== Logout ====================

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Çıkış");
        alert.setHeaderText("Çıkış yapmak istediğinize emin misiniz?");

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

                System.out.println("👋 Öğrenci çıkış yaptı: " + currentUser.getFullName());

            } catch (Exception e) {
                System.err.println("❌ Çıkış yapılırken hata: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}