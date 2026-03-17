package server;

import database.MessageDAO;
import database.RoomDAO;
import database.UserDAO;
import model.ChatMessage;
import protocol.JsonUtil;
import protocol.ProtocolMessageType;
import util.Constants;
import util.TimeUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ClientManager clientManager;
    private final UserDAO userDAO = new UserDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private boolean authenticated = false;
    private boolean cleanedUp = false;

    public ClientHandler(Socket socket, ClientManager clientManager) {
        this.socket = socket;
        this.clientManager = clientManager;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            if (!handleAuthentication()) {
                cleanup();
                return;
            }

            clientManager.addClient(this);
            clientManager.announceJoin(username, this);
            clientManager.broadcastPresenceUpdate(username, "ONLINE", this);
            sendPublicHistory();

            String line;
            while ((line = reader.readLine()) != null) {
                ChatMessage message = JsonUtil.fromJson(line);

                if (message == null || message.getType() == null) {
                    sendSystemMessage("Invalid message format.");
                    continue;
                }

                switch (message.getType()) {
                    case PUBLIC_MESSAGE -> handlePublicMessage(message);
                    case PRIVATE_MESSAGE -> handlePrivateMessage(message);
                    case USERS_REQUEST -> clientManager.sendUserList(this);

                    case CREATE_ROOM -> handleCreateRoom(message);
                    case JOIN_ROOM -> handleJoinRoom(message);
                    case LEAVE_ROOM -> handleLeaveRoom(message);
                    case ROOM_LIST_REQUEST -> handleRoomListRequest();
                    case ROOM_MEMBERS_REQUEST -> handleRoomMembersRequest(message);
                    case ROOM_MESSAGE -> handleRoomMessage(message);

                    case TYPING -> handleTyping(message);
                    case STOP_TYPING -> handleStopTyping(message);

                    case DISCONNECT -> {
                        clientManager.broadcastPresenceUpdate(username, "OFFLINE", this);
                        clientManager.announceLeave(username, this);
                        cleanup();
                        return;
                    }

                    default -> sendSystemMessage("Unsupported message type.");
                }
            }

        } catch (Exception e) {
            if (authenticated && username != null && !username.isBlank()) {
                clientManager.broadcastPresenceUpdate(username, "OFFLINE", this);
                clientManager.announceLeave(username, this);
            }
        } finally {
            cleanup();
        }
    }

    private boolean handleAuthentication() {
        try {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                return false;
            }

            ChatMessage request = JsonUtil.fromJson(firstLine);
            if (request == null || request.getType() == null) {
                sendAuthResponse(false, "Invalid authentication request.");
                return false;
            }

            String requestedUsername = request.getUsername() == null
                    ? ""
                    : request.getUsername().trim();

            String password = request.getPassword() == null
                    ? ""
                    : request.getPassword().trim();

            if (requestedUsername.isEmpty() || password.isEmpty()) {
                sendAuthResponse(false, "Username and password are required.");
                return false;
            }

            if (clientManager.isUserOnline(requestedUsername)) {
                sendAuthResponse(false, "This user is already logged in.");
                return false;
            }

            if (request.getType() == ProtocolMessageType.SIGNUP) {
                if (userDAO.userExists(requestedUsername)) {
                    sendAuthResponse(false, "Username already exists.");
                    return false;
                }

                boolean created = userDAO.createUser(requestedUsername, password);
                if (!created) {
                    sendAuthResponse(false, "Signup failed.");
                    return false;
                }

                username = requestedUsername;
                authenticated = true;
                sendAuthResponse(true, "Signup successful.");
                return true;
            }

            if (request.getType() == ProtocolMessageType.LOGIN) {
                boolean valid = userDAO.login(requestedUsername, password);
                if (!valid) {
                    sendAuthResponse(false, "Invalid username or password.");
                    return false;
                }

                username = requestedUsername;
                authenticated = true;
                sendAuthResponse(true, "Login successful.");
                return true;
            }

            sendAuthResponse(false, "Unsupported authentication type.");
            return false;

        } catch (Exception e) {
            sendAuthResponse(false, "Authentication error.");
            return false;
        }
    }

    private void sendAuthResponse(boolean success, String text) {
        ChatMessage response = new ChatMessage();
        response.setType(ProtocolMessageType.AUTH_RESPONSE);
        response.setSender("SERVER");
        response.setReceiver(username);
        response.setContent(text);
        response.setTimestamp(TimeUtil.now());
        response.setSuccess(success);
        response.setUsername(username);

        sendMessage(JsonUtil.toJson(response));
    }

    private void sendPublicHistory() {
        String history = messageDAO.getRecentPublicMessages(Constants.PUBLIC_HISTORY_LIMIT);

        ChatMessage historyMessage = new ChatMessage();
        historyMessage.setType(ProtocolMessageType.HISTORY_RESPONSE);
        historyMessage.setSender("SERVER");
        historyMessage.setReceiver(username);
        historyMessage.setContent(history);
        historyMessage.setTimestamp(TimeUtil.now());

        sendMessage(JsonUtil.toJson(historyMessage));
    }

    private void handlePublicMessage(ChatMessage message) {
        String content = message.getContent() == null ? "" : message.getContent().trim();

        if (content.isEmpty()) {
            sendSystemMessage("Message cannot be empty.");
            return;
        }

        ChatMessage outgoing = new ChatMessage(
                ProtocolMessageType.PUBLIC_MESSAGE,
                username,
                null,
                null,
                content,
                TimeUtil.now()
        );

        messageDAO.saveMessage(
                outgoing.getSender(),
                null,
                outgoing.getContent(),
                outgoing.getType().name(),
                outgoing.getTimestamp()
        );

        clientManager.broadcastPublicMessage(outgoing, this);
    }

    private void handlePrivateMessage(ChatMessage message) {
        String receiver = message.getReceiver() == null ? "" : message.getReceiver().trim();
        String content = message.getContent() == null ? "" : message.getContent().trim();

        if (receiver.isEmpty()) {
            sendSystemMessage("Receiver is required.");
            return;
        }

        if (content.isEmpty()) {
            sendSystemMessage("Message cannot be empty.");
            return;
        }

        if (receiver.equalsIgnoreCase(username)) {
            sendSystemMessage("You cannot message yourself.");
            return;
        }

        ChatMessage outgoing = new ChatMessage(
                ProtocolMessageType.PRIVATE_MESSAGE,
                username,
                receiver,
                null,
                content,
                TimeUtil.now()
        );

        boolean sent = clientManager.sendPrivateMessage(outgoing, this);

        if (!sent) {
            sendSystemMessage("User not found or offline.");
            return;
        }

        messageDAO.saveMessage(
                outgoing.getSender(),
                outgoing.getReceiver(),
                outgoing.getContent(),
                outgoing.getType().name(),
                outgoing.getTimestamp()
        );
    }

    private void handleCreateRoom(ChatMessage message) {
        String roomName = message.getRoomName() == null ? "" : message.getRoomName().trim();

        if (roomName.isEmpty()) {
            sendSystemMessage("Room name is required.");
            return;
        }

        if (roomDAO.roomExists(roomName)) {
            sendSystemMessage("Room already exists.");
            return;
        }

        boolean created = roomDAO.createRoom(roomName, username);
        if (!created) {
            sendSystemMessage("Failed to create room.");
            return;
        }

        roomDAO.joinRoom(roomName, username);
        sendSystemMessage("Room created successfully: " + roomName);
    }

    private void handleJoinRoom(ChatMessage message) {
        String roomName = message.getRoomName() == null ? "" : message.getRoomName().trim();

        if (roomName.isEmpty()) {
            sendSystemMessage("Room name is required.");
            return;
        }

        if (!roomDAO.roomExists(roomName)) {
            sendSystemMessage("Room does not exist.");
            return;
        }

        boolean joined = roomDAO.joinRoom(roomName, username);
        if (!joined) {
            sendSystemMessage("Failed to join room.");
            return;
        }

        sendSystemMessage("Joined room: " + roomName);
    }

    private void handleLeaveRoom(ChatMessage message) {
        String roomName = message.getRoomName() == null ? "" : message.getRoomName().trim();

        if (roomName.isEmpty()) {
            sendSystemMessage("Room name is required.");
            return;
        }

        boolean left = roomDAO.leaveRoom(roomName, username);
        if (!left) {
            sendSystemMessage("You are not in that room.");
            return;
        }

        sendSystemMessage("Left room: " + roomName);
    }

    private void handleRoomListRequest() {
        List<String> rooms = roomDAO.getAllRooms();

        ChatMessage response = new ChatMessage();
        response.setType(ProtocolMessageType.ROOM_LIST_RESPONSE);
        response.setSender("SERVER");
        response.setReceiver(username);
        response.setContent(rooms.isEmpty() ? "No rooms found." : String.join("\n", rooms));
        response.setTimestamp(TimeUtil.now());

        sendMessage(JsonUtil.toJson(response));
    }

    private void handleRoomMembersRequest(ChatMessage message) {
        String roomName = message.getRoomName() == null ? "" : message.getRoomName().trim();

        if (roomName.isEmpty()) {
            sendSystemMessage("Room name is required.");
            return;
        }

        if (!roomDAO.roomExists(roomName)) {
            sendSystemMessage("Room does not exist.");
            return;
        }

        List<String> members = roomDAO.getRoomMembers(roomName);

        ChatMessage response = new ChatMessage();
        response.setType(ProtocolMessageType.ROOM_MEMBERS_RESPONSE);
        response.setSender("SERVER");
        response.setReceiver(username);
        response.setRoomName(roomName);
        response.setContent(members.isEmpty() ? "No members found." : String.join("\n", members));
        response.setTimestamp(TimeUtil.now());

        sendMessage(JsonUtil.toJson(response));
    }

    private void handleRoomMessage(ChatMessage message) {
        String roomName = message.getRoomName() == null ? "" : message.getRoomName().trim();
        String content = message.getContent() == null ? "" : message.getContent().trim();

        if (roomName.isEmpty()) {
            sendSystemMessage("Room name is required.");
            return;
        }

        if (content.isEmpty()) {
            sendSystemMessage("Message cannot be empty.");
            return;
        }

        if (!roomDAO.roomExists(roomName)) {
            sendSystemMessage("Room does not exist.");
            return;
        }

        if (!roomDAO.isUserInRoom(roomName, username)) {
            sendSystemMessage("You must join the room first.");
            return;
        }

        ChatMessage outgoing = new ChatMessage(
                ProtocolMessageType.ROOM_MESSAGE,
                username,
                null,
                roomName,
                content,
                TimeUtil.now()
        );

        messageDAO.saveMessage(
                outgoing.getSender(),
                roomName,
                outgoing.getContent(),
                outgoing.getType().name(),
                outgoing.getTimestamp()
        );

        clientManager.sendRoomMessage(outgoing, this, roomDAO);
    }

    private void handleTyping(ChatMessage message) {
        if (message.getReceiver() != null && !message.getReceiver().isBlank()) {
            ChatMessage typingMsg = new ChatMessage();
            typingMsg.setType(ProtocolMessageType.TYPING);
            typingMsg.setSender(username);
            typingMsg.setReceiver(message.getReceiver().trim());
            typingMsg.setTimestamp(TimeUtil.now());

            clientManager.sendTypingToPrivateTarget(typingMsg, this);
            return;
        }

        if (message.getRoomName() != null && !message.getRoomName().isBlank()) {
            String roomName = message.getRoomName().trim();

            if (!roomDAO.roomExists(roomName) || !roomDAO.isUserInRoom(roomName, username)) {
                return;
            }

            ChatMessage typingMsg = new ChatMessage();
            typingMsg.setType(ProtocolMessageType.TYPING);
            typingMsg.setSender(username);
            typingMsg.setRoomName(roomName);
            typingMsg.setTimestamp(TimeUtil.now());

            clientManager.sendTypingToRoomMembers(typingMsg, this, roomDAO);
        }
    }

    private void handleStopTyping(ChatMessage message) {
        if (message.getReceiver() != null && !message.getReceiver().isBlank()) {
            ChatMessage stopMsg = new ChatMessage();
            stopMsg.setType(ProtocolMessageType.STOP_TYPING);
            stopMsg.setSender(username);
            stopMsg.setReceiver(message.getReceiver().trim());
            stopMsg.setTimestamp(TimeUtil.now());

            clientManager.sendTypingToPrivateTarget(stopMsg, this);
            return;
        }

        if (message.getRoomName() != null && !message.getRoomName().isBlank()) {
            String roomName = message.getRoomName().trim();

            if (!roomDAO.roomExists(roomName) || !roomDAO.isUserInRoom(roomName, username)) {
                return;
            }

            ChatMessage stopMsg = new ChatMessage();
            stopMsg.setType(ProtocolMessageType.STOP_TYPING);
            stopMsg.setSender(username);
            stopMsg.setRoomName(roomName);
            stopMsg.setTimestamp(TimeUtil.now());

            clientManager.sendTypingToRoomMembers(stopMsg, this, roomDAO);
        }
    }

    private void sendSystemMessage(String text) {
        ChatMessage msg = new ChatMessage();
        msg.setType(ProtocolMessageType.SYSTEM_MESSAGE);
        msg.setSender("SERVER");
        msg.setReceiver(username);
        msg.setContent(text);
        msg.setTimestamp(TimeUtil.now());

        sendMessage(JsonUtil.toJson(msg));
    }

    public void sendMessage(String json) {
        if (writer != null) {
            writer.println(json);
        }
    }

    public String getUsername() {
        return username;
    }

    private void cleanup() {
        if (cleanedUp) {
            return;
        }
        cleanedUp = true;

        try {
            clientManager.removeClient(this);
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