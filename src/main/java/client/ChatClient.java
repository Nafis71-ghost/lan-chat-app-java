package client;

import client.ui.LoginFrame;

import javax.swing.SwingUtilities;

public class ChatClient {
    public void start() {
        SwingUtilities.invokeLater(LoginFrame::new);
    }

    public static void main(String[] args) {
        new ChatClient().start();
    }
}