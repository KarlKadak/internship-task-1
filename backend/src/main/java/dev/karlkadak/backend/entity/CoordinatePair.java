package dev.karlkadak.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Used to store coordinates of cities
 */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatePair {

    /**
     * Geographical latitude of the city
     */
    @Column(nullable = false)
    private double latitude;

    /**
     * Geographical longitude of the city
     */
    @Column(nullable = false)
    private double longitude;
}
