// Refer to backend's MovieController.java for respective methods
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/movies';

export const importMovie = (name) => {
  return axios.post(`${API_BASE}/import`, null, { params: { name } });
};

export const findMovies = () => {
  return axios.get(`${API_BASE}/findAll`);
};

export const findSelectedMovie = (id) => {
  return axios.get(`${API_BASE}/${id}`);
};

export const updateSelectedMovieViaApi = (id) => {
  return axios.put(`${API_BASE}/apiupdate/${id}`, {});
};

export const updateSelectedMovieAllInfo = (id, updatedMovie) => {
  return axios.put(`${API_BASE}/allupdate/${id}`, updatedMovie);
};

export const deleteSelectedMovie = (id) => {
  return axios.delete(`${API_BASE}/delete/${id}`);
};
