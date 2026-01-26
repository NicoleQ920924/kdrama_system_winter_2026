import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/ai';

export const generateAiResponse = (prompt) => {
  return axios.post(`${API_BASE}/generate`, { prompt });
};

/**
 * Search for dramas based on user criteria
 * Returns top 3 results with titles and summaries
 */
export const searchDramasByPrompt = (prompt) => {
  return axios.post(`${API_BASE}/drama/search`, { prompt });
};
