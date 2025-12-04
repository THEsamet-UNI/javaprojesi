package com.ehliyet.controllers;

import com.ehliyet.dao.ExamDAO;
import com.ehliyet.dao.ExamResultDAO;
import com.ehliyet.models.Exam;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExamManagementController {

    // Filter Controls
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TextField searchField;
    @FXML private Label totalExamsLabel;

    // Table
    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, String> idCol;
    @FXML private TableColumn<Exam, String> examCodeCol;
    @FXML private TableColumn<Exam, String> examNameCol;
    @FXML private TableColumn<Exam, String> totalQuestionsCol;
    @FXML private TableColumn<Exam, String> durationCol;
    @FXML private TableColumn<Exam, String> passingScoreCol;
    @FXML private TableColumn<Exam, String> startDateCol;
    @FXML private TableColumn<Exam, String> endDateCol;
    @FXML private TableColumn<Exam, String> statusCol;
    @FXML private TableColumn<Exam, Void> actionsCol;

    // Statistics
    @FXML private Label activeExamsCount;
    @FXML private Label scheduledExamsCount;
    @FXML private Label completedExamsCount;
    @FXML private Label averageSuccessRate;

    // DAOs
    private ExamDAO examDAO;
    private ExamResultDAO resultDAO;

    // Data
    private ObservableList<Exam> allExams;
    private ObservableList<Exam> filteredExams;

    // Current Admin
    private User currentAdmin;

    // Date Formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("🚀 ExamManagementController initialize başladı");

        examDAO = new ExamDAO();
        resultDAO = new ExamResultDAO();
        allExams = FXCollections.observableArrayList();
        filteredExams = FXCollections.observableArrayList();

        setupFilterComboBox();
        setupTableColumns();
        loadExams();

        System.out.println("✅ ExamManagementController initialize tamamlandı");
    }

    /**
     * Set current admin user
     */
    public void setCurrentAdmin(User admin) {
        this.currentAdmin = admin;
        System.out.println("✅ ExamManagement'a admin set edildi: " + (admin != null ? admin.getFullName() : "null"));
    }

    /**
     * Setup filter combo box
     */
    private void setupFilterComboBox() {
        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "Tümü",
                "Aktif",
                "Planlanan",
                "Sona Ermiş"
        ));
        statusFilterCombo.getSelectionModel().selectFirst();
    }

    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        // ID Column
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId()))
        );

        // Exam Code Column
        examCodeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getExamCode())
        );

        // Exam Name Column
        examNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getExamName())
        );

        // Total Questions Column
        totalQuestionsCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getTotalQuestions()))
        );

        // Duration Column
        durationCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDurationMinutes() + " dk")
        );

        // Passing Score Column
        passingScoreCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPassingScore() + " puan")
        );

        // Start Date Column
        startDateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getStartDate() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getStartDate().format(dateFormatter)
                );
            }
            return new SimpleStringProperty("-");
        });

        // End Date Column
        endDateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEndDate() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getEndDate().format(dateFormatter)
                );
            }
            return new SimpleStringProperty("-");
        });

        // Status Column
        statusCol.setCellValueFactory(cellData -> {
            Exam exam = cellData.getValue();
            String status;
            if (!exam.isActive()) {
                status = "❌ Pasif";
            } else if (exam.isAvailable()) {
                status = "✅ Aktif";
            } else {
                status = "🕐 Planlı";
            }
            return new SimpleStringProperty(status);
        });

        // Actions Column
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");
            private final Button questionsBtn = new Button("📋");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox actionBox = new HBox(5, viewBtn, editBtn, questionsBtn, deleteBtn);

            {
                actionBox.setAlignment(Pos.CENTER);

                viewBtn.setTooltip(new Tooltip("Detayları Gör"));
                editBtn.setTooltip(new Tooltip("Düzenle"));
                questionsBtn.setTooltip(new Tooltip("Soruları Yönet"));
                deleteBtn.setTooltip(new Tooltip("Sil"));

                viewBtn.setOnAction(event -> {
                    Exam exam = getTableView().getItems().get(getIndex());
                    viewExam(exam);
                });

                editBtn.setOnAction(event -> {
                    Exam exam = getTableView().getItems().get(getIndex());
                    editExam(exam);
                });

                questionsBtn.setOnAction(event -> {
                    Exam exam = getTableView().getItems().get(getIndex());
                    manageExamQuestions(exam);
                });

                deleteBtn.setOnAction(event -> {
                    Exam exam = getTableView().getItems().get(getIndex());
                    deleteExam(exam);
                });

                // Styling
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand;");
                questionsBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    /**
     * Load all exams
     */
    private void loadExams() {
        new Thread(() -> {
            try {
                List<Exam> exams = examDAO.getAllActiveExams();

                Platform.runLater(() -> {
                    allExams.setAll(exams);
                    applyFilters();
                    updateStatistics();
                    System.out.println("✅ " + exams.size() + " sınav yüklendi");
                });

            } catch (Exception e) {
                System.err.println("❌ Sınavlar yüklenirken hata: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Hata");
                    alert.setHeaderText("Sınavlar yüklenemedi");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    /**
     * Apply filters
     */
    @FXML
    private void applyFilters() {
        String statusFilter = statusFilterCombo.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        filteredExams.clear();

        List<Exam> filtered = allExams.stream()
                .filter(exam -> {
                    // Status filter
                    if (!statusFilter.equals("Tümü")) {
                        boolean matches = switch (statusFilter) {
                            case "Aktif" -> exam.isActive() && exam.isAvailable();
                            case "Planlanan" -> exam.isActive() && !exam.isAvailable();
                            case "Sona Ermiş" -> !exam.isActive();
                            default -> true;
                        };
                        if (!matches) return false;
                    }

                    // Search text filter
                    if (!searchText.isEmpty()) {
                        return exam.getExamName().toLowerCase().contains(searchText) ||
                                exam.getExamCode().toLowerCase().contains(searchText);
                    }

                    return true;
                })
                .collect(Collectors.toList());

        filteredExams.addAll(filtered);
        examsTable.setItems(filteredExams);
        totalExamsLabel.setText("Toplam: " + filteredExams.size() + " sınav");
    }

    /**
     * Update statistics
     */
    private void updateStatistics() {
        long active = allExams.stream()
                .filter(e -> e.isActive() && e.isAvailable())
                .count();

        long scheduled = allExams.stream()
                .filter(e -> e.isActive() && !e.isAvailable())
                .count();

        long completed = allExams.stream()
                .filter(e -> !e.isActive())
                .count();

        activeExamsCount.setText(String.valueOf(active));
        scheduledExamsCount.setText(String.valueOf(scheduled));
        completedExamsCount.setText(String.valueOf(completed));

        // Calculate average success rate
        // TODO: Implement when we have exam results
        averageSuccessRate.setText("-%");
    }

    /**
     * Refresh table
     */
    @FXML
    private void refreshTable() {
        loadExams();
    }

    /**
     * Show create exam dialog
     */
    @FXML
    private void showCreateExamDialog() {
        showExamDialog(null);
    }

    /**
     * View exam details
     */
    private void viewExam(Exam exam) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sınav Detayı");
        alert.setHeaderText(exam.getExamName());

        String content = "Sınav Kodu: " + exam.getExamCode() + "\n" +
                "Toplam Soru: " + exam.getTotalQuestions() + "\n" +
                "Süre: " + exam.getDurationMinutes() + " dakika\n" +
                "Geçme Puanı: " + exam.getPassingScore() + "\n" +
                "Açıklama: " + (exam.getDescription() != null ? exam.getDescription() : "-") + "\n" +
                "Durum: " + (exam.isAvailable() ? "Aktif" : "Planlanmış");

        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Edit exam
     */
    private void editExam(Exam exam) {
        showExamDialog(exam);
    }

    /**
     * Manage exam questions
     */
    private void manageExamQuestions(Exam exam) {
        // TODO: Open exam questions management dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Soru Yönetimi");
        alert.setHeaderText(exam.getExamName());
        alert.setContentText("Soru yönetimi özelliği geliştirilmekte...");
        alert.showAndWait();
    }

    /**
     * Delete exam
     */
    private void deleteExam(Exam exam) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sınav Sil");
        alert.setHeaderText("Bu sınavı silmek istediğinize emin misiniz?");
        alert.setContentText("Sınav: " + exam.getExamName() + "\nKod: " + exam.getExamCode());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (examDAO.deleteExam(exam.getId())) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Başarılı");
                success.setHeaderText(null);
                success.setContentText("Sınav başarıyla silindi!");
                success.showAndWait();

                loadExams();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Hata");
                error.setHeaderText("Sınav silinemedi");
                error.setContentText("Bir hata oluştu. Lütfen tekrar deneyin.");
                error.showAndWait();
            }
        }
    }

    /**
     * Show exam dialog (add or edit)
     */
    private void showExamDialog(Exam exam) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exam_dialog.fxml"));
            Parent root = loader.load();

            ExamDialogController controller = loader.getController();

            if (currentAdmin != null) {
                controller.setCurrentAdmin(currentAdmin);
            }

            if (exam != null) {
                controller.setExam(exam);
            }
            controller.setParentController(this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(exam == null ? "Yeni Sınav Oluştur" : "Sınav Düzenle");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            System.err.println("❌ Sınav dialog açılamadı: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Callback when exam is saved
     */
    public void onExamSaved() {
        loadExams();
    }
}
