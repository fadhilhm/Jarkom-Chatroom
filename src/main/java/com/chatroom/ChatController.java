package com.chatroom;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatController {
    @FXML private TextArea chatArea;
    @FXML private TextField inputField;

    private static final int PORT = 12345;
    private static final String ADDRESS = "localhost";
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public void initChat(String username) {
        this.username = username;
    }

    public void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket(ADDRESS, PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String serverPrompt = in.readLine();
                out.println(username);

                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    String messageToPrint = serverMessage;
                    Platform.runLater(() -> chatArea.appendText(messageToPrint + "\n"));
                }
            } catch (IOException e) {
                Platform.runLater(() -> chatArea.appendText("Connection lost or failed.\n"));
            }
        }).start();
    }

    @FXML
    protected void onSendButtonClick() {
        String text = inputField.getText().trim();
        if (!text.isEmpty() && out != null) {
            out.println(text);
            chatArea.appendText("You " + text + "\n");
            inputField.clear();
        }
    }
}
