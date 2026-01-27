package htwsaar.nordpol.config;

import java.sql.Connection;
import java.sql.DriverManager;

public final class DatabaseInitializer {

    public void run() {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:f1data.db")) {
            JooqConfig.initializeSchema(connection);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static void main(String[] args) {
        new DatabaseInitializer().run();
    }
}