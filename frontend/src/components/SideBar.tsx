import React, { useRef } from "react";
import CityList, { CityListRef } from "./CityList";
import AddCity from "./AddCity";
import "./SideBar.css";

interface SideBarProps {
  selectedCity: number | null;
  selectCity: (id: number | null) => void;
}

const SideBar: React.FC<SideBarProps> = (props) => {
  // Create a ref to hold the city list
  const cityListRef = useRef<CityListRef>(null);

  const refreshCityList = () => {
    if (cityListRef.current) cityListRef.current?.refresh();
  };

  return (
    <div className="d-flex flex-column flex-shrink-0 p-3 bg-light">
      <span className="text-dark fs-4">Weather app</span>
      <hr />
      <CityList
        ref={cityListRef}
        selectCity={props.selectCity}
        selectedCity={props.selectedCity}
      />
      <AddCity notifyRefresh={refreshCityList} />
    </div>
  );
};

export default SideBar;
