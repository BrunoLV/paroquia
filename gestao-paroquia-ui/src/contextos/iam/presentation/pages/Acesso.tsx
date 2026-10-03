import React, { useState } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import LockResetIcon from '@mui/icons-material/LockReset';
import LoginIcon from '@mui/icons-material/Login';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { useLocation, useNavigate } from 'react-router-dom';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { solicitarResetSenha } from '@/contextos/iam/infrastructure/http/auth';
import { useConfiguracaoParoquia } from '@/contextos/governanca/presentation/hooks/useConfiguracaoParoquia';

interface LoginLocationState {
  from?: { pathname?: string };
}

/**
 * Rotas protegidas que podem ser retomadas com segurança após autenticação.
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

  // Reset password states
  const [isResetModalOpen, setIsResetModalOpen] = useState(false);
  const [resetUsername, setResetUsername] = useState('');
  const [isResetLoading, setIsResetLoading] = useState(false);

  const { showError, showSuccess } = useNotification();
  const { login } = useAuth();
  const { configuracao } = useConfiguracaoParoquia();
  const navigate = useNavigate();
  const location = useLocation();

  const logoClaroSrc = configuracao?.temLogoFundoClaro
    ? '/api/v1/configuracao-paroquia/logo/claro'
    : '/logo-paroquia-claro.png';
  const paroquiaNome = configuracao?.nome || 'Nossa Senhora de Fátima';
  const paroquiaSubtitulo = configuracao?.diocese || 'Gestão Paroquial';

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

  const handleSolicitarReset = async () => {
    if (!resetUsername.trim()) {
      showError('Informe o nome de usuário ou e-mail.');
      return;
    }

    setIsResetLoading(true);

    try {
      await solicitarResetSenha(resetUsername.trim());
      showSuccess('Solicitação de redefinição enviada com sucesso! Entre em contato com a secretaria paroquial ou pároco para a liberação.');
      setIsResetModalOpen(false);
      setResetUsername('');
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Erro ao solicitar redefinição de senha.'));
    } finally {
      setIsResetLoading(false);
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
          <Paper elevation={0} sx={(theme) => ({ p: { xs: 3, sm: 4 }, width: '100%', textAlign: 'center', border: '1px solid', borderColor: 'divider', boxShadow: theme.shadows[8] })}>
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2.5 }}>
              <img
                src={logoClaroSrc}
                alt={paroquiaNome}
                onError={(e) => {
                  const target = e.currentTarget;
                  if (target.src !== '/logo-paroquia-claro.png') {
                    target.src = '/logo-paroquia-claro.png';
                  }
                }}
                style={{ width: '112px', height: '112px', objectFit: 'contain' }}
              />
            </Box>
            <Typography component="h1" variant="h5" align="center" gutterBottom sx={{ fontWeight: 700, color: 'primary.main', letterSpacing: '-0.02em' }}>
              {paroquiaNome}
            </Typography>
            <Typography component="h2" variant="body2" align="center" sx={{ mb: 3.5, color: 'text.secondary', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.08em' }}>
              {paroquiaSubtitulo}
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
                sx={{ mt: 3, mb: 1, fontWeight: 700, py: 1.2, textTransform: 'none' }}
                disabled={isLoading}
              />

              <Box sx={{ mt: 1.5, display: 'flex', justifyContent: 'center' }}>
                <Button
                  variant="text"
                  size="small"
                  onClick={() => {
                    setResetUsername(username);
                    setIsResetModalOpen(true);
                  }}
                  sx={{ textTransform: 'none', fontWeight: 500 }}
                >
                  Esqueci minha senha
                </Button>
              </Box>
            </Box>
          </Paper>
        </Box>
      </Container>

      {/* Modal de Solicitação de Redefinição de Senha */}
      <Dialog
        open={isResetModalOpen}
        onClose={() => setIsResetModalOpen(false)}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle>Recuperação de Acesso</DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Informe seu nome de usuário ou e-mail cadastrado. Uma solicitação de redefinição será registrada para liberação pela administração da paróquia.
          </Typography>
          <TextField
            label="Nome de Usuário ou E-mail"
            size="small"
            fullWidth
            required
            autoFocus
            value={resetUsername}
            onChange={(e) => setResetUsername(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            color="secondary"
            onClick={() => setIsResetModalOpen(false)}
          />
          <ActionButton
            title="Solicitar Redefinição"
            icon={isResetLoading ? <CircularProgress size={20} color="inherit" /> : <LockResetIcon />}
            color="primary"
            onClick={handleSolicitarReset}
            disabled={!resetUsername.trim() || isResetLoading}
          />
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Acesso;
