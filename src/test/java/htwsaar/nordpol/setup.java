package htwsaar.nordpol;

import org.junit.jupiter.api.BeforeAll;

public class setup {

    @BeforeAll
    static void loadDriver() throws Exception {
        Class.forName("org.sqlite.JDBC");
    }
}
