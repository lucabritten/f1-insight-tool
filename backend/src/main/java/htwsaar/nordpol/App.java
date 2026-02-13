package htwsaar.nordpol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
}
