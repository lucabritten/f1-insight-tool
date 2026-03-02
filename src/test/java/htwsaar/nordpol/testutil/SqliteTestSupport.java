package htwsaar.nordpol.testutil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Ensures the SQLite JDBC driver can load in restricted environments.
 *
 * <p>Some environments restrict write access outside the workspace. The
 * sqlite-jdbc driver extracts native libs into a temp directory, so we pin the
 * temp dir to {@code target/sqlite-tmp} (workspace-local) for tests.</p>
 */
public final class SqliteTestSupport {

    private static boolean initialized = false;

    private SqliteTestSupport() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        try {
            Path tmpDir = Paths.get("target", "sqlite-tmp").toAbsolutePath().normalize();
            Files.createDirectories(tmpDir);

            System.setProperty("java.io.tmpdir", tmpDir.toString());
            System.setProperty("org.sqlite.tmpdir", tmpDir.toString());

            Class.forName("org.sqlite.JDBC");
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SQLite test environment", e);
        }
    }
}

