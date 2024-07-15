package dev.karlkadak.backend.exception;

/**
 * Custom Exception to indicate errors during city management operations
 */
public class CityManagementException extends Exception {

    /**
     * Primary constructor
     *
     * @param message error message contents
     */
    public CityManagementException(String message) {
        super(message);
    }
}
