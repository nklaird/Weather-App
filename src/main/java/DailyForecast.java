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
import javafx.stage.Stage;
import weather.Period;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DailyForecast extends SceneBuilder{
	private static String stylesheet; //stylesheet used for this scene and the dialog stage's scene
	private static int hourlyIndex; //Index for the current hour of the week

	private static ScrollPane hourlyScrollPane;
	private static VBox rootVBox;
	private static DropShadow dropShadow = new DropShadow();

	public static Scene getScene(){
		BorderPane root = getRoot();
		BorderPane rootPane = new BorderPane(root);
		rootPane.setBottom(NavigationBar.getNavigationBar());
		rootPane.setPrefSize(360,640);

		Pane pane = new Pane();
		pane.getChildren().add(rootPane);

		Image cat = new Image("/images/cat_peeking.png", 110, 110, false, true);
		ImageView catView = new ImageView(cat);
		catView.setId("catView");

		pane.getChildren().add(catView);
		catView.setLayoutX(85);
		catView.setLayoutY(510);

		Scene scene = new Scene(pane, 360, 640);
		stylesheet = "style.css";
		switch(theme){
			case "Matcha":
				stylesheet = "/css/daily/daily_matcha.css";
				break;
			case "Cocoa":
				stylesheet = "/css/daily/daily_cocoa.css";
				break;
			case "Milk":
				stylesheet = "/css/daily/daily_milk.css";
				break;
			case "Ube":
				stylesheet = "/css/daily/daily_ube.css";
				break;
		}
		scene.getStylesheets().add(SceneBuilder.class.getResource(stylesheet).toExternalForm());

		//makes sure to close the hourlyDialog stage when the main stage is closed
		stage.setOnCloseRequest(e -> {
			if(hourlyStage != null && hourlyStage.isShowing()){
				hourlyStage.close();
			}
		});

		return scene;
	}

	public static BorderPane getRoot(){
		ComboBox<String> numDaysChoices = new ComboBox<>(); // Dropdown of all day choices
		numDaysChoices.setPromptText("Select Days");
		numDaysChoices.setEffect(dropShadow);
		numDaysChoices.setId("comboBox");
		numDaysChoices.getItems().addAll("3 Day", "5 Day", "7 Day");
		numDaysChoices.setPrefWidth(200);

		numDaysChoices.setOnAction(event -> {
			// Gets number of days desired
			String dayChoice = numDaysChoices.getValue();
			int dayChoiceNumber = Integer.parseInt(dayChoice.split("")[0]); //"3 day" -> 3

			rootVBox.getChildren().remove(hourlyScrollPane); //removes the scrollpane from the root

			hourlyScrollPane = getHourlyScroll(dayChoiceNumber); //updates the scrollpane to hold the right amount of boxes

			rootVBox.getChildren().add(hourlyScrollPane); //reinserts the updated scrollpane to the root
		});

		hourlyScrollPane = new ScrollPane(); //allows scrolling when the boxes exceed the screen
		hourlyScrollPane.setPrefHeight(600);

		Label dailyForecast = new Label("Daily Forecast");
		dailyForecast.setTextFill(Color.rgb(255,255,255));
		dailyForecast.setFont(Font.font("Verdana",FontWeight.BOLD, 40));
		dailyForecast.setEffect(dropShadow);

		VBox rootTop = new VBox(5, dailyForecast, numDaysChoices);
		rootTop.setAlignment(Pos.CENTER);

		rootVBox = new VBox(5, rootTop, hourlyScrollPane);
		rootVBox.setBackground(new Background(backgroundImage));

		return new BorderPane(rootVBox);
	}

	//Creating the ScrollPane used to display the forecasts of the number of days the user inputs
	private static ScrollPane getHourlyScroll(int numDays){
		VBox daysVBox = new VBox(5); //empty vbox which will be filled with boxes for each day
		daysVBox.setStyle("-fx-background-color: transparent");

		ArrayList<BorderPane> dayPanes = new ArrayList<>();
		int forecastIndex = 0; //used to find the forecast for the current 12-hour period
		hourlyIndex = 0; //used to find the forecast for the current hour of the week

		//converting date into day of month
		SimpleDateFormat dayFormat = new SimpleDateFormat("d");

		//iterates through the list until the next day is reached
		while(dayFormat.format(hourlyPeriods.get(0).startTime).equals(dayFormat.format(hourlyPeriods.get(hourlyIndex).startTime))){
			hourlyIndex++;
		}

		for (int i = 0; i < numDays; i++) {
			String dateString = minAndMaxTemps.get(i).getKey();
			double[] minAndMax = minAndMaxTemps.get(i).getValue();
			int tonightIndex = forecastIndex + 1;

			Period todayPeriod = periods.get(forecastIndex);
			Period tonightPeriod = periods.get(tonightIndex);

			/*
				The API's list of forecasts can start in 3 ways:
				1. During the day, the list starts with the daytime, followed by the nighttime, then the next day's forecast.
				2. At night, the list starts with the nighttime forecast, followed by the next day's forecast.
				3. During midnight, the list starts with the "overnight" forecast, followed by the daytime, then the nighttime forecast.

				Case 1: increment forecastIndex by 2, next box starts with the next day
				Case 2: increment forecastIndex by 1, next box starts with the next day
				Case 3: increment forecastIndex by 1, this box displays the forecast for the rest of the day (overnight), then
													  next box displays the same day but during daytime and nighttime
			 */
			if((periods.get(0).name.equals("Tonight") || periods.get(0).name.equals("Overnight")) && i == 0){
				forecastIndex++; //incrementing by one if the first forecast only had one period
			}
			else{
				forecastIndex+=2; //normally increments by two because each day holds the forecast for the day and night
			}

			//Converting from ISO string to date object
			String dateTimeString = dateString.split("/")[0]; // Makes the date ISO format
			DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
			OffsetDateTime dateTime = OffsetDateTime.parse(dateTimeString, formatter);

			int month = dateTime.getMonthValue();
			int monthDay = dateTime.getDayOfMonth();
			DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
			String dayString = dayOfWeek.toString().substring(0,3); //getting day abbreviation

			String dateLabelString = dayString + " " + month + "/" + monthDay;
			if(periods.get(i).name.equals("Overnight")){ //have to clarify that this box is displaying the overnight info, not a duplicated day
				dateLabelString = "(Overnight) " + dateLabelString;
			}
			Label dateLabel = new Label(dateLabelString);
			dateLabel.setId("dailyLabel");
			dateLabel.setTextFill(Color.WHITE);

			int min = (int) minAndMax[0];
			int max = (int) minAndMax[1];
			if(temperatureUnit.equals("Fahrenheit")){
				min = convertCelsiusToFahrenheit(min);
				max = convertCelsiusToFahrenheit(max);
			}
			Label minMaxLabel = new Label("L: " + min +"°" + " H: " + max + "°");
			minMaxLabel.setId("dailyLabel");
			minMaxLabel.setTextFill(Color.WHITE);

			//creating the top box to display the day, min, and max temps
			BorderPane dayPaneTop = new BorderPane();
			dayPaneTop.setLeft(dateLabel);
			dayPaneTop.setRight(minMaxLabel);
			dayPaneTop.setPadding(new Insets(10));
			dayPaneTop.setPrefWidth(330);
			dayPaneTop.setPrefHeight(40);
			dayPaneTop.setId("dailyBox");
			dayPanes.add(dayPaneTop);

			//displays the hourly forecast for the day the user clicks
			dayPaneTop.setOnMouseClicked(e -> {
				if(hourlyStage != null && hourlyStage.isShowing()){
					hourlyStage.close();
				}

				int paneIndex = dayPanes.indexOf(dayPaneTop);

				boolean singlePeriod = (periods.get(0).name.equals("Tonight") || periods.get(0).name.equals("Overnight"));

				BorderPane hourlyRoot = getHourlyForecastRoot(hourlyIndex, paneIndex, singlePeriod);
				hourlyRoot.setBackground(new Background(backgroundImage));

				Scene nextScene = new Scene(hourlyRoot, 340, 600);
				nextScene.getStylesheets().add(SceneBuilder.class.getResource(stylesheet).toExternalForm());
				hourlyStage = new Stage();
				hourlyStage.setTitle("Hourly Forecast for " + dayString + " " + month + "/" + monthDay);

				hourlyStage.setResizable(false);
				hourlyStage.getIcons().add(new Image("/images/cloudy.png"));
				hourlyStage.setScene(nextScene);
				hourlyStage.show();
			});

			//displaying the data for the current period
			Label descriptionLabel = new Label(todayPeriod.shortForecast);
			descriptionLabel.setId("dailyBottomLabel");
			descriptionLabel.setWrapText(true);

			BorderPane descriptionPane = new BorderPane();
			descriptionPane.setLeft(descriptionLabel);
			descriptionPane.setId("dailyBottomBox");
			descriptionPane.setPadding(new Insets(5));

			BorderPane todayPane = get12HourForecastPane(todayPeriod);

			VBox dayVBox;
			//if there is only one period, no need to make a pane for the night
			if(todayPeriod.name.equals("Tonight") || todayPeriod.name.equals("Overnight")) {
				dayVBox = new VBox(dayPaneTop, descriptionPane, todayPane);
			}
			else {
				//displaying the data for the night period
				BorderPane tonightPane = get12HourForecastPane(tonightPeriod);

				dayVBox = new VBox(dayPaneTop, descriptionPane, todayPane, tonightPane);
			}
			daysVBox.getChildren().add(dayVBox);
		}
		return new ScrollPane(daysVBox);
	}

	//displaying the period, temperature, and probability of precipitation
	private static BorderPane get12HourForecastPane(Period period){
		BorderPane periodPane = new BorderPane();
		int temperature = period.temperature;
		if(temperatureUnit.equals("Celsius")){
			temperature = convertFahrenheitToCelsius(temperature);
		}
		Label periodText = new Label(period.name + ": " + temperature + "°");
		periodText.setId("dailyBottomLabel");

		Label periodRain = new Label(period.probabilityOfPrecipitation.value + "%");
		periodRain.setFont(Font.font("Verdana", 14));
		periodRain.setTextFill(Color.web("#36454f"));

		periodPane.setLeft(periodText);
		periodPane.setRight(periodRain);
		periodPane.setId("dailyBottomBox");
		periodPane.setPrefHeight(20);
		periodPane.setPadding(new Insets(0,5,0,5));

		return periodPane;
	}


	//used for the hourlyDialog stage to create the root of the scene
	private static BorderPane getHourlyForecastRoot(int hourlyIndex, int paneIndex, boolean singlePeriod){
		SimpleDateFormat dayFormat = new SimpleDateFormat("E M/d");
		SimpleDateFormat hourFormat = new SimpleDateFormat("hh a");
		if(timeFormat.equals("24hr")){
			hourFormat = new SimpleDateFormat("HH:mm");
		}

		VBox hourlyVBox = new VBox(5);

		int forecastIndex = paneIndex * 2;
		int startIndex, endIndex;
		if(paneIndex == 0){ //if this is the first pane:
			startIndex = 0; //start at zero
			endIndex = hourlyIndex; //end at the index of the next day

		}
		else{
			startIndex = hourlyIndex + (paneIndex - 1) * 24; //start at the index of the next day + 24 hours for each day after the first
			endIndex = Math.min(startIndex + 24, 155); //the api only gives us the forecast for the next 155 hours, ensures that the end is not out of bounds
		}

		if(singlePeriod && paneIndex == 1){ //if the first day only has one period, then the second day's period starts at index 1
			forecastIndex = 1;
		}
		else if(singlePeriod && paneIndex != 0){ //if the first day only has one period, have to shift the index down by one
			forecastIndex--;
		}

		Period currPeriod = periods.get(forecastIndex);

		Label day = new Label("Hourly Forecast for " + dayFormat.format(currPeriod.startTime));
		day.setFont(Font.font("Verdana", FontWeight.BOLD,15));
		day.setTextFill(Color.rgb(255,255,255));
		day.setEffect(dropShadow);

		TextArea description = new TextArea(currPeriod.detailedForecast);
		description.setEditable(false);
		description.setWrapText(true);
		description.setMaxWidth(200);
		description.setMaxHeight(150);
		description.setEffect(dropShadow);

		Image forecastImage = new Image(currPeriod.icon);
		ImageView forecastView = new ImageView(forecastImage);
		forecastView.setFitHeight(100);
		forecastView.setFitWidth(100);
		forecastView.setEffect(dropShadow);

		//from the starting index of this day to the index of the next day
		//(e.g. 0-17 for the first day when there's only 17 hours left in the day, then 17-41 (17+24=41) for the next day's indices
		for(int i = startIndex; i < endIndex; i++){
			HourlyPeriod currHour = hourlyPeriods.get(i);

			Label hour = new Label( hourFormat.format(currHour.startTime));
			hour.setId("dailyLabel");
			hour.setTextFill(Color.WHITE);

			if(!currHour.isDaytime){
				hour.setTextFill(Color.BLACK);
			}

			int tempValue = currHour.temperature;
			if(temperatureUnit.equals("Celsius")){
				tempValue = convertFahrenheitToCelsius(tempValue);
			}
			Label temperature = new Label(tempValue + "°");
			temperature.setId("dailyLabel");
			temperature.setStyle("-fx-text-fill: white;");

			Label rainChance = new Label(currHour.probabilityOfPrecipitation.value + "%");
			rainChance.setId("dailyLabel");
			if(currHour.probabilityOfPrecipitation.value == 0){
				rainChance.setTextFill(Color.web("#36454f")); //different text color when rain chance is 0
			}
			else{
				rainChance.setTextFill(Color.web("#89CFF0"));
			}

			Label wind = new Label(currHour.windSpeed + " " + currHour.windDirection);
			wind.setId("dailyBottomLabel");
			Label humidity = new Label("Humidity: " + currHour.relativeHumidity.value + "%");
			humidity.setId("dailyBottomLabel");

			BorderPane hourPaneTop = new BorderPane();
			hourPaneTop.setPrefSize(290, 25);
			hourPaneTop.setLeft(hour);
			hourPaneTop.setCenter(temperature);
			hourPaneTop.setRight(rainChance);
			hourPaneTop.setPadding(new Insets(5));

			BorderPane hourPaneBottom = new BorderPane();
			hourPaneBottom.setPrefSize(290, 25);
			hourPaneBottom.setLeft(wind);
			hourPaneBottom.setRight(humidity);
			hourPaneBottom.setPadding(new Insets(5));

			VBox hourPane = new VBox(hourPaneTop, hourPaneBottom);
			hourPane.setAlignment(Pos.CENTER);

			if(i % 2 == 0){ //alternating styles for the boxes
				hourPane.setId("hourlyBoxOne");
			}
			else{
				hourPane.setId("hourlyBoxTwo");
			}

			hourPane.setPadding(new Insets(5));
			hourlyVBox.getChildren().add(hourPane);
		}

		ScrollPane scrollPane = new ScrollPane(hourlyVBox);
		scrollPane.setMaxHeight(500);

		Button back = new Button("Back");
		back.setOnAction(e -> hourlyStage.close()); //back only closes the dialog stage
		back.setId("back");

		HBox dayBox = new HBox(day);
		dayBox.setAlignment(Pos.CENTER);

		BorderPane rootTop = new BorderPane();
		rootTop.setLeft(back);
		rootTop.setCenter(dayBox);

		BorderPane rootCenter = new BorderPane();
		rootCenter.setLeft(forecastView);
		rootCenter.setRight(description);

		VBox rootVBox = new VBox(10, rootTop, rootCenter, scrollPane);

		BorderPane pane = new BorderPane();
		pane.setCenter(rootVBox);
		pane.setPadding(new Insets(10));
		return pane;
	}
}
