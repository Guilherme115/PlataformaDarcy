import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  define: {
    // 👇 A MÁGICA: Força o React a usar a versão leve de navegador (sem 'exports')
    'process.env.NODE_ENV': '"production"',
    'global': 'window',
  },
  build: {
    outDir: '../src/main/resources/static/notebook',
    emptyOutDir: true,
    lib: {
      entry: path.resolve(__dirname, 'src/main.tsx'),
      name: 'DarcyNotebook',
      fileName: (format) => `darcy-notebook.js`,
      formats: ['es']
    }
  }
})