import { useEffect, useState } from "react";
import {
  CityResponse,
  requestCityById,
  requestCityWeather,
  WeatherResponse,
} from "../services/api";
import axios from "axios";

interface WeatherViewerProps {
  selectedCity: number | null;
}

const WeatherViewer: React.FC<WeatherViewerProps> = (props) => {
  // Prepare stateful values
  const [city, setCity] = useState<CityResponse | null>(null);
  const [weatherData, setWeatherData] = useState<WeatherResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Requests and updates the city and weatherData values
  const updateData = async () => {
    if (!props.selectedCity) {
      setWeatherData(null);
      return;
    }
    // Reset error and loading status
    setError(null);
    setLoading(true);
    try {
      // Request the data
      const weatherData = await requestCityWeather(props.selectedCity);
      setWeatherData(weatherData);
      const city = await requestCityById(props.selectedCity);
      setCity(city);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        // Set the error message as the API error response message
        setError(error.response?.data.message);
      } else {
        setError("API connection failure");
      }
    } finally {
      // Disable loading state
      setLoading(false);
    }
  };

  // Execute updateData when selectedCity changes
  useEffect(() => {
    updateData();
    // eslint-disable-next-line
  }, [props.selectedCity]);

  // Render the contents of the WeatherViewer
  // Also sets loading or error messages
  const renderWeatherViewer = () => {
    return (
      <div className="flex-grow-1 bg-dark text-white">
        <div className="w-100 h-100 d-flex justify-content-center align-items-center">
          <div>
            {error}
            {loading && "Loading..."}
            {!error && !loading
              ? weatherData && city
                ? renderWeatherDataDiv()
                : loading ||
                  "Select a city from the left to view its weather data"
              : ""}
          </div>
        </div>
      </div>
    );
  };

  // Render the div containing weather data
  const renderWeatherDataDiv = () => {
    const measuredTime = weatherData
      ? new Date(weatherData.timestamp * 1000)
      : null;

    return (
      <div>
        {/* Add the coutry flag if server returns href to it */}
        {city?.flagHref ? (
          <img src={city.flagHref} className="flag" alt={`${city.name} flag`} />
        ) : null}
        {/* Add the weather condition icon if server returns href to it */}
        {weatherData?.iconHref ? (
          <img
            src={weatherData?.iconHref}
            alt={`${city?.name} weather condition`}
          />
        ) : null}
        <h1>{city?.name}</h1>
        <br />
        {/* Populate the rest of the div with weather data */}
        <span>
          Air temperature:{" "}
          {weatherData?.airTemp ? weatherData.airTemp + " °C" : "N/A"}
        </span>
        <br />
        <span>
          Wind speed:{" "}
          {weatherData?.windSpeed ? weatherData.windSpeed + " m/s" : "N/A"}
        </span>
        <br />
        <span>
          Humidity: {weatherData?.humidity ? weatherData.humidity + "%" : "N/A"}
        </span>
        <br />
        <span className="text-secondary">
          Measured at{" "}
          {measuredTime
            ? measuredTime.toLocaleTimeString().replaceAll(".", ":")
            : "N/A"}
        </span>
      </div>
    );
  };

  return renderWeatherViewer();
};

export default WeatherViewer;
