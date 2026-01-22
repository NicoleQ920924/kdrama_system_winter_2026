// Refer to backend's ActorController.java for respective methods
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/actors';

export const importActor = (name) => {
  return axios.post(`${API_BASE}/import`, null, { params: { name } });
};

export const findActors = (displayNameMode) => {
  return axios.get(`${API_BASE}/findAll`, {
    params: { displayNameMode }
  });
};

export const findSelectedActor = (id, displayNameMode) => {
  return axios.get(`${API_BASE}/${id}`, {
    params: { displayNameMode }
  });
};

export const updateSelectedActorViaApi = (id, includesExistingWork) => {
  return axios.put(`${API_BASE}/apiupdate/${id}`, null, {
    params: { includesExistingWork }
  });
};

export const updateSelectedActorAllInfo = (id, updatedActor) => {
  return axios.put(`${API_BASE}/allupdate/${id}`, updatedActor);
};

export const deleteSelectedActor = (id) => {
  return axios.delete(`${API_BASE}/delete/${id}`);
};
