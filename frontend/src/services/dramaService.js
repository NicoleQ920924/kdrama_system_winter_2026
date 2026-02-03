// Refer to backend's DramaController.java for respective methods
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/dramas';

export const importDrama = (name) => {
  return axios.post(`${API_BASE}/import`, null, { params: { name } });
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
  return axios.put(`${API_BASE}/apiupdate/${id}`, {});
};

export const updateSelectedDramaViaAiAndForm = (id, dramaToUpdate) => {
  return axios.put(`${API_BASE}/aiupdate/${id}`, dramaToUpdate);
};

export const updateSelectedDramaViaForm = (id, dramaToUpdate) => {
  return axios.put(`${API_BASE}/formupdate/${id}`, dramaToUpdate);
};

export const deleteSelectedDrama = (id) => {
  return axios.delete(`${API_BASE}/delete/${id}`);
};
