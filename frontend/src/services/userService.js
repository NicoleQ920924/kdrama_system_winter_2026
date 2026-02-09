// User service for managing user authentication and profiles
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/users';

// Create a new user with password
export const createUser = (username, displayName, password, role = 'USER') => {
  return axios.post(`${API_BASE}/create`, null, {
    params: { 
      username, 
      displayName: displayName || username,
      password,
      role 
    }
  });
};

// Login user
export const loginUser = (username, password) => {
  return axios.post(`${API_BASE}/login`, null, {
    params: { username, password }
  });
};

// Get user by ID
export const getUserById = (userId) => {
  return axios.get(`${API_BASE}/${userId}`);
};

// Get user by username
export const getUserByUsername = (username) => {
  return axios.get(`${API_BASE}/username/${username}`);
};

// List all users
export const listAllUsers = () => {
  return axios.get(`${API_BASE}/all`);
};
