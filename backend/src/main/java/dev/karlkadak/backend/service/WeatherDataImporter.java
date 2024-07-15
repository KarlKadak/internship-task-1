package dev.karlkadak.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karlkadak.backend.entity.City;
import dev.karlkadak.backend.entity.WeatherData;
import dev.karlkadak.backend.exception.FailedWeatherDataFetchException;
import dev.karlkadak.backend.repository.CityRepository;
import dev.karlkadak.backend.repository.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Used for importing weather data to the database from <a href="https://openweathermap.org/api">OpenWeather API</a><br>
 * Also logs the results of imports using {@link java.util.logging.Logger}
 */
@Service
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
     * {@link dev.karlkadak.backend.entity.City#importingData importingData} set as {@link java.lang.Boolean#TRUE TRUE}
     * and saves it to the database
     */
    public void defaultImport() {
        final List<City> citiesToFetch = cityRepository.findAllByImportingDataTrue();
        if (citiesToFetch.isEmpty()) return;
        Integer fetchedCityCount = 0;

        logger.info(String.format("Starting to fetch weather data for %d cities.", citiesToFetch.size()));

        for (City city : citiesToFetch) {
            try {
                fetchAndSave(city);
                fetchedCityCount++;
            } catch (FailedWeatherDataFetchException e) {
                logger.warning(String.format("Failed fetching weather data for city \"%s\". Reason: %s", city.getName(),
                                             e.getMessage()));
            }

            logger.info(String.format("Fetched weather data for city \"%s\".", city.getName()));
        }

        logger.info(String.format("Saved weather data for %d cities.", fetchedCityCount));
    }

    /**
     * Fetches the weather data of the specified {@link dev.karlkadak.backend.entity.City City} and saves it to the
     * database
     *
     * @param city {@link dev.karlkadak.backend.entity.City City} to fetch weather data about
     */
    private void fetchAndSave(City city) {
        WeatherData fetchedData;

        // Attempt requesting the weather data, throw a
        try {
            fetchedData = requestData(city);
        } catch (Exception e) {
            throw new FailedWeatherDataFetchException(e.getMessage());
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
     */
    private WeatherData requestData(City city)
            throws JsonProcessingException {
        // Initialize datapoint variables
        final long timestamp;
        Double airTemperature = null;
        Double windSpeed = null;
        Integer humidity = null;
        String iconCode = null;

        // Variables needed for performing the API request
        Double latitude = city.getCoordinatePair().getLatitude();
        Double longitude = city.getCoordinatePair().getLongitude();
        String requestUrl = String.format(
                "https://api.openweathermap.org/data/2.5/weather?units=metric&lat=%f&lon=%f&appid=%s", latitude,
                longitude, apiKey);
        Map<String, Object> responseMap;

        // Process the API response
        String jsonResponse = restTemplate.getForObject(requestUrl, String.class);
        responseMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
        timestamp = ((Integer) responseMap.get("dt")).longValue();
        try {
            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) responseMap.get("weather");
            Map<String, Object> weatherObject = weatherList.get(0);
            iconCode = (String) weatherObject.get("icon");
        } catch (Exception _) {
        }
        try {
            Map<String, Object> mainObject = (Map<String, Object>) responseMap.get("main");
            Map<String, Object> mainData = (Map<String, Object>) mainObject;
            airTemperature = (Double) mainData.get("temp");
            humidity = (Integer) mainData.get("humidity");
        } catch (Exception _) {
        }
        try {
            Map<String, Object> windObject = (Map<String, Object>) responseMap.get("wind");
            Map<String, Object> windData = (Map<String, Object>) windObject;
            windSpeed = (Double) windData.get("speed");
        } catch (Exception _) {
        }

        return new WeatherData(city, timestamp, airTemperature, windSpeed, humidity, iconCode);
    }
}
