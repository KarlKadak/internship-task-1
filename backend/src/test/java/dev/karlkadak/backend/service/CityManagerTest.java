package dev.karlkadak.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karlkadak.backend.entity.City;
import dev.karlkadak.backend.exception.CityManagementException;
import dev.karlkadak.backend.repository.CityRepository;
import dev.karlkadak.backend.repository.WeatherDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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

    @InjectMocks
    private CityManager cityManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cityManager = new CityManager(cityRepository, logger, restTemplate, objectMapper);

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

        try {
            doReturn(newCity).when(spyManager).retrieveCompleteCity(anyString());
            spyManager.enableImporting("tallinn");
        } catch (CityManagementException e) {
            throw new RuntimeException(e);
        }

        verify(cityRepository, times(1)).save(newCity);
        verify(logger, times(1)).info(anyString());
    }

    @Test
    void testEnableImporting_ExistingNotTrackedCity() {
        City newCity = new City("Tallinn", 59.4372155, 24.7453688);
        newCity.setImportingData(false);
        CityManager spyManager = spy(cityManager);

        try {
            doReturn(newCity).when(spyManager).retrieveCompleteCity(anyString());
            doReturn(true).when(cityRepository).exists(Example.of(newCity));
            spyManager.enableImporting("tallinn");
        } catch (CityManagementException e) {
            throw new RuntimeException(e);
        }

        verify(cityRepository, times(1)).save(newCity);
        verify(logger, times(1)).info(anyString());
    }

    @Test
    void testEnableImporting_ExistingTrackedCity() {
        City newCity = new City("Tallinn", 59.4372155, 24.7453688);
        CityManager spyManager = spy(cityManager);
        try {
            doReturn(newCity).when(spyManager).retrieveCompleteCity(anyString());
        } catch (CityManagementException e) {
            throw new RuntimeException(e);
        }
        doReturn(true).when(cityRepository).exists(Example.of(newCity));

        assertThrows(CityManagementException.class, () -> spyManager.enableImporting("tallinn"));

        verify(cityRepository, times(0)).save(newCity);
        verify(logger, times(0)).info(anyString());
    }

    @Test
    void testDisableImporting_EnabledCity() {
        City newCity = new City("Tallinn", 59.4372155, 24.7453688);
        doReturn(Optional.of(newCity)).when(cityRepository).findById(any());

        try {
            cityManager.disableImporting(1);
        } catch (CityManagementException e) {
            throw new RuntimeException(e);
        }

        assertFalse(newCity.isImportingData());
        verify(cityRepository, times(1)).save(any());
        verify(logger, times(1)).info(anyString());
    }

    @Test
    void testDisableImporting_DisabledCity() {
        City newCity = new City("Tallinn", 59.4372155, 24.7453688);
        newCity.setImportingData(false);
        doReturn(Optional.of(newCity)).when(cityRepository).findById(any());

        assertThrows(CityManagementException.class, () -> cityManager.disableImporting(1));
        verify(cityRepository, times(0)).save(any());
        verify(logger, times(0)).info(anyString());
    }

    @Test
    void testDisableImporting_NotExistingCity() {
        doReturn(Optional.empty()).when(cityRepository).findById(any());

        assertThrows(CityManagementException.class, () -> cityManager.disableImporting(1));
    }

    @Test
    void testRetrieveCompleteCity_NewCity() {
        // Don't mock the ObjectMapper for this test, otherwise it returns null values leading to throwing an exception
        cityManager = new CityManager(cityRepository, logger, restTemplate, new ObjectMapper());

        City returnedCity;
        when(cityRepository.findByName(anyString())).thenReturn(Optional.empty());

        try {
            returnedCity = cityManager.retrieveCompleteCity("tallinn");
        } catch (CityManagementException e) {
            throw new RuntimeException(e);
        }

        assertTrue(
                returnedCity.getName().equals("Tallinn") && returnedCity.getCoordinatePair().getLatitude() == 59.4372155
                && returnedCity.getCoordinatePair().getLongitude() == 24.7453688);
    }

    @Test
    void testRetrieveCompleteCity_ExistingCity() {
        // Don't mock the ObjectMapper for this test, otherwise it returns null values leading to throwing an exception
        cityManager = new CityManager(cityRepository, logger, restTemplate, new ObjectMapper());

        City existingCity = new City("Tallinn", 59.4372155, 24.7453688);
        City returnedCity;
        when(cityRepository.findByName(anyString())).thenReturn(Optional.of(existingCity));

        try {
            returnedCity = cityManager.retrieveCompleteCity("tallinn");
        } catch (CityManagementException e) {
            throw new RuntimeException(e);
        }

        assertEquals(existingCity, returnedCity);
    }

    @Test
    void testRetrieveCompleteCity_MalformattedName() {
        assertThrows(CityManagementException.class, () -> cityManager.retrieveCompleteCity(""));
        assertThrows(CityManagementException.class, () -> cityManager.retrieveCompleteCity("111"));
        assertThrows(CityManagementException.class, () -> cityManager.retrieveCompleteCity("asd123"));
        assertThrows(CityManagementException.class, () -> cityManager.retrieveCompleteCity("   "));
    }

    @Test
    void testRetrieveCompleteCity_FailedRequest() {
        // Don't need to prepare anything, since mocks return null by default
        assertThrows(CityManagementException.class, () -> cityManager.retrieveCompleteCity("tallinn"));
        verify(logger, times(1)).warning(anyString());
    }

    @Test
    void testRetrieveCompleteCity_MalformattedResponse() {
        // Don't mock the ObjectMapper for this test, otherwise it returns null values leading to throwing an exception
        cityManager = new CityManager(cityRepository, logger, restTemplate, new ObjectMapper());

        // For JSON formatting rule infringement (missing trailing bracket)
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("""
                                                                                          [
                                                                                            {
                                                                                              "": "Tallinn",
                                                                                              "": 59.4372155,
                                                                                              "": 24.7453688
                                                                                            }
                                                                                          """);

        assertThrows(CityManagementException.class, () -> cityManager.retrieveCompleteCity("tallinn"));
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


        assertThrows(CityManagementException.class, () -> cityManager.retrieveCompleteCity("tallinn"));
        verify(logger, times(2)).warning(anyString());

        // For other format irregularity (response is not an array or is an empty one)
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("""
                                                                                          {
                                                                                            "name": "Tallinn",
                                                                                            "lat": 59.4372155,
                                                                                            "lon": 24.7453688
                                                                                          }""");

        assertThrows(CityManagementException.class, () -> cityManager.retrieveCompleteCity("tallinn"));
        verify(logger, times(3)).warning(anyString());

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("[]");

        assertThrows(CityManagementException.class, () -> cityManager.retrieveCompleteCity("tallinn"));
        verify(logger, times(3)).warning(anyString());
    }
}