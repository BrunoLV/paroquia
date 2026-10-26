import axios from 'axios';
import logger from '@/shared/infrastructure/logging/logger';

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
    const status = error.response?.status;
    if (status === 401) {
      // Remove window.location.href = '/acesso' to avoid infinite refresh loops.
      // Component-level logic (e.g., ProtectedRoute) should handle redirection.
    } else if (status && status >= 400 && !error.config?.url?.includes('/observabilidade/logs')) {
      logger.warn(
        `Falha na requisição HTTP: ${error.config?.method?.toUpperCase()} ${error.config?.url} [Status ${status}]`,
        {
          componente: 'ApiClient',
          detalhes: JSON.stringify({
            status,
            url: error.config?.url,
            method: error.config?.method,
            data: error.response?.data,
          }),
        }
      );
    }
    return Promise.reject(error);
  }
);

export { apiClient };
export default apiClient;
