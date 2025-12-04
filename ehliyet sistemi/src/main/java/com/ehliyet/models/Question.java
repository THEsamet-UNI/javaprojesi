package com.ehliyet.models;

import java.time.LocalDateTime;

public class Question {
    private int id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer; // A, B, C, D
    private String category; // trafik_kuralları, işaretler, ilkyardım, motor
    private String difficulty; // kolay, orta, zor
    private String imagePath;
    private int points;
    private int createdBy;
    private LocalDateTime createdDate;
    private boolean isActive;

    // Constructors
    public Question() {
        this.points = 5;
        this.isActive = true;
        this.difficulty = "orta";
    }

    public Question(String questionText, String optionA, String optionB,
                    String optionC, String optionD, String correctAnswer, String category) {
        this();
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.category = category;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Helper methods
    public boolean hasImage() {
        return imagePath != null && !imagePath.isEmpty();
    }

    public String getCategoryDisplayName() {
        return switch (category) {
            case "trafik_kuralları" -> "Trafik Kuralları";
            case "işaretler" -> "Trafik İşaretleri";
            case "ilkyardım" -> "İlk Yardım";
            case "motor" -> "Motor ve Araç Tekniği";
            default -> category;
        };
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}
