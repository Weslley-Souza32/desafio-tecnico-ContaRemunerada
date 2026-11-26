
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
