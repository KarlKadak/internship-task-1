package dev.karlkadak.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data transfer object for HTTP responses for indicating an error has occurred
 */
@AllArgsConstructor
@Getter
public class ErrorResponse {

    private String message;
}

