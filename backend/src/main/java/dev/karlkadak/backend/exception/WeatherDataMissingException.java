package dev.karlkadak.backend.exception;

public class WeatherDataMissingException extends RuntimeException {

    public WeatherDataMissingException() {
        super("Weather data for given city has not been recorded yet.");
    }
}
