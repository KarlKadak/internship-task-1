import React, {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useState,
} from "react";
import {
  CityResponse,
  requestAllCities,
  requestDeleteCity,
} from "../services/api";
import axios from "axios";
import { ReactComponent as IconXLg } from "bootstrap-icons/icons/x-lg.svg";
import "./CityList.css";

interface CityListProps {
  selectedCity: number | null;
  selectCity: (id: number | null) => void;
}

export interface CityListRef {
  refresh: () => void;
}

const CityList = forwardRef<CityListRef, CityListProps>((props, ref) => {
  // Prepare stateful values
  const [cities, setCities] = useState<CityResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Requests the list of cities and refreshes it
  const refresh = async () => {
    try {
      const cities = await requestAllCities();
      setCities(cities);
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

  // Expose the refresh function to the parent
  useImperativeHandle(ref, () => ({
    refresh,
  }));

  // Deletes a single city with the given id
  const deleteCity = async (id: number) => {
    try {
      await requestDeleteCity(id);
      if (props.selectedCity === id) props.selectCity(null);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        // Set the error message as the API error response message
        setError(error.response?.data.message);
      } else {
        setError("API connection failure");
      }
    }
    refresh();
  };

  // Execute fetchCities when component mounts
  useEffect(() => {
    refresh();
  }, []);

  if (loading) {
    return (
      <div className="mb-auto">
        <p>Loading...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mb-auto">
        <p>{error}</p>
      </div>
    );
  }

  if (cities.length === 0) {
    return (
      <div className="mb-auto">
        <p>Add some cities to get started!</p>
      </div>
    );
  }

  return (
    <div className="mb-auto">
      <span className="text-dark fs-5">City list</span>
      <table>
        <tbody>
          {/* Populate the list with city objects */}
          {cities.map((city) => (
            <tr key={city.id}>
              {/* Add the flag of the city's country if href present in the API response */}
              <td>
                {city.flagHref ? (
                  <div className="float-end">
                    <img
                      src={city.flagHref}
                      alt={`${city.name} flag`}
                      className="ms-auto"
                    />
                  </div>
                ) : null}
              </td>
              {/* Add the city name */}
              <td
                className={
                  props.selectedCity === city.id
                    ? "city-name-cell active-city"
                    : "city-name-cell"
                }
              >
                <div
                  className="h-100"
                  onClick={() => props.selectCity(city.id)}
                >
                  {city.name}
                </div>
              </td>
              {/* Add a delete button */}
              <td>
                <div
                  className="delete-button"
                  onClick={() => deleteCity(city.id)}
                >
                  <IconXLg />
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
});

export default CityList;
