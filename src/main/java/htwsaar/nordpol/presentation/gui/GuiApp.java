package htwsaar.nordpol.presentation.gui;

import htwsaar.nordpol.App;
import htwsaar.nordpol.config.DatabaseInitializer;
import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.presentation.gui.Controller.DriverController;
import htwsaar.nordpol.presentation.gui.Scenes.DriverResultView;
import htwsaar.nordpol.presentation.gui.Scenes.DriverView;
import htwsaar.nordpol.service.driver.IDriverService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class GuiApp extends Application {

    private static final double WINDOW_WIDTH = 720;
    private static final double WINDOW_HEIGHT = 480;

    private final DriverView driverView = new DriverView();
    private final DriverResultView driverResultView = new DriverResultView();
    private DriverController driverController;

    private ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(App.class)
                .web(WebApplicationType.NONE)
                .run();

        applicationContext.getBean(DatabaseInitializer.class).run();
        driverController = new DriverController(applicationContext.getBean(IDriverService.class));
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("F1 Insight Tool");
        showHomeScene(stage);
        stage.show();
    }

    private void showHomeScene(Stage stage) {
        Label title = new Label("F1 Insight Tool");
        Label status = new Label("GUI mode is running with the Spring backend context.");

        Button driverServiceButton = createNavigationButton("Driver Information",
                () -> showDriverScreen(stage));

        Button weatherServiceButton = createNavigationButton("Weather",
                () -> showServiceScene(stage, "Weather"));

        Button lapServiceButton = createNavigationButton("Laps",
                () -> showServiceScene(stage, "Laps"));

        Button sessionResultServiceButton = createNavigationButton("Session Results",
                () -> showServiceScene(stage, "Session Results"));

        Button sessionReportServiceButton = createNavigationButton("Session Report",
                () -> showServiceScene(stage, "Session Report"));

        VBox root = new VBox(12,
                title,
                status,
                driverServiceButton,
                weatherServiceButton,
                lapServiceButton,
                sessionResultServiceButton,
                sessionReportServiceButton);
        root.setPadding(new Insets(24));

        stage.setScene(createScene(root));
    }

    private void showServiceScene(Stage stage, String serviceName) {
        Label title = new Label(serviceName);
        Label description = new Label("This is the " + serviceName + " screen.");
        Label info = new Label("This is where you can fetch infos about "+ serviceName);

        Button backButton = createNavigationButton("Back", () -> showHomeScene(stage));

        VBox root = new VBox(12, title, description, info, backButton);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER_LEFT);

        stage.setScene(createScene(root));
    }

    private void showDriverScreen(Stage stage) {
        stage.setScene(driverView.createScene(
            request -> {
                try {
                    Driver driver = driverController.searchDriver(request);
                    showDriverResultScreen(stage, driver);
                } catch (RuntimeException exception) {
                    showDriverErrorScreen(stage, exception.getMessage());
                }
            },
            () -> showHomeScene(stage)
        ));
    }

    private void showDriverResultScreen(Stage stage, Driver driver) {
        stage.setScene(driverResultView.createScene(driver, () -> showDriverScreen(stage)));
    }

    private void showDriverErrorScreen(Stage stage, String message) {
        stage.setScene(driverResultView.createErrorScene(message, () -> showDriverScreen(stage)));
    }

    private Button createNavigationButton(String label, Runnable action) {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    private Scene createScene(VBox root) {
        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    @Override
    public void stop() {
        if (applicationContext != null) {
            applicationContext.close();
        }
        Platform.exit();
    }
}
