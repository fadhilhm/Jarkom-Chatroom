package com.chatroom;

import com.chatroom.backend.ChatServer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Application.class.getResource("login.fxml"));
        Parent root = loader.load();

        Scene loginScene = new Scene(root, 450, 360);

        stage.setTitle("Chatroom Launcher - Login");
        stage.setScene(loginScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
