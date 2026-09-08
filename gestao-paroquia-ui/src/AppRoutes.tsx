import {lazy, type ReactNode, Suspense} from 'react';
import {Box, Skeleton} from '@mui/material';
import {Navigate, Route, Routes} from 'react-router-dom';
import ProtectedRoute from '@/auth/ProtectedRoute';
import Layout from '@/components/Layout';
import AppErrorBoundary from '@/components/AppErrorBoundary';
import {useAuth} from '@/auth/AuthContext';
import {canApprove, canManageAnoParoquial, canManageLocais, canManageOrganizacoes} from '@/auth/permissions';

const Login = lazy(() => import('@/pages/Login'));
const Calendar = lazy(() => import('@/pages/Calendar'));
const Dashboard = lazy(() => import('@/pages/Dashboard'));
const Aprovacoes = lazy(() => import('@/pages/Aprovacoes'));
const Projects = lazy(() => import('@/pages/Projects'));
const Ativos = lazy(() => import('@/pages/Ativos'));
const Settings = lazy(() => import('@/pages/Settings'));
const Organograma = lazy(() => import('@/pages/Organograma'));
const Reports = lazy(() => import('@/pages/Reports'));
const ConselhosAtas = lazy(() => import('@/pages/ConselhosAtas'));
const Fieis = lazy(() => import('@/pages/Fieis'));
const Clerigos = lazy(() => import('@/pages/Clerigos'));
const Locais = lazy(() => import('@/pages/Locais'));
const Organizacoes = lazy(() => import('@/pages/Organizacoes'));
const Notificacoes = lazy(() => import('@/pages/Notificacoes'));
const AjustesCadastrais = lazy(() => import('@/pages/AjustesCadastrais'));
const Reservas = lazy(() => import('@/pages/Reservas'));

const RouteFallback = () => (
  <Box sx={{p: 4}}>
    <Skeleton variant="text" width={240} height={48} sx={{mb: 3}} />
    <Skeleton variant="rounded" height={320} />
  </Box>
);

const RouteContent = ({children}: {children: ReactNode}) => (
  <AppErrorBoundary>
    <Suspense fallback={<RouteFallback />}>{children}</Suspense>
  </AppErrorBoundary>
);

const AprovacoesRoute = ({ children }: { children: ReactNode }) => {
  const { user } = useAuth();
  if (!canApprove(user)) {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
};

const ConfiguracoesRoute = ({ children }: { children: ReactNode }) => {
  const { user } = useAuth();
  if (!canManageAnoParoquial(user) && !canManageOrganizacoes(user)) {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
};

type CadastroTipo = 'locais' | 'organizacoes';

const CadastroRoute = ({ children, tipo }: { children: ReactNode; tipo: CadastroTipo }) => {
  const { user } = useAuth();
  const canAccess = tipo === 'locais'
    ? canManageLocais(user)
    : tipo === 'organizacoes'
      ? canManageOrganizacoes(user)
      : true;

  if (!canAccess) return <Navigate to="/dashboard" replace />;
  return <>{children}</>;
};

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/login" element={<RouteContent><Login /></RouteContent>} />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <Layout>
              <RouteContent>
              <Routes>
                <Route path="dashboard" element={<Dashboard />} />
                <Route path="calendar" element={<Calendar />} />
                <Route path="fieis" element={<Fieis />} />
                <Route path="clerigos" element={<Clerigos />} />
                <Route path="notificacoes" element={<Notificacoes />} />
                <Route path="ajustes-cadastrais" element={<AprovacoesRoute><AjustesCadastrais /></AprovacoesRoute>} />
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
                <Route path="reservas" element={<Reservas />} />
                <Route path="ativos" element={<Ativos />} />
                <Route path="locais" element={<CadastroRoute tipo="locais"><Locais /></CadastroRoute>} />
                <Route path="organizacoes" element={<CadastroRoute tipo="organizacoes"><Organizacoes /></CadastroRoute>} />
                <Route path="cadastros" element={<Navigate to="/fieis" replace />} />
                <Route path="cadastros/fieis" element={<Navigate to="/fieis" replace />} />
                <Route path="cadastros/locais" element={<Navigate to="/locais" replace />} />
                <Route path="cadastros/organizacoes" element={<Navigate to="/organizacoes" replace />} />
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
              </RouteContent>
            </Layout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
};

export default AppRoutes;
