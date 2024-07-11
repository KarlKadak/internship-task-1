package dev.karlkadak.backend.repository;

import dev.karlkadak.backend.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for interacting with the WeatherData table
 */
@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    /**
     * Finds the latest {@link dev.karlkadak.backend.entity.WeatherData WeatherData} object for a
     * {@link dev.karlkadak.backend.entity.City City} with the specified
     * {@link dev.karlkadak.backend.entity.City#id id}
     *
     * @param city_id The {@link dev.karlkadak.backend.entity.City City} object's
     *                {@link dev.karlkadak.backend.entity.City#id id}
     * @return An {@link java.util.Optional} with the latest
     * {@link dev.karlkadak.backend.entity.WeatherData WeatherData} object for a
     * {@link dev.karlkadak.backend.entity.City City} with the specified {@link dev.karlkadak.backend.entity.City#id id}
     * or an empty one if weather data for the {@link dev.karlkadak.backend.entity.City City} with the specified
     * {@link dev.karlkadak.backend.entity.City#id id} has not been recorded
     */
    Optional<WeatherData> findTopByCity_IdOrderByTimestampDesc(Long city_id);
}
