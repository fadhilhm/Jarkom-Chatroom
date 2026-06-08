package com.chatroom;

import com.chatroom.backend.ChatServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatController {
    @FXML private SplitPane roomArea;
    @FXML private TextArea chatArea;
    @FXML private TextField inputField;

    @FXML private ListView<String> roomListView;
    @FXML private TextField newRoomField;
    @FXML private Label currentRoomLabel;

    private static final int PORT = ChatServer.PORT;
    private static final String ADDRESS = ChatServer.ADDRESS;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public void initChat(String username) {
        this.username = username;

        // listen for when user clicks a room name in the sidebar
        roomListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && out != null) {
                out.println("JOIN_ROOM:" + newValue);
            }
        });

        connectToServer();

        // adjust chatArea dynamically
        chatArea.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                    double dynamicSize = newWidth.doubleValue() / 70.0;

                    if (dynamicSize < 13) dynamicSize = 13;
                    if (dynamicSize > 24) dynamicSize = 24;

                    chatArea.setStyle(
                        "-fx-control-inner-background: #1E293B; " +
                        "-fx-text-fill: #F1F5F9; " +
                        "-fx-border-color: #334155; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-font-size: " + dynamicSize + "px;"
                    );

                    inputField.setStyle(
                        "-fx-background-color: #1E293B; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-border-color: #334155; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-font-size: " + dynamicSize + "px;"
                    );
                });
            }
        });

        // adjust roomArea dynamically
        roomArea.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                    double windowWidth = newWidth.doubleValue();
                    double dynamicSize = windowWidth / 70.0;

                    if (dynamicSize < 13) dynamicSize = 13;
                    if (dynamicSize > 22) dynamicSize = 22;

                    roomArea.setStyle(
                        "-fx-background-color: #0B111E; " +
                        "-fx-box-border: transparent; " +
                        "-fx-padding: 0; " +
                        "-fx-font-size: " + dynamicSize + "px;"
                    );
                });
            }
        });
    }

    public void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket(ADDRESS, PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // handle handshake
                String serverPrompt = in.readLine();
                out.println(username);

                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    final String msg = serverMessage;
                    Platform.runLater(() -> handleIncomingProtocol(msg));
                }
            } catch (IOException e) {
                chatArea.appendText("Could not connect to ChatServer.\n");
                chatArea.appendText("Please ensure ChatServer is running and restart the app.\n");
                inputField.setDisable(true);
            }
        }).start();
    }

    private void handleIncomingProtocol(String message) {
        System.out.println("Client Received From Server: \"" + message + "\"");

        if (message.startsWith("ROOM_LIST:")) {
            roomListView.getItems().clear();

            String rawRooms = message.substring(10);
            if (!rawRooms.trim().isEmpty()) {
                // update chatroom sidebar
                String[] rooms = rawRooms.split(",");
                roomListView.getItems().addAll(rooms);
            }
        } else if (message.startsWith("JOIN_SUCCESS:")) {
            // successfully swapped room
            String roomName = message.substring(13);
            currentRoomLabel.setText("Currently in Room: @" + roomName);
            chatArea.clear();
        } else {
            // message
            chatArea.appendText(message + "\n");
        }
    }

    @FXML
    public void onCreateRoomClick() {
        String text = newRoomField.getText().trim();

        if (!text.isEmpty() && out != null) {
            out.println("CREATE_ROOM:" + text);
            newRoomField.clear();
        }
    }

    @FXML
    protected void onSendButtonClick() {
        String text = inputField.getText().trim();
        if (!text.isEmpty() && out != null) {
            out.println(text);
            chatArea.appendText(username + ": " + text + "\n\n");
            inputField.clear();
        }
    }
}
