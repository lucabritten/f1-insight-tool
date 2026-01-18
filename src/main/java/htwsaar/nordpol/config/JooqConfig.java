package htwsaar.nordpol.config;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;

public class JooqConfig {

    /**
     * Creates and returns a jOOQ {@link DSLContext}.
     *
     * @return a configured DSLContext connected to the local SQLite database
     * @throws RuntimeException if the database connection cannot be established
     */
    public static DSLContext createContext(){
        try{
            Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:f1data.db"
            );
            return DSL.using(connection);
        } catch (Exception e){
            throw new RuntimeException("Failed to create jooq context", e);
        }
    }
}
