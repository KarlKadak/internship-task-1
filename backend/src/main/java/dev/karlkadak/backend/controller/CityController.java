package dev.karlkadak.backend.controller;

import dev.karlkadak.backend.dto.AddCityRequest;
import dev.karlkadak.backend.entity.City;
import dev.karlkadak.backend.exception.CityNotFoundException;
import dev.karlkadak.backend.exception.MalformedCityNameException;
import dev.karlkadak.backend.repository.CityRepository;
import dev.karlkadak.backend.service.CityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for interacting with the city database
 */
@RestController
@RequestMapping("${api.prefix}/cities")
public class CityController {

    private final CityRepository cityRepository;
    private final CityManager cityManager;

    @Autowired
    public CityController(CityRepository cityRepository, CityManager cityManager) {
        this.cityRepository = cityRepository;
        this.cityManager = cityManager;
    }

    /**
     * API endpoint for retrieving all {@link City} objects
     *
     * @return list of all {@link City} objects
     */
    @GetMapping
    ResponseEntity<List<City>> all() {
        return ResponseEntity.ok(cityRepository.findAll());
    }

    /**
     * API endpoint for adding and / or enabling weather data tracking for a {@link City} object
     *
     * @param addCityRequest request body (city name)
     * @return the added / enabled {@link City} object
     */
    @PostMapping
    ResponseEntity<City> enable(@RequestBody AddCityRequest addCityRequest) {
        if (addCityRequest.getName() == null || addCityRequest.getName().isEmpty()) {
            throw new MalformedCityNameException();
        }

        City city = cityManager.enableImporting(addCityRequest.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(city);
    }

    /**
     * API endpoint for retrieving a single {@link City} object
     *
     * @param id the {@link City} object's {@link City#id id}
     * @return the selected {@link City} object
     */
    @GetMapping("/{id}")
    ResponseEntity<City> one(@PathVariable Long id) {
        City city = cityRepository.findById(id).orElse(null);

        if (city == null) throw new CityNotFoundException(id);
        return ResponseEntity.ok(city);
    }

    /**
     * API endpoint for disabling weather data tracking for a {@link City} object
     *
     * @param id the {@link City} object's {@link City#id id}
     * @return an empty {@link ResponseEntity}
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> disable(@PathVariable Long id) {
        City city = cityManager.disableImporting(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
