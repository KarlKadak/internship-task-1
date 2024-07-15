package dev.karlkadak.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karlkadak.backend.entity.City;
import dev.karlkadak.backend.exception.CityManagementException;
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
 * Used for managing the cities and their tracking in the database Also logs the management actions using
 * {@link java.util.logging.Logger}
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
     * @throws CityManagementException if a city with the given name does not exist or tracking for it is already
     *                                 enabled
     */
    public City enableImporting(String name)
            throws CityManagementException {
        // Retrieve the complete city object
        City city = retrieveCompleteCity(name);

        boolean cityIsPresent = cityRepository.exists(Example.of(city));

        // Throw exception if city is already being tracked
        if (cityIsPresent && city.isImportingData())
            throw new CityManagementException("City is already being tracked.");

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
     * @throws CityManagementException if the city with the given name is not saved
     */
    public City disableImporting(long id)
            throws CityManagementException {
        // Retrieve the city object
        Optional<City> retrievedCityOptional = cityRepository.findById(id);
        if (retrievedCityOptional.isEmpty()) throw new CityManagementException("City does not exist.");
        City city = retrievedCityOptional.get();

        // Throw exception if tracking for city is already disabled
        if (!city.isImportingData()) throw new CityManagementException("City is already not being tracked.");

        // Toggle the tracking
        city.setImportingData(false);

        // Save the city to database
        cityRepository.save(city);

        // Log the action
        logger.info(String.format("Disabled tracking for city \"%s\".", city.getName()));
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
     * @throws CityManagementException in case of a processing error or when city with given name does not exist
     */
    City retrieveCompleteCity(String name)
            throws CityManagementException {
        // Check the name for correct formatting
        if (!nameFormattedCorrectly(name)) throw new CityManagementException("Malformatted city name.");

        // Initialize variables
        JsonNode arrayNode = null;
        String completeName;
        double latitude;
        double longitude;

        // Perform the API request
        try {
            String requestUrl = String.format("http://api.openweathermap.org/geo/1.0/direct?q=%s=&limit=1&appid=%s",
                                              name, apiKey);
            String jsonResponse = restTemplate.getForObject(requestUrl, String.class);
            arrayNode = objectMapper.readTree(jsonResponse);

            // Throw exception in case of a processing error or an empty response
            if (arrayNode == null || !arrayNode.isArray()) throw new JsonMappingException("");
            if (arrayNode.isEmpty()) throw new Exception("City with given name does not exist.");
        } catch (JsonProcessingException e) {
            logger.warning(String.format("API response processing error when retrieving data for city \"%s\".", name));
            throw new CityManagementException("API response processing error.");
        } catch (Exception e) {
            throw new CityManagementException(e.getMessage());
        }


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
            throw new CityManagementException("Error when processing retrieved city data.");
        }
    }

    /**
     * Used for preliminary checking of a city name's validity<br> Refuses malformatted names
     *
     * @param name complete name of the city to check
     * @return true for acceptable, false for malformatted city name
     */
    private boolean nameFormattedCorrectly(String name) {
        return (!name.isBlank() && name.chars().noneMatch(Character::isDigit));
    }
}
