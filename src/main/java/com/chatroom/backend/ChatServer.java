package com.chatroom.backend;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    public static final int PORT = 12345;
    public static final String ADDRESS = "localhost";
    private static final Set<ClientHandler> globalClients = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, Set<ClientHandler>> chatRooms = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> roomsHistory = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Connecting to Server...");

        createRoom("General");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Connected to Server! Listening on Port: " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected: " + socket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(socket);
                globalClients.add(handler);

                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    // broadcast message to other user after a message is sent
    public static void broadcastToRoom(String roomName, String message) {
        Set<ClientHandler> roomClients = chatRooms.get(roomName);
        if (roomClients != null) {
            synchronized (roomClients) {
                for (ClientHandler client : roomClients) {
                    client.sendMessage(message);
                }
            }
        }
    }

    // broadcast active rooms to all user
    public static void broadcastRoomList() {
        String roomList = "ROOM_LIST:" + String.join(",", chatRooms.keySet());

        synchronized (globalClients) {
            for (ClientHandler clients : globalClients) {
                clients.sendMessage(roomList);
            }
        }
    }

    public static void archiveMessage(String roomName, String message) {
        List<String> history = roomsHistory.get(roomName);

        if (history != null) history.add(message);
    }

    static void sendRoomHistory(String roomName, ClientHandler client) {
        List<String> history = roomsHistory.get(roomName);
        if (history != null) {
            synchronized (history) {
                for (String pastMessage : history) {
                    client.sendMessage(pastMessage);
                }
            }
        }
    }

    public static void createRoom(String roomName) {
        chatRooms.putIfAbsent(roomName, Collections.synchronizedSet(new HashSet<>()));
        roomsHistory.putIfAbsent(roomName, Collections.synchronizedList(new ArrayList<>()));
    }

    static boolean joinRoom(String roomName, ClientHandler client) {
        if (!chatRooms.containsKey(roomName)) return false;

        // remove old room if they were in one
        if (client.getCurrentRoom() != null) {
            chatRooms.get(client.getCurrentRoom()).remove(client);
        }

        chatRooms.get(roomName).add(client);
        return true;
    }

    static void addGlobalClient(ClientHandler client) {
        globalClients.add(client);
    }

    static void removeGlobalClient(ClientHandler client) {
        globalClients.remove(client);

        if (client.getCurrentRoom() != null && chatRooms.containsKey(client.getCurrentRoom())) {
            chatRooms.get(client.getCurrentRoom()).remove(client);
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private String currentRoom = null;

    public ClientHandler(Socket socket) {
        clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            this.clientName = in.readLine();
            System.out.println("User registered as: " + clientName);

            ChatServer.broadcastRoomList();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("CONNECT:")) {
                    this.clientName = inputLine.substring(8);

                } else if (inputLine.startsWith("CREATE_ROOM:")) {
                    String roomName = inputLine.substring(12);

                    ChatServer.createRoom(roomName);
                    ChatServer.broadcastRoomList();
                } else if (inputLine.startsWith("JOIN_ROOM:")) {
                    String roomName = inputLine.substring(10);
                    String oldRoom = this.currentRoom;

                    if (ChatServer.joinRoom(roomName, this)) {
                        this.currentRoom = roomName;
                        out.println("JOIN_SUCCESS:" + roomName);

                        // Alert old room that user left
                        if (oldRoom != null) {
                            String leaveAlert = clientName + " has left the room.";
                            ChatServer.archiveMessage(oldRoom, leaveAlert);
                            ChatServer.broadcastToRoom(oldRoom, leaveAlert);
                        }

                        ChatServer.sendRoomHistory(roomName, this);

                        // Alert new room that user joined
                        String joinAlert = clientName + " has joined the room.";
                        ChatServer.archiveMessage(roomName, joinAlert);
                        ChatServer.broadcastToRoom(roomName, joinAlert);
                    }
                }
                else {
                    if (currentRoom != null) {
                        String message = clientName + ": " + inputLine;
                        ChatServer.archiveMessage(currentRoom, message);
                        ChatServer.broadcastToRoom(currentRoom, clientName + ": " + inputLine);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (currentRoom != null) {
                String departureAlert = clientName + " has left the chat.";
                ChatServer.broadcastToRoom(currentRoom, departureAlert);
            }

            ChatServer.removeGlobalClient(this);
            try { clientSocket.close(); } catch (IOException ignore) {}
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getClientName() {
        return clientName;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }
}