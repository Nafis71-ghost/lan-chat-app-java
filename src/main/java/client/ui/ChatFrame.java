package client.ui;

import client.ClientConnection;
import client.ServerListener;
import model.ChatMessage;
import protocol.ProtocolMessageType;
import util.Constants;
import util.TimeUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

public class ChatFrame extends JFrame {
    private final ClientConnection connection;
    private final String username;

    private JTabbedPane chatTabs;

    private JPanel publicMessagesPanel;
    private JScrollPane publicScrollPane;

    private final Map<String, JPanel> privatePanels = new HashMap<>();
    private final Map<String, JScrollPane> privateScrollPanes = new HashMap<>();

    private final Map<String, JPanel> roomPanels = new HashMap<>();
    private final Map<String, JScrollPane> roomScrollPanes = new HashMap<>();

    private final Map<String, Integer> unreadCounts = new HashMap<>();

    private JTextField inputField;
    private JButton sendButton;
    private JButton usersButton;
    private JButton privateButton;
    private JButton createRoomButton;
    private JButton joinRoomButton;
    private JButton roomListButton;
    private JButton roomMembersButton;
    private JButton darkModeButton;
    private JLabel statusLabel;
    private JLabel subtitleLabel;
    private JLabel typingLabel = new JLabel(" ");

    private boolean manualDisconnect = false;

    private static final Color HEADER_BG = new Color(52, 73, 94);
    private static final Color HEADER_ACCENT = new Color(41, 128, 185);

    private static final Color SEND_COLOR = new Color(52, 152, 219);
    private static final Color USERS_COLOR = new Color(46, 204, 113);
    private static final Color PRIVATE_COLOR = new Color(142, 68, 173);
    private static final Color CREATE_ROOM_COLOR = new Color(230, 126, 34);
    private static final Color JOIN_ROOM_COLOR = new Color(241, 196, 15);
    private static final Color ROOM_LIST_COLOR = new Color(26, 188, 156);
    private static final Color ROOM_MEMBERS_COLOR = new Color(231, 76, 60);
    private static final Color DARK_TOGGLE_COLOR = new Color(44, 62, 80);

    private static final Color MY_BUBBLE = new Color(52, 152, 219);
    private static final Color SYSTEM_BUBBLE_LIGHT = new Color(236, 240, 241);
    private static final Color SYSTEM_BUBBLE_DARK = new Color(70, 73, 80);
    private static final Color ROOM_BUBBLE_LIGHT = new Color(232, 244, 252);
    private static final Color ROOM_BUBBLE_DARK = new Color(64, 78, 94);

    public ChatFrame(ClientConnection connection, String username) {
        super(Constants.APP_NAME + " - " + username);
        this.connection = connection;
        this.username = username;

        setAppIcon();
        initializeUI();
        startListener();
        addCloseHandler();
        applyTheme();
    }

    private void setAppIcon() {
        try {
            setIconImage(new ImageIcon("icon.png").getImage());
        } catch (Exception e) {
            System.out.println("Could not load app icon.");
        }
    }

