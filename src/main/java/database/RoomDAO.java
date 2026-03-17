package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public boolean createRoom(String roomName, String createdBy) {
        String sql = "INSERT INTO rooms(room_name, created_by) VALUES(?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, roomName);
            ps.setString(2, createdBy);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean roomExists(String roomName) {
        String sql = "SELECT id FROM rooms WHERE room_name = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, roomName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            return false;
        }
    }

    public boolean joinRoom(String roomName, String username) {
        String sql = "INSERT OR IGNORE INTO room_members(room_name, username) VALUES(?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, roomName);
            ps.setString(2, username);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean leaveRoom(String roomName, String username) {
        String sql = "DELETE FROM room_members WHERE room_name = ? AND username = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, roomName);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getAllRooms() {
        String sql = "SELECT room_name FROM rooms ORDER BY room_name";
        List<String> rooms = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(rs.getString("room_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rooms;
    }

    public List<String> getRoomMembers(String roomName) {
        String sql = "SELECT username FROM room_members WHERE room_name = ? ORDER BY username";
        List<String> members = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, roomName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(rs.getString("username"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return members;
    }

    public boolean isUserInRoom(String roomName, String username) {
        String sql = "SELECT id FROM room_members WHERE room_name = ? AND username = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, roomName);
            ps.setString(2, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            return false;
        }
    }
}