# Task description

Build the specified backend and frontend, save the code to a repository and write a README file with information on how to test the code.

## Backend

Use some public weather API (for example [OpenWeather](https://openweathermap.org/api)) and on top of that build an API in Java / Spring Boot with the following API functionalities:

- Saving a city's name to a database. Every 15 minutes, that city's weather data will be pulled and the air temperature, wind speed, humidity and timestamp will be saved to a database.
- Querying a city's weather data.
- Deleting a city's name, so that they don't poll for information anymore.

## Frontend

The frontend should be a screen through which these endpoints can be used (enter new cities, view saved data, and delete cities). Functionality is important. Frontend should be written in React.
