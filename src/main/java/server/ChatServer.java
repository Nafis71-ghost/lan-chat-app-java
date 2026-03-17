package server;

import database.DatabaseInitializer;
import util.Constants;

import javax.swing.JOptionPane;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private final ClientManager clientManager = new ClientManager();

    public void start() {
        DatabaseInitializer.initialize();

        System.out.println("Starting LAN Chat Server...");

        try (ServerSocket serverSocket = new ServerSocket(Constants.PORT)) {
            String ip = InetAddress.getLocalHost().getHostAddress();

            String message = "Server is running.\nIP: " + ip + "\nPort: " + Constants.PORT;
            System.out.println(message);

            JOptionPane.showMessageDialog(
                    null,
                    message,
                    "LAN Chat Server Started",
                    JOptionPane.INFORMATION_MESSAGE
            );

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, clientManager);
                new Thread(handler).start();
            }

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "Server failed to start:\n" + e.getMessage(),
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        new ChatServer().start();
    }
}