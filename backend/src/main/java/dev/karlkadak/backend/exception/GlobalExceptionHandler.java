package dev.karlkadak.backend.exception;

import dev.karlkadak.backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Allows for handling exceptions globally and returning specific HTTP response codes
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CityAlreadyBeingTrackedException.class)
    public ResponseEntity<ErrorResponse> handleCityAlreadyBeingTrackedException(CityAlreadyBeingTrackedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CityAlreadyNotBeingTrackedException.class)
    public ResponseEntity<ErrorResponse> handleCityAlreadyNotBeingTrackedException(
            CityAlreadyNotBeingTrackedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCityNotFoundException(CityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(FailedCityDataImportException.class)
    public ResponseEntity<ErrorResponse> handleFailedCityDataImportException(FailedCityDataImportException ex) {
        return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MalformedCityNameException.class)
    public ResponseEntity<ErrorResponse> handleMalformedCityNameException(MalformedCityNameException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(WeatherDataMissingException.class)
    public ResponseEntity<ErrorResponse> handleWeatherDataMissingException(WeatherDataMissingException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }
}
