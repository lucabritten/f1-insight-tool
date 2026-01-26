package htwsaar.nordpol.config;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

public class JooqConfig {

    private JooqConfig(){

    }

    /**
     * Creates and returns a jOOQ {@link DSLContext}.
     *
     * @return a configured DSLContext connected to the local SQLite database
     * @throws RuntimeException if the database connection cannot be established
     */
    public static DSLContext createContext(){
        try{
            Path dbPath = Path.of("f1data.db");
            boolean needsInit = Files.notExists(dbPath);
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            if (needsInit) {
                initializeSchema(connection);
            }
            return DSL.using(connection);
        } catch (Exception e){
            throw new RuntimeException("Failed to create jooq context", e);
        }
    }

    private static void initializeSchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys=ON");
        }

        String schemaSql = readResource();
        for (String ddl : schemaSql.split(";")) {
            String trimmed = ddl.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute(trimmed);
            }
        }
    }

    private static String readResource() throws Exception {
        InputStream inputStream = JooqConfig.class.getClassLoader().getResourceAsStream("schema.sql");
        if (inputStream == null) {
            throw new IllegalStateException("Missing resource: " + "schema.sql");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));
        }
    }
}
