import hourlyWeather.HourlyPeriod;
import hourlyWeather.HourlyRoot;
import javafx.util.Pair;
import weather.WeatherAPI;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class MyWeatherAPI extends WeatherAPI {

	//Retrieves coordinates from geocoding a location (basically converting location to coordinates) using Mapbox
	public static ArrayList<Pair<String, Double[]>> getCoords(String city, String state){
		city = city.replace(" ", "&"); //formatting the strings properly to work with the api
		state = state.replace(" ", "&");
		String requestString = "https://api.mapbox.com/search/geocode/v6/forward?"+
		"place=" + city + "&" + "region=" + state + "&country=United&States&access_token=" + System.getenv("MAPBOX_API_KEY");

		System.out.println("Coords : " + requestString); //for debugging purposes

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(requestString))
				//.method("GET", HttpRequest.BodyPublishers.noBody())
				.build();
		HttpResponse<String> response = null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Pair<String, Double[]>> coordsArray = new ArrayList<>();
		try{
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.body());

			Iterator<JsonNode> iterator = root.at("/features").elements();
			int index = 0;
			while(iterator.hasNext()){ //iterates through list of locations
				JsonNode countryNode = root.at("/features/" + index + "/properties/context/country/name");
				if(!countryNode.asText().equals("United States")){ //only retrieves coords in the US as required by NWS API
					iterator.next();
					index++;
					continue;
				}

				JsonNode latitudeNode = root.at("/features/" + index + "/properties/coordinates/latitude");
				JsonNode longitudeNode = root.at("/features/" + index + "/properties/coordinates/longitude");
				JsonNode addressNode = root.at("/features/" + index + "/properties/full_address");

				if(longitudeNode == null || latitudeNode == null || addressNode == null){
					System.out.println("Failed to parse features JSon");
					return null;
				}

				Double[] coords = new Double[2];
				String address = addressNode.asText();

				//rounds coords to 4 decimal places as required by NWS API
				String lat = String.format("%.4f", latitudeNode.asDouble());
				String lon = String.format("%.4f", longitudeNode.asDouble());

				coords[0] = Double.parseDouble(lat);
				coords[1] = Double.parseDouble(lon);

				Pair<String, Double[]> coordsPair = new Pair<>(address, coords);
				coordsArray.add(coordsPair);

				iterator.next();
				index++;
			}

		}catch (JsonProcessingException e){
			e.printStackTrace();
		}

		if(coordsArray.isEmpty()){
			System.err.println("Failed to parse MapBox JSon");
			return null;
		}

		return coordsArray;
	}

	//Retrieves gridpoints from coordinates which will be used to get forecast data for that location
	public static Pair<String, int[]> getGridInfo(Double latitude, Double longitude){
		String requestString = "https://api.weather.gov/points/" + latitude + "," + longitude;

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(requestString))
				//.method("GET", HttpRequest.BodyPublishers.noBody())
				.build();
		HttpResponse<String> response = null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Pair<String, int[]> gridPair = null;
		try{
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.body());

			JsonNode gridIDNode = root.at("/properties/gridId");
			JsonNode gridXNode = root.at("/properties/gridX");
			JsonNode gridYNode = root.at("/properties/gridY");

			if(gridIDNode == null || gridXNode == null || gridYNode == null){
				System.out.println("Failed to parse properties JSon");
				return null;
			}

			int[] gridCoords = {gridXNode.asInt(), gridYNode.asInt()};
			gridPair = new Pair<>(gridIDNode.asText(), gridCoords);

		}catch (JsonProcessingException e){
			e.printStackTrace();
		}

		if(gridPair == null){
			System.err.println("Failed to parse points JSon");
			return null;
		}

		return gridPair;
	}

	//Gets the lowest and highest temperatures for the next week
	//Returns a list of days and arrays of temperatures
	//One thing to note: the temperatures retrieved are in Celsius
	public static ArrayList<Pair<String, double[]>> getMinAndMaxTemperatures(String region, int gridx, int gridy){
		String requestString = "https://api.weather.gov/gridpoints/" + region + "/" + gridx + "," + gridy;
		System.out.println("Min/Max : " + requestString);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(requestString))
				//.method("GET", HttpRequest.BodyPublishers.noBody())
				.build();
		HttpResponse<String> response = null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Pair<String, double[]>> minAndMaxTemps = new ArrayList<>();
		try{
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.body());

			for(int i = 0; i < 8; i++){
				JsonNode dateNode = root.at("/properties/maxTemperature/values/" + i + "/validTime");
				JsonNode minNode = root.at("/properties/minTemperature/values/" + i + "/value");
				JsonNode maxNode = root.at("/properties/maxTemperature/values/" + i + "/value");

				String dateString = dateNode.asText();
				double minTemp = minNode.asDouble();
				double maxTemp = maxNode.asDouble();

				Pair<String, double[]> minAndMaxTemp = new Pair<>(dateString, new double[]{minTemp, maxTemp});
				minAndMaxTemps.add(minAndMaxTemp);
			}

		}catch (JsonProcessingException e){
			e.printStackTrace();
		}

		if(minAndMaxTemps.isEmpty()){
			System.err.println("Failed to parse properties JSon");
		}

		return minAndMaxTemps;
	}

	//Gets a list of active alerts in the area
	//Alert objects have a headline, description, and instruction
	public static ArrayList<Alert> getActiveAlerts(double latitude, double longitude){
		String requestString = "https://api.weather.gov/alerts/active?point=" + latitude + "," + longitude;
		System.out.println("Alerts : " + requestString);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(requestString))
				//.method("GET", HttpRequest.BodyPublishers.noBody())
				.build();
		HttpResponse<String> response = null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Alert> activeAlerts = new ArrayList<>();
		try{
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.body());

			Iterator<JsonNode> iterator = root.at("/features").elements();

			int i = 0;
			while(iterator.hasNext()){
				JsonNode headlineNode = root.at("/features/" + i + "/properties/headline");
				JsonNode descriptionNode = root.at("/features/" + i + "/properties/description");
				JsonNode instructionNode = root.at("/features/" + i + "/properties/instruction");

				Alert activeAlert = new Alert();
				activeAlert.headline = headlineNode.asText();
				activeAlert.description = descriptionNode.asText();
				activeAlert.instruction = instructionNode.asText();

				activeAlerts.add(activeAlert);

				iterator.next();
				i++;
			}

		}catch (JsonProcessingException e){
			e.printStackTrace();
		}

		return activeAlerts;
	}

	//Extending from the WeatherAPI class, retrieves the forecast for each hourly period instead of every 12-hour period.
	public static ArrayList<HourlyPeriod> getHourlyForecast(String region, int gridx, int gridy) {
		String requestString = "https://api.weather.gov/gridpoints/"+region+"/"+String.valueOf(gridx)+","+String.valueOf(gridy)+"/forecast/hourly";
		System.out.println("hourly: " + requestString); //debug purposes

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(requestString))
				//.method("GET", HttpRequest.BodyPublishers.noBody())
				.build();
		HttpResponse<String> response = null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		//The period objects retrieved from the hourly url have additional variables (dewpoint and humidity) thus it was
		//needed to create a separate package (hourlyWeather) used to serialize these objects.
		HourlyRoot r = getHourlyObject(response.body());

		System.out.println("forecast generated at: " + r.properties.generatedAt); //debugging purposes, to check if the forecast displayed is delayed

		if(r == null){
			System.err.println("Failed to parse JSon");
			return null;
		}
		return r.properties.periods;
	}
	public static HourlyRoot getHourlyObject(String json){
		ObjectMapper om = new ObjectMapper();
		HourlyRoot toRet = null;
		try {
			toRet = om.readValue(json, HourlyRoot.class);
			ArrayList<HourlyPeriod> p = toRet.properties.periods;

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return toRet;
	}
}