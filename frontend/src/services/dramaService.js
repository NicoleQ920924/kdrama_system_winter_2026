// Refer to backend's DramaController.java for respective methods
import axios from 'axios';
import { userStore } from '@/store'

const API_BASE = 'http://localhost:8080/api/dramas';

export const importDrama = (name) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.post(`${API_BASE}/import`, null, { params: { name }, headers });
};

export const findDramas = (displayNameMode) => {
  return axios.get(`${API_BASE}/findAll`, {
    params: { displayNameMode }
  });
};

export const findSelectedDramaById = (id, displayNameMode) => {
  return axios.get(`${API_BASE}/${id}`, {
    params: { displayNameMode }
  });
};

export const findSelectedDramaByChineseName = (chineseName, displayNameMode) => {
  return axios.get(`${API_BASE}/chineseName`, {
    params: { chineseName, displayNameMode }
  });
};

export const updateSelectedDramaViaApi = (id) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.put(`${API_BASE}/apiupdate/${id}`, {}, { headers });
};

export const updateSelectedDramaViaAiAndForm = (id, dramaToUpdate) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.put(`${API_BASE}/aiupdate/${id}`, dramaToUpdate, { headers });
};

export const updateSelectedDramaViaForm = (id, dramaToUpdate) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.put(`${API_BASE}/formupdate/${id}`, dramaToUpdate, { headers });
};

export const deleteSelectedDrama = (id) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.delete(`${API_BASE}/delete/${id}`, { headers });
};
