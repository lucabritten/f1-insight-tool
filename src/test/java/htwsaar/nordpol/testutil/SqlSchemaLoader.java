package htwsaar.nordpol.testutil;

import org.jooq.DSLContext;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class SqlSchemaLoader {

    private SqlSchemaLoader() {
    }

    public static void loadSchema(DSLContext create, String resourceName) {
        try (InputStream is = SqlSchemaLoader.class
                .getClassLoader()
                .getResourceAsStream(resourceName)) {

            if (is == null) {
                throw new IllegalStateException(
                        "SQL schema resource not found: " + resourceName
                );
            }

            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            for (String statement : sql.split(";")) {
                if (!statement.isBlank()) {
                    create.execute(statement);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load SQL schema from " + resourceName, e
            );
        }
    }
}
