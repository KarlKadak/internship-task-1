package dev.karlkadak.backend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data transfer object for an HTTP POST request for adding a city to the list of tracked cities
 */
@Setter
@Getter
public class AddCityRequest {

    private String name;
}
