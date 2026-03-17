package client.ui;

import client.ClientConnection;
import model.ChatMessage;
import protocol.ProtocolMessageType;
import util.Constants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public class LoginFrame extends JFrame {

    private JTextField ipField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton signupButton;

    private JLabel statusLabel;

    public LoginFrame() {
        super(Constants.APP_NAME + " Login");
        setAppIcon();
        initializeUI();
        setVisible(true);
    }

    private void setAppIcon() {
        try {
            setIconImage(new ImageIcon("icon.png").getImage());
        } catch (Exception e) {
            System.out.println("Could not load app icon.");
        }
    }

    private void initializeUI() {
        setSize(520, 430);
        setMinimumSize(new Dimension(480, 400));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(createBottom(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(52, 73, 94));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(52, 73, 94));
        content.setBorder(new EmptyBorder(20, 20, 18, 20));

        JLabel title = new JLabel(Constants.APP_NAME);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("LAN Messenger - Same WiFi Chat");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(200, 210, 220));

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);

        JPanel accent = new JPanel();
        accent.setBackground(new Color(41, 128, 185));
        accent.setPreferredSize(new Dimension(0, 8));

        header.add(content, BorderLayout.CENTER);
        header.add(accent, BorderLayout.SOUTH);

        return header;
    }

    private JPanel createCenter() {
        JPanel wrapper = new JPanel(new GridLayout(1, 1));
        wrapper.setBorder(new EmptyBorder(25, 25, 10, 25));
        wrapper.setBackground(ThemeManager.frameBg());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeManager.otherBubble());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 220, 228), 1, true),
                new EmptyBorder(25, 25, 25, 25)
        ));

        JLabel title = new JLabel("Login or Sign Up");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ThemeManager.text());

        card.add(title);
        card.add(Box.createVerticalStrut(15));

        ipField = createField("192.168.1.100");
        usernameField = createField("");
        passwordField = createPassword();

        card.add(createBlock("Server IP", ipField));
        card.add(Box.createVerticalStrut(12));

        card.add(createBlock("Username", usernameField));
        card.add(Box.createVerticalStrut(12));

        card.add(createBlock("Password", passwordField));

        wrapper.add(card);
        return wrapper;
    }

    private JPanel createBottom() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(10, 25, 25, 25));
        wrapper.setBackground(ThemeManager.frameBg());

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeManager.otherBubble());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 220, 228), 1, true),
                new EmptyBorder(15, 20, 15, 20)
        ));

        statusLabel = new JLabel("Enter server IP, username and password");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(new Color(100, 110, 120));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);

        loginButton = createButton("Login", new Color(52, 152, 219));
        signupButton = createButton("Sign Up", new Color(46, 204, 113));

        loginButton.addActionListener(e -> authenticateAsync(ProtocolMessageType.LOGIN));
        signupButton.addActionListener(e -> authenticateAsync(ProtocolMessageType.SIGNUP));

        buttons.add(loginButton);
        buttons.add(signupButton);

        card.add(statusLabel, BorderLayout.NORTH);
        card.add(buttons, BorderLayout.CENTER);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createBlock(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(ThemeManager.text());

        panel.add(l, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JTextField createField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 216, 224), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return field;
    }

    private JPasswordField createPassword() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 216, 224), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return field;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 16, 10, 16));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(color.brighter());
                }
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void authenticateAsync(ProtocolMessageType type) {
        String ip = ipField.getText().trim();
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();

        if (ip.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required");
            return;
        }

        setBusyState(true);
        setStatus("Connecting...", new Color(52, 152, 219));

        SwingWorker<AuthResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AuthResult doInBackground() {
                try {
                    ClientConnection connection = new ClientConnection();
                    ChatMessage response = connection.authenticate(ip, Constants.PORT, type, user, pass);
                    return new AuthResult(connection, response, null);
                } catch (Exception e) {
                    return new AuthResult(null, null, e);
                }
            }

            @Override
            protected void done() {
                try {
                    AuthResult result = get();

                    if (result.error != null) {
                        result.error.printStackTrace();

                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "Could not connect / open chat:\n" + result.error.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );

                        setStatus("Connection failed", Color.RED);
                        setBusyState(false);
                        return;
                    }

                    if (result.response == null) {
                        JOptionPane.showMessageDialog(LoginFrame.this, "No response from server");
                        setStatus("No server response", Color.RED);
                        setBusyState(false);
                        return;
                    }

                    if (!result.response.isSuccess()) {
                        JOptionPane.showMessageDialog(LoginFrame.this, result.response.getContent());
                        setStatus(result.response.getContent(), Color.RED);

                        if (result.connection != null) {
                            result.connection.disconnect();
                        }

                        setBusyState(false);
                        return;
                    }

                    setStatus("Login successful!", new Color(46, 204, 113));

                    ChatFrame frame = new ChatFrame(result.connection, user);
                    frame.setVisible(true);
                    dispose();

                } catch (Exception e) {
                    e.printStackTrace();

                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Unexpected error:\n" + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                    setStatus("Unexpected error", Color.RED);
                    setBusyState(false);
                }
            }
        };

        worker.execute();
    }

    private void setBusyState(boolean busy) {
        loginButton.setEnabled(!busy);
        signupButton.setEnabled(!busy);
        ipField.setEnabled(!busy);
        usernameField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    private static class AuthResult {
        final ClientConnection connection;
        final ChatMessage response;
        final Exception error;

        AuthResult(ClientConnection connection, ChatMessage response, Exception error) {
            this.connection = connection;
            this.response = response;
            this.error = error;
        }
    }
}