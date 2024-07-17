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
  const [city, setCity] = useState<CityResponse | null>(null);
  const [weatherData, setWeatherData] = useState<WeatherResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const updateData = async () => {
    if (!props.selectedCity) return;
    setLoading(true);
    try {
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

  useEffect(() => {
    if (props.selectedCity) {
      updateData();
    }
    setWeatherData(null);
  }, [props.selectedCity]);

  const renderWeatherViewer = () => {
    return (
      <div className="flex-grow-1 bg-dark text-white">
        <div className="w-100 h-100 d-flex justify-content-center align-items-center hei">
          <div>
            {loading && <span>Loading...</span>}
            {weatherData && city
              ? renderWeatherDataDiv()
              : loading || (
                  <span>
                    Select a city from the left to view its weather data
                  </span>
                )}
          </div>
        </div>
      </div>
    );
  };

  const renderWeatherDataDiv = () => {
    const measuredTime = weatherData
      ? new Date(weatherData.timestamp * 1000)
      : null;

    return (
      <>
        {city?.flagHref ? (
          <img
            src={city.flagHref}
            alt={`${city.name} flag`}
            className="ms-auto float-start"
          />
        ) : null}
        {weatherData?.iconHref ? (
          <img
            src={weatherData?.iconHref}
            alt={`${city?.name} weather condition`}
            className="ms-auto float-end"
          />
        ) : null}
        <h1>{city?.name}</h1>
        <br />
        <h3>
          Air temperature:{" "}
          {weatherData?.airTemp ? weatherData.airTemp + " °C" : "N/A"}
        </h3>
        <h3>
          Wind speed:{" "}
          {weatherData?.windSpeed ? weatherData.windSpeed + " m/s" : "N/A"}
        </h3>
        <h3>
          Humidity: {weatherData?.humidity ? weatherData.humidity + "%" : "N/A"}
        </h3>
        <h3>
          Measured at {measuredTime ? measuredTime.toLocaleTimeString() : "N/A"}
        </h3>
      </>
    );
  };

  return renderWeatherViewer();
};

export default WeatherViewer;
