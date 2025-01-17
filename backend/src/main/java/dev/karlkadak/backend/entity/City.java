package dev.karlkadak.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Class representing a single city for weather data collection
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Required for JPA
public class City {

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The human-readable name of the city
     */
    @NonNull
    @Column(unique = true, nullable = false)
    private String name;

    /**
     * Specifies if weather data collection for the city is enabled
     */
    @Column(nullable = false)
    @Setter
    private boolean importingData;

    /**
     * Specifies the geographical location of the city
     */
    @NonNull
    @Embedded
    private CoordinatePair coordinatePair;

    /**
     * Country code of the location, used for flag image generation
     */
    @Getter
    private String countryCode;

    /**
     * Default constructor using coordinate values, sets {@link #importingData} field to
     * {@link java.lang.Boolean#TRUE TRUE}
     *
     * @param name      The human-readable name of the city
     * @param latitude  Geographical latitude of the city
     * @param longitude Geographical longitude of the city
     */
    public City(@NonNull String name, @NonNull Double latitude, @NonNull Double longitude, String countryCode) {
        this.name = name;
        this.importingData = Boolean.TRUE;
        this.coordinatePair = new CoordinatePair(latitude, longitude);
        this.countryCode = countryCode;
    }
}
