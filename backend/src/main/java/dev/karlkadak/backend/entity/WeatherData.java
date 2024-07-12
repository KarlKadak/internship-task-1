package dev.karlkadak.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Class representing a single point of gathered weather data for a single city
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Required for JPA
public class WeatherData {

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The city for which the data is collected
     */
    @NonNull
    @JoinColumn(name = "city_id", nullable = false)
    @ManyToOne
    @Getter
    private City city;

    /**
     * UNIX timestamp of the data calculation, UTC
     */
    @NonNull
    @Column(nullable = false)
    private Long timestamp;

    /**
     * Air temperature at time of data calculation, Celsius<br> Null in case of missing value
     */
    @Getter
    private Double airTemperature;

    /**
     * Wind speed at time of data calculation, meter/sec<br> Null in case of missing value
     */
    @Getter
    private Double windSpeed;

    /**
     * Humidity at time of data calculation, %<br> Null in case of missing value
     */
    @Getter
    private Integer humidity;

    /**
     * Default constructor
     *
     * @param city           The city for which the data is collected
     * @param timestamp      UNIX timestamp of the data calculation, UTC
     * @param airTemperature Air temperature at time of data calculation, Celsius<br> Null in case of missing value
     * @param windSpeed      Wind speed at time of data calculation, meter/sec<br> Null in case of missing value
     * @param humidity       Humidity at time of data calculation, %<br> Null in case of missing value
     */
    public WeatherData(@NonNull City city, @NonNull Long timestamp, Double airTemperature, Double windSpeed,
                       Integer humidity) {
        this.city = city;
        this.timestamp = timestamp;
        this.airTemperature = airTemperature;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
    }
}
