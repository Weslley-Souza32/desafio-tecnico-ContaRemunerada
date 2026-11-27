
import axios from 'axios';
import { tokenStorage } from '../auth/tokenStorage';

export const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

httpClient.interceptors.request.use((config) => {
  const token = tokenStorage.get();

  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }

  return config;
})

httpClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status;
      const url = error.config?.url ?? '';

      const endpointPublico = 
      url === '/auth/login' || url === '/contas';

      if (status === 401 && !endpointPublico && tokenStorage.get()) {
        tokenStorage.remove();

        window.location.replace('/login?session=expired');
      }
    }

    return Promise.reject(error);
  }
)
