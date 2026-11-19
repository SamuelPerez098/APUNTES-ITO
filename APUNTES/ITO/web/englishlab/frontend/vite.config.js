import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Proxy de desarrollo: /api -> backend Spring Boot (evita CORS en local).
// En producción la URL de la API se configura con VITE_API_URL (ver .env.example).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
