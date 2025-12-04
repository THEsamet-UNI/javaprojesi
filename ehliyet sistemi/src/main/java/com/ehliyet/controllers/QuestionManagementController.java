package com.ehliyet.controllers;

import com.ehliyet.dao.QuestionDAO;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuestionManagementController {

    // Filter Controls
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private ComboBox<String> difficultyFilterCombo;
    @FXML private TextField searchField;
    @FXML private Label totalQuestionsLabel;

    // Table
    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, String> idCol;
    @FXML private TableColumn<Question, String> questionTextCol;
    @FXML private TableColumn<Question, String> categoryCol;
    @FXML private TableColumn<Question, String> difficultyCol;
    @FXML private TableColumn<Question, String> pointsCol;
    @FXML private TableColumn<Question, String> correctAnswerCol;
    @FXML private TableColumn<Question, String> hasImageCol;
    @FXML private TableColumn<Question, Void> actionsCol;

    // Statistics
    @FXML private Label trafficRulesCount;
    @FXML private Label signsCount;
    @FXML private Label firstAidCount;
    @FXML private Label motorCount;

    // DAO
    private QuestionDAO questionDAO;

    // All questions (for filtering)
    private ObservableList<Question> allQuestions;
    private ObservableList<Question> filteredQuestions;

    // Current Admin
    private User currentAdmin;

    @FXML
    public void initialize() {
        System.out.println("🚀 QuestionManagementController initialize başladı");

        questionDAO = new QuestionDAO();
        allQuestions = FXCollections.observableArrayList();
        filteredQuestions = FXCollections.observableArrayList();

        setupFilterComboBoxes();
        setupTableColumns();
        loadQuestions();

        System.out.println("✅ QuestionManagementController initialize tamamlandı");
    }

    /**
     * Set current admin user
     */
    public void setCurrentAdmin(User admin) {
        this.currentAdmin = admin;
        System.out.println("✅ QuestionManagement'a admin set edildi: " + (admin != null ? admin.getFullName() : "null"));
    }

    /**
     * Setup filter combo boxes
     */
    private void setupFilterComboBoxes() {
        // Category filter
        categoryFilterCombo.setItems(FXCollections.observableArrayList(
                "Tümü",
                "Trafik Kuralları",
                "Trafik İşaretleri",
                "İlk Yardım",
                "Motor ve Araç Tekniği"
        ));
        categoryFilterCombo.getSelectionModel().selectFirst();

        // Difficulty filter
        difficultyFilterCombo.setItems(FXCollections.observableArrayList(
                "Tümü",
                "Kolay",
                "Orta",
                "Zor"
        ));
        difficultyFilterCombo.getSelectionModel().selectFirst();
    }

    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        // ID Column
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getId()))
        );

        // Question Text Column (truncated)
        questionTextCol.setCellValueFactory(cellData -> {
            String text = cellData.getValue().getQuestionText();
            if (text.length() > 50) {
                text = text.substring(0, 50) + "...";
            }
            return new SimpleStringProperty(text);
        });

        // Category Column
        categoryCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategoryDisplayName())
        );

        // Difficulty Column
        difficultyCol.setCellValueFactory(cellData -> {
            String difficulty = cellData.getValue().getDifficulty();
            String display = switch (difficulty) {
                case "kolay" -> "🟢 Kolay";
                case "orta" -> "🟡 Orta";
                case "zor" -> "🔴 Zor";
                default -> difficulty;
            };
            return new SimpleStringProperty(display);
        });

        // Points Column
        pointsCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getPoints()))
        );

        // Correct Answer Column
        correctAnswerCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCorrectAnswer())
        );

        // Has Image Column
        hasImageCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().hasImage() ? "✅" : "❌")
        );

        // Actions Column
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox actionBox = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                actionBox.setAlignment(Pos.CENTER);

                viewBtn.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    viewQuestion(question);
                });

                editBtn.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    editQuestion(question);
                });

                deleteBtn.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    deleteQuestion(question);
                });

                // Styling
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
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

    /**
     * Load all questions
     */
    private void loadQuestions() {
        new Thread(() -> {
            try {
                List<Question> questions = questionDAO.getAllQuestions();

                Platform.runLater(() -> {
                    allQuestions.setAll(questions);
                    applyFilters();
                    updateStatistics();
                    System.out.println("✅ " + questions.size() + " soru yüklendi");
                });

            } catch (Exception e) {
                System.err.println("❌ Sorular yüklenirken hata: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Hata");
                    alert.setHeaderText("Sorular yüklenemedi");
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
        String categoryFilter = categoryFilterCombo.getValue();
        String difficultyFilter = difficultyFilterCombo.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        filteredQuestions.clear();

        List<Question> filtered = allQuestions.stream()
                .filter(q -> {
                    // Category filter
                    if (!categoryFilter.equals("Tümü")) {
                        String category = mapCategoryToDb(categoryFilter);
                        if (!q.getCategory().equals(category)) {
                            return false;
                        }
                    }

                    // Difficulty filter
                    if (!difficultyFilter.equals("Tümü")) {
                        String difficulty = difficultyFilter.toLowerCase();
                        if (!q.getDifficulty().equals(difficulty)) {
                            return false;
                        }
                    }

                    // Search text filter
                    if (!searchText.isEmpty()) {
                        return q.getQuestionText().toLowerCase().contains(searchText);
                    }

                    return true;
                })
                .collect(Collectors.toList());

        filteredQuestions.addAll(filtered);
        questionsTable.setItems(filteredQuestions);
        totalQuestionsLabel.setText("Toplam: " + filteredQuestions.size() + " soru");
    }

    /**
     * Update statistics
     */
    private void updateStatistics() {
        trafficRulesCount.setText(String.valueOf(
                questionDAO.getQuestionCountByCategory("trafik_kuralları")
        ));
        signsCount.setText(String.valueOf(
                questionDAO.getQuestionCountByCategory("işaretler")
        ));
        firstAidCount.setText(String.valueOf(
                questionDAO.getQuestionCountByCategory("ilkyardım")
        ));
        motorCount.setText(String.valueOf(
                questionDAO.getQuestionCountByCategory("motor")
        ));
    }

    /**
     * Refresh table
     */
    @FXML
    private void refreshTable() {
        loadQuestions();
    }

    /**
     * Show add question dialog
     */
    @FXML
    private void showAddQuestionDialog() {
        showQuestionDialog(null);
    }

    /**
     * View question details
     */
    private void viewQuestion(Question question) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Soru Detayı");
        alert.setHeaderText("ID: " + question.getId());

        String content = "Soru: " + question.getQuestionText() + "\n\n" +
                "A) " + question.getOptionA() + "\n" +
                "B) " + question.getOptionB() + "\n" +
                "C) " + question.getOptionC() + "\n" +
                "D) " + question.getOptionD() + "\n\n" +
                "Doğru Cevap: " + question.getCorrectAnswer() + "\n" +
                "Kategori: " + question.getCategoryDisplayName() + "\n" +
                "Zorluk: " + question.getDifficulty() + "\n" +
                "Puan: " + question.getPoints();

        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Edit question
     */
    private void editQuestion(Question question) {
        showQuestionDialog(question);
    }

    /**
     * Delete question
     */
    private void deleteQuestion(Question question) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Soru Sil");
        alert.setHeaderText("Bu soruyu silmek istediğinize emin misiniz?");
        alert.setContentText("ID: " + question.getId() + "\n" +
                question.getQuestionText().substring(0, Math.min(50, question.getQuestionText().length())) + "...");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (questionDAO.deleteQuestion(question.getId())) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Başarılı");
                success.setHeaderText(null);
                success.setContentText("Soru başarıyla silindi!");
                success.showAndWait();

                loadQuestions();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Hata");
                error.setHeaderText("Soru silinemedi");
                error.setContentText("Bir hata oluştu. Lütfen tekrar deneyin.");
                error.showAndWait();
            }
        }
    }

    /**
     * Show question dialog (add or edit)
     */
    private void showQuestionDialog(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/question_dialog.fxml"));
            Parent root = loader.load();

            QuestionDialogController controller = loader.getController();

            // Set current admin
            if (currentAdmin != null) {
                controller.setCurrentAdmin(currentAdmin);
            } else {
                System.err.println("⚠️ Admin bilgisi bulunamadı!");
            }

            if (question != null) {
                controller.setQuestion(question);
            }
            controller.setParentController(this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(question == null ? "Yeni Soru Ekle" : "Soru Düzenle");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            System.err.println("❌ Soru dialog açılamadı: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper: Map category display name to database value
     */
    private String mapCategoryToDb(String displayName) {
        return switch (displayName) {
            case "Trafik Kuralları" -> "trafik_kuralları";
            case "Trafik İşaretleri" -> "işaretler";
            case "İlk Yardım" -> "ilkyardım";
            case "Motor ve Araç Tekniği" -> "motor";
            default -> displayName;
        };
    }

    /**
     * Callback when question is saved
     */
    public void onQuestionSaved() {
        loadQuestions();
    }
}
