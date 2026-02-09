// Refer to backend's MovieController.java for respective methods
import axios from 'axios';
import { userStore } from '@/store'

const API_BASE = 'http://localhost:8080/api/movies';

export const importMovie = (name) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.post(`${API_BASE}/import`, null, { params: { name }, headers });
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
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.put(`${API_BASE}/apiupdate/${id}`, {}, { headers });
};

export const updateSelectedMovieViaAiAndForm = (id, movieToUpdate) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.put(`${API_BASE}/aiupdate/${id}`, movieToUpdate, { headers });
};

export const updateSelectedMovieViaForm = (id, movieToUpdate) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.put(`${API_BASE}/formupdate/${id}`, movieToUpdate, { headers });
};

export const deleteSelectedMovie = (id) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.delete(`${API_BASE}/delete/${id}`, { headers });
};
