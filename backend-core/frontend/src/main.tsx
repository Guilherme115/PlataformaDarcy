import React from 'react'
import ReactDOM from 'react-dom/client'
import { Excalidraw } from '@excalidraw/excalidraw'

// ❌ COMENTAMOS PARA O BUILD PARAR DE RECLAMAR
// import '@excalidraw/excalidraw/index.css'

const rootElement = document.getElementById('root-excalidraw')

if (rootElement) {
  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <div style={{ width: '100%', height: '100%' }}>
        <Excalidraw
           langCode="pt-BR"
           placeholder="Espaço livre para cálculos..."
        />
      </div>
    </React.StrictMode>
  )
}