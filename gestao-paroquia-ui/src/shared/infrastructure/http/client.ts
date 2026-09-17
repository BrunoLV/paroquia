import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('user');
      // Remove window.location.href = '/acesso' to avoid infinite refresh loops.
      // Component-level logic (e.g., ProtectedRoute) should handle redirection.
    }
    return Promise.reject(error);
  }
);

export { apiClient };
export default apiClient;
