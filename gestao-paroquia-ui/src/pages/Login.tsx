import React, { useState } from 'react';
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Paper,
} from '@mui/material';
import { useAuth } from '../auth/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { useNotification } from '../context/NotificationContext';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { showError } = useNotification();
  
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as any)?.from?.pathname || '/dashboard';

  const handleSubmit = async (e: React.FormEvent) => {
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
    } catch (err: any) {
      showError('Falha ao entrar. Verifique suas credenciais.');
    } finally {
      setIsLoading(false);
    }
  };


  return (
    <Container maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%', textAlign: 'center' }}>
          <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
            <img 
              src="/logo-paroquia.png" 
              alt="Logo da Paróquia" 
              style={{ width: '130px', height: '130px', objectFit: 'contain' }} 
            />
          </Box>
          <Typography component="h1" variant="h5" align="center" gutterBottom sx={{ fontWeight: 700, color: 'primary.main' }}>
            Nossa Senhora de Fátima
          </Typography>
          <Typography component="h2" variant="h6" align="center" sx={{ mb: 3, color: 'text.secondary' }}>
            Gestão Paroquial
          </Typography>
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
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
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={isLoading}
            >
              {isLoading ? 'Entrando...' : 'Entrar'}
            </Button>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Login;
