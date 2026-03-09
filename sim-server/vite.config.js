import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import ViteMonacoPlugin from 'vite-plugin-monaco-editor'

import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
      vue(),
      ViteMonacoPlugin({
        languageWorkers: ['editorWorkerService', 'typescript', 'json', 'html']
      }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
