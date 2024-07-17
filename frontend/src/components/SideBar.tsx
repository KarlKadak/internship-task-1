import React, { useRef, useState } from "react";
import CityList, { CityListRef } from "./CityList";
import AddCity from "./AddCity";
import "./SideBar.css";

const SideBar: React.FC = () => {
  // Create a ref to hold the city list
  const cityListRef = useRef<CityListRef>(null);

  const refreshCityList = () => {
    if (cityListRef.current) cityListRef.current?.refresh();
  };

  const [selectedCity, setSelectedCity] = useState<number | null>(null);

  return (
    <div className="d-flex flex-column flex-shrink-0 p-3 bg-light">
      <span className="text-dark fs-4">Weather app</span>
      <hr />
      <CityList ref={cityListRef} selectCity={setSelectedCity} />
      <AddCity notifyRefresh={refreshCityList} />
    </div>
  );
};

export default SideBar;
