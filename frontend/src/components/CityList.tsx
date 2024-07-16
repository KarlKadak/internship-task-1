import React, {
  useEffect,
  useState,
  useImperativeHandle,
  forwardRef,
} from "react";
import { getAllCities, CityResponse } from "../services/api";
import axios from "axios";

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

  // Execute fetchCities when component mounts
  useEffect(() => {
    fetchCities();
  }, []);

  if (loading) {
    return <p>Loading...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <div>
      <h2 className="text-center text-info">City list</h2>
      <ul className="list-unstyled">
        {/* Populate the list with city objects */}
        {cities.map((city) => (
          <li key={city.id}>
            ID: {city.id} - {city.name}
          </li>
        ))}
      </ul>
    </div>
  );
});

export default CityList;
