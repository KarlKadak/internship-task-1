package dev.karlkadak.backend.exception;

public class FailedWeatherDataFetchException extends RuntimeException {

    public FailedWeatherDataFetchException(String message) {
        super(message);
    }
}
