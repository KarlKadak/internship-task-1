package dev.karlkadak.backend.dto;

import dev.karlkadak.backend.entity.City;
import lombok.Getter;

/**
 * Data transfer object for HTTP responses for city data requests
 */
@Getter
public class CityResponse {

    private final long id;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final String flagHref;

    /**
     * Default constructor, build the {@link #flagHref} URL if given {@link City} object's
     * {@link City#countryCode countryCode} is not null
     *
     * @param city {@link City} object to build the response object from
     */
    public CityResponse(City city) {
        this.id = city.getId();
        this.name = city.getName();
        this.latitude = city.getCoordinatePair().getLatitude();
        this.longitude = city.getCoordinatePair().getLongitude();
        if (city.getCountryCode() == null || city.getCountryCode().isBlank()) this.flagHref = null;
        else this.flagHref = "https://flagcdn.com/h60/" + city.getCountryCode().toLowerCase() + ".png";
    }
}
