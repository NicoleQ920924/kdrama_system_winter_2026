import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/ai';

export const generateAiResponse = (prompt) => {
  return axios.post(`${API_BASE}/generate`, { prompt });
};

