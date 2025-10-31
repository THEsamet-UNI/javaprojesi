package com.ehliyet.controllers;

import com.ehliyet.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Hoş Geldin " + user.getFullName() + "!");
        System.out.println("Admin paneli açıldı: " + user.getFullName());
    }
}