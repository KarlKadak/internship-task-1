package dev.karlkadak.backend.exception;

public class MalformedCityNameException extends RuntimeException {

    public MalformedCityNameException() {
        super("Malformed city name.");
    }
}
