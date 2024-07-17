import React, { useRef } from "react";
import CityList from "./CityList";
import AddCity from "./AddCity";
import "./SideBar.css";

const SideBar: React.FC = () => {
  // Used for updating the city list when new cities are successfully added with AddCity
  const cityListRef = useRef<{ fetchCities: () => void }>(null);

  // Call CityList's fetchCities function when new cities are successfully added with AddCity
  const handleAddCityAndFetchCities = () => {
    if (cityListRef.current) {
      cityListRef.current.fetchCities();
    }
  };

  return (
    <div className="d-flex flex-column flex-shrink-0 p-3 bg-light">
      <span className="text-dark fs-4">Weather app</span>
      <hr />
      <CityList ref={cityListRef} />
      <AddCity onAddCity={handleAddCityAndFetchCities} />
    </div>
  );
};

export default SideBar;
