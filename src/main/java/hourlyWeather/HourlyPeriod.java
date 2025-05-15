package hourlyWeather;

import java.util.Date;

public class HourlyPeriod {
    public int number;
    public String name;
    public Date startTime;
    public Date endTime;
    public boolean isDaytime;
    public int temperature;
    public String temperatureUnit;
    public String temperatureTrend;
    public HourlyProbabilityOfPrecipitation probabilityOfPrecipitation;
    public HourlyDewpoint dewpoint;
    public HourlyRelativeHumidity relativeHumidity;
    public String windSpeed;
    public String windDirection;
    public String icon;
    public String shortForecast;
    public String detailedForecast;
}
