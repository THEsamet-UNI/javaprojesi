package com.ehliyet.models;

import java.time.LocalDateTime;

public class Exam {
    private int id;
    private String examName;
    private String examCode;
    private String description;
    private int durationMinutes;
    private int totalQuestions;
    private int passingScore;
    private int createdBy;
    private LocalDateTime createdDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;

    // Constructors
    public Exam() {
        this.durationMinutes = 45;
        this.totalQuestions = 50;
        this.passingScore = 70;
        this.isActive = true;
    }

    public Exam(String examName, String examCode) {
        this();
        this.examName = examName;
        this.examCode = examCode;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getPassingScore() { return passingScore; }
    public void setPassingScore(int passingScore) { this.passingScore = passingScore; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Helper methods
    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
                (startDate == null || now.isAfter(startDate)) &&
                (endDate == null || now.isBefore(endDate));
    }

    @Override
    public String toString() {
        return "Exam{" +
                "id=" + id +
                ", examName='" + examName + '\'' +
                ", examCode='" + examCode + '\'' +
                '}';
    }
}
