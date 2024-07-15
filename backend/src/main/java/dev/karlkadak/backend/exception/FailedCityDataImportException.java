package dev.karlkadak.backend.exception;

public class FailedCityDataImportException extends RuntimeException {

    public FailedCityDataImportException() {
        super("Error occurred when importing city data.");
    }
}
