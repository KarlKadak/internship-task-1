import React, { useState, useEffect } from "react";
import { addCity, CityResponse } from "../services/api";
import axios from "axios";

// Used for refreshing the city list when new cities are added using this component
interface AddCityProps {
  onAddCity: () => void;
}

const AddCity: React.FC<AddCityProps> = ({ onAddCity }) => {
  // Prepare stateful values
  const [cityName, setCityName] = useState<string>("");
  const [newCity, setNewCity] = useState<CityResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Handles user's request for adding a city
  const handleAddCity = async () => {
    try {
      // Make an HTTP request
      const city = await addCity({ name: cityName });
      setNewCity(city);
      // Clear the input field
      setCityName("");
      // Notify the parent component of new city being added
      onAddCity();
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        // Set the error message as the API error response message
        setError(error.response.data.message);
      } else {
        setError("API connection failure");
      }
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
    <div>
      <h2 className="text-center text-muted">Add city</h2>
      <div className="d-flex">
        <input
          type="text"
          value={cityName}
          onChange={(e) => setCityName(e.target.value)}
          placeholder="Enter city name"
        />
        <button className="btn btn-primary" onClick={handleAddCity}>
          Add City
        </button>
      </div>
      {newCity && (
        <div className="alert alert-primary">Added city: {newCity.name}</div>
      )}
      {error && <div className="alert alert-warning">{error}</div>}
    </div>
  );
};

export default AddCity;
