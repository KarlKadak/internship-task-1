package dev.karlkadak.backend.exception;

public class CityAlreadyBeingTrackedException extends RuntimeException {

    public CityAlreadyBeingTrackedException() {
        super("City is already being tracked.");
    }
}
