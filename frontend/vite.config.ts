import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        // Development-only proxy. Production uses VITE_API_BASE_URL or same-origin routing.
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
