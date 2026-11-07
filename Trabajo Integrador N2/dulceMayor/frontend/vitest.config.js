import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import { fileURLToPath } from 'node:url';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@dulcemayor/shared': fileURLToPath(new URL('./shared/src/index.js', import.meta.url)) },
    dedupe: ['react', 'react-dom', 'react-router-dom'],
  },
  test: {
    environment: 'jsdom',
    globals: true,
    css: false,
    include: ['tests/**/*.test.jsx'],
    env: { VITE_USE_MOCK: 'true', VITE_ROUTING_URL: 'off' },
  },
});