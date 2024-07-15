package dev.karlkadak.backend.exception;

public class CityAlreadyNotBeingTrackedException extends RuntimeException {

    public CityAlreadyNotBeingTrackedException() {
        super("City is already not being tracked.");
    }
}
