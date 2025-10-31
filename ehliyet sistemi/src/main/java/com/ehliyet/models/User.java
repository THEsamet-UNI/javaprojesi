package com.ehliyet.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private int id;
    private String tcNo;
    private String username;
    private String passwordHash;
    private String fullName;
    private String userType;
    private String studentNo;
    private String phone;
    private String email;
    private LocalDate birthDate;
    private LocalDateTime createdDate;
    private boolean isActive;

    // Constructor
    public User() {
        this.isActive = true;
    }

    public User(String username, String fullName, String userType) {
        this.username = username;
        this.fullName = fullName;
        this.userType = userType;
        this.isActive = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTcNo() {
        return tcNo;
    }

    public void setTcNo(String tcNo) {
        this.tcNo = tcNo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Helper Methods
    public boolean isStudent() {
        return "student".equals(userType);
    }

    public boolean isAdmin() {
        return "admin".equals(userType);
    }

    public String toStradminadmining() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}
