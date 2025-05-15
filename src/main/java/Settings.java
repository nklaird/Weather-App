import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Arrays;
import java.util.List;

public class Settings extends SceneBuilder{
	private static DropShadow dropShadow = new DropShadow();

	public static Scene getScene(){
		BorderPane root = getRoot();
		BorderPane rootPane = new BorderPane(root);
		rootPane.setBottom(NavigationBar.getNavigationBar());
		rootPane.setPrefSize(360, 640);

		Pane pane = new Pane();
		pane.getChildren().add(rootPane);

		Image fox = new Image("/images/fox_peeking.jpg", 135, 90, false, true); // fox image
		ImageView foxView = new ImageView(fox);

		pane.getChildren().add(foxView);
		foxView.setLayoutX(250);
		foxView.setLayoutY(520);

		// Allows the changing of themes based on settings
		String stylesheet = "style.css";
		switch(theme){
			case "Matcha":
				stylesheet = "/css/settings/settings_matcha.css";
				break;
			case "Cocoa":
				stylesheet = "/css/settings/settings_cocoa.css";
				break;
			case "Milk":
				stylesheet = "/css/settings/settings_milk.css";
				break;
			case "Ube":
				stylesheet = "/css/settings/settings_ube.css";
				break;
		}

		Scene scene = new Scene(pane, 360, 640);
		scene.getStylesheets().add(SceneBuilder.class.getResource(stylesheet).toExternalForm());
		return scene;
	}

	public static BorderPane getRoot() {
		Label settingsLabel = new Label("Settings");
		settingsLabel.setTextFill(Color.WHITE);
		settingsLabel.setFont(Font.font("Inter", FontWeight.BOLD ,40));
		settingsLabel.setPadding(new Insets(20,20,20,20));
		settingsLabel.setEffect(dropShadow);

		Label themesText = new Label("Themes");
		Label temperatureText = new Label("Temperature");
		Label chooseHourText = new Label("24hr/12hr");

		// Creates options for dropdowns
		List<String> themesOptions = Arrays.asList("Matcha", "Cocoa", "Milk", "Ube");
		List<String> temperatureOptions = Arrays.asList("Fahrenheit", "Celsius");
		List<String> hourOptions = Arrays.asList("12hr", "24hr");

		ComboBox<String> themesDropdown = new ComboBox(FXCollections.observableArrayList(themesOptions));
		themesDropdown.getSelectionModel().select(themesOptions.indexOf(theme)); //displays the current setting as the default selected option
		themesDropdown.setOnAction(e->{
			String theme = themesDropdown.getValue();
			switch(theme){
				case "Matcha":
					SceneBuilder.setTheme("Matcha"); //changing theme of app
					SceneBuilder.setBackgroundImage("/images/backgrounds/plant_wallpaper.jpg");
					stage.setScene(getScene());
					break;
				case "Cocoa":
					SceneBuilder.setTheme("Cocoa");
					SceneBuilder.setBackgroundImage("/images/backgrounds/brown_background.jpg");
					stage.setScene(getScene());
					break;
				case "Milk":
					SceneBuilder.setTheme("Milk");
					SceneBuilder.setBackgroundImage("/images/backgrounds/cream_background.jpg");
					stage.setScene(getScene());
					break;
				case "Ube":
					SceneBuilder.setTheme("Ube");
					SceneBuilder.setBackgroundImage("/images/backgrounds/ube_background.jpg");
					stage.setScene(getScene());
					break;
			}
		});

		ComboBox<String> temperatureDropdown = new ComboBox(FXCollections.observableArrayList(temperatureOptions));
		temperatureDropdown.getSelectionModel().select(temperatureOptions.indexOf(temperatureUnit));
		temperatureDropdown.setOnAction(e->{
			String unit = temperatureDropdown.getValue();
			SceneBuilder.temperatureUnit = unit;
		});

		ComboBox<String> hourDropdown = new ComboBox(FXCollections.observableArrayList(hourOptions));
		hourDropdown.getSelectionModel().select(hourOptions.indexOf(timeFormat));
		hourDropdown.setOnAction(e->{
			String unit = hourDropdown.getValue();
			SceneBuilder.timeFormat = unit;
		});

		BorderPane settingBoxThemes = createSettingsBox(themesText, themesDropdown);
		BorderPane settingBoxTemperature = createSettingsBox(temperatureText, temperatureDropdown);
		BorderPane settingBoxHour = createSettingsBox(chooseHourText, hourDropdown);

		//Putting all the elements together
		VBox settingBoxTop = new VBox(4, settingsLabel, settingBoxThemes, settingBoxTemperature, settingBoxHour);
		settingBoxTop.setAlignment(Pos.CENTER);

		VBox root = new VBox(20, settingBoxTop);
		root.setBackground(new Background(backgroundImage));

		BorderPane borderPane = new BorderPane(root);
		return borderPane;
	}

	//creating and designing each settings box
	private static BorderPane createSettingsBox(Label text, ComboBox dropdown){
		text.setFont(new Font("Inter", 25));
		text.setTextFill(Color.WHITE);
		text.setEffect(dropShadow);

		dropdown.setPrefWidth(110);
		dropdown.setPrefHeight(40);
		dropdown.setEffect(dropShadow);
		dropdown.setId("comboBox");

		BorderPane settingPanel = new BorderPane();
		settingPanel.setEffect(dropShadow);
		settingPanel.setLeft(text);
		settingPanel.setRight(dropdown);

		settingPanel.setPadding(new Insets(5));
		settingPanel.setBorder(Border.stroke(Color.BLACK));
		settingPanel.setMaxWidth(340);
		settingPanel.setPrefHeight(45);
		settingPanel.setOpacity(0.9);
		settingPanel.setId("settingsBox");
		return settingPanel;
	}
}
