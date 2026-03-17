package client;

import client.ui.ChatFrame;
import model.ChatMessage;

import javax.swing.SwingUtilities;

public class ServerListener implements Runnable {
    private final ClientConnection connection;
    private final ChatFrame chatFrame;

    public ServerListener(ClientConnection connection, ChatFrame chatFrame) {
        this.connection = connection;
        this.chatFrame = chatFrame;
    }

    @Override
    public void run() {
        try {
            ChatMessage message;
            while ((message = connection.readMessage()) != null) {
                ChatMessage finalMessage = message;
                SwingUtilities.invokeLater(() -> chatFrame.handleIncomingMessage(finalMessage));
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(chatFrame::handleServerDisconnected);
        }
    }
}