package dev.karlkadak.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Class representing a single city for weather data collection
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Required for JPA
public class City {

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The human-readable name of the city
     */
    @NonNull
    @Column(unique = true, nullable = false)
    @Getter
    private String name;

    /**
     * Specifies if weather data collection for the city is enabled
     */
    @NonNull
    @Column(nullable = false)
    @Getter
    @Setter
    private Boolean gatherData;

    /**
     * Geographical latitude of the city
     */
    @NonNull
    @Column(nullable = false)
    @Getter
    private Double latitude;

    /**
     * Geographical longitude of the city
     */
    @NonNull
    @Column(nullable = false)
    @Getter
    private Double longitude;

    /**
     * Default constructor, sets {@link #gatherData} field to
     * {@link java.lang.Boolean#TRUE}
     *
     * @param name      The human-readable name of the city
     * @param latitude  Geographical latitude of the city
     * @param longitude Geographical longitude of the city
     */
    public City(@NonNull String name, @NonNull Double latitude, @NonNull Double longitude) {
        this.name = name;
        this.gatherData = Boolean.TRUE;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructor with specifying a value for the {@link #gatherData} field
     *
     * @param name       The human-readable name of the city
     * @param gatherData Specifies if weather data collection for the city is enabled
     * @param latitude   Geographical latitude of the city
     * @param longitude  Geographical longitude of the city
     */
    public City(@NonNull String name, @NonNull Boolean gatherData, @NonNull Double latitude,
                @NonNull Double longitude) {
        this.name = name;
        this.gatherData = gatherData;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
