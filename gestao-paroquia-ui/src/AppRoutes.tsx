import React from 'react';
import {Navigate, Route, Routes} from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute';
import Login from './pages/Login';
import Calendar from './pages/Calendar';
import Dashboard from './pages/Dashboard';
import Aprovacoes from './pages/Aprovacoes';
import Projects from './pages/Projects';
import Ativos from './pages/Ativos';
import Settings from './pages/Settings';
import Organograma from './pages/Organograma';
import Reports from './pages/Reports';
import ConselhosAtas from './pages/ConselhosAtas';
import Fieis from './pages/Fieis';
import Layout from './components/Layout';
import {useAuth} from './auth/AuthContext';
import {canApprove, canManageAnoParoquial, canManageLocais} from './auth/permissions';

const AprovacoesRoute = ({ children }: { children: React.ReactNode }) => {
  const { user } = useAuth();
  if (!canApprove(user)) {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
};

const ConfiguracoesRoute = ({ children }: { children: React.ReactNode }) => {
  const { user } = useAuth();
  if (!canManageAnoParoquial(user) && !canManageLocais(user)) {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
};

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <Layout>
              <Routes>
                <Route path="dashboard" element={<Dashboard />} />
                <Route path="calendar" element={<Calendar />} />
                <Route path="fieis" element={<Fieis />} />
                <Route path="organograma" element={<Organograma />} />
                <Route path="reports" element={<Reports />} />
                <Route path="conselhos" element={<ConselhosAtas />} />
                <Route
                  path="approvals"
                  element={
                    <AprovacoesRoute>
                      <Aprovacoes />
                    </AprovacoesRoute>
                  }
                />
                <Route path="projects" element={<Projects />} />
                <Route path="ativos" element={<Ativos />} />
                <Route
                  path="configuracoes"
                  element={
                    <ConfiguracoesRoute>
                      <Settings />
                    </ConfiguracoesRoute>
                  }
                />
                <Route index element={<Navigate to="/dashboard" replace />} />
              </Routes>
            </Layout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
};

export default AppRoutes;

