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
  ListItemText,
  OutlinedInput,
  SelectChangeEvent
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
  const [selectedOrgs, setSelectedOrgs] = useState<string[]>([]);
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
      showError('Falha ao entrar. Verifique suas credenciais.');
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

    setIsLoading(true);

    try {
      await apiClient.post('/usuarios/registrar', {
        nome,
        username: signupUsername,
        password: signupPassword,
        telefone,
        dataNascimento,
        organizacoesSolicitadas: selectedOrgs
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
      setSelectedOrgs([]);
    } catch (err: any) {
      const errMsg = err.response?.data?.message || 'Falha ao realizar cadastro. Tente novamente.';
      showError(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleOrgChange = (event: SelectChangeEvent<typeof selectedOrgs>) => {
    const {
      target: { value },
    } = event;
    setSelectedOrgs(
      typeof value === 'string' ? value.split(',') : value,
    );
  };

  return (
    <Container maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          mb: 4
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
                InputLabelProps={{ shrink: true }}
                value={dataNascimento}
                onChange={(e) => setDataNascimento(e.target.value)}
              />

              <FormControl fullWidth margin="normal">
                <InputLabel id="orgs-label">Pastorais/Movimentos Desejados</InputLabel>
                <Select
                  labelId="orgs-label"
                  id="orgs"
                  multiple
                  value={selectedOrgs}
                  onChange={handleOrgChange}
                  input={<OutlinedInput label="Pastorais/Movimentos Desejados" />}
                  renderValue={(selected) => 
                    selected
                      .map((id) => availableOrgs.find((o) => o.id === id)?.nome || '')
                      .join(', ')
                  }
                >
                  {availableOrgs.map((org) => (
                    <MenuItem key={org.id} value={org.id}>
                      <Checkbox checked={selectedOrgs.indexOf(org.id) > -1} />
                      <ListItemText primary={org.nome} secondary={org.tipo} />
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

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
  );
};

export default Login;
