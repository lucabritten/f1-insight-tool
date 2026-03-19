package htwsaar.nordpol.presentation.gui.Scenes;

import htwsaar.nordpol.domain.Driver;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DriverResultView {

    public Scene createScene(Driver driver, Runnable onBack) {
        Label title = new Label("Driver Result");
        Label name = new Label("Name: " + driver.firstName() + " " + driver.lastName());
        Label number = new Label("Number: " + driver.driverNumber());
        Label team = new Label("Team: " + driver.teamName());

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> onBack.run());

        VBox root = new VBox(12, title, name, number, team, backButton);
        root.setPadding(new Insets(24));

        return new Scene(root, 720, 480);
    }

    public Scene createErrorScene(String message, Runnable onBack) {
        Label title = new Label("Driver Search Error");
        Label error = new Label(message);

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> onBack.run());

        VBox root = new VBox(12, title, error, backButton);
        root.setPadding(new Insets(24));

        return new Scene(root, 720, 480);
    }
}
