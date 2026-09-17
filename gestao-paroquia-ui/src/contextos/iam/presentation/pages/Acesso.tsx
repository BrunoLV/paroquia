import React, { useState } from 'react';
import {
  Box,
  CircularProgress,
  Container,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { useLocation, useNavigate } from 'react-router-dom';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import ActionButton from '@/shared/presentation/components/ActionButton';

import LoginIcon from '@mui/icons-material/Login';

interface LoginLocationState {
  from?: {pathname?: string};
}

/**
 * Rotas protegidas que podem ser retomadas com segurança após autenticação.
 *
 * Uma origem preservada pelo guard pode apontar para URL obsoleta de favorito,
 * como `/login`. Reaplicá-la após uma sessão válida deixaria a pessoa em uma
 * rota sem tela; destinos desconhecidos voltam sempre ao painel canônico.
 */
const rotasRetomaveis = new Set([
  '/painel',
  '/calendario',
  '/fieis',
  '/clerigos',
  '/notificacoes',
  '/ajustes-cadastrais',
  '/organograma',
  '/relatorios',
  '/conselhos',
  '/aprovacoes',
  '/projetos',
  '/reservas',
  '/ativos',
  '/locais',
  '/organizacoes',
  '/cadastros',
  '/cadastros/fieis',
  '/cadastros/locais',
  '/cadastros/organizacoes',
  '/configuracoes',
]);

export const obterDestinoPosAutenticacao = (state: unknown): string => {
  if (typeof state !== 'object' || state === null || !('from' in state)) return '/painel';
  const from = (state as LoginLocationState).from;
  return typeof from?.pathname === 'string' && rotasRetomaveis.has(from.pathname)
    ? from.pathname
    : '/painel';
};

const Acesso = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { showError } = useNotification();
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = obterDestinoPosAutenticacao(location.state);

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!username.trim()) {
      showError('O nome de usuário é obrigatório.');
      return;
    }
    if (!password) {
      showError('A senha é obrigatória.');
      return;
    }

    setIsLoading(true);

    try {
      await login(username, password);
      navigate(from, { replace: true });
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Falha ao entrar. Verifique suas credenciais.'));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Box
      data-testid="login-layout-wrapper"
      sx={{
        minHeight: '100vh',
        width: '100%',
        bgcolor: 'background.default',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        py: 4,
        px: 2,
        overflowY: 'auto',
        background: (theme) => `linear-gradient(135deg, ${theme.palette.background.default} 0%, ${theme.custom.surfaceMuted} 100%)`,
      }}
    >
      <Container maxWidth="xs">
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <Paper elevation={0} sx={(theme) => ({ p: {xs: 3, sm: 4}, width: '100%', textAlign: 'center', border: '1px solid', borderColor: 'divider', boxShadow: theme.shadows[8] })}>
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2.5 }}>
              <img
                src="/logo-paroquia.png"
                alt="Logo da Paróquia"
                style={{ width: '112px', height: '112px', objectFit: 'contain' }}
              />
            </Box>
            <Typography component="h1" variant="h5" align="center" gutterBottom sx={{ fontWeight: 700, color: 'primary.main', letterSpacing: '-0.02em' }}>
              Nossa Senhora de Fátima
            </Typography>
            <Typography component="h2" variant="body2" align="center" sx={{ mb: 3.5, color: 'text.secondary', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.08em' }}>
              Gestão Paroquial
            </Typography>

            <Box component="form" onSubmit={handleLoginSubmit} noValidate sx={{ mt: 1 }}>
              <TextField
                margin="normal"
                required
                fullWidth
                id="username"
                label="Nome de Usuário"
                name="username"
                autoComplete="username"
                autoFocus
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                name="password"
                label="Senha"
                type="password"
                id="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <ActionButton
                title="Entrar no sistema"
                type="submit"
                fullWidth
                variant="contained"
                color="primary"
                icon={isLoading ? <CircularProgress size={20} color="inherit" /> : <LoginIcon />}
                label={isLoading ? 'Entrando...' : 'Entrar'}
                sx={{ mt: 3, mb: 0.5, fontWeight: 700, py: 1.2, textTransform: 'none' }}
                disabled={isLoading}
              />
            </Box>
          </Paper>
        </Box>
      </Container>
    </Box>
  );
};

export default Acesso;
