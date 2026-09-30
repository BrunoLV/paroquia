import {BrowserRouter} from 'react-router-dom';
import {QueryProvider} from '@/shared/presentation/context/QueryProvider';
import {AuthProvider} from '@/contextos/iam/presentation/auth/AuthContext';
import {NotificationProvider} from '@/shared/presentation/context/NotificationContext';
import {ThemeModeProvider} from '@/shared/presentation/context/ThemeModeContext';
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
