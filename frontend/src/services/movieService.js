// Refer to backend's MovieController.java for respective methods
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/movies';

export const importMovie = (name) => {
  return axios.post(`${API_BASE}/import`, null, { params: { name } });
};

export const findMovies = () => {
  return axios.get(`${API_BASE}/findAll`);
};

export const findSelectedMovieById = (id) => {
  return axios.get(`${API_BASE}/${id}`);
};

export const findSelectedMovieByChineseName = (chineseName) => {
  return axios.get(`${API_BASE}/chineseName`, {
    params: { chineseName }
  });
};

export const updateSelectedMovieViaApi = (id) => {
  return axios.put(`${API_BASE}/apiupdate/${id}`, {});
};

export const updateSelectedMovieViaAiAndForm = (id, movieToUpdate) => {
  return axios.put(`${API_BASE}/aiupdate/${id}`, movieToUpdate);
};

export const updateSelectedMovieViaForm = (id, movieToUpdate) => {
  return axios.put(`${API_BASE}/formupdate/${id}`, movieToUpdate);
};

export const deleteSelectedMovie = (id) => {
  return axios.delete(`${API_BASE}/delete/${id}`);
};
