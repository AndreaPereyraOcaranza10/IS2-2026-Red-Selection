import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath } from 'node:url';

export default defineConfig({
  plugins: [react()],
  envDir: '..',
  resolve: {
    alias: { '@dulcemayor/shared': fileURLToPath(new URL('../shared/src/index.js', import.meta.url)) },
    dedupe: ['react', 'react-dom', 'react-router-dom'],
  },
  server: {
    port: 5173,
    proxy: { '/api': { target: process.env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true } },
  },
});
