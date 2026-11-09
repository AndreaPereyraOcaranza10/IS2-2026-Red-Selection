import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import '@dulcemayor/shared';
import './styles.css';
import './admin.css';
import './mobile-polish.css';
import App from './App.jsx';

createRoot(document.getElementById('root')).render(
  <React.StrictMode><BrowserRouter><App /></BrowserRouter></React.StrictMode>,
);
