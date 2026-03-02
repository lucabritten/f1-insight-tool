package htwsaar.nordpol;

import htwsaar.nordpol.config.DatabaseInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.xml.crypto.Data;

@SpringBootApplication
public class App {
//    public static void main(String[] args) {
//        new DatabaseInitializer().run();
//        CommandLine commandLine = new CommandLine(new F1CLI());
//        int exitCode = commandLine.execute(args);
//        System.exit(exitCode);
//    }
    public static void main(String[] args){
        SpringApplication.run(App.class, args);
    }

    @Bean
    CommandLineRunner init(DatabaseInitializer databaseInitializer) {
        return args -> databaseInitializer.run();
    }
}
