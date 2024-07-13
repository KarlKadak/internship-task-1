package dev.karlkadak.backend.repository;

import dev.karlkadak.backend.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for interacting with the WeatherData table
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    /**
     * Finds the {@link dev.karlkadak.backend.entity.City City} object with the specified
     * {@link dev.karlkadak.backend.entity.City#name name}
     *
     * @param name The {@link dev.karlkadak.backend.entity.City City} object's
     *             {@link dev.karlkadak.backend.entity.City#name name}
     * @return An {@link java.util.Optional} with the {@link dev.karlkadak.backend.entity.City City} object with the
     * specified {@link dev.karlkadak.backend.entity.City#name name} or an empty one if a
     * {@link dev.karlkadak.backend.entity.City City} with the specified
     * {@link dev.karlkadak.backend.entity.City#name name} was not found
     */
    Optional<City> findByName(String name);

    /**
     * Finds all {@link dev.karlkadak.backend.entity.City City} objects which have
     * {@link dev.karlkadak.backend.entity.City#importingData importingData} set as {@link java.lang.Boolean#TRUE TRUE}
     *
     * @return A {@link java.util.List} containing all {@link dev.karlkadak.backend.entity.City City} objects which have
     * {@link dev.karlkadak.backend.entity.City#importingData importingData} set as {@link java.lang.Boolean#TRUE TRUE}
     */
    List<City> findAllByImportingDataTrue();
}
