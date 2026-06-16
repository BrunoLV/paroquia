import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute';
import Login from './pages/Login';
import Calendar from './pages/Calendar';
import Dashboard from './pages/Dashboard';
import Approvals from './pages/Approvals';
import Projects from './pages/Projects';
import Settings from './pages/Settings';
import Layout from './components/Layout';
import { useAuth } from './auth/AuthContext';
import { canApprove, canManageAnoParoquial, canManageLocais } from './auth/permissions';

const ApprovalsRoute = ({ children }: { children: React.ReactNode }) => {
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
                <Route
                  path="approvals"
                  element={
                    <ApprovalsRoute>
                      <Approvals />
                    </ApprovalsRoute>
                  }
                />
                <Route path="projects" element={<Projects />} />
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

