package dev.karlkadak.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karlkadak.backend.entity.City;
import dev.karlkadak.backend.entity.WeatherData;
import dev.karlkadak.backend.repository.CityRepository;
import dev.karlkadak.backend.repository.WeatherDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

class WeatherDataImporterTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Logger logger;

    @InjectMocks
    private WeatherDataImporter weatherDataImporter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherDataImporter = new WeatherDataImporter(weatherDataRepository, cityRepository, logger, restTemplate,
                                                      objectMapper);
    }

    @Test
    void testDefaultImport_NoCitiesToFetch() {
        when(cityRepository.findAllByImportingDataTrue()).thenReturn(Collections.emptyList());

        weatherDataImporter.defaultImport();

        verify(cityRepository, times(1)).findAllByImportingDataTrue();
        verifyNoInteractions(logger);
        verifyNoInteractions(weatherDataRepository);
    }

    @Test
    void testDefaultImport_LogsError()
            throws JsonProcessingException {
        List<City> cities = List.of(new City("City1", 1.0, 1.0));
        Map<String, Object> responseMap = new HashMap<>();
        doReturn(cities).when(cityRepository).findAllByImportingDataTrue();
        doReturn(responseMap).when(objectMapper).readValue(anyString(), any(TypeReference.class));

        weatherDataImporter.defaultImport();

        verify(logger, times(1)).warning(anyString());
        verifyNoInteractions(weatherDataRepository);
    }

    @Test
    void testDefaultImport_WithCitiesToFetch_WithAllData() {
        // Don't mock the ObjectMapper for this test, otherwise it returns null values leading to throwing an exception
        weatherDataImporter = new WeatherDataImporter(weatherDataRepository, cityRepository, logger, restTemplate,
                                                      new ObjectMapper());

        City city1 = new City("Tallinn", 59.4372155, 24.7453688);
        List<City> cities = List.of(city1);
        String response = """
                {
                  "weather": [
                    {
                      "icon": "01d"
                    }
                  ],
                  "main": {
                    "temp": 29.48,
                    "humidity": 64
                  },
                  "wind": {
                    "speed": 3.62
                  },
                  "dt": 1661870592
                }
                """;
        doReturn(cities).when(cityRepository).findAllByImportingDataTrue();
        doReturn(response).when(restTemplate).getForObject(anyString(), eq(String.class));

        weatherDataImporter.defaultImport();

        verify(logger, times(cities.size() + 2)).info(anyString());
        verify(cityRepository, times(1)).findAllByImportingDataTrue();
        verify(weatherDataRepository, times(1)).save(any(WeatherData.class));
    }

    @Test
    void testDefaultImport_WithCitiesToFetch_WithRequiredData() {
        // Don't mock the ObjectMapper for this test, otherwise it returns null values leading to throwing an exception
        weatherDataImporter = new WeatherDataImporter(weatherDataRepository, cityRepository, logger, restTemplate,
                                                      new ObjectMapper());

        City city1 = new City("Tallinn", 59.4372155, 24.7453688);
        List<City> cities = List.of(city1);
        doReturn(cities).when(cityRepository).findAllByImportingDataTrue();
        doReturn("{ \"dt\": 1661870592 } ").when(restTemplate).getForObject(anyString(), eq(String.class));

        weatherDataImporter.defaultImport();

        verify(logger, times(cities.size() + 2)).info(anyString());
        verify(cityRepository, times(1)).findAllByImportingDataTrue();
        verify(weatherDataRepository, times(1)).save(any(WeatherData.class));
    }
}