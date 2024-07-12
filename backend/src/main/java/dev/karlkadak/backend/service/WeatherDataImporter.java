package dev.karlkadak.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karlkadak.backend.entity.City;
import dev.karlkadak.backend.entity.WeatherData;
import dev.karlkadak.backend.exception.WeatherDataFetchException;
import dev.karlkadak.backend.repository.CityRepository;
import dev.karlkadak.backend.repository.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Used for importing weather data to the database from <a href="https://openweathermap.org/api">OpenWeather API</a><br>
 * Also logs the results of imports using {@link java.util.logging.Logger}
 */
@Component
public class WeatherDataImporter {

    private final WeatherDataRepository weatherDataRepository;
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
    public WeatherDataImporter(WeatherDataRepository weatherDataRepository, CityRepository cityRepository,
                               Logger logger, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.weatherDataRepository = weatherDataRepository;
        this.cityRepository = cityRepository;
        this.logger = logger;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches the weather data of all {@link dev.karlkadak.backend.entity.City City} objects which have
     * {@link dev.karlkadak.backend.entity.City#gatherData gatherData} set as {@link java.lang.Boolean#TRUE TRUE} and
     * saves it to the database
     */
    public void defaultImport() {
        final List<City> citiesToFetch = cityRepository.findAllByGatherDataTrue();
        if (citiesToFetch.isEmpty()) return;
        Integer fetchedCityCount = 0;

        logger.info(String.format("Starting to fetch weather data for %d cities.", citiesToFetch.size()));

        for (City city : citiesToFetch) {
            try {
                fetchAndSave(city);
                fetchedCityCount++;
            } catch (WeatherDataFetchException e) {
                logger.warning(String.format("Failed fetching weather data for city \"%s\". Reason: %s", city.getName(),
                                             e.getMessage()));
            }

            logger.info(String.format("Fetched weather data for city \"%s\".", city.getName()));
        }

        logger.info(String.format("Saved weather data for %d cities.", fetchedCityCount));
    }

    /**
     * Fetches the weather data of the specified {@link dev.karlkadak.backend.entity.City City} and saves it to the
     * database<br> Needs to be package-private in order to test
     *
     * @param city {@link dev.karlkadak.backend.entity.City City} to fetch weather data about
     * @throws WeatherDataFetchException in case of an error
     */
    void fetchAndSave(City city)
            throws WeatherDataFetchException {
        WeatherData fetchedData;

        // Attempt requesting the weather data, throw a
        try {
            fetchedData = requestData(city);
        } catch (Exception e) {
            throw new WeatherDataFetchException(e.getMessage());
        }

        // Save the fetched data to the database
        weatherDataRepository.save(fetchedData);
    }

    /**
     * Requests and returns the weather data of the specified city
     *
     * @param city {@link dev.karlkadak.backend.entity.City City} to fetch weather data about
     * @return {@link dev.karlkadak.backend.entity.WeatherData} about the specified
     * {@link dev.karlkadak.backend.entity.City City}
     * @throws Exception in case of a JSON parse error or when a required value is missing from the parsed JSON
     */
    private WeatherData requestData(City city)
            throws Exception {
        // Initialize datapoint variables
        final long timestamp;
        Double airTemperature = null;
        Double windSpeed = null;
        Integer humidity = null;

        // Variables needed for performing the API request
        Double latitude = city.getLatitude();
        Double longitude = city.getLongitude();
        String requestUrl = String.format(
                "https://api.openweathermap.org/data/2.5/weather?units=metric&lat=%f&lon=%f&appid=%s", latitude,
                longitude, apiKey);
        Map<String, Object> responseMap;

        // Process the API response
        String jsonResponse = restTemplate.getForObject(requestUrl, String.class);
        responseMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
        timestamp = ((Integer) responseMap.get("dt")).longValue();
        Object mainDataObject = responseMap.get("main");
        if (mainDataObject instanceof Map) {
            Map<String, Object> mainData = (Map<String, Object>) mainDataObject;
            airTemperature = (Double) mainData.get("temp");
            humidity = (Integer) mainData.get("humidity");
        }
        Object windDataObject = responseMap.get("wind");
        if (windDataObject instanceof Map) {
            Map<String, Object> windData = (Map<String, Object>) windDataObject;
            windSpeed = (Double) windData.get("speed");
        }

        return new WeatherData(city, timestamp, airTemperature, windSpeed, humidity);
    }
}
