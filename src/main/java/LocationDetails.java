import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationDetails extends SceneBuilder{

	private static ArrayList<Pair<String, int[]>> gridpoints;
	private static ArrayList<Pair<String, Double[]>> coordinatesArray;
	private static VBox locationsBox;
	private static ScrollPane locationsScroll;
	private static BorderPane root;
	private static TextField cityInput, stateInput, longitudeInput, latitudeInput;
	private static Label results;

	private static List<String> randomLocations;
	private static List<Double[]> randomCoordinates;
	private static List<String> randomRegions;
	private static List<Integer> randomGridX;
	private static List<Integer> randomGridY;

	public static Scene getScene(){
		randomLocations = List.of(
				"Chicago, Illinois",
				"Los Angeles, California",
				"Fairbanks, Alaska",
				"Phoenix, Arizona",
				"Washington, D.C.",
				"Austin, Texas",
				"New York City, New York",
				"Seattle, Washington",
				"Atlanta, Georgia",
				"Miami, Florida"
		);
		randomCoordinates = List.of(
				new Double[]{41.882, -87.6324}, //chicago
				new Double[]{34.0481, -118.2542}, //LA
				new Double[]{64.8363, -147.7181}, //fairbanks
				new Double[]{33.4482, -112.0751}, //phoenix
				new Double[]{38.895, -77.0367}, //dc
				new Double[]{30.2681, -97.7428}, //austin
				new Double[]{40.7127, -74.006}, //nyc
				new Double[]{47.6032, -122.3303}, //seattle
				new Double[]{33.7508, -84.3899}, //atlanta
				new Double[]{25.7734, -80.1919} //miami
		);
		randomRegions = List.of(
				"LOT", "LOX", "AFG", "PSR", "LWX", "EWX", "OKX", "SEW", "FFC", "MFL"
		);

		randomGridX = List.of(
				75, 155, 492, 159, 97, 156, 33, 125, 51, 110
		);

		randomGridY = List.of(
				73, 45, 119, 58, 71, 91, 35, 68, 87, 51
		);

		BorderPane root = getRoot();
		Scene scene = new Scene(root, 315, 560);

		scene.getStylesheets().add(SceneBuilder.class.getResource("/css/location.css").toExternalForm());

		return scene;
	}

	public static BorderPane getRoot() {
		//Building the upper half portion of the screen
		Button back = new Button("Back");
		Button random = new Button("Random");
		BorderPane locationTop = new BorderPane();
		locationTop.setLeft(back);
		locationTop.setRight(random);
		locationTop.setPrefHeight(40);

		//No changes are made when back is clicked
		back.setOnAction(e -> {
			locationStage.close();
		});

		random.setOnAction(LocationDetails::getRandomLocation);

		Label locationLabel = new Label("Enter City/State (MAPBOX_API_KEY required) :");
		Label cityLabel = new Label("City:");
		Label stateLabel = new Label("State:");
		Label coordsLabel = new Label("Coordinates (WGS 84/EPSG 4326 format) :");
		Label longitudeLabel = new Label("Longitude:");
		Label latitudeLabel = new Label("Latitude:");

		cityInput = new TextField();
		stateInput = new TextField();
		longitudeInput = new TextField();
		latitudeInput = new TextField();

		HBox cityBox = getDetailsBox(cityLabel, cityInput);
		HBox stateBox = getDetailsBox(stateLabel, stateInput);
		HBox latitudeBox = getDetailsBox(latitudeLabel, latitudeInput);
		HBox longitudeBox = getDetailsBox(longitudeLabel, longitudeInput);

		VBox placeBox = new VBox(5, locationLabel, cityBox, stateBox);
		VBox coordsBox = new VBox(5, coordsLabel, latitudeBox, longitudeBox);

		//MAPBOX_API_KEY is required to input a city/state
		if(System.getenv("MAPBOX_API_KEY") == null){
			cityInput.setEditable(false);
			stateInput.setEditable(false);
		}

		//Elements for the bottom half of the screen
		Button submitButton = new Button("Submit");
		HBox submitHBox = new HBox(submitButton);

		results = new Label("Results : (Please be patient while the app loads!)");
		results.setStyle("-fx-font-size: 11");
		locationsScroll = new ScrollPane();
		locationsScroll.setPrefHeight(225);

		//Placing all the elements together
		VBox rootCenterBox = new VBox(10, placeBox, coordsBox);
		BorderPane rootCenter = new BorderPane(rootCenterBox);
		//putting the submit button in the bottom right corner of rootCenter
		rootCenter.setBottom(submitHBox);
		submitHBox.setAlignment(Pos.CENTER_RIGHT);

		locationsBox = new VBox(5, results, locationsScroll);
		root = new BorderPane(rootCenter);

		root.setTop(locationTop);
		root.setBottom(locationsBox);
		root.setPadding(new Insets(10));
		root.setId("rootBox");

		submitButton.setOnAction(LocationDetails::submitHandler);

		return new BorderPane(root);
	}

	//Creates the HBox for each input box
	private static HBox getDetailsBox(Label detailLabel, TextField detailInput){
		detailInput.setPrefWidth(100);

		BorderPane detailPane = new BorderPane();
		detailPane.setLeft(detailLabel);
		detailPane.setRight(detailInput);

		detailPane.setPadding(new Insets(5));
		detailPane.setBorder(Border.stroke(Color.BLACK));
		detailPane.setPrefWidth(295);
		detailPane.setId("hbox");

		HBox detailBox = new HBox(detailPane);
		detailBox.setAlignment(Pos.CENTER);

		return detailBox;
	}

	/*  Handler for the submit button
		When the user clicks submit, two things happen:
		- If the user has inputted a city/state, then MyWeatherAPI.getCoords() retrieves the coordinates
		of that location. Multiple locations may share the same name, so multiple coordinates will be
		retrieved. MyWeatherAPI.getGridInfo() retrieves the grid information for each pair of
		coordinates.
		- If the user has inputted a pair of coordinates, then MyWeatherAPI.getGridInfo() retrieves the
		grid information of that location.

		If the user has inputted both a pair of coordinates AND a place, the information for both
		locations will be retrieved even if they are different locations.

		On default, if the user has inputted an invalid location, the results will display the
		information for the United States.
	*/
	private static void submitHandler(ActionEvent event){
		String city = cityInput.getText();
		String state = stateInput.getText();
		String latitudeText = latitudeInput.getText();
		String longitudeText = longitudeInput.getText();

		Pair<String, int[]> gridInfo;
		gridpoints = new ArrayList<>();
		coordinatesArray = new ArrayList<>();

		if(!city.isEmpty() || !state.isEmpty()){
			coordinatesArray = MyWeatherAPI.getCoords(city, state); //Returns a Pair<String, Double[]> of (Address, Coordinates)

			for(Pair<String, Double[]> coordPair : coordinatesArray){
				System.out.println(Arrays.toString(coordPair.getValue()) + " " + coordPair.getKey()); //for debugging purposes

				gridInfo = MyWeatherAPI.getGridInfo(coordPair.getValue()[0], coordPair.getValue()[1]); //Returns a Pair<String, int[]> of (gridId, gridX, gridY)
				if(gridInfo == null || gridInfo.getKey().isEmpty()){
					continue;
				}
				gridpoints.add(gridInfo);
			}
		}

		if(!latitudeText.isEmpty() && !longitudeText.isEmpty()){
			//Formats the lat/lon coords to follow the format for the NWS API service
			latitudeText = String.format("%.4f", Double.parseDouble(latitudeInput.getText()));
			longitudeText = String.format("%.4f", Double.parseDouble(longitudeInput.getText()));

			//yes... we converted a string -> double -> string -> double... only way to format a double without using another package
			Double latitudeVal = Double.parseDouble(latitudeText);
			Double longitudeVal = Double.parseDouble(longitudeText);

			gridInfo = MyWeatherAPI.getGridInfo(latitudeVal, longitudeVal);
			if (gridInfo != null && !gridInfo.getKey().isEmpty()){
				gridpoints.add(gridInfo);

				String location = latitudeVal + "," + longitudeVal;
				//since coordinates is an ArrayList of Pair<String, Double[]> objects, it's necessary to create an "address" for this coordinate
				Pair<String, Double[]> coords = new Pair<>(location, new Double[]{latitudeVal, longitudeVal});
				coordinatesArray.addFirst(coords);
			}
		}

		//Once all the data has been retrieved, getLocationScroll() is called to display all the necessary outputs.
		locationsScroll = getLocationScroll();
		locationsBox = new VBox(5, results, locationsScroll);
		root.setBottom(locationsBox);
	}

	//Creates ScrollPane to display the results
	//Each result is displayed onto a button, where the user can scroll and select the location they desire
	private static ScrollPane getLocationScroll(){
		VBox locationsBox = new VBox();

		ArrayList<Button> buttonsArray = new ArrayList<>();
		for(int i = 0; i < gridpoints.size(); i++){
			Button locationButton = new Button(coordinatesArray.get(i).getKey()); //The text for the button is the address of the location
			buttonsArray.add(locationButton);

			locationButton.setPrefSize(280, 50);
			locationButton.setAlignment(Pos.CENTER);
			locationsBox.getChildren().add(locationButton);

			locationButton.setOnAction(e -> {
				int index = buttonsArray.indexOf(locationButton);
				Pair<String, int[]> buttonInfo = gridpoints.get(index);
				Pair<String, Double[]> coords = coordinatesArray.get(index);

				//this sets the location for the whole app
				setLocation(coords.getKey());
				setCoordinates(coords.getValue()[0], coords.getValue()[1]);
				setGridpoint(buttonInfo.getKey(), buttonInfo.getValue()[0], buttonInfo.getValue()[1]);
				updateData();

				//prepares the home screen to send the user back
				stage.setScene(HomeScreen.getScene());

				//closes the dialog window
				locationStage.close();
			});
		}

		ScrollPane locationScroll = new ScrollPane(locationsBox);
		locationScroll.setPrefHeight(225);

		return locationScroll;
	}

	private static void getRandomLocation(ActionEvent event){
		gridpoints = new ArrayList<>();
		coordinatesArray = new ArrayList<>();

		int randomIndex = (int)	(Math.random() * 10);
		Pair<String, Double[]> coords = new Pair<>(randomLocations.get(randomIndex), randomCoordinates.get(randomIndex));
		Pair<String, int[]> gridpoint = new Pair<>(randomRegions.get(randomIndex), new int[]{randomGridX.get(randomIndex), randomGridY.get(randomIndex)});
		coordinatesArray.add(coords);
		gridpoints.add(gridpoint);

		locationsScroll = getLocationScroll();
		locationsBox = new VBox(5, results, locationsScroll);
		root.setBottom(locationsBox);
	}
}
