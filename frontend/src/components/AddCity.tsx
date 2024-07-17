import React, { useState, useEffect } from "react";
import { requestAddCity, CityResponse } from "../services/api";
import axios from "axios";
import { ReactComponent as IconGithub } from "bootstrap-icons/icons/github.svg";

interface AddCityProps {
  notifyRefresh: () => void;
}

const AddCity: React.FC<AddCityProps> = (props) => {
  // Prepare stateful values
  const [cityName, setCityName] = useState<string>("");
  const [newCity, setNewCity] = useState<CityResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Handles user's request for adding a city
  const handleAddCity = async () => {
    try {
      // Make an HTTP request
      const city = await requestAddCity({ name: cityName });
      setNewCity(city);
      // Clear the input field
      setCityName("");
      // Notify the parent component of new city being added
      props.notifyRefresh();
      setError(null);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        // Set the error message as the API error response message
        setError(error.response.data.message);
      } else {
        setError("API connection failure");
      }
      setNewCity(null);
    }
  };

  // Clear the value of newCity and error 3s after a change
  // Used for automatic clearing of the alerts
  useEffect(() => {
    if (newCity || error) {
      const timer = setTimeout(() => {
        setNewCity(null);
        setError(null);
      }, 3000);

      return () => clearTimeout(timer);
    }
  }, [newCity, error]);

  return (
    <div className="mt-2">
      {error && <div className="alert alert-warning">{error}</div>}
      {newCity && (
        <div className="alert alert-primary">Added city: {newCity.name}</div>
      )}
      {/* onKeyDown handles enter press when the input field is in focus */}
      <input
        type="text"
        value={cityName}
        onChange={(e) => setCityName(e.target.value)}
        onKeyDown={(event: React.KeyboardEvent<HTMLInputElement>) => {
          if (event.key === "Enter") {
            handleAddCity();
          }
        }}
        placeholder="Enter city name"
        className="input-group mb-2"
      />
      <div className="d-flex justify-content-between align-items-center">
        {/* Button for adding city */}
        <button className="btn btn-primary" onClick={handleAddCity}>
          Add
        </button>
        {/* Link to the GitHub repository */}
        <a
          href="https://github.com/KarlKadak/internship-task-1"
          target="_blank"
          rel="noreferrer"
        >
          <IconGithub
            style={{
              width: "2em",
              height: "2em",
            }}
          />
        </a>
      </div>
    </div>
  );
};

export default AddCity;
