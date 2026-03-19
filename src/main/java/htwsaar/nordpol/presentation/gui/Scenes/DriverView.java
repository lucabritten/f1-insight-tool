package htwsaar.nordpol.presentation.gui.Scenes;

import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DriverView {

    public Scene createScene(Consumer<DriverSearchRequest> onSearch, Runnable onBack) {
        Label title = new Label("Driver Information");
        Label firstInput = new Label("First Name");
        Label secondInput = new Label("Last Name");
        Label thirdInput = new Label("Number");
        Label fourthInput = new Label("Year");

        TextField driverFirstName = new TextField();
        driverFirstName.setPromptText("First Name");

        TextField driverLastName = new TextField();
        driverLastName.setPromptText("Last Name");

        TextField driverNumber = new TextField();
        driverNumber.setPromptText("Driver Number");

        TextField year = new TextField();
        year.setPromptText("Year");

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> onBack.run());

        Button getDataButton = new Button("Get Data");
        getDataButton.setOnAction(event -> { 
            DriverSearchRequest request = new DriverSearchRequest(
                driverFirstName.getText(),
                driverLastName.getText(),
                driverNumber.getText(),
                year.getText()
            );
            onSearch.accept(request);
        });        

        VBox root = new VBox(
            12, 
            title, 
            firstInput,
            driverFirstName, 
            secondInput,
            driverLastName, 
            thirdInput,
            driverNumber,
            fourthInput,
            year,
            getDataButton, 
            backButton
        );
        root.setPadding(new Insets(24));
        
        return new Scene(root, 720, 480);
    }

}
