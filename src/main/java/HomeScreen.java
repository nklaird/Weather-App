import hourlyWeather.HourlyPeriod;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import weather.Period;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class HomeScreen extends SceneBuilder{
	private static BorderPane prevHourBox; //static global variable used for lambda method which requires 'final' variables
	static DropShadow dropShadow = new DropShadow();

	public static Scene getScene(){
		BorderPane root = getRoot();
		BorderPane rootPane = new BorderPane(root);
		rootPane.setBottom(NavigationBar.getNavigationBar());
		rootPane.setPrefSize(360,640);

		//Pane is the abstract class for layout panes that has no constraints
		//This allows the ability to freely move its children along the screen
		Pane pane = new Pane();
		pane.getChildren().add(rootPane);

		Image plantImage = new Image("/images/small_indoor_plant.png", 70, 70, false, true);
		ImageView plantView = new ImageView(plantImage);

		Image catImage = new Image("/images/cat_lamp.png", 150, 150, false, true);
		ImageView catView = new ImageView(catImage);

		Image pingu = new Image("/images/pingu_orange.png", 80, 80, false, true);
		ImageView pinguView = new ImageView(pingu);
		pinguView.setId("pinguView");

		Image matcha = new Image("/images/matcha.png", 100, 100, false, true);
		ImageView matchaView = new ImageView(matcha);

		pane.getChildren().add(plantView);
		plantView.setLayoutX(-10);
		plantView.setLayoutY(223);

		pane.getChildren().add(catView);
		catView.setLayoutX(270);
		catView.setLayoutY(125);

		pane.getChildren().add(pinguView);
		pinguView.setLayoutX(-18);
		pinguView.setLayoutY(520);

		pane.getChildren().add(matchaView);
		matchaView.setLayoutX(260);
		matchaView.setLayoutY(-25);

		Label quoteLabel = new Label();
		quoteLabel.setFont(Font.font("Lucida Calligraphy", 12));
		quoteLabel.setTextFill(Color.WHITE);
		pane.getChildren().add(quoteLabel);
		quoteLabel.setLayoutX(15);
		quoteLabel.setLayoutY(15);

		String stylesheet = "/style.css";
		switch(theme){
			case "Matcha":
				stylesheet = "/css/home/home_matcha.css";
				quoteLabel.setText("\"♡ I love you\nso matcha ♡\"");
				break;
			case "Cocoa":
				stylesheet = "/css/home/home_cocoa.css";
				quoteLabel.setText("\"♡ I love you\na choco-lot ♡\"");
				break;
			case "Milk":
				stylesheet = "/css/home/home_milk.css";
				quoteLabel.setText("\"♡ I love you\na latte ♡\"");
				quoteLabel.setTextFill(Color.BLACK);
				break;
			case "Ube":
				stylesheet = "/css/home/home_ube.css";
				quoteLabel.setText("\"♡ Will 'u-be'\n     mine? ♡\"");
				break;
		}

		Scene homeScene = new Scene(pane, 360, 640);
		homeScene.getStylesheets().add(SceneBuilder.class.getResource(stylesheet).toExternalForm());

		//Ensures that the alerts dialog stage is closed with the main stage
		stage.setOnCloseRequest(e -> {
			if(alertsStage != null && alertsStage.isShowing()){
				alertsStage.close();
			}
		});

		return homeScene;
	}

	//Creates the root for the scene
	public static BorderPane getRoot(){
		HourlyPeriod hourlyForecast = hourlyPeriods.get(0);
		Period currentForecast = periods.get(0);
		double[] minAndMax = minAndMaxTemps.get(0).getValue();

		int temperature = hourlyForecast.temperature;
		if(temperatureUnit.equals("Celsius")) {
			temperature = convertFahrenheitToCelsius(temperature);
		}

		int min = (int) minAndMax[0];
		int max = (int) minAndMax[1];
		if(temperatureUnit.equals("Fahrenheit")){
			min = convertCelsiusToFahrenheit(min);
			max = convertCelsiusToFahrenheit(max);
		}

		//Elements for homeBoxOne (top part of the screen)
		Button locationButton = getLocationButton();

		Label temperatureLabel = new Label(temperature + "°");
		temperatureLabel.setFont(Font.font("Verdana", FontWeight.BOLD,50));
		temperatureLabel.setTextFill(Color.rgb(255,255,255));
		temperatureLabel.setEffect(dropShadow);

		Label forecastLabel = new Label(hourlyForecast.shortForecast);
		forecastLabel.setFont(Font.font("Verdana", 14));
		forecastLabel.setId("label");

		Label minMaxText = new Label("L: " + min + "°" + " H: " + max + "°");
		minMaxText.setFont(Font.font("Verdana", 13));
		minMaxText.setId("label");

		HBox homeBoxOneTop = new HBox(locationButton);
		homeBoxOneTop.setAlignment(Pos.CENTER);

		VBox homeBoxOneCenter = new VBox(temperatureLabel, forecastLabel, minMaxText);
		homeBoxOneCenter.setAlignment(Pos.CENTER);

		//Putting all the elements together for homeBoxOne (top part of the screen)
		BorderPane homeBoxOne = new BorderPane(homeBoxOneCenter);
		homeBoxOne.setPrefHeight(150);
		homeBoxOne.setTop(homeBoxOneTop);

		//Between homeBoxOne and Two (center of screen)
		TextArea descriptionText = new TextArea(currentForecast.detailedForecast);
		descriptionText.setWrapText(true);
		descriptionText.setFont(Font.font("Verdana", FontWeight.MEDIUM,12));
		descriptionText.setEffect(dropShadow);
		descriptionText.setEditable(false);
		descriptionText.setPrefSize(300,70);

		//Elements for homeBoxTwo (lower part of screen)
		//homeBoxTwo is split into a left and right component
		ScrollPane hourlyForecastScroll = getHourlyScroll(); //ScrollPane to display the weather for the next 24 hours

		Label todayWeatherText = new Label("      Today's Weather"); //spacing to center the label
		todayWeatherText.setFont(Font.font("Verdana",FontWeight.BOLD, 16));
		todayWeatherText.setTextFill(Color.rgb(255,255,255));
		todayWeatherText.setPrefSize(220,18);
		todayWeatherText.setEffect(dropShadow);

		Image forecastImage = new Image(currentForecast.icon); //this causes some lag when the image is loading from the url
		ImageView forecastView = new ImageView(forecastImage);
		forecastView.setFitHeight(100);
		forecastView.setFitWidth(100);
		forecastView.setEffect(dropShadow);

		TextArea alertsText = getAlertsText(); //Displays expanded information when clicked

		//Putting all elements together for homeBoxTwo (bottom part of screen)
		VBox homeBoxTwoLeft = new VBox(todayWeatherText, hourlyForecastScroll);
		VBox homeBoxTwoRight = new VBox(10, forecastView, alertsText);

		HBox homeBoxTwo = new HBox(10, homeBoxTwoLeft, homeBoxTwoRight);
		homeBoxTwo.setAlignment(Pos.CENTER);

		//Putting all elements together in root (whole screen)
		VBox centerRoot = new VBox(10, homeBoxOne, descriptionText);

		BorderPane root = new BorderPane(centerRoot);
		root.setPadding(new Insets(10, 10, 0, 10));
		root.setBottom(homeBoxTwo); //aligns homeBoxTwo to the bottom of the screen

		root.setBackground(new Background(backgroundImage));

		return root;
	}

	/*
		Creates a TextArea node that displays the active alerts for the area
		When clicked, creates a dialog stage with the detailed alert and instructions
	 */
	private static TextArea getAlertsText(){
		TextArea alertsText = new TextArea("Alerts: None currently.");
		alertsText.setWrapText(true);
		alertsText.setFont(Font.font("Verdana", FontWeight.MEDIUM,12));
		alertsText.setEffect(dropShadow);
		alertsText.setEditable(false);
		alertsText.setPrefSize(100, 200);
		alertsText.setId("alertsText");

		if(!currAlerts.isEmpty()){
			Alert currAlert = currAlerts.get(0); //currAlerts can have multiple active alerts, this only displays the first one
			alertsText.setText("Alerts: " + currAlert.headline);

			alertsText.setOnMouseClicked(e-> {
				if(alertsStage != null && alertsStage.isShowing()) {
					alertsStage.close();
				}
				else{
					Scene nextScene = getAlertsScene(currAlert); //getting the scene for the alerts dialog stage

					alertsStage = new Stage();
					alertsStage.getIcons().add(new Image("/images/cloudy.png"));
					alertsStage.setScene(nextScene);
					alertsStage.setTitle("Active Alert");
					alertsStage.setResizable(false);
					alertsStage.initOwner(stage);

					alertsStage.show();
				}
			});
		}

		return alertsText;
	}

	//Creating the scene for the alerts dialog stage
	private static Scene getAlertsScene(Alert currAlert){
		TextArea headlineText = new TextArea(currAlert.headline);
		headlineText.setPrefSize(400,100);
		headlineText.setEditable(false);
		headlineText.setId("headlineText");

		//the alert description from the api comes with weirdly placed newlines and '*' characters which
		//has to be reformatted before displaying.
		String description = currAlert.description.replaceAll("\n", " ").replaceAll("\\*", "\n*");
		TextArea descriptionText = new TextArea(description);
		descriptionText.setPrefSize(400,300);
		descriptionText.setEditable(false);

		String instructions = "Instructions: " + currAlert.instruction.replaceAll("\n", " ").replaceAll("\\*", "\n*");
		TextArea instructionText = new TextArea(instructions);
		instructionText.setPrefSize(400,150);
		instructionText.setEditable(false);

		VBox alertsBox = new VBox(headlineText, descriptionText, instructionText);

		Scene nextScene = new Scene(alertsBox);

		String stylesheet = "/style.css";
		switch(theme){
			case "Matcha":
				stylesheet = "/css/alerts/alerts_matcha.css";
				break;
			case "Cocoa":
				stylesheet = "/css/alerts/alerts_cocoa.css";
				break;
			case "Milk":
				stylesheet = "/css/alerts/alerts_milk.css";
				break;
			case "Ube":
				stylesheet = "/css/alerts/alerts_ube.css";
				break;
		}
		nextScene.getStylesheets().add(SceneBuilder.class.getResource(stylesheet).toExternalForm());

		return nextScene;
	}

	//Creates dialog window for changing location
	private static Button getLocationButton(){
		Button locationButton = new Button(getLocation());
		locationButton.setPrefSize(150,40);
		locationButton.setId("locationButton");

		locationButton.setOnAction(e -> {
			if(alertsStage != null && alertsStage.isShowing()){
				alertsStage.close();
			}

			Scene nextScene = LocationDetails.getScene();

			locationStage = new Stage();
			locationStage.setScene(nextScene);
			locationStage.setTitle("LocationDetails");
			locationStage.setResizable(false);
			locationStage.getIcons().add(new Image("/images/cloudy.png"));

			locationStage.initOwner(stage);
			locationStage.initModality(Modality.WINDOW_MODAL); //prevents user interaction with main stage until the dialog window has closed
			locationStage.show();
		});

		return locationButton;
	}

	//Creates the crazy ScrollPane displaying the weather for the next 24 hours
	private static ScrollPane getHourlyScroll(){
		VBox hourlyForecastBox = new VBox(); //empty vbox to be filled with nodes

		ScrollPane hourlyForecastScroll = new ScrollPane(hourlyForecastBox);
		hourlyForecastScroll.setPrefSize(220, 300);
		hourlyForecastScroll.setId("shelfScroll");

		ArrayList<BorderPane> hourBoxes = new ArrayList<>(); //BorderPanes which display the hour, temperature, and rain chance
		ArrayList<HBox> expandedInfoBoxes = new ArrayList<>(); //HBoxes which display the wind speed and humidity

		for (int i = 0; i <= 24; i++) {
			HourlyPeriod currentPeriod = hourlyPeriods.get(i);

			SimpleDateFormat localDateFormat; //formatter used to extract the hour from the date object
			if (timeFormat.equals("24hr")) {
				localDateFormat = new SimpleDateFormat("HH:mm");
			}
			else{
				localDateFormat = new SimpleDateFormat("hh a");
			}
			String hour = localDateFormat.format(currentPeriod.startTime);

			int temperature = currentPeriod.temperature;
			if (temperatureUnit.equals("Celsius")) {
				temperature = convertFahrenheitToCelsius(temperature);
			}

			Label time = new Label(hour);
			time.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 15));
			time.setEffect(dropShadow);
			time.setTextFill(Color.web("#FFFFFF"));
			if (!currentPeriod.isDaytime) {
				time.setTextFill(Color.web("#B3B3B3"));
			}

			Label temp = new Label(temperature + "°");
			temp.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
			temp.setEffect(dropShadow);
			temp.setTextFill(Color.rgb(255,255,255));

			Label rainChance = new Label(currentPeriod.probabilityOfPrecipitation.value + "%");
			rainChance.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
			rainChance.setEffect(dropShadow);
			rainChance.setTextFill(Color.web("#7393B3"));

			//putting the elements together for the top part of the box
			HBox statusBox = new HBox(30, time, temp,rainChance);
			statusBox.setAlignment(Pos.CENTER);

			Label wind = new Label("Wind: " + currentPeriod.windSpeed + " " + currentPeriod.windDirection);
			wind.setFont(Font.font("Verdana", 9));
			wind.setTextFill(Color.rgb(255,255,255));

			Label humidity = new Label("Humidity: " + currentPeriod.relativeHumidity.value + "%");
			humidity.setFont(Font.font("Verdana", 9));
			humidity.setTextFill(Color.rgb(255,255,255));

			//putting the elements together for the bottom part of the box
			HBox expandedStatusBox = new HBox(20, wind, humidity);
			expandedStatusBox.setAlignment(Pos.CENTER);
			expandedStatusBox.setVisible(false); //hides this so it will be shown when the user clicks the box
			expandedInfoBoxes.add(expandedStatusBox);

			//putting the top and bottom together into one box
			BorderPane currHourBox = new BorderPane();
			currHourBox.setCenter(statusBox);
			currHourBox.setBottom(expandedStatusBox);
			currHourBox.setPadding(new Insets(6));
			currHourBox.setPrefSize(204,50);

			if(i == 0){ //first box is the top of the shelf
				currHourBox.setId("shelfTop");
			}
			else if(i == 24){ //last box is te bottom of the shelf
				currHourBox.setId("shelfBottom");
			}
			else{
				currHourBox.setId("shelfMiddle");
			}

			hourBoxes.add(currHourBox);
			hourlyForecastBox.getChildren().add(currHourBox);

			prevHourBox = currHourBox; //this just ensures that prevHourBox is not null and exists in the hourBoxes array
			currHourBox.setOnMouseClicked(e -> {
				int index = hourBoxes.indexOf(currHourBox); //index of the box the user clicked on
				int prevIndex = hourBoxes.indexOf(prevHourBox); //index of the previous box the user clicked on

				if(!expandedInfoBoxes.get(index).isVisible()){ //checks if the wind speed and humidity are already displayed
					expandedInfoBoxes.get(index).setVisible(true);

					if(currHourBox != prevHourBox){ //hides the wind/humidity of the previous box the user clicked
						expandedInfoBoxes.get(prevIndex).setVisible(false);
					}

					prevHourBox = currHourBox;
				}
				else{ //hides the wind speed and humidity if they are already shown
					expandedInfoBoxes.get(index).setVisible(false);
				}
			});
		}

		hourlyForecastScroll.setStyle("-fx-background-color: transparent");

		return hourlyForecastScroll;
	}
}
