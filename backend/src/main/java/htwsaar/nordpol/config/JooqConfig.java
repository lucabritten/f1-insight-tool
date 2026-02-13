package htwsaar.nordpol.config;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
public class JooqConfig {

    /**
     * Creates and returns a jOOQ {@link DSLContext}.
     *
     * @return a configured DSLContext connected to the local SQLite database
     * @throws RuntimeException if the database connection cannot be established
     */
    @Bean
    public DSLContext createContext(){
        try{
            Path dbPath = Path.of("f1data.db");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            if (schemaNeedsInitialization(connection)) {
                initializeSchema(connection);
            }
            return DSL.using(connection);
        } catch (Exception e){
            throw new RuntimeException("Failed to create jooq context", e);
        }
    }

    static void initializeSchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys=ON");
        }

        String schemaSql = readSchemaSql();
        for (String ddl : schemaSql.split(";")) {
            String trimmed = ddl.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute(trimmed);
            } catch (SQLException e) {
                if (!isAlreadyExistsError(e)) {
                    throw e;
                }
            }
        }
    }

    static boolean schemaNeedsInitialization(Connection connection) throws Exception {
        Set<String> expectedTables = extractTableNames(readSchemaSql());
        if (expectedTables.isEmpty()) {
            return false;
        }
        Set<String> existingTables = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
            while (resultSet.next()) {
                existingTables.add(resultSet.getString(1).toLowerCase(Locale.ROOT));
            }
        }
        for (String table : expectedTables) {
            if (!existingTables.contains(table.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    static String readSchemaSql() throws Exception {
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

    private static Set<String> extractTableNames(String schemaSql) {
        Set<String> tableNames = new HashSet<>();
        Pattern pattern = Pattern.compile("(?i)create\\s+table\\s+\"?(\\w+)\"?");
        Matcher matcher = pattern.matcher(schemaSql);
        while (matcher.find()) {
            tableNames.add(matcher.group(1));
        }
        return tableNames;
    }

    private static boolean isAlreadyExistsError(SQLException e) {
        String message = e.getMessage();
        return message != null && message.toLowerCase(Locale.ROOT).contains("already exists");
    }
}
