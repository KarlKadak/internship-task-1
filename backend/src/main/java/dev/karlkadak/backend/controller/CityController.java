package dev.karlkadak.backend.controller;

import dev.karlkadak.backend.dto.AddCityRequest;
import dev.karlkadak.backend.dto.CityResponse;
import dev.karlkadak.backend.dto.WeatherResponse;
import dev.karlkadak.backend.entity.City;
import dev.karlkadak.backend.entity.WeatherData;
import dev.karlkadak.backend.exception.CityNotFoundException;
import dev.karlkadak.backend.exception.MalformedCityNameException;
import dev.karlkadak.backend.exception.WeatherDataMissingException;
import dev.karlkadak.backend.repository.CityRepository;
import dev.karlkadak.backend.repository.WeatherDataRepository;
import dev.karlkadak.backend.service.CityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for interacting with the city database
 */
@RestController
@RequestMapping("${api.prefix}/cities")
public class CityController {

    private final CityRepository cityRepository;
    private final CityManager cityManager;
    private final WeatherDataRepository weatherDataRepository;

    @Autowired
    public CityController(CityRepository cityRepository, CityManager cityManager,
                          WeatherDataRepository weatherDataRepository) {
        this.cityRepository = cityRepository;
        this.cityManager = cityManager;
        this.weatherDataRepository = weatherDataRepository;
    }

    /**
     * API endpoint for retrieving information about all {@link City} objects for which data collection is enabled
     *
     * @return list of {@link CityResponse} objects representing all {@link City} objects for which data collection is
     * enabled
     */
    @GetMapping
    ResponseEntity<List<CityResponse>> all() {
        List<City> cities = cityRepository.findAllByImportingDataTrue();
        List<CityResponse> responseList = new ArrayList<>();
        for (City city : cities) {
            responseList.add(new CityResponse(city));
        }
        return ResponseEntity.ok(responseList);
    }

    /**
     * API endpoint for adding and / or enabling weather data tracking for a {@link City} object
     *
     * @param addCityRequest request body (city name)
     * @return a {@link CityResponse} object representing the added / enabled {@link City} object
     */
    @PostMapping
    ResponseEntity<CityResponse> enable(@RequestBody AddCityRequest addCityRequest) {
        if (addCityRequest.getName() == null || addCityRequest.getName().isEmpty()) {
            throw new MalformedCityNameException();
        }

        City city = cityManager.enableImporting(addCityRequest.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(new CityResponse(city));
    }

    /**
     * API endpoint for retrieving information about a single {@link City} object
     *
     * @param id the {@link City} object's {@link City#id id}
     * @return a {@link CityResponse} object representing the selected {@link City} object
     */
    @GetMapping("/{id}")
    ResponseEntity<CityResponse> one(@PathVariable Long id) {
        City city = cityRepository.findById(id).orElse(null);

        if (city == null) throw new CityNotFoundException(id);
        return ResponseEntity.ok(new CityResponse(city));
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

    /**
     * API endpoint for requesting the most recent weather data linked to a {@link City} object
     *
     * @param id the {@link City} object's {@link City#id id}
     * @return the DTO of latest recorded {@link WeatherData} object
     */
    @GetMapping("/{id}/weather")
    ResponseEntity<WeatherResponse> weather(@PathVariable Long id) {
        City city = cityRepository.findById(id).orElse(null);
        if (city == null) throw new CityNotFoundException(id);
        Optional<WeatherData> weatherDataOptional = weatherDataRepository.findTopByCity_IdOrderByTimestampDesc(id);
        if (weatherDataOptional.isEmpty()) throw new WeatherDataMissingException();
        return ResponseEntity.ok(new WeatherResponse(weatherDataOptional.get()));
    }
}
