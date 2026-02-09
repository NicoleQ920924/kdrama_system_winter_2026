// Refer to backend's ActorController.java for respective methods
import axios from 'axios';
import { userStore } from '@/store'

const API_BASE = 'http://localhost:8080/api/actors';

export const importActor = (name) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.post(`${API_BASE}/import`, null, { params: { name }, headers });
};

export const findActors = (displayNameMode) => {
  return axios.get(`${API_BASE}/findAll`, {
    params: { displayNameMode }
  });
};

export const findSelectedActorById = (id, displayNameMode) => {
  return axios.get(`${API_BASE}/${id}`, {
    params: { displayNameMode }
  });
};

export const findSelectedActorByChineseName = (chineseName) => {
  return axios.get(`${API_BASE}/chineseName`, {
    params: { chineseName }
  });
};

export const updateSelectedActorViaApi = (id, includesExistingWork) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.put(`${API_BASE}/apiupdate/${id}`, null, {
    params: { includesExistingWork },
    headers
  });
};

export const updateSelectedActorViaAiAndForm = (id, actorToUpdate) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.put(`${API_BASE}/aiupdate/${id}`, actorToUpdate, { headers });
};

export const updateSelectedActorViaForm = (id, actorToUpdate) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.put(`${API_BASE}/formupdate/${id}`, actorToUpdate, { headers });
};

export const deleteSelectedActor = (id) => {
  const headers = {}
  if (userStore && userStore.userRole && userStore.userRole.value) {
    headers['X-User-Role'] = userStore.userRole.value
  }
  return axios.delete(`${API_BASE}/delete/${id}`, { headers });
};
