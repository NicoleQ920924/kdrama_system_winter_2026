// Watchlist service for managing user watches (dramas and movies)
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/watchlist';

// Add movie to watchlist
export const addMovieToWatchlist = (userId, movieId) => {
  return axios.post(`${API_BASE}/movie/add`, null, {
    params: { userId, movieId },
    headers: {
      'X-User-Role': 'USER'
    }
  });
};

// Remove movie from watchlist
export const removeMovieFromWatchlist = (userId, movieId) => {
  return axios.post(`${API_BASE}/movie/remove`, null, {
    params: { userId, movieId },
    headers: {
      'X-User-Role': 'USER'
    }
  });
};

// Add drama to watchlist
export const addDramaToWatchlist = (userId, dramaId) => {
  return axios.post(`${API_BASE}/drama/add`, null, {
    params: { userId, dramaId },
    headers: {
      'X-User-Role': 'USER'
    }
  });
};

// Remove drama from watchlist
export const removeDramaFromWatchlist = (userId, dramaId) => {
  return axios.post(`${API_BASE}/drama/remove`, null, {
    params: { userId, dramaId },
    headers: {
      'X-User-Role': 'USER'
    }
  });
};
