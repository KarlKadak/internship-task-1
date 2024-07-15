package dev.karlkadak.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karlkadak.backend.entity.City;
import dev.karlkadak.backend.exception.*;
import dev.karlkadak.backend.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CityManagerTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private Logger logger;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WeatherDataImporter weatherDataImporter;

    @InjectMocks
    private CityManager cityManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cityManager = new CityManager(cityRepository, logger, restTemplate, objectMapper, weatherDataImporter);

        // Sample data to be returned from the geocoding API
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("""
                                                                                          [
                                                                                            {
                                                                                              "name": "Tallinn",
                                                                                              "lat": 59.4372155,
                                                                                              "lon": 24.7453688
                                                                                            }
                                                                                          ]""");
    }

    @Test
    void testEnableImporting_NewCity() {
        City newCity = new City("Tallinn", 59.4372155, 24.7453688);
        CityManager spyManager = spy(cityManager);
        doReturn(newCity).when(spyManager).retrieveCompleteCity(anyString());

        spyManager.enableImporting("tallinn");

        verify(cityRepository, times(1)).save(newCity);
        verify(weatherDataImporter, times(1)).fetchAndSave(newCity);
        verify(logger, times(1)).info(anyString());
    }

    @Test
    void testEnableImporting_ExistingNotTrackedCity() {
        City newCity = new City("Tallinn", 59.4372155, 24.7453688);
        newCity.setImportingData(false);
        CityManager spyManager = spy(cityManager);
        doReturn(newCity).when(spyManager).retrieveCompleteCity(anyString());
        doReturn(true).when(cityRepository).exists(Example.of(newCity));

        spyManager.enableImporting("tallinn");

        verify(cityRepository, times(1)).save(newCity);
        verify(weatherDataImporter, times(1)).fetchAndSave(newCity);
        verify(logger, times(1)).info(anyString());
    }

    @Test
    void testEnableImporting_ExistingTrackedCity() {
        City newCity = new City("Tallinn", 59.4372155, 24.7453688);
        CityManager spyManager = spy(cityManager);
        doReturn(newCity).when(spyManager).retrieveCompleteCity(anyString());
        doReturn(true).when(cityRepository).exists(Example.of(newCity));

        assertThrows(CityAlreadyBeingTrackedException.class, () -> spyManager.enableImporting("tallinn"));

        verify(cityRepository, times(0)).save(newCity);
        verify(weatherDataImporter, times(0)).fetchAndSave(newCity);
        verify(logger, times(0)).info(anyString());
    }

    @Test
    void testDisableImporting_ExistingTrackedCity() {
        City newCity = new City("Tallinn", 59.4372155, 24.7453688);
        doReturn(Optional.of(newCity)).when(cityRepository).findById(any());

        cityManager.disableImporting(1);

        assertFalse(newCity.isImportingData());
        verify(cityRepository, times(1)).save(any());
        verify(logger, times(1)).info(anyString());
    }

    @Test
    void testDisableImporting_ExistingNotTrackedCity() {
        City newCity = new City("Tallinn", 59.4372155, 24.7453688);
        newCity.setImportingData(false);
        doReturn(Optional.of(newCity)).when(cityRepository).findById(any());

        assertThrows(CityAlreadyNotBeingTrackedException.class, () -> cityManager.disableImporting(1));

        verify(cityRepository, times(0)).save(any());
        verify(logger, times(0)).info(anyString());
    }

    @Test
    void testDisableImporting_NotExistingCity() {
        doReturn(Optional.empty()).when(cityRepository).findById(any());

        assertThrows(CityNotFoundException.class, () -> cityManager.disableImporting(1));
    }

    @Test
    void testRetrieveCompleteCity_NewCity() {
        // Don't mock the ObjectMapper for this test, otherwise it returns null values leading to throwing an exception
        cityManager = new CityManager(cityRepository, logger, restTemplate, new ObjectMapper(), weatherDataImporter);

        City returnedCity;
        when(cityRepository.findByName(anyString())).thenReturn(Optional.empty());

        returnedCity = cityManager.retrieveCompleteCity("tallinn");

        assertTrue(
                returnedCity.getName().equals("Tallinn") && returnedCity.getCoordinatePair().getLatitude() == 59.4372155
                && returnedCity.getCoordinatePair().getLongitude() == 24.7453688);
    }

    @Test
    void testRetrieveCompleteCity_ExistingCity() {
        // Don't mock the ObjectMapper for this test, otherwise it returns null values leading to throwing an exception
        cityManager = new CityManager(cityRepository, logger, restTemplate, new ObjectMapper(), weatherDataImporter);

        City existingCity = new City("Tallinn", 59.4372155, 24.7453688);
        City returnedCity;
        when(cityRepository.findByName(anyString())).thenReturn(Optional.of(existingCity));

        returnedCity = cityManager.retrieveCompleteCity("tallinn");

        assertEquals(existingCity, returnedCity);
    }

    @Test
    void testRetrieveCompleteCity_MalformedName() {
        assertThrows(MalformedCityNameException.class, () -> cityManager.retrieveCompleteCity(""));
        assertThrows(MalformedCityNameException.class, () -> cityManager.retrieveCompleteCity("111"));
        assertThrows(MalformedCityNameException.class, () -> cityManager.retrieveCompleteCity("asd123"));
        assertThrows(MalformedCityNameException.class, () -> cityManager.retrieveCompleteCity("   "));
    }

    @Test
    void testRetrieveCompleteCity_FailedRequest() {
        // Don't need to prepare anything, since mocks return null by default

        assertThrows(FailedCityDataImportException.class, () -> cityManager.retrieveCompleteCity("tallinn"));

        verify(logger, times(1)).warning(anyString());
    }

    @Test
    void testRetrieveCompleteCity_MalformedResponse() {
        // Don't mock the ObjectMapper for this test, otherwise it returns null values leading to throwing an exception
        cityManager = new CityManager(cityRepository, logger, restTemplate, new ObjectMapper(), weatherDataImporter);

        // For JSON formatting rule infringement (missing trailing bracket)

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("""
                                                                                          [
                                                                                            {
                                                                                              "": "Tallinn",
                                                                                              "": 59.4372155,
                                                                                              "": 24.7453688
                                                                                            }
                                                                                          """);

        assertThrows(FailedCityDataImportException.class, () -> cityManager.retrieveCompleteCity("tallinn"));

        verify(logger, times(1)).warning(anyString());

        // For contextual format irregularity

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("""
                                                                                          [
                                                                                            {
                                                                                              "": "Tallinn",
                                                                                              "": 59.4372155,
                                                                                              "": 24.7453688
                                                                                            }
                                                                                          ]""");


        assertThrows(FailedCityDataImportException.class, () -> cityManager.retrieveCompleteCity("tallinn"));

        verify(logger, times(2)).warning(anyString());

        // For other format irregularity (response is not an array)

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("""
                                                                                          {
                                                                                            "name": "Tallinn",
                                                                                            "lat": 59.4372155,
                                                                                            "lon": 24.7453688
                                                                                          }""");

        assertThrows(FailedCityDataImportException.class, () -> cityManager.retrieveCompleteCity("tallinn"));

        verify(logger, times(3)).warning(anyString());
    }

    @Test
    void testRetrieveCompleteCity_NotExistingCity() {
        // Don't mock the ObjectMapper for this test, otherwise it returns null values leading to throwing an exception
        cityManager = new CityManager(cityRepository, logger, restTemplate, new ObjectMapper(), weatherDataImporter);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("[]");

        assertThrows(CityNotFoundException.class, () -> cityManager.retrieveCompleteCity("tallinn"));

        verify(logger, times(0)).warning(anyString());
    }
}