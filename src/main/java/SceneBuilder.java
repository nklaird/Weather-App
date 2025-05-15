import hourlyWeather.HourlyPeriod;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.stage.Stage;
import javafx.util.Pair;
import weather.Period;
import weather.WeatherAPI;

import java.util.ArrayList;

/*
	The SceneBuilder class is used to share variables between each screen of the app.
	SceneBuilder has 5 subclasses: HomeScreen, DailyForecast, LocationDetails, Settings, WeeklyTrends
 */
public class SceneBuilder {
	public static Stage stage; //The main stage of the app
	public static Stage locationStage; //A dialog stage used to create the LocationDetails screen
	public static Stage alertsStage; //Dialog stage used to display alerts in the area
	public static Stage hourlyStage; //Dialog stage that displays the 24-hour forecast for the day

	public static String location; //Location of the app (e.g. "Chicago, IL")
	public static double latitude;
	public static double longitude;
	public static String region;
	public static int gridX;
	public static int gridY;
	public static String theme;
	public static BackgroundImage backgroundImage; //Background image for each main screen of the app
	public static String temperatureUnit; //Fahrenheit or Celsius
	public static String timeFormat; //12hr or 24hr

	public static ArrayList<HourlyPeriod> hourlyPeriods; //Hourly periods from the API's forecastHourly property
	public static ArrayList<Period> periods; //12 hour periods from the API's forecast property
	public static ArrayList<Pair<String, double[]>> minAndMaxTemps; //Lowest and highest temperatures of the week
	public static ArrayList<Alert> currAlerts; //Alerts from the API's alerts property

	//By default, all the data are for Chicago
	public SceneBuilder(){
		location = "Chicago, Illinois";
		latitude = 41.882;
		longitude = -87.6324;
		region = "LOT";
		gridX = 75;
		gridY = 73;
		theme = "Matcha";
		temperatureUnit = "Fahrenheit";
		timeFormat = "12hr";

		setBackgroundImage("/images/backgrounds/plant_wallpaper.jpg");
		updateData();
	}

	public static void setGridpoint(String region, int gridX, int gridY){
		SceneBuilder.region = region;
		SceneBuilder.gridX = gridX;
		SceneBuilder.gridY = gridY;
	}

	public static void setCoordinates(double latitude, double longitude){
		SceneBuilder.latitude = latitude;
		SceneBuilder.longitude = longitude;
	}

	public static void setLocation(String location) {
		SceneBuilder.location = location;
	}

	public static void updateData(){
		System.out.println("Updating data, please be patient...");
		hourlyPeriods = MyWeatherAPI.getHourlyForecast(region, gridX, gridY);
		periods = WeatherAPI.getForecast(region, gridX, gridY);
		minAndMaxTemps = MyWeatherAPI.getMinAndMaxTemperatures(region, gridX, gridY);
		currAlerts = MyWeatherAPI.getActiveAlerts(latitude, longitude);
		System.out.println("Done.\n");
	}

	public static void setBackgroundImage(String url){
		Image image = new Image(url, 360, 640, false, true);
		backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, null, null);
	}

	public static String getLocation(){
		return location;
	}


	public static void setTheme(String theme){
		SceneBuilder.theme = theme;
	}

	public void setStage(Stage stage) {
		SceneBuilder.stage = stage;
	}

	public static int convertFahrenheitToCelsius(int fahrenheit){
		return (fahrenheit - 32) * 5 / 9;
	}

	public static int convertCelsiusToFahrenheit(int celsius){
		return (celsius * 9 / 5) + 32;
	}
}
