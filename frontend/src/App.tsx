import React, { useRef } from "react";
import CityList from "./components/CityList";
import AddCity from "./components/AddCity";

const App: React.FC = () => {
  // Used for updating the city list when new cities are successfully added with AddCity
  const cityListRef = useRef<{ fetchCities: () => void }>(null);

  // Call CityList's fetchCities function when new cities are successfully added with AddCity
  const handleAddCityAndFetchCities = () => {
    if (cityListRef.current) {
      cityListRef.current.fetchCities();
    }
  };

  return (
    <div className="d-flex flex-column align-items-center justify-content-center">
      <h1 className="text-primary">Weather app</h1>
      <AddCity onAddCity={handleAddCityAndFetchCities} />
      <CityList ref={cityListRef} />
    </div>
  );
};

export default App;
