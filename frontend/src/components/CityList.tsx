import React, {
  useEffect,
  useState,
  useImperativeHandle,
  forwardRef,
} from "react";
import { getAllCities, deleteCity, CityResponse } from "../services/api";
import axios from "axios";
import { ReactComponent as IconXLg } from "bootstrap-icons/icons/x-lg.svg";
import "./CityList.css";

const CityList = forwardRef((props, ref) => {
  // Prepare stateful values
  const [cities, setCities] = useState<CityResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Expose the fetchCities method to the parent component
  useImperativeHandle(ref, () => ({
    fetchCities,
  }));

  // Fetches data about all of the cities
  const fetchCities = async () => {
    try {
      const cities = await getAllCities();
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

  // Deletes a single city with the given id
  const handleDeleteCity = async (id: number) => {
    try {
      await deleteCity(id);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        // Set the error message as the API error response message
        setError(error.response?.data.message);
      } else {
        setError("API connection failure");
      }
    }
    fetchCities();
  };

  // Execute fetchCities when component mounts
  useEffect(() => {
    fetchCities();
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
      <table className="list-unstyled">
        {/* Populate the list with city objects */}
        {cities.map((city) => (
          <tr>
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
            <td className="city-name-cell">
              <div className="city-button">{city.name}</div>
            </td>
            {/* Add a delete button */}
            <td>
              <div
                className="delete-button"
                onClick={() => handleDeleteCity(city.id)}
              >
                <IconXLg />
              </div>
            </td>
          </tr>
        ))}
      </table>
    </div>
  );
});

export default CityList;
