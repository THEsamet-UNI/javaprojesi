package com.ehliyet.controllers;

import com.ehliyet.dao.QuestionDAO;
import com.ehliyet.models.Question;
import com.ehliyet.models.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class QuestionDialogController {

    @FXML private Label dialogTitle;
    @FXML private TextArea questionTextArea;
    @FXML private TextField optionAField;
    @FXML private TextField optionBField;
    @FXML private TextField optionCField;
    @FXML private TextField optionDField;
    @FXML private ComboBox<String> correctAnswerCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private Spinner<Integer> pointsSpinner;
    @FXML private TextField imagePathField;

    private QuestionDAO questionDAO;
    private Question currentQuestion;
    private QuestionManagementController parentController;

    // ✅ Admin user bilgisini tutalım
    private User currentAdmin;

    @FXML
    public void initialize() {
        questionDAO = new QuestionDAO();

        // Setup combo boxes
        correctAnswerCombo.setItems(FXCollections.observableArrayList("A", "B", "C", "D"));

        categoryCombo.setItems(FXCollections.observableArrayList(
                "Trafik Kuralları",
                "Trafik İşaretleri",
                "İlk Yardım",
                "Motor ve Araç Tekniği"
        ));

        difficultyCombo.setItems(FXCollections.observableArrayList("Kolay", "Orta", "Zor"));

        // Setup spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5);
        pointsSpinner.setValueFactory(valueFactory);

        // Default selections
        correctAnswerCombo.getSelectionModel().selectFirst();
        categoryCombo.getSelectionModel().selectFirst();
        difficultyCombo.getSelectionModel().select(1);
    }

    /**
     * ✅ Admin user'ı set et
     */
    public void setCurrentAdmin(User admin) {
        this.currentAdmin = admin;
    }

    public void setQuestion(Question question) {
        this.currentQuestion = question;
        dialogTitle.setText("Soru Düzenle");

        questionTextArea.setText(question.getQuestionText());
        optionAField.setText(question.getOptionA());
        optionBField.setText(question.getOptionB());
        optionCField.setText(question.getOptionC());
        optionDField.setText(question.getOptionD());
        correctAnswerCombo.setValue(question.getCorrectAnswer());

        String categoryDisplay = question.getCategoryDisplayName();
        categoryCombo.setValue(categoryDisplay);

        String difficultyDisplay = question.getDifficulty().substring(0, 1).toUpperCase() +
                question.getDifficulty().substring(1);
        difficultyCombo.setValue(difficultyDisplay);

        pointsSpinner.getValueFactory().setValue(question.getPoints());
        imagePathField.setText(question.getImagePath());
    }

    public void setParentController(QuestionManagementController controller) {
        this.parentController = controller;
    }

    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Görsel Seç");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) imagePathField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            imagePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        Question question = currentQuestion != null ? currentQuestion : new Question();

        question.setQuestionText(questionTextArea.getText().trim());
        question.setOptionA(optionAField.getText().trim());
        question.setOptionB(optionBField.getText().trim());
        question.setOptionC(optionCField.getText().trim());
        question.setOptionD(optionDField.getText().trim());
        question.setCorrectAnswer(correctAnswerCombo.getValue());
        question.setCategory(mapCategoryToDb(categoryCombo.getValue()));
        question.setDifficulty(difficultyCombo.getValue().toLowerCase());
        question.setPoints(pointsSpinner.getValue());
        question.setImagePath(imagePathField.getText().trim());

        // ✅ Gerçek admin ID'sini kullan
        if (currentQuestion == null) {
            if (currentAdmin != null) {
                question.setCreatedBy(currentAdmin.getId());
            } else {
                // Eğer admin bilgisi yoksa NULL olarak ayarla (Foreign key ON DELETE SET NULL destekliyor)
                question.setCreatedBy(0); // veya null kabul ediyorsa null
            }
        }

        boolean success;
        if (currentQuestion == null) {
            success = questionDAO.addQuestion(question);
        } else {
            success = questionDAO.updateQuestion(question);
        }

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Başarılı");
            alert.setHeaderText(null);
            alert.setContentText("Soru başarıyla " + (currentQuestion == null ? "eklendi" : "güncellendi") + "!");
            alert.showAndWait();

            if (parentController != null) {
                parentController.onQuestionSaved();
            }

            closeDialog();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Soru kaydedilemedi");
            alert.setContentText("Bir hata oluştu. Lütfen tekrar deneyin.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (questionTextArea.getText().trim().isEmpty()) {
            errors.append("- Soru metni boş olamaz\n");
        }
        if (optionAField.getText().trim().isEmpty()) {
            errors.append("- A şıkkı boş olamaz\n");
        }
        if (optionBField.getText().trim().isEmpty()) {
            errors.append("- B şıkkı boş olamaz\n");
        }
        if (optionCField.getText().trim().isEmpty()) {
            errors.append("- C şıkkı boş olamaz\n");
        }
        if (optionDField.getText().trim().isEmpty()) {
            errors.append("- D şıkkı boş olamaz\n");
        }
        if (correctAnswerCombo.getValue() == null) {
            errors.append("- Doğru cevap seçilmeli\n");
        }
        if (categoryCombo.getValue() == null) {
            errors.append("- Kategori seçilmeli\n");
        }
        if (difficultyCombo.getValue() == null) {
            errors.append("- Zorluk seçilmeli\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Eksik Bilgi");
            alert.setHeaderText("Lütfen aşağıdaki alanları doldurunuz:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private String mapCategoryToDb(String displayName) {
        return switch (displayName) {
            case "Trafik Kuralları" -> "trafik_kuralları";
            case "Trafik İşaretleri" -> "işaretler";
            case "İlk Yardım" -> "ilkyardım";
            case "Motor ve Araç Tekniği" -> "motor";
            default -> displayName;
        };
    }

    private void closeDialog() {
        Stage stage = (Stage) dialogTitle.getScene().getWindow();
        stage.close();
    }
}
