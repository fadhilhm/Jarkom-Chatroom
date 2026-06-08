package com.chatroom;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {
    @FXML TextField usernameField;

    @FXML
    public void onJoinPressed() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) return;

        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("login"));
            loader.load();

        } catch (IOException e) {

        }
    }
}
