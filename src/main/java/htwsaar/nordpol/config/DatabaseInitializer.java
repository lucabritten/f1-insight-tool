package htwsaar.nordpol.config;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

@Component
public final class DatabaseInitializer {

    public void run() {
        String dbPath = System.getenv().getOrDefault("F1_DB_PATH", "/tmp/f1data.db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            if (JooqConfig.schemaNeedsInitialization(connection)) {
                JooqConfig.initializeSchema(connection);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
