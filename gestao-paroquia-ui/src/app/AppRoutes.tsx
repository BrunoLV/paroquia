import {lazy, type ReactNode, Suspense} from 'react';
import {Box, Skeleton} from '@mui/material';
import {Navigate, Route, Routes} from 'react-router-dom';
import ProtectedRoute from '@/contextos/iam/presentation/auth/ProtectedRoute';
import Layout from '@/shared/presentation/components/Layout';
import AppErrorBoundary from '@/shared/presentation/components/AppErrorBoundary';
import {useAuth} from '@/contextos/iam/presentation/auth/AuthContext';
import {canApprove, canManageAnoParoquial, canManageLocais, canManageOrganizacoes} from '@/contextos/iam/presentation/auth/permissions';

const Acesso = lazy(() => import('@/contextos/iam/presentation/pages/Acesso'));
const Calendario = lazy(() => import('@/contextos/calendario/presentation/pages/Calendario'));
const Painel = lazy(() => import('@/contextos/painel/presentation/pages/Painel'));
const Aprovacoes = lazy(() => import('@/contextos/aprovacao/presentation/pages/Aprovacoes'));
const Projetos = lazy(() => import('@/contextos/projeto/presentation/pages/Projetos'));
const Ativos = lazy(() => import('@/contextos/patrimonio/presentation/pages/Ativos'));
const Configuracoes = lazy(() => import('@/contextos/governanca/presentation/pages/Configuracoes'));
const Organograma = lazy(() => import('@/contextos/organizacao/presentation/pages/Organograma'));
const Relatorios = lazy(() => import('@/contextos/painel/presentation/pages/Relatorios'));
const ConselhosAtas = lazy(() => import('@/contextos/conselho/presentation/pages/ConselhosAtas'));
const Fieis = lazy(() => import('@/contextos/fiel/presentation/pages/Fieis'));
const Clerigos = lazy(() => import('@/contextos/clero/presentation/pages/Clerigos'));
const Locais = lazy(() => import('@/contextos/local/presentation/pages/Locais'));
const Organizacoes = lazy(() => import('@/contextos/organizacao/presentation/pages/Organizacoes'));
const Notificacoes = lazy(() => import('@/contextos/iam/presentation/pages/Notificacoes'));
const AjustesCadastrais = lazy(() => import('@/contextos/governanca/presentation/pages/AjustesCadastrais'));
const Reservas = lazy(() => import('@/contextos/reserva/presentation/pages/Reservas'));
const UnidadesTerritoriais = lazy(() => import('@/contextos/organizacao/presentation/pages/UnidadesTerritoriais'));

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
    return <Navigate to="/painel" replace />;
  }
  return <>{children}</>;
};

const ConfiguracoesRoute = ({ children }: { children: ReactNode }) => {
  const { user } = useAuth();
  if (!canManageAnoParoquial(user) && !canManageOrganizacoes(user)) {
    return <Navigate to="/painel" replace />;
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

  if (!canAccess) return <Navigate to="/painel" replace />;
  return <>{children}</>;
};

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/acesso" element={<RouteContent><Acesso /></RouteContent>} />
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <Layout>
              <RouteContent>
              <Routes>
                <Route path="painel" element={<Painel />} />
                <Route path="calendario" element={<Calendario />} />
                <Route path="fieis" element={<Fieis />} />
                <Route path="clerigos" element={<Clerigos />} />
                <Route path="notificacoes" element={<Notificacoes />} />
                <Route path="ajustes-cadastrais" element={<AprovacoesRoute><AjustesCadastrais /></AprovacoesRoute>} />
                <Route path="organograma" element={<Organograma />} />
                <Route path="relatorios" element={<Relatorios />} />
                <Route path="conselhos" element={<ConselhosAtas />} />
                <Route
                  path="aprovacoes"
                  element={
                    <AprovacoesRoute>
                      <Aprovacoes />
                    </AprovacoesRoute>
                  }
                />
                <Route path="projetos" element={<Projetos />} />
                <Route path="reservas" element={<Reservas />} />
                <Route path="ativos" element={<Ativos />} />
                <Route path="locais" element={<CadastroRoute tipo="locais"><Locais /></CadastroRoute>} />
                <Route path="organizacoes" element={<CadastroRoute tipo="organizacoes"><Organizacoes /></CadastroRoute>} />
                <Route path="governo-territorio" element={<UnidadesTerritoriais />} />
                <Route path="unidades-territoriais" element={<Navigate to="/governo-territorio" replace />} />
                <Route path="cadastros" element={<Navigate to="/fieis" replace />} />
                <Route path="cadastros/fieis" element={<Navigate to="/fieis" replace />} />
                <Route path="cadastros/locais" element={<Navigate to="/locais" replace />} />
                <Route path="cadastros/organizacoes" element={<Navigate to="/organizacoes" replace />} />
                <Route
                  path="configuracoes"
                  element={
                    <ConfiguracoesRoute>
                      <Configuracoes />
                    </ConfiguracoesRoute>
                  }
                />
                <Route index element={<Navigate to="/painel" replace />} />
                <Route path="*" element={<Navigate to="/painel" replace />} />
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
