import React from 'react';
import {BrowserRouter} from 'react-router-dom';
import {QueryProvider} from '@/api/QueryProvider';
import {AuthProvider} from '@/auth/AuthContext';
import {NotificationProvider} from '@/context/NotificationContext';
import {ThemeModeProvider} from '@/context/ThemeModeContext';
import AppRoutes from './AppRoutes';

function App() {
  return (
    <QueryProvider>
      <AuthProvider>
        <ThemeModeProvider>
          <NotificationProvider>
            <BrowserRouter>
              <AppRoutes />
            </BrowserRouter>
          </NotificationProvider>
        </ThemeModeProvider>
      </AuthProvider>
    </QueryProvider>
  );
}

export default App;
