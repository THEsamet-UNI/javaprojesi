package com.ehliyet.controllers;

import com.ehliyet.dao.ExamDAO;
import com.ehliyet.dao.QuestionDAO;
import com.ehliyet.models.Exam;
import com.ehliyet.models.Question;
import com.ehliyet.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

public class ExamDialogController {

    @FXML private Label dialogTitle;
    @FXML private TextField examNameField;
    @FXML private TextField examCodeField;
    @FXML private Spinner<Integer> totalQuestionsSpinner;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private Spinner<Integer> passingScoreSpinner;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private RadioButton randomQuestionsRadio;
    @FXML private RadioButton manualQuestionsRadio;

    private ExamDAO examDAO;
    private QuestionDAO questionDAO;
    private Exam currentExam;
    private ExamManagementController parentController;
    private User currentAdmin;
    private ToggleGroup questionSelectionGroup;

    @FXML
    public void initialize() {
        examDAO = new ExamDAO();
        questionDAO = new QuestionDAO();

        // Setup spinners
        SpinnerValueFactory<Integer> questionsFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 100, 50);
        totalQuestionsSpinner.setValueFactory(questionsFactory);

        SpinnerValueFactory<Integer> durationFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 180, 45);
        durationSpinner.setValueFactory(durationFactory);

        SpinnerValueFactory<Integer> passingScoreFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(50, 100, 70);
        passingScoreSpinner.setValueFactory(passingScoreFactory);

        // Setup radio button group
        questionSelectionGroup = new ToggleGroup();
        randomQuestionsRadio.setToggleGroup(questionSelectionGroup);
        manualQuestionsRadio.setToggleGroup(questionSelectionGroup);
        randomQuestionsRadio.setSelected(true);

        System.out.println("✅ ExamDialogController initialize tamamlandı");
    }

    /**
     * Set current admin user
     */
    public void setCurrentAdmin(User admin) {
        this.currentAdmin = admin;
        System.out.println("✅ Exam dialog'a admin set edildi: " + (admin != null ? admin.getFullName() : "null"));
    }

    /**
     * Set exam for edit mode
     */
    public void setExam(Exam exam) {
        this.currentExam = exam;
        dialogTitle.setText("Sınav Düzenle");

        examNameField.setText(exam.getExamName());
        examCodeField.setText(exam.getExamCode());
        examCodeField.setDisable(true); // Exam code cannot be changed
        totalQuestionsSpinner.getValueFactory().setValue(exam.getTotalQuestions());
        durationSpinner.getValueFactory().setValue(exam.getDurationMinutes());
        passingScoreSpinner.getValueFactory().setValue(exam.getPassingScore());

        if (exam.getDescription() != null) {
            descriptionArea.setText(exam.getDescription());
        }

        if (exam.getStartDate() != null) {
            startDatePicker.setValue(exam.getStartDate().toLocalDate());
        }

        if (exam.getEndDate() != null) {
            endDatePicker.setValue(exam.getEndDate().toLocalDate());
        }

        // Disable question selection for edit mode
        randomQuestionsRadio.setDisable(true);
        manualQuestionsRadio.setDisable(true);
    }

    /**
     * Set parent controller
     */
    public void setParentController(ExamManagementController controller) {
        this.parentController = controller;
    }

    /**
     * Generate random exam code
     */
    @FXML
    private void generateExamCode() {
        String prefix = "EXAM";
        int year = LocalDate.now().getYear();
        int randomNum = new Random().nextInt(9000) + 1000; // 1000-9999
        String code = String.format("%s-%d-%04d", prefix, year, randomNum);
        examCodeField.setText(code);
    }

    /**
     * Handle save button
     */
    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        Exam exam = currentExam != null ? currentExam : new Exam();

        exam.setExamName(examNameField.getText().trim());
        exam.setExamCode(examCodeField.getText().trim());
        exam.setTotalQuestions(totalQuestionsSpinner.getValue());
        exam.setDurationMinutes(durationSpinner.getValue());
        exam.setPassingScore(passingScoreSpinner.getValue());

        String description = descriptionArea.getText().trim();
        exam.setDescription(description.isEmpty() ? null : description);

        // Set dates
        if (startDatePicker.getValue() != null) {
            exam.setStartDate(LocalDateTime.of(startDatePicker.getValue(), LocalTime.of(0, 0)));
        }

        if (endDatePicker.getValue() != null) {
            exam.setEndDate(LocalDateTime.of(endDatePicker.getValue(), LocalTime.of(23, 59)));
        }

        // Set created_by to current admin's ID
        if (currentExam == null) {
            if (currentAdmin != null) {
                exam.setCreatedBy(currentAdmin.getId());
                System.out.println("✅ Sınav oluşturan admin ID: " + currentAdmin.getId());
            } else {
                System.err.println("⚠️ Admin bilgisi bulunamadı!");
                exam.setCreatedBy(1);
            }
        }

        boolean success;
        if (currentExam == null) {
            success = examDAO.createExam(exam);

            // If random questions selected, add questions
            if (success && randomQuestionsRadio.isSelected()) {
                addRandomQuestionsToExam(exam);
            }
        } else {
            success = examDAO.updateExam(exam);
        }

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Başarılı");
            alert.setHeaderText(null);
            alert.setContentText("Sınav başarıyla " + (currentExam == null ? "oluşturuldu" : "güncellendi") + "!");
            alert.showAndWait();

            if (parentController != null) {
                parentController.onExamSaved();
            }

            closeDialog();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Sınav kaydedilemedi");
            alert.setContentText("Bir hata oluştu. Lütfen tekrar deneyin.");
            alert.showAndWait();
        }
    }

    /**
     * Add random questions to exam
     */
    private void addRandomQuestionsToExam(Exam exam) {
        try {
            int questionCount = exam.getTotalQuestions();
            List<Question> randomQuestions = questionDAO.getRandomQuestions(questionCount);

            if (randomQuestions.size() < questionCount) {
                Alert warning = new Alert(Alert.AlertType.WARNING);
                warning.setTitle("Uyarı");
                warning.setHeaderText("Yeterli soru bulunamadı");
                warning.setContentText("Soru havuzunda sadece " + randomQuestions.size() +
                        " soru var. İstenen: " + questionCount);
                warning.showAndWait();
            }

            // Add questions to exam
            int order = 1;
            for (Question question : randomQuestions) {
                examDAO.addQuestionToExam(exam.getId(), question.getId(), order++);
            }

            System.out.println("✅ " + randomQuestions.size() + " rastgele soru sınava eklendi");

        } catch (Exception e) {
            System.err.println("❌ Sorular eklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle cancel button
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }

    /**
     * Validate form
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (examNameField.getText().trim().isEmpty()) {
            errors.append("- Sınav adı boş olamaz\n");
        }

        if (examCodeField.getText().trim().isEmpty()) {
            errors.append("- Sınav kodu boş olamaz\n");
        }

        // Check if exam code already exists (only for new exams)
        if (currentExam == null) {
            Exam existingExam = examDAO.getExamByCode(examCodeField.getText().trim());
            if (existingExam != null) {
                errors.append("- Bu sınav kodu zaten kullanılıyor\n");
            }
        }

        // Validate date range
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                errors.append("- Bitiş tarihi başlangıç tarihinden önce olamaz\n");
            }
        }

        // Validate question count
        int totalQuestions = questionDAO.getTotalQuestionCount();
        if (totalQuestionsSpinner.getValue() > totalQuestions) {
            errors.append("- Soru havuzunda yeterli soru yok (Mevcut: " + totalQuestions + ")\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Eksik Bilgi");
            alert.setHeaderText("Lütfen aşağıdaki hataları düzeltin:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    /**
     * Close dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) dialogTitle.getScene().getWindow();
        stage.close();
    }
}
