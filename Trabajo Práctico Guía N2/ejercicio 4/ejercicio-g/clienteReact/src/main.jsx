import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import { AvisosProvider } from './components/Avisos.jsx'
import './styles.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AvisosProvider>
        <App />
      </AvisosProvider>
    </BrowserRouter>
  </React.StrictMode>,
)