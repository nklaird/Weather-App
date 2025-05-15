package hourlyWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyRoot {
    public String type;
    public HourlyGeometry geometry;
    public HourlyProperties properties;
}
