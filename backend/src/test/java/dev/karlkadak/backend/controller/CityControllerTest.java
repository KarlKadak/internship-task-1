package dev.karlkadak.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.karlkadak.backend.dto.AddCityRequest;
import dev.karlkadak.backend.entity.City;
import dev.karlkadak.backend.entity.WeatherData;
import dev.karlkadak.backend.exception.*;
import dev.karlkadak.backend.repository.CityRepository;
import dev.karlkadak.backend.repository.WeatherDataRepository;
import dev.karlkadak.backend.service.CityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CityController.class)
@ContextConfiguration(classes = {CityController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc
class CityControllerTest {

    @MockBean
    private CityRepository cityRepository;

    @MockBean
    private WeatherDataRepository weatherDataRepository;

    @MockBean
    private CityManager cityManager;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${api.prefix}")
    private String apiPrefix;

    @Test
    void testAll_WithoutCities()
            throws Exception {
        doReturn(new ArrayList<City>()).when(cityRepository).findAll();

        mockMvc.perform(get(apiPrefix + "/cities").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void testAll_WithCities()
            throws Exception {
        List<City> cities = new ArrayList<>();
        cities.add(new City("Tallinn", 59.4372155, 24.7453688));
        doReturn(cities).when(cityRepository).findAll();

        mockMvc.perform(get(apiPrefix + "/cities").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void testEnable_Success()
            throws Exception {
        City city = new City("Tallinn", 59.4372155, 24.7453688);
        doReturn(city).when(cityManager).enableImporting(anyString());
        AddCityRequest request = new AddCityRequest();
        request.setName("tallinn");

        mockMvc.perform(post(apiPrefix + "/cities").contentType(MediaType.APPLICATION_JSON).content(
                objectMapper.writeValueAsString(request)).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isCreated());
    }

    @Test
    void testEnable_Failure()
            throws Exception {
        AddCityRequest request = new AddCityRequest();
        request.setName(null);

        mockMvc.perform(post(apiPrefix + "/cities").contentType(MediaType.APPLICATION_JSON).content(
                objectMapper.writeValueAsString(request)).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isBadRequest());

        request.setName("");

        mockMvc.perform(post(apiPrefix + "/cities").contentType(MediaType.APPLICATION_JSON).content(
                objectMapper.writeValueAsString(request)).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isBadRequest());

        request.setName("Tallinn");
        doThrow(CityAlreadyBeingTrackedException.class).when(cityManager).enableImporting(anyString());

        mockMvc.perform(post(apiPrefix + "/cities").contentType(MediaType.APPLICATION_JSON).content(
                objectMapper.writeValueAsString(request)).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isBadRequest());

        doThrow(FailedCityDataImportException.class).when(cityManager).enableImporting(anyString());

        mockMvc.perform(post(apiPrefix + "/cities").contentType(MediaType.APPLICATION_JSON).content(
                objectMapper.writeValueAsString(request)).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isFailedDependency());
    }

    @Test
    void testOne_Success()
            throws Exception {
        Long cityId = 1L;
        City city = new City("Tallinn", 59.4372155, 24.7453688);
        doReturn(Optional.of(city)).when(cityRepository).findById(any());

        mockMvc.perform(get(apiPrefix + "/cities/{id}", cityId).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isOk());
    }

    @Test
    void testOne_Failure()
            throws Exception {
        Long cityId = 1L;
        doReturn(Optional.empty()).when(cityRepository).findById(cityId);

        mockMvc.perform(get(apiPrefix + "/cities/{id}", cityId).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isNotFound());
    }

    @Test
    void testDisable_Success()
            throws Exception {
        Long cityId = 1L;
        City city = new City("Tallinn", 59.4372155, 24.7453688);
        doReturn(city).when(cityManager).disableImporting(anyLong());

        mockMvc.perform(delete(apiPrefix + "/cities/{id}", cityId).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isNoContent());
    }

    @Test
    void testDisable_Failure()
            throws Exception {
        Long cityId = 1L;
        doThrow(CityAlreadyNotBeingTrackedException.class).when(cityManager).disableImporting(anyLong());

        mockMvc.perform(delete(apiPrefix + "/cities/{id}", cityId).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isBadRequest());

        doThrow(CityNotFoundException.class).when(cityManager).disableImporting(anyLong());

        mockMvc.perform(delete(apiPrefix + "/cities/{id}", cityId).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isNotFound());
    }

    @Test
    void testWeather_Success()
            throws Exception {
        Long cityId = 1L;
        City city = new City("Tallinn", 59.4372155, 24.7453688);
        WeatherData weatherData = new WeatherData(city, 10000L, 10D, 5D, 60, "01d");
        doReturn(Optional.of(city)).when(cityRepository).findById(any());
        doReturn(Optional.of(weatherData)).when(weatherDataRepository).findTopByCity_IdOrderByTimestampDesc(anyLong());

        mockMvc.perform(get(apiPrefix + "/cities/{id}/weather", cityId).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isOk());

        weatherData = new WeatherData(city, 10000L, null, null, null, null);
        doReturn(Optional.of(weatherData)).when(weatherDataRepository).findTopByCity_IdOrderByTimestampDesc(anyLong());

        mockMvc.perform(get(apiPrefix + "/cities/{id}/weather", cityId).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isOk());
    }

    @Test
    void testWeather_Failure()
            throws Exception {
        Long cityId = 1L;
        City city = new City("Tallinn", 59.4372155, 24.7453688);

        mockMvc.perform(get(apiPrefix + "/cities/{id}/weather", cityId).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isNotFound());

        doReturn(Optional.of(city)).when(cityRepository).findById(any());

        mockMvc.perform(get(apiPrefix + "/cities/{id}/weather", cityId).accept(MediaType.APPLICATION_JSON)).andExpect(
                status().isNotFound());
    }
}