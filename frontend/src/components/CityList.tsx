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

  // Construct a message indicating some irregularity
  const constructMessage = (message: string | null) => {
    return (
      <div className="mb-auto">
        <p>{message}</p>
      </div>
    );
  };

  const buildNameCell = (city: CityResponse) => {
    const innerElement = (
      <div style={{ marginLeft: "0.5em", marginRight: "0.5em" }}>
        {city.name}
      </div>
    );
    if (props.selectedCity === city.id)
      return <td className="name-cell active">{innerElement}</td>;
    return (
      <td className="name-cell" onClick={() => props.selectCity(city.id)}>
        {innerElement}
      </td>
    );
  };

  // Return messages for different situations
  if (loading) return constructMessage("Loading...");
  if (error) return constructMessage(error);
  if (cities.length === 0)
    return constructMessage("Add some cities to get started");

  return (
    <div className="mb-auto overflow-auto">
      <table>
        <tbody>
          {/* Populate the list with city objects */}
          {cities.map((city) => (
            <tr key={city.id}>
              {/* Add the flag of the city's country if href present in the API response */}
              <td className="" style={{ height: "100%" }}>
                {city.flagHref ? (
                  <div>
                    <img
                      src={city.flagHref}
                      style={{ height: "2em" }}
                      alt={`${city.name} flag`}
                      className="flag"
                    />
                  </div>
                ) : null}
              </td>
              {/* Add the city name */}
              {buildNameCell(city)}
              {/* Add a delete button */}
              <td className="delete-cell" onClick={() => deleteCity(city.id)}>
                <div>
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
