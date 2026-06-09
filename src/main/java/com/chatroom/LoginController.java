package com.chatroom;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField serverIpField;
    @FXML private TextField usernameField;

    @FXML
    public void onJoinPressed() {
        String username = usernameField.getText().trim();
        String ipAddress = serverIpField.getText().trim();

        if (username.isEmpty() || ipAddress.isEmpty()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("mainwindow.fxml"));
            Parent root = loader.load();

            ChatController chatController = loader.getController();
            chatController.initChat(username, ipAddress);

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