    private void initializeUI() {
        setSize(1000, 780);
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                sendTypingEvent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                sendTypingEvent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                sendTypingEvent();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setBackground(HEADER_BG);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(HEADER_BG);
        topBar.setBorder(new EmptyBorder(14, 18, 8, 18));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(Constants.APP_NAME);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        subtitleLabel = new JLabel("Same Wi-Fi messenger platform");
        subtitleLabel.setForeground(new Color(210, 220, 230));
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(subtitleLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        statusLabel = new JLabel("● Connected as " + username);
        statusLabel.setForeground(new Color(46, 204, 113));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        rightPanel.add(statusLabel);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionBar.setBackground(HEADER_ACCENT);
        actionBar.setBorder(new EmptyBorder(4, 12, 6, 12));

        createRoomButton = createStyledButton("Create Room", CREATE_ROOM_COLOR);
        joinRoomButton = createStyledButton("Join Room", JOIN_ROOM_COLOR);
        roomListButton = createStyledButton("Room List", ROOM_LIST_COLOR);
        roomMembersButton = createStyledButton("Room Members", ROOM_MEMBERS_COLOR);
        usersButton = createStyledButton("Active Users", USERS_COLOR);
        privateButton = createStyledButton("Private Msg", PRIVATE_COLOR);
        darkModeButton = createStyledButton("Dark Mode", DARK_TOGGLE_COLOR);

        actionBar.add(createRoomButton);
        actionBar.add(joinRoomButton);
        actionBar.add(roomListButton);
        actionBar.add(roomMembersButton);
        actionBar.add(usersButton);
        actionBar.add(privateButton);
        actionBar.add(darkModeButton);

        headerWrapper.add(topBar, BorderLayout.NORTH);
        headerWrapper.add(actionBar, BorderLayout.SOUTH);

        createRoomButton.addActionListener(e -> createRoom());
        joinRoomButton.addActionListener(e -> joinRoom());
        roomListButton.addActionListener(e -> requestRoomList());
        roomMembersButton.addActionListener(e -> requestRoomMembers());
        usersButton.addActionListener(e -> requestUsers());
        privateButton.addActionListener(e -> openPrivateTabDialog());
        darkModeButton.addActionListener(e -> {
            ThemeManager.toggle();
            applyTheme();
        });

        return headerWrapper;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(14, 14, 10, 14));

        chatTabs = new JTabbedPane();
        chatTabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chatTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        chatTabs.addChangeListener(e -> {
            String currentTitle = getCurrentBaseTabTitle();
            if (currentTitle != null) {
                clearUnread(currentTitle);
            }

            if (typingLabel != null) {
                typingLabel.setText(" ");
            }

            sendStopTypingForCurrentTab();
        });

        publicMessagesPanel = createMessagesPanel();
        publicScrollPane = createStyledScrollPane(publicMessagesPanel);
        chatTabs.addTab("Public Chat", publicScrollPane);

        addSystemBubble(publicMessagesPanel, publicScrollPane,
                "Welcome, " + username + "!");
        addSystemBubble(publicMessagesPanel, publicScrollPane,
                "You are now connected to the local LAN chat platform.");
        addSystemBubble(publicMessagesPanel, publicScrollPane,
                "Use the buttons above to open private chats, create rooms, and explore active users.");

        centerPanel.add(chatTabs, BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setBorder(new EmptyBorder(8, 14, 14, 14));

        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setOpaque(false);

        typingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 216, 224), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        inputField = new JTextField();
        inputField.setBorder(null);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        inputField.addActionListener(e -> sendCurrentMessage());

        sendButton = createStyledButton("Send", SEND_COLOR);
        sendButton.setPreferredSize(new Dimension(100, 42));
        sendButton.addActionListener(e -> sendCurrentMessage());

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        outer.add(typingLabel, BorderLayout.NORTH);
        outer.add(inputPanel, BorderLayout.CENTER);

        bottomWrapper.add(outer, BorderLayout.CENTER);
        return bottomWrapper;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(8, 14, 8, 14));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private JScrollPane createStyledScrollPane(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 220, 228), 1, true),
                BorderFactory.createEmptyBorder()
        ));
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        return scrollPane;
    }

    private void startListener() {
        new Thread(new ServerListener(connection, this)).start();
    }

    private void addCloseHandler() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });
    }

    private void disconnect() {
        manualDisconnect = true;
        sendStopTypingForCurrentTab();
        connection.disconnect();
    }

    private void requestUsers() {
        ChatMessage request = new ChatMessage();
        request.setType(ProtocolMessageType.USERS_REQUEST);
        request.setSender(username);
        request.setTimestamp(TimeUtil.now());
        connection.send(request);
    }

    private void openPrivateTabDialog() {
        String recipient = JOptionPane.showInputDialog(
                this,
                "Enter username:",
                "Open Private Chat",
                JOptionPane.QUESTION_MESSAGE
        );

        if (recipient == null) {
            return;
        }

        recipient = recipient.trim();

        if (recipient.isEmpty()) {
            return;
        }

        if (recipient.equalsIgnoreCase(username)) {
            JOptionPane.showMessageDialog(this, "You cannot open chat with yourself.");
            return;
        }

        openPrivateTab(recipient);
    }

    private void openPrivateTab(String user) {
        String title = "Private: " + user;
        int index = findTabIndexByBaseTitle(title);

        if (index != -1) {
            chatTabs.setSelectedIndex(index);
            clearUnread(title);
            return;
        }

        JPanel panel = createMessagesPanel();
        JScrollPane scrollPane = createStyledScrollPane(panel);

        privatePanels.put(user, panel);
        privateScrollPanes.put(user, scrollPane);

        chatTabs.addTab(title, scrollPane);
        chatTabs.setSelectedIndex(chatTabs.getTabCount() - 1);

        addSystemBubble(panel, scrollPane, "Private conversation with " + user);
        clearUnread(title);
        applyTheme();
    }

    private void openRoomTab(String roomName) {
        String title = "Room: " + roomName;
        int index = findTabIndexByBaseTitle(title);

        if (index != -1) {
            chatTabs.setSelectedIndex(index);
            clearUnread(title);
            return;
        }

        JPanel panel = createMessagesPanel();
        JScrollPane scrollPane = createStyledScrollPane(panel);

        roomPanels.put(roomName, panel);
        roomScrollPanes.put(roomName, scrollPane);

        chatTabs.addTab(title, scrollPane);
        chatTabs.setSelectedIndex(chatTabs.getTabCount() - 1);

        addSystemBubble(panel, scrollPane, "Room chat: " + roomName);
        clearUnread(title);
        applyTheme();
    }

    private void createRoom() {
        String roomName = JOptionPane.showInputDialog(this, "Enter new room name:");

        if (roomName == null || roomName.trim().isEmpty()) {
            return;
        }

        roomName = roomName.trim();

        ChatMessage msg = new ChatMessage();
        msg.setType(ProtocolMessageType.CREATE_ROOM);
        msg.setSender(username);
        msg.setRoomName(roomName);
        msg.setTimestamp(TimeUtil.now());

        connection.send(msg);
        openRoomTab(roomName);
    }

    private void joinRoom() {
        String roomName = JOptionPane.showInputDialog(this, "Enter room name to join:");

        if (roomName == null || roomName.trim().isEmpty()) {
            return;
        }

        roomName = roomName.trim();

        ChatMessage msg = new ChatMessage();
        msg.setType(ProtocolMessageType.JOIN_ROOM);
        msg.setSender(username);
        msg.setRoomName(roomName);
        msg.setTimestamp(TimeUtil.now());

        connection.send(msg);
        openRoomTab(roomName);
    }

    private void requestRoomList() {
        ChatMessage msg = new ChatMessage();
        msg.setType(ProtocolMessageType.ROOM_LIST_REQUEST);
        msg.setSender(username);
        msg.setTimestamp(TimeUtil.now());

        connection.send(msg);
    }

    private void requestRoomMembers() {
        String currentTab = getCurrentBaseTabTitle();

        if (currentTab == null || !currentTab.startsWith("Room: ")) {
            JOptionPane.showMessageDialog(this, "Open a room tab first.");
            return;
        }

        String roomName = currentTab.substring("Room: ".length()).trim();

        ChatMessage msg = new ChatMessage();
        msg.setType(ProtocolMessageType.ROOM_MEMBERS_REQUEST);
        msg.setSender(username);
        msg.setRoomName(roomName);
        msg.setTimestamp(TimeUtil.now());

        connection.send(msg);
    }

    private void sendCurrentMessage() {
        String text = inputField.getText().trim();

        if (text.isEmpty()) {
            return;
        }

        String currentTab = getCurrentBaseTabTitle();

        if ("Public Chat".equals(currentTab)) {
            ChatMessage message = new ChatMessage(
                    ProtocolMessageType.PUBLIC_MESSAGE,
                    username,
                    null,
                    null,
                    text,
                    TimeUtil.now()
            );

            addMyBubble(publicMessagesPanel, publicScrollPane, text, message.getTimestamp());
            connection.send(message);

        } else if (currentTab != null && currentTab.startsWith("Private: ")) {
            String recipient = currentTab.substring("Private: ".length()).trim();

            ChatMessage message = new ChatMessage(
                    ProtocolMessageType.PRIVATE_MESSAGE,
                    username,
                    recipient,
                    null,
                    text,
                    TimeUtil.now()
            );

            JPanel panel = privatePanels.get(recipient);
            JScrollPane scrollPane = privateScrollPanes.get(recipient);
            if (panel != null && scrollPane != null) {
                addMyBubble(panel, scrollPane, text, message.getTimestamp());
            }

            connection.send(message);

        } else if (currentTab != null && currentTab.startsWith("Room: ")) {
            String roomName = currentTab.substring("Room: ".length()).trim();

            ChatMessage message = new ChatMessage(
                    ProtocolMessageType.ROOM_MESSAGE,
                    username,
                    null,
                    roomName,
                    text,
                    TimeUtil.now()
            );

            JPanel panel = roomPanels.get(roomName);
            JScrollPane scrollPane = roomScrollPanes.get(roomName);
            if (panel != null && scrollPane != null) {
                addMyBubble(panel, scrollPane, text, message.getTimestamp());
            }

            connection.send(message);
        }

        inputField.setText("");
        if (typingLabel != null) {
            typingLabel.setText(" ");
        }
        sendStopTypingForCurrentTab();
    }

    private void sendTypingEvent() {
        String currentTab = getCurrentBaseTabTitle();
        if (currentTab == null) {
            return;
        }

        String text = inputField.getText().trim();
        boolean typing = !text.isEmpty();

        ChatMessage msg = new ChatMessage();
        msg.setType(typing ? ProtocolMessageType.TYPING : ProtocolMessageType.STOP_TYPING);
        msg.setSender(username);
        msg.setTimestamp(TimeUtil.now());

        if (currentTab.startsWith("Private: ")) {
            msg.setReceiver(currentTab.substring("Private: ".length()).trim());
            connection.send(msg);
        } else if (currentTab.startsWith("Room: ")) {
            msg.setRoomName(currentTab.substring("Room: ".length()).trim());
            connection.send(msg);
        }
    }

    private void sendStopTypingForCurrentTab() {
        String currentTab = getCurrentBaseTabTitle();
        if (currentTab == null) {
            return;
        }

        ChatMessage msg = new ChatMessage();
        msg.setType(ProtocolMessageType.STOP_TYPING);
        msg.setSender(username);
        msg.setTimestamp(TimeUtil.now());

        if (currentTab.startsWith("Private: ")) {
            msg.setReceiver(currentTab.substring("Private: ".length()).trim());
            connection.send(msg);
        } else if (currentTab.startsWith("Room: ")) {
            msg.setRoomName(currentTab.substring("Room: ".length()).trim());
            connection.send(msg);
        }
    }

    public void handleIncomingMessage(ChatMessage message) {
        if (message == null || message.getType() == null) {
            return;
        }

        switch (message.getType()) {
            case HISTORY_RESPONSE -> {
                addSystemBubble(publicMessagesPanel, publicScrollPane, "Recent Public History");
                String content = message.getContent() == null ? "" : message.getContent();
                if (!content.isBlank()) {
                    String[] lines = content.split("\n");
                    for (String line : lines) {
                        addOtherBubble(publicMessagesPanel, publicScrollPane, "History", line, message.getTimestamp(), ThemeManager.otherBubble());
                    }
                }
            }

            case PUBLIC_MESSAGE -> {
                addOtherBubble(
                        publicMessagesPanel,
                        publicScrollPane,
                        message.getSender(),
                        message.getContent(),
                        message.getTimestamp(),
                        ThemeManager.otherBubble()
                );

                String currentTab = getCurrentBaseTabTitle();
                if (currentTab == null || !currentTab.equals("Public Chat")) {
                    incrementUnread("Public Chat");
                }
            }

            case PRIVATE_MESSAGE -> {
                String otherUser = message.getSender();

                if (otherUser == null || otherUser.isBlank()) {
                    return;
                }

                String tabTitle = "Private: " + otherUser;

                if (!privatePanels.containsKey(otherUser)) {
                    openPrivateTabSilently(otherUser);
                }

                JPanel panel = privatePanels.get(otherUser);
                JScrollPane scrollPane = privateScrollPanes.get(otherUser);
                if (panel != null && scrollPane != null) {
                    addOtherBubble(panel, scrollPane, otherUser, message.getContent(), message.getTimestamp(), ThemeManager.otherBubble());
                }

                String currentTab = getCurrentBaseTabTitle();
                if (currentTab == null || !currentTab.equals(tabTitle)) {
                    incrementUnread(tabTitle);
                }
            }

            case ROOM_MESSAGE -> {
                String roomName = message.getRoomName();

                if (roomName == null || roomName.isBlank()) {
                    return;
                }

                String tabTitle = "Room: " + roomName;

                if (!roomPanels.containsKey(roomName)) {
                    openRoomTabSilently(roomName);
                }

                JPanel panel = roomPanels.get(roomName);
                JScrollPane scrollPane = roomScrollPanes.get(roomName);
                if (panel != null && scrollPane != null) {
                    addOtherBubble(panel, scrollPane, message.getSender(), message.getContent(), message.getTimestamp(), roomBubbleColor());
                }

                String currentTab = getCurrentBaseTabTitle();
                if (currentTab == null || !currentTab.equals(tabTitle)) {
                    incrementUnread(tabTitle);
                }
            }

            case USERS_RESPONSE -> {
                addSystemBubble(publicMessagesPanel, publicScrollPane, "Active Users\n" + message.getContent());
            }

            case ROOM_LIST_RESPONSE -> {
                addSystemBubble(publicMessagesPanel, publicScrollPane, "Rooms\n" + message.getContent());
            }

            case ROOM_MEMBERS_RESPONSE -> {
                addSystemBubble(publicMessagesPanel, publicScrollPane,
                        "Members of room " + message.getRoomName() + "\n" + message.getContent());
            }

            case SYSTEM_MESSAGE -> {
                addSystemBubble(publicMessagesPanel, publicScrollPane, message.getContent());
            }

            case TYPING -> {
                if (message.getSender() != null && !message.getSender().equalsIgnoreCase(username)) {
                    if (message.getRoomName() != null && !message.getRoomName().isBlank()) {
                        showTyping(message.getSender() + " is typing in room " + message.getRoomName() + "...");
                    } else {
                        showTyping(message.getSender() + " is typing...");
                    }
                }
            }

            case STOP_TYPING -> {
                if (typingLabel != null) {
                    typingLabel.setText(" ");
                }
            }

            case PRESENCE_UPDATE -> {
                addSystemBubble(
                        publicMessagesPanel,
                        publicScrollPane,
                        message.getSender() + " is now " + message.getStatus()
                );
            }

            default -> {
            }
        }
    }

    public void handleServerDisconnected() {
        if (!manualDisconnect) {
            addSystemBubble(publicMessagesPanel, publicScrollPane, "SERVER DISCONNECTED");
            JOptionPane.showMessageDialog(this, "Disconnected from server.");
        }
    }

    private void openPrivateTabSilently(String user) {
        String title = "Private: " + user;
        int index = findTabIndexByBaseTitle(title);

        if (index != -1) {
            return;
        }

        JPanel panel = createMessagesPanel();
        JScrollPane scrollPane = createStyledScrollPane(panel);

        privatePanels.put(user, panel);
        privateScrollPanes.put(user, scrollPane);

        chatTabs.addTab(title, scrollPane);
        addSystemBubble(panel, scrollPane, "Private conversation with " + user);
        applyTheme();
    }

    private void openRoomTabSilently(String roomName) {
        String title = "Room: " + roomName;
        int index = findTabIndexByBaseTitle(title);

        if (index != -1) {
            return;
        }

        JPanel panel = createMessagesPanel();
        JScrollPane scrollPane = createStyledScrollPane(panel);

        roomPanels.put(roomName, panel);
        roomScrollPanes.put(roomName, scrollPane);

        chatTabs.addTab(title, scrollPane);
        addSystemBubble(panel, scrollPane, "Room chat: " + roomName);
        applyTheme();
    }

    private void incrementUnread(String tabTitle) {
        int current = unreadCounts.getOrDefault(tabTitle, 0);
        unreadCounts.put(tabTitle, current + 1);
        updateTabTitle(tabTitle);
    }

    private void clearUnread(String tabTitle) {
        unreadCounts.remove(tabTitle);
        updateTabTitle(tabTitle);
    }

    private void updateTabTitle(String originalTitle) {
        int index = findTabIndexByBaseTitle(originalTitle);
        if (index == -1) {
            return;
        }

        int count = unreadCounts.getOrDefault(originalTitle, 0);

        if (count > 0) {
            chatTabs.setTitleAt(index, originalTitle + " (" + count + ")");
        } else {
            chatTabs.setTitleAt(index, originalTitle);
        }
    }

    private int findTabIndexByBaseTitle(String baseTitle) {
        for (int i = 0; i < chatTabs.getTabCount(); i++) {
            String title = chatTabs.getTitleAt(i);
            if (title.equals(baseTitle) || title.startsWith(baseTitle + " (")) {
                return i;
            }
        }
        return -1;
    }

    private String getCurrentBaseTabTitle() {
        if (chatTabs == null || chatTabs.getTabCount() == 0) {
            return null;
        }

        int index = chatTabs.getSelectedIndex();
        if (index == -1) {
            return null;
        }

        String title = chatTabs.getTitleAt(index);
        return title.replaceAll("\\s\\(\\d+\\)$", "");
    }

    private void addMyBubble(JPanel container, JScrollPane scrollPane, String text, String time) {
        addBubble(container, scrollPane, "Me", text, time, true, MY_BUBBLE, Color.WHITE);
    }

    private void addOtherBubble(JPanel container, JScrollPane scrollPane, String sender, String text, String time, Color bubbleColor) {
        addBubble(container, scrollPane, sender, text, time, false, bubbleColor, ThemeManager.text());
    }

    private void addSystemBubble(JPanel container, JScrollPane scrollPane, String text) {
        addBubble(container, scrollPane, "System", text, "", false, systemBubbleColor(), ThemeManager.text());
    }

    private void addBubble(
            JPanel container,
            JScrollPane scrollPane,
            String sender,
            String text,
            String time,
            boolean mine,
            Color bubbleColor,
            Color textColor
    ) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel alignPanel = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        alignPanel.setOpaque(false);

        RoundedPanel bubble = new RoundedPanel(bubbleColor, 18);
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel senderLabel = new JLabel(sender == null ? "" : sender);
        senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        senderLabel.setForeground(mine ? new Color(230, 245, 255) : mutedTextColor());

        JLabel textLabel = new JLabel(toHtml(text));
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setForeground(textColor);

        JLabel timeLabel = new JLabel(time == null ? "" : time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(mine ? new Color(220, 235, 250) : secondaryTextColor());

        bubble.add(senderLabel);
        bubble.add(Box.createVerticalStrut(4));
        bubble.add(textLabel);

        if (time != null && !time.isBlank()) {
            bubble.add(Box.createVerticalStrut(6));
            bubble.add(timeLabel);
        }

        bubble.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));
        alignPanel.add(bubble);

        row.add(alignPanel, BorderLayout.CENTER);
        container.add(row);
        container.add(Box.createVerticalStrut(6));

        container.revalidate();
        container.repaint();

        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(
                        scrollPane.getVerticalScrollBar().getMaximum()
                )
        );
    }

    private String toHtml(String text) {
        if (text == null) {
            text = "";
        }
        String escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<html><body style='width: 260px; margin: 0; padding: 0;'>" + escaped + "</body></html>";
    }

    private void applyTheme() {
        getContentPane().setBackground(ThemeManager.frameBg());

        if (publicMessagesPanel != null) {
            publicMessagesPanel.setBackground(ThemeManager.chatBg());
        }
        if (publicScrollPane != null) {
            publicScrollPane.getViewport().setBackground(ThemeManager.chatBg());
        }

        for (JPanel p : privatePanels.values()) {
            p.setBackground(ThemeManager.chatBg());
        }
        for (JScrollPane sp : privateScrollPanes.values()) {
            sp.getViewport().setBackground(ThemeManager.chatBg());
        }

        for (JPanel p : roomPanels.values()) {
            p.setBackground(ThemeManager.chatBg());
        }
        for (JScrollPane sp : roomScrollPanes.values()) {
            sp.getViewport().setBackground(ThemeManager.chatBg());
        }

        if (chatTabs != null) {
            chatTabs.setBackground(ThemeManager.frameBg());
            chatTabs.setForeground(ThemeManager.text());
        }

        if (inputField != null) {
            inputField.setBackground(ThemeManager.otherBubble());
            inputField.setForeground(ThemeManager.text());
            inputField.setCaretColor(ThemeManager.text());
        }

        if (typingLabel != null) {
            typingLabel.setForeground(ThemeManager.isDark()
                    ? new Color(180, 185, 195)
                    : new Color(120, 130, 145));
        }

        repaint();
    }

    private void showTyping(String text) {
        if (typingLabel == null) {
            return;
        }

        typingLabel.setText(text);

        javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
            if (typingLabel != null) {
                typingLabel.setText(" ");
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private Color systemBubbleColor() {
        return ThemeManager.isDark() ? SYSTEM_BUBBLE_DARK : SYSTEM_BUBBLE_LIGHT;
    }

    private Color roomBubbleColor() {
        return ThemeManager.isDark() ? ROOM_BUBBLE_DARK : ROOM_BUBBLE_LIGHT;
    }

    private Color mutedTextColor() {
        return ThemeManager.isDark()
                ? new Color(190, 195, 205)
                : new Color(90, 100, 120);
    }

    private Color secondaryTextColor() {
        return ThemeManager.isDark()
                ? new Color(170, 175, 185)
                : new Color(130, 140, 150);
    }

    private static class RoundedPanel extends JPanel {
        private final Color backgroundColor;
        private final int cornerRadius;

        public RoundedPanel(Color backgroundColor, int cornerRadius) {
            this.backgroundColor = backgroundColor;
            this.cornerRadius = cornerRadius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(backgroundColor);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        }

        @Override
        public Insets getInsets() {
            return new Insets(8, 10, 8, 10);
        }
    }
}