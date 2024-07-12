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
        when(cityRepository.findAllByGatherDataTrue()).thenReturn(Collections.emptyList());

        weatherDataImporter.defaultImport();

        verify(cityRepository, times(1)).findAllByGatherDataTrue();
        verifyNoInteractions(logger);
        verifyNoInteractions(weatherDataRepository);
    }

    @Test
    void testDefaultImport_LogsError()
            throws JsonProcessingException {
        List<City> cities = List.of(new City("City1", 1.0, 1.0));
        when(cityRepository.findAllByGatherDataTrue()).thenReturn(cities);
        Map<String, Object> responseMap = new HashMap<>();

        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(responseMap);

        weatherDataImporter.defaultImport();

        verify(logger, times(1)).warning(anyString());
        verifyNoInteractions(weatherDataRepository);
    }

    @Test
    void testDefaultImport_WithCitiesToFetch() {

        City city1 = new City("City1", 10.08, 15.0015);
        City city2 = new City("City2", -70.02, -25.00005);
        List<City> cities = Arrays.asList(city1, city2);
        when(cityRepository.findAllByGatherDataTrue()).thenReturn(cities);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("""
                                                                                          {
                                                                                            "main": {
                                                                                              "temp": 29.48,
                                                                                              "humidity": 64
                                                                                            },
                                                                                            "wind": {
                                                                                              "speed": 3.62
                                                                                            },
                                                                                            "dt": 1661870592
                                                                                          }
                                                                                          """);

        when(weatherDataRepository.save(any(WeatherData.class))).thenReturn(null);

        // Don't mock the ObjectMapper for this test, otherwise it returns null values leading to throwing an exception
        weatherDataImporter = new WeatherDataImporter(weatherDataRepository, cityRepository, logger, restTemplate,
                                                      new ObjectMapper());

        weatherDataImporter.defaultImport();

        verify(cityRepository, times(1)).findAllByGatherDataTrue();
        verify(weatherDataRepository, times(2)).save(any(WeatherData.class));
    }
}