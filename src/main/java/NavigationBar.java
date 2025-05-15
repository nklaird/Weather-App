import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class NavigationBar {
	public static HBox navigationBar;
	private static Stage mainStage;

	public static HBox getNavigationBar() {
		return navigationBar;
	}

	public NavigationBar(Stage stage){
		mainStage = stage;

		Button homeButton, dailyButton, trendsButton, settingsButton;

		homeButton = makeNavigationButton("/images/home_icon.png");
		dailyButton = makeNavigationButton("/images/calendar_icon.png");
		trendsButton = makeNavigationButton("/images/bar_chart_icon.png");
		settingsButton = makeNavigationButton("/images/settings_icon.png");

		homeButton.setOnAction(e -> navigationBarHandler(e, HomeScreen.getScene(), "Home Screen"));
		dailyButton.setOnAction(e -> navigationBarHandler(e, DailyForecast.getScene(), "Daily Forecast"));
		trendsButton.setOnAction(e -> navigationBarHandler(e, WeeklyTrends.getScene(), "Weekly Trends"));
		settingsButton.setOnAction(e -> navigationBarHandler(e, Settings.getScene(), "Settings"));

		HBox navigationBarBox = new HBox(homeButton, dailyButton, trendsButton, settingsButton);
		Image wood = new Image("/images/grey-wood.jpg", 360, 160, false, true);
		BackgroundImage woodBackground = new BackgroundImage(wood, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, null, null);
		navigationBarBox.setBackground(new Background(woodBackground));
		navigationBar = navigationBarBox;
	}

	/*	Event handler for each button in the navigation bar.
		Changes the stage's scene and title
	*/
	private static void navigationBarHandler(ActionEvent event, Scene nextScene, String title){
		//closing any open dialog stages
		if(SceneBuilder.alertsStage != null && SceneBuilder.alertsStage.isShowing()){
			SceneBuilder.alertsStage.hide();
		}
		if(SceneBuilder.hourlyStage != null && SceneBuilder.hourlyStage.isShowing()){
			SceneBuilder.hourlyStage.hide();
		}

		mainStage.setScene(nextScene);
		mainStage.setTitle(title);
	}

	private static Button makeNavigationButton(String url){
		Button button = new Button();
		button.setPrefSize(160, 50);
		Image icon = new Image(url);
		ImageView view = new ImageView(icon);
		view.setFitHeight(40);
		view.setPreserveRatio(true);
		button.setGraphic(view);
		button.setId("navigationButton");

		return button;
	}
}
