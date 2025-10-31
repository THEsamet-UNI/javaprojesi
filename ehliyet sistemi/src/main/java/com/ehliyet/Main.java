package com.ehliyet;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.ehliyet.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Login ekranını yükle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 500, 600);

            primaryStage.setTitle("Ehliyet Sınav Sistemi - Giriş");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            System.out.println("🚀 Login ekranı açıldı!");
            //String password = "Samet2005.";
            //String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            //System.out.println("Doğru formatlı hash: " + hashedPassword);


        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Login ekranı yüklenemedi!");
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
        System.out.println("👋 Program kapatıldı!");
    }

    public static void main(String[] args) {
        launch(args);


    }
}