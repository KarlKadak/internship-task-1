import React from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import SideBar from "./components/SideBar";

const App: React.FC = () => {
  return (
    <div className="d-flex">
      <SideBar />
      <div className="flex-grow-1 bg-dark text-white">
        <span>Weather data area</span>
      </div>
    </div>
  );
};

export default App;
