import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode })=> ({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  publicDir: mode === 'development',
  json: {
    stringify : true,
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  build: {
    manifest: true,
    outDir: '../plugin/src/main/resources',
  }
}))
