package database;

import java.sql.Connection;
import java.sql.Statement;

public final class DatabaseInitializer {
    private DatabaseInitializer() {}

    public static void initialize() {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {

            String usersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL
                    );
                    """;

            String messagesTable = """
                    CREATE TABLE IF NOT EXISTS messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        sender TEXT NOT NULL,
                        receiver TEXT,
                        content TEXT NOT NULL,
                        message_type TEXT NOT NULL,
                        timestamp TEXT NOT NULL
                    );
                    """;
            String roomsTable = """
        CREATE TABLE IF NOT EXISTS rooms (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            room_name TEXT UNIQUE NOT NULL,
            created_by TEXT NOT NULL
        );
        """;

            String roomMembersTable = """
        CREATE TABLE IF NOT EXISTS room_members (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            room_name TEXT NOT NULL,
            username TEXT NOT NULL,
            UNIQUE(room_name, username)
        );
        """;

            statement.execute(usersTable);
            statement.execute(messagesTable);
            statement.execute(roomsTable);
            statement.execute(roomMembersTable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}