package dev.karlkadak.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karlkadak.backend.entity.City;
import dev.karlkadak.backend.exception.*;
import dev.karlkadak.backend.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Used for managing the cities and their tracking in the database
 * <br>Also logs the management actions using {@link java.util.logging.Logger}
 */
@Service
public class CityManager {

    private final CityRepository cityRepository;
    private final Logger logger;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * API key gathered from application.properties which is used for accessing the
     * <a href="https://openweathermap.org/api">OpenWeather API</a>
     */
    @Value("${openweather.api.key}")
    private String apiKey;

    @Autowired
    public CityManager(CityRepository cityRepository, Logger logger, RestTemplate restTemplate,
                       ObjectMapper objectMapper) {
        this.cityRepository = cityRepository;
        this.logger = logger;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Adds a {@link City} object with the given name to the database or enables tracking its weather data in case it
     * already exists
     *
     * @param name name of the city to add / toggle
     * @return generated / toggled {@link City} object
     */
    public City enableImporting(String name) {
        // Retrieve the complete city object
        City city = retrieveCompleteCity(name);

        boolean cityIsPresent = cityRepository.exists(Example.of(city));

        // Throw exception if city is already being tracked
        if (cityIsPresent && city.isImportingData()) throw new CityAlreadyBeingTrackedException();

        // Enable data tracking if city is present and not being tracked
        if (cityIsPresent) city.setImportingData(true);

        // Save the city to database (adds it if not present / edits the current instance if present)
        cityRepository.save(city);

        // Log the action
        logger.info(String.format("Enabled tracking for city \"%s\".", city.getName()));

        return city;
    }

    /**
     * Disables the weather data tracking for {@link City} object with given ID
     *
     * @param id ID of the {@link City} object to disable
     * @return toggled {@link City} object
     */
    public City disableImporting(long id) {
        // Retrieve the city object
        Optional<City> retrievedCityOptional = cityRepository.findById(id);
        if (retrievedCityOptional.isEmpty()) throw new CityNotFoundException(id);
        City city = retrievedCityOptional.get();

        // Throw exception if tracking for city is already disabled
        if (!city.isImportingData()) throw new CityAlreadyNotBeingTrackedException();

        // Toggle the tracking
        city.setImportingData(false);

        // Save the city to database
        cityRepository.save(city);

        // Log the action
        logger.info(String.format("Disabled tracking for city \"%s\".", city.getName()));

        return city;
    }

    /**
     * Retrieves a complete {@link City} object using the city name received using the <a
     * href="https://openweathermap.org/api">OpenWeather API</a><br> In case such city already exists in the database,
     * returns that instance instead
     * <br>Needs to be package-private in order to test directly
     *
     * @param name city name to look up
     * @return a {@link City} object linked to the name of the city input, if city is present in database returns that
     * instance
     */
    City retrieveCompleteCity(String name) {
        // Check the name for correct formatting
        if (!nameFormattedCorrectly(name)) throw new MalformedCityNameException();

        // Initialize variables
        JsonNode arrayNode;
        String completeName;
        double latitude;
        double longitude;

        // Perform the API request
        try {
            String requestUrl = String.format("https://api.openweathermap.org/geo/1.0/direct?q=%s=&limit=1&appid=%s",
                                              name, apiKey);
            String jsonResponse = restTemplate.getForObject(requestUrl, String.class);
            arrayNode = objectMapper.readTree(jsonResponse);

            // Throw exception in case of a processing error or an empty response
            if (arrayNode == null || !arrayNode.isArray()) throw new Exception();
        } catch (Exception e) {
            logger.warning(String.format("API response processing error when retrieving data for city \"%s\".", name));
            throw new FailedCityDataImportException();
        }

        // Throw exception if city is not present
        if (arrayNode.isEmpty()) throw new CityNotFoundException();

        // Process the API response
        try {
            Map<String, Object> responseMap = objectMapper.convertValue(arrayNode.get(0), new TypeReference<>() {
            });
            completeName = (String) responseMap.get("name");

            // Return the existing city if present
            Optional<City> cityOptional = cityRepository.findByName(completeName);
            if (cityOptional.isPresent()) return cityOptional.get();

            // Return a new city object
            latitude = (double) responseMap.get("lat");
            longitude = (double) responseMap.get("lon");
            return new City(completeName, latitude, longitude);
        } catch (Exception e) {
            logger.warning(String.format("Error when processing retrieved city data for city \"%s\".", name));
            throw new FailedCityDataImportException();
        }
    }

    /**
     * Used for preliminary checking of a city name's validity<br> Refuses malformed names
     *
     * @param name complete name of the city to check
     * @return true for acceptable, false for malformed city name
     */
    private boolean nameFormattedCorrectly(String name) {
        return (!name.isBlank() && name.chars().noneMatch(Character::isDigit));
    }
}
