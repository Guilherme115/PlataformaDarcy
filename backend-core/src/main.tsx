import React from 'react'
import ReactDOM from 'react-dom/client'
import { Excalidraw } from '@excalidraw/excalidraw'

// Lembre-se: O CSS está vindo do HTML agora!

const rootElement = document.getElementById('root-excalidraw')

if (rootElement) {
  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <div style={{ width: '100%', height: '100%' }}>
        <Excalidraw
           langCode="pt-BR"
           // 👇 AQUI ESTÁ O SEGREDO: Começa com FREEDRAW (Pincel) 👇
           initialData={{
               appState: {
                   viewBackgroundColor: "#ffffff",
                   activeTool: { type: "freedraw" }, // <--- FORÇA O PINCEL
                   currentItemStrokeWidth: 2
               }
           }}
           placeholder="Espaço livre para cálculos..."
        />
      </div>
    </React.StrictMode>
  )
}