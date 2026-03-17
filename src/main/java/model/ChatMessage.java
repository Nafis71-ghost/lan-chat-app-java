package model;

import protocol.ProtocolMessageType;

public class ChatMessage {
    private ProtocolMessageType type;
    private String sender;
    private String receiver;
    private String roomName;
    private String content;
    private String timestamp;
    private String username;
    private String password;
    private boolean success;
    private String status;

    public ChatMessage() {
    }

    public ChatMessage(ProtocolMessageType type,
                       String sender,
                       String receiver,
                       String roomName,
                       String content,
                       String timestamp) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.roomName = roomName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public ProtocolMessageType getType() {
        return type;
    }

    public void setType(ProtocolMessageType type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}