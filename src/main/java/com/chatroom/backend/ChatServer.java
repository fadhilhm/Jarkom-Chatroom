package com.chatroom.backend;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 12345;
    private static final String ADDRESS = "localhost";
    private static final Set<ClientHandler> globalClients = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, Set<ClientHandler>> chatRooms = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Connecting to Server...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
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

    public static void createRoom(String roomName) {
        chatRooms.putIfAbsent(roomName, Collections.synchronizedSet(new HashSet<>()));

    }

    public static boolean joinRoom(String roomName, ClientHandler client) {
        if (!chatRooms.containsKey(roomName)) return false;

        // remove old room if they were in one

        chatRooms.get(roomName).add(client);
        return true;
    }

    public void addGlobalClient(ClientHandler client) {
        globalClients.add(client);
    }

    public void removeGlobalClient(ClientHandler client) {
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
            out = new PrintWriter(clientSocket.getOutputStream());

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("CONNECT:")) {
                    this.clientName = inputLine.substring(8);

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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