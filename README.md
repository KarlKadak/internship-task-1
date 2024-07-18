# Internship task

This repository solves an internship application sample task. The task description can be found in [task.md](https://github.com/KarlKadak/internship-task-1/blob/main/task.md).

## Repository structure

The repository's root folder holds specification files and two folders: `backend` and `frontend`. These folders contain the backend and frontend code, respectively.

## Extensions

The code also has some more advanced functionalities which have not been specified in the [task.md](https://github.com/KarlKadak/internship-task-1/blob/main/task.md). They are:

- when querying for weather data, the backend API also returns a link to an image representing the weather condition
- when querying for city data, the backend API also returns a link to an image of the flag of the country where the queried city resides
- the frontend app shows the image representing the weather condition when viewing a city's weather data
- the frontend app shows the flags of the countries where cities reside in the city list and when viewing a city's weather data
- deployed the production build of the frontend app to [GitHub pages](https://karlkadak.github.io/internship-task-1/) (running the backend locally is still required)
- generated and deployed Javadoc documentation of the backend code to [GitHub pages](https://karlkadak.github.io/internship-task-1/javadoc)
- generated and deployed a code coverage report of the tests for the backend code to [GitHub pages](https://karlkadak.github.io/internship-task-1/coverage)

## Guide for testing functionality

### Backend

#### Setup

Before running the backend code, some prearations must be made.

The program requires three environment variables to function correctly:

- **JAVA_HOME** - path to the JDK installation directory
- **H2_DB_PATH** - path to the database to use for storing the application data, for simple testing `mem:/db` will suffice
- **OPENWEATHER_API_KEY** - authentication key for using OpenWeather's API, can be acquired [here](https://home.openweathermap.org/api_keys)

You can set the environment variables in your IDE or at the OS level. A basic guide for setting environment variables can be found [here](https://eclipse.dev/openj9/docs/env_var/).

#### Running the application

You can run the backend application by cloning the repository, navigating to the `backend` directory and running the following command:

- on Mac / Linux: `./mvnw spring-boot:run`
- on Windows: `./mvnw.cmd spring-boot:run`

#### Testing endpoints

The previous command opens a web server on port `8080`. You can test the different endpoints by using [Postman](https://chromewebstore.google.com/detail/fhbjgbiflinjbdggehcddcbncdddomop) on Chrome, [RESTer](https://addons.mozilla.org/en-US/firefox/addon/rester/) on Firefox, or any other similar tool.

The REST endpoints opened by the application are the following:

1. `GET /cities`

   **Description**: Retrieves information about all cities for which data collection is enabled.

   **Steps**:

   - Send a `GET` request to http://localhost:8080/v1/cities.

   **Expected Response**:

   - HTTP Status Code: **200 OK**
   - Response Body: A JSON array containing CityResponse objects representing the cities.

2. `POST /cities`

   **Description**: Adds or enables weather data tracking for a city.

   **Steps**:

   - Send a `POST` request to http://localhost:8080/v1/cities.
   - Include a JSON body with a city name, e.g., `{ "name" : "New York" }`.

   **Expected Response**:

   - HTTP Status Code: **201 Created**
   - Response Body: A JSON object representing the newly added city in CityResponse format.

3. `GET /cities/{id}`

   **Description**: Retrieves information about a single city by its ID.

   **Steps**:

   - Replace {id} with an existing city ID in the URL.
   - Send a `GET` request to http://localhost:8080/v1/cities/{id}.

   **Expected Response**:

   - HTTP Status Code: **200 OK**
   - Response Body: A JSON object representing the city in CityResponse format.

4. `DELETE /cities/{id}`

   **Description**: Disables weather data tracking for a city.

   **Steps**:

   - Replace {id} with an existing city ID in the URL.
   - Send a `DELETE` request to http://localhost:8080/v1/cities/{id}.

   **Expected Response**:

   - HTTP Status Code: **204 No Content**

5. `GET /cities/{id}/weather`

   **Description**: Retrieves the most recently saved weather data entry for a city.

   **Steps**:

   - Replace {id} with an existing city ID in the URL.
   - Send a `GET` request to http://localhost:8080/v1/cities/{id}/weather.

   **Expected Response**:

   - HTTP Status Code: **200 OK**
   - Response Body: A JSON object representing the weather data in WeatherResponse format.

### Frontend

Testing the frontend is easy, just head to [this repository's GitHub pages deployment URL](https://karlkadak.github.io/internship-task-1/).

If you want to run the app locally, you can do so by following these steps:

- ensure you have [Node.js](https://nodejs.org) installed
- clone the repository
- navigate to the `frontend` directory
- run `npm install` to download the prerequisites
- run `npm start` to start serving the application on port `3000`
- open http://localhost:3000 on your browser

## Licensing

The code in this repository is licensed under the MIT License. Please see [LICENSE](https://github.com/KarlKadak/internship-task-1/blob/main/LICENSE) for details.
