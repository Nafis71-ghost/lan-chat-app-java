package server;

import database.RoomDAO;
import model.ChatMessage;
import protocol.JsonUtil;
import protocol.ProtocolMessageType;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientManager {
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public void addClient(ClientHandler clientHandler) {
        synchronized (clients) {
            clients.add(clientHandler);
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        synchronized (clients) {
            clients.remove(clientHandler);
        }
    }

    public boolean isUserOnline(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getUsername() != null &&
                        client.getUsername().equalsIgnoreCase(username.trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    public ClientHandler findOnlineUser(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getUsername() != null &&
                        client.getUsername().equalsIgnoreCase(username.trim())) {
                    return client;
                }
            }
        }

        return null;
    }

    public void broadcastPublicMessage(ChatMessage message, ClientHandler sender) {
        String json = JsonUtil.toJson(message);

        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(json);
                }
            }
        }
    }

    public boolean sendPrivateMessage(ChatMessage message, ClientHandler sender) {
        if (message == null || message.getReceiver() == null || message.getReceiver().isBlank()) {
            return false;
        }

        ClientHandler recipient = findOnlineUser(message.getReceiver());
        if (recipient == null) {
            return false;
        }

        ChatMessage deliver = new ChatMessage(
                ProtocolMessageType.PRIVATE_MESSAGE,
                sender.getUsername(),
                recipient.getUsername(),
                null,
                message.getContent(),
                message.getTimestamp()
        );

        recipient.sendMessage(JsonUtil.toJson(deliver));
        return true;
    }

    public void sendUserList(ClientHandler requester) {
        StringBuilder builder = new StringBuilder();

        synchronized (clients) {
            for (ClientHandler client : clients) {
                builder.append("- ").append(client.getUsername()).append("\n");
            }
        }

        ChatMessage response = new ChatMessage();
        response.setType(ProtocolMessageType.USERS_RESPONSE);
        response.setSender("SERVER");
        response.setReceiver(requester.getUsername());
        response.setContent(builder.toString().trim());
        response.setTimestamp(TimeUtil.now());

        requester.sendMessage(JsonUtil.toJson(response));
    }

    public void announceJoin(String username, ClientHandler sender) {
        ChatMessage msg = new ChatMessage(
                ProtocolMessageType.SYSTEM_MESSAGE,
                "SERVER",
                null,
                null,
                username + " has joined the chat.",
                TimeUtil.now()
        );

        String json = JsonUtil.toJson(msg);

        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(json);
                }
            }
        }
    }

    public void announceLeave(String username, ClientHandler sender) {
        ChatMessage msg = new ChatMessage(
                ProtocolMessageType.SYSTEM_MESSAGE,
                "SERVER",
                null,
                null,
                username + " has left the chat.",
                TimeUtil.now()
        );

        String json = JsonUtil.toJson(msg);

        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(json);
                }
            }
        }
    }

    public void sendRoomMessage(ChatMessage message, ClientHandler sender, RoomDAO roomDAO) {
        if (message == null || message.getRoomName() == null || message.getRoomName().isBlank()) {
            return;
        }

        List<String> members = roomDAO.getRoomMembers(message.getRoomName());
        if (members == null || members.isEmpty()) {
            return;
        }

        String json = JsonUtil.toJson(message);

        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender && members.contains(client.getUsername())) {
                    client.sendMessage(json);
                }
            }
        }
    }

    public void sendTypingToPrivateTarget(ChatMessage message, ClientHandler sender) {
        ClientHandler recipient = findOnlineUser(message.getReceiver());
        if (recipient != null) {
            recipient.sendMessage(JsonUtil.toJson(message));
        }
    }

    public void sendTypingToRoomMembers(ChatMessage message, ClientHandler sender, RoomDAO roomDAO) {
        if (message.getRoomName() == null || message.getRoomName().isBlank()) {
            return;
        }

        List<String> members = roomDAO.getRoomMembers(message.getRoomName());
        String json = JsonUtil.toJson(message);

        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender && members.contains(client.getUsername())) {
                    client.sendMessage(json);
                }
            }
        }
    }

    public void broadcastPresenceUpdate(String username, String status, ClientHandler sender) {
        ChatMessage msg = new ChatMessage();
        msg.setType(ProtocolMessageType.PRESENCE_UPDATE);
        msg.setSender(username);
        msg.setStatus(status);
        msg.setTimestamp(TimeUtil.now());

        String json = JsonUtil.toJson(msg);

        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(json);
                }
            }
        }
    }
}