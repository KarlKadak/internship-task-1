import React, { useState } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import "./styles/main.css";
import SideBar from "./components/SideBar";
import WeatherViewer from "./components/WeatherViewer";

const App: React.FC = () => {
  // Prepare stateful values
  const [selectedCity, setSelectedCity] = useState<number | null>(null);

  return (
    <div className="d-flex">
      <SideBar selectCity={setSelectedCity} selectedCity={selectedCity} />
      <WeatherViewer selectedCity={selectedCity} />
    </div>
  );
};

export default App;
