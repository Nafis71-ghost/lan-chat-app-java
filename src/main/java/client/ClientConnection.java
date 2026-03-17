package client;

import model.ChatMessage;
import protocol.JsonUtil;
import protocol.ProtocolMessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ChatMessage authenticate(String serverIp, int port, ProtocolMessageType authType, String username, String password) throws IOException {
        socket = new Socket(serverIp, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);

        ChatMessage authRequest = new ChatMessage();
        authRequest.setType(authType);
        authRequest.setUsername(username);
        authRequest.setPassword(password);

        writer.println(JsonUtil.toJson(authRequest));

        String responseLine = reader.readLine();
        if (responseLine == null) {
            throw new IOException("No response from server.");
        }

        return JsonUtil.fromJson(responseLine);
    }

    public void send(ChatMessage message) {
        if (writer != null) {
            writer.println(JsonUtil.toJson(message));
        }
    }

    public ChatMessage readMessage() throws IOException {
        if (reader == null) {
            return null;
        }

        String line = reader.readLine();
        if (line == null) {
            return null;
        }

        return JsonUtil.fromJson(line);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void disconnect() {
        try {
            if (writer != null) {
                ChatMessage disconnect = new ChatMessage();
                disconnect.setType(ProtocolMessageType.DISCONNECT);
                writer.println(JsonUtil.toJson(disconnect));
            }
        } catch (Exception ignored) {
        }

        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
    }
}