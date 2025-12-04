package com.ehliyet.models;

import java.time.LocalDateTime;
import java.time.Duration;

public class ExamResult {
    private int id;
    private int examId;
    private int studentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int score;
    private int correctAnswers;
    private int wrongAnswers;
    private int emptyAnswers;
    private String status;
    private String photoPath;
    private LocalDateTime createdDate;

    // Relations
    private Exam exam;
    private User student;

    // Constructors
    public ExamResult() {  // Bu satır düzeltildi (önceki: public Exam())
        this.status = "in_progress";
        this.correctAnswers = 0;
        this.wrongAnswers = 0;
        this.emptyAnswers = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public int getEmptyAnswers() { return emptyAnswers; }
    public void setEmptyAnswers(int emptyAnswers) { this.emptyAnswers = emptyAnswers; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    // Helper methods
    public boolean isPassed() {
        return exam != null && score >= exam.getPassingScore();
    }

    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }

    @Override
    public String toString() {
        return "ExamResult{" +
                "id=" + id +
                ", score=" + score +
                ", status='" + status + '\'' +
                '}';
    }
}
