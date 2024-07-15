package dev.karlkadak.backend.exception;

public class CityNotFoundException extends RuntimeException {

    public CityNotFoundException() {
        super("City not found.");
    }

    public CityNotFoundException(long id) {
        super("City with ID " + id + " not found.");
    }
}
