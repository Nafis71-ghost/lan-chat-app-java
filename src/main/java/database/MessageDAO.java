package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public void saveMessage(String sender, String receiver, String content, String type, String timestamp) {
        String sql = "INSERT INTO messages(sender, receiver, content, message_type, timestamp) VALUES(?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, sender);
            ps.setString(2, receiver);
            ps.setString(3, content);
            ps.setString(4, type);
            ps.setString(5, timestamp);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRecentPublicMessages(int limit) {
        String sql = """
                SELECT sender, content, timestamp
                FROM messages
                WHERE message_type = 'PUBLIC_MESSAGE'
                ORDER BY id DESC
                LIMIT ?
                """;

        List<String> lines = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sender = rs.getString("sender");
                    String content = rs.getString("content");
                    String timestamp = rs.getString("timestamp");
                    lines.add(0, "[" + timestamp + "] [" + sender + "]: " + content);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (lines.isEmpty()) {
            return "No public chat history yet.";
        }

        return String.join("\n", lines);
    }
}