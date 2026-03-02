package htwsaar.nordpol;

import htwsaar.nordpol.config.DatabaseInitializer;
import htwsaar.nordpol.presentation.cli.F1CLI;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

/**
 * Entry point for the CLI variant of the application.
 *
 * This boots the Spring context in non-web mode, initializes the database,
 * and then delegates to the Picocli-based F1CLI command hierarchy.
 */
public class CliApp {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(App.class)
                .web(WebApplicationType.NONE)
                .run(args);

        try {
            // Ensure database is initialized in CLI mode as well
            DatabaseInitializer initializer = ctx.getBean(DatabaseInitializer.class);
            initializer.run();

            F1CLI cliRoot = ctx.getBean(F1CLI.class);
            int exitCode = new CommandLine(cliRoot).execute(args);
            System.exit(exitCode);
        } finally {
            ctx.close();
        }
    }
}

