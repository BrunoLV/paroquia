import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  MenuItem,
  Select,
  InputLabel,
  FormControl,
  Checkbox,
  FormControlLabel
} from '@mui/material';

import { useAuth } from '../auth/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { useNotification } from '../context/NotificationContext';
import apiClient from '../api/client';
import { getOrganizacoes } from '../api/organizacoes';
import { Organizacao } from '../api/types';

const Login = () => {
  const [isRegistering, setIsRegistering] = useState(false);
  
  // Login fields
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  
  // Signup fields
  const [nome, setNome] = useState('');
  const [signupUsername, setSignupUsername] = useState('');
  const [signupPassword, setSignupPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [telefone, setTelefone] = useState('');
  const [dataNascimento, setDataNascimento] = useState('');
  const [churchId, setChurchId] = useState('');
  const [batizado, setBatizado] = useState(false);
  const [primeiraComunhao, setPrimeiraComunhao] = useState(false);
  const [crismado, setCrismado] = useState(false);
  const [casadoIgrejaCatolica, setCasadoIgrejaCatolica] = useState(false);

  const [availableOrgs, setAvailableOrgs] = useState<Organizacao[]>([]);

  const [isLoading, setIsLoading] = useState(false);
  const { showError, showSuccess } = useNotification();
  
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as any)?.from?.pathname || '/dashboard';

  useEffect(() => {
    if (isRegistering) {
      getOrganizacoes()
        .then(setAvailableOrgs)
        .catch((err) => {
          console.error('Error loading organizations:', err);
        });
    }
  }, [isRegistering]);

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
    } catch (err: any) {
      const errMsg = err.response?.data?.message || 'Falha ao entrar. Verifique suas credenciais.';
      showError(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSignupSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!nome.trim()) {
      showError('O nome completo é obrigatório.');
      return;
    }
    if (!signupUsername.trim()) {
      showError('O e-mail é obrigatório.');
      return;
    }
    if (!signupPassword) {
      showError('A senha é obrigatória.');
      return;
    }
    if (signupPassword !== confirmPassword) {
      showError('As senhas não coincidem.');
      return;
    }
    if (!telefone.trim()) {
      showError('O telefone é obrigatório.');
      return;
    }
    if (!dataNascimento) {
      showError('A data de nascimento é obrigatória.');
      return;
    }
    if (!churchId) {
      showError('A igreja / comunidade é obrigatória.');
      return;
    }

    setIsLoading(true);

    try {
      await apiClient.post('/usuarios/registrar', {
        nome,
        username: signupUsername,
        password: signupPassword,
        telefone,
        dataNascimento,
        igrejaId: churchId,
        batizado,
        primeiraComunhao,
        crismado,
        casadoIgrejaCatolica
      });

      showSuccess('Cadastro solicitado com sucesso! Aguarde a aprovação de um administrador.');
      setIsRegistering(false);
      
      // Reset signup fields
      setNome('');
      setSignupUsername('');
      setSignupPassword('');
      setConfirmPassword('');
      setTelefone('');
      setDataNascimento('');
      setChurchId('');
      setBatizado(false);
      setPrimeiraComunhao(false);
      setCrismado(false);
      setCasadoIgrejaCatolica(false);

    } catch (err: any) {
      const errMsg = err.response?.data?.message || 'Falha ao realizar cadastro. Tente novamente.';
      showError(errMsg);
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
        overflowY: 'auto',
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
            {isRegistering ? 'Cadastro de Usuário' : 'Gestão Paroquial'}
          </Typography>

          {!isRegistering ? (
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
              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={isLoading}
              >
                {isLoading ? 'Entrando...' : 'Entrar'}
              </Button>
              <Button
                fullWidth
                variant="text"
                onClick={() => setIsRegistering(true)}
                sx={{ textTransform: 'none' }}
              >
                Não tem uma conta? Cadastre-se
              </Button>
            </Box>
          ) : (
            <Box component="form" onSubmit={handleSignupSubmit} noValidate sx={{ mt: 1 }}>
              <TextField
                margin="normal"
                required
                fullWidth
                id="nome"
                label="Nome Completo"
                name="nome"
                autoFocus
                value={nome}
                onChange={(e) => setNome(e.target.value)}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                id="signup-username"
                label="E-mail"
                name="username"
                type="email"
                value={signupUsername}
                onChange={(e) => setSignupUsername(e.target.value)}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                name="password"
                label="Senha"
                type="password"
                id="signup-password"
                value={signupPassword}
                onChange={(e) => setSignupPassword(e.target.value)}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                name="confirmPassword"
                label="Confirmar Senha"
                type="password"
                id="confirmPassword"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                id="telefone"
                label="Telefone"
                name="telefone"
                value={telefone}
                onChange={(e) => setTelefone(e.target.value)}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                id="dataNascimento"
                label="Data de Nascimento"
                name="dataNascimento"
                type="date"
                slotProps={{ inputLabel: { shrink: true } }}
                value={dataNascimento}
                onChange={(e) => setDataNascimento(e.target.value)}
              />
              <FormControl fullWidth margin="normal" required>
                <InputLabel id="signup-church-label">Igreja / Comunidade</InputLabel>
                <Select
                  labelId="signup-church-label"
                  id="signup-church"
                  value={churchId}
                  label="Igreja / Comunidade"
                  onChange={(e) => setChurchId(e.target.value)}
                  inputProps={{ 'data-testid': 'signup-church-select' }}
                >
                  {availableOrgs
                    .filter((o) => o.ativo && (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE'))
                    .map((o) => (
                      <MenuItem key={o.id} value={o.id}>
                        {o.nome} ({o.tipo === 'MATRIZ' ? 'Matriz' : 'Comunidade'})
                      </MenuItem>
                    ))}
                </Select>
              </FormControl>

              <Typography variant="subtitle2" align="left" sx={{ mt: 2, mb: 1, fontWeight: 600, color: 'text.primary' }}>
                Indicadores Sacramentais
              </Typography>
              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 0.5, textAlign: 'left' }}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={batizado}
                      onChange={(e) => setBatizado(e.target.checked)}
                    />
                  }
                  label="Batizado"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={primeiraComunhao}
                      onChange={(e) => setPrimeiraComunhao(e.target.checked)}
                    />
                  }
                  label="1ª Comunhão"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={crismado}
                      onChange={(e) => setCrismado(e.target.checked)}
                    />
                  }
                  label="Crismado"
                />
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={casadoIgrejaCatolica}
                      onChange={(e) => setCasadoIgrejaCatolica(e.target.checked)}
                    />
                  }
                  label="Casado na Igreja"
                />
              </Box>

              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={isLoading}
              >
                {isLoading ? 'Cadastrando...' : 'Cadastrar'}
              </Button>
              <Button
                fullWidth
                variant="text"
                onClick={() => setIsRegistering(false)}
                sx={{ textTransform: 'none' }}
              >
                Já tem uma conta? Entre
              </Button>
            </Box>
          )}
        </Paper>
        </Box>
      </Container>
    </Box>
  );
};

export default Login;
