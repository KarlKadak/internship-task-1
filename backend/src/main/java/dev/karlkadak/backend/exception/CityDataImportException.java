package dev.karlkadak.backend.exception;

public class CityDataImportException extends RuntimeException {

    public CityDataImportException() {
        super("Error occurred when importing city data.");
    }
}
