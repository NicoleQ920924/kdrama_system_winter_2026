// Global user state management
import { ref, computed } from 'vue';

// Reactive state
const currentUser = ref(null);
const isLoggedIn = ref(false);
const userRole = ref(null);

// Load user from localStorage if available
const loadUserFromStorage = () => {
  const stored = localStorage.getItem('currentUser');
  if (stored) {
    try {
      const user = JSON.parse(stored);
      currentUser.value = user;
      isLoggedIn.value = true;
      userRole.value = user.role;
      return true;
    } catch (e) {
      console.error('Failed to load user from storage:', e);
      clearUser();
      return false;
    }
  }
  return false;
};

// Set current user
export const setCurrentUser = (user) => {
  currentUser.value = user;
  isLoggedIn.value = true;
  userRole.value = user.role;
  localStorage.setItem('currentUser', JSON.stringify(user));
};

// Clear current user
export const clearUser = () => {
  currentUser.value = null;
  isLoggedIn.value = false;
  userRole.value = null;
  localStorage.removeItem('currentUser');
};

// Get current user
export const getCurrentUser = () => {
  return currentUser.value;
};

// Check if user is logged in
export const getIsLoggedIn = () => {
  return isLoggedIn.value;
};

// Get user role
export const getUserRole = () => {
  return userRole.value;
};

// Check if current user is admin
export const isAdmin = computed(() => {
  return userRole.value === 'ADMIN';
});

// Initialize - load user from storage on app start
export const initializeStore = () => {
  loadUserFromStorage();
};

// Export reactive refs for use in components
export const userStore = {
  currentUser,
  isLoggedIn,
  userRole,
  isAdmin,
  setCurrentUser,
  clearUser,
  getCurrentUser,
  getIsLoggedIn,
  getUserRole,
  initializeStore
};
