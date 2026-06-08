package com.chatroom;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML TextField usernameField;

    @FXML
    public void onJoinPressed() {
        String username = usernameField.getText().trim();

        if (username.isEmpty() || username.contains(",")) {
            usernameField.setStyle("-fx-border-color: #EF4444; -fx-background-color: #1E293B; -fx-text-fill: #FFFFFF;");
            usernameField.setPromptText("Invalid username!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("mainwindow.fxml"));
            Parent root = loader.load();

            ChatController chatController = loader.getController();
            chatController.initChat(username);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Chatroom - Logged in as: " + username);
            stage.show();
        } catch (IOException e) {
            System.out.println("Failed to transition to mainwindow.fxml");
            System.out.println(e.getMessage());
        }
    }
}
