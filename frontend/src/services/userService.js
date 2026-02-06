// User service for managing user authentication and profiles
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/users';

// Create a new user
export const createUser = (username, displayName, role = 'USER') => {
  return axios.post(`${API_BASE}/create`, null, {
    params: { username, displayName, role }
  });
};

// Get user by ID
export const getUserById = (userId) => {
  return axios.get(`${API_BASE}/${userId}`);
};

// List all users
export const listAllUsers = () => {
  return axios.get(`${API_BASE}/all`);
};

// Login user (attempts to authenticate by username)
export const loginUser = (username) => {
  return axios.get(`${API_BASE}/username/${username}`);
};
