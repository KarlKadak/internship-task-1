package dev.karlkadak.backend.dto;

import dev.karlkadak.backend.entity.WeatherData;
import lombok.Getter;

/**
 * Data transfer object for HTTP responses for weather data requests
 */
@Getter
public class WeatherResponse {

    private final long timestamp;
    private final Double airTemp;
    private final Double windSpeed;
    private final Integer humidity;
    private final String iconHref;

    /**
     * Default constructor, build the {@link WeatherResponse#iconHref} URL if given {@link WeatherData} object's
     * {@link WeatherData#iconCode iconCode} is not null
     *
     * @param weatherData {@link WeatherData} object to build the response object from
     */
    public WeatherResponse(WeatherData weatherData) {
        this.timestamp = weatherData.getTimestamp();
        this.airTemp = weatherData.getAirTemperature();
        this.windSpeed = weatherData.getWindSpeed();
        this.humidity = weatherData.getHumidity();
        if (weatherData.getIconCode() == null || weatherData.getIconCode().isBlank()) this.iconHref = null;
        else this.iconHref = "https://openweathermap.org/img/wn/" + weatherData.getIconCode() + "@2x.png";
    }
}
