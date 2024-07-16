import axios from 'axios';

const API_URL = 'http://localhost:8080/v1/cities';

export interface CityResponse {
    id: number;
    name: string;
    flagHref?: string;
}

export interface AddCityRequest {
    name: string;
}

export interface WeatherResponse {
    airTemp?: number;
    windSpeed?: number;
    humidity?: number;
    iconHref?: string;
}

export const getAllCities = async (): Promise<CityResponse[]> => {
    const response = await axios.get<CityResponse[]>(API_URL);
    return response.data;
};

export const addCity = async (city: AddCityRequest): Promise<CityResponse> => {
    const response = await axios.post<CityResponse>(API_URL, city);
    return response.data;
};

export const getCityById = async (id: number): Promise<CityResponse> => {
    const response = await axios.get<CityResponse>(`${API_URL}/${id}`);
    return response.data;
};

export const deleteCity = async (id: number): Promise<void> => {
    await axios.delete(`${API_URL}/${id}`);
};

export const getCityWeather = async (id: number): Promise<WeatherResponse> => {
    const response = await axios.get<WeatherResponse>(`${API_URL}/${id}/weather`);
    return response.data;
};
