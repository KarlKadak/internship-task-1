package dev.karlkadak.backend.exception;

/**
 * Custom Exception to indicate errors during weather data fetch
 */
public class WeatherDataFetchException extends Exception {

    /**
     * Primary constructor
     *
     * @param message error message contents
     */
    public WeatherDataFetchException(String message) {
        super(message);
    }
}
