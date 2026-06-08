module com.chatroom.chatroom {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.chatroom to javafx.fxml;
    exports com.chatroom;
}