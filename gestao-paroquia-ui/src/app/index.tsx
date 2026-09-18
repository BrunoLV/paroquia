import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import logger from '@/shared/infrastructure/logging/logger';

// Listeners globais de falhas de execução e promises rejeitadas para telemetria/Loki
if (typeof window !== 'undefined') {
  window.addEventListener('error', (event) => {
    logger.error(`Erro global não capturado: ${event.message}`, event.error, {
      componente: 'WindowGlobal',
      detalhes: `${event.filename}:${event.lineno}:${event.colno}`,
    });
  });

  window.addEventListener('unhandledrejection', (event) => {
    logger.error('Promise rejeitada não tratada (unhandledrejection)', event.reason, {
      componente: 'WindowGlobal',
    });
  });
}

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
