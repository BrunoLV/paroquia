import React from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Box,
  IconButton,
  Container,
  Button,
} from '@mui/material';
import {
  Event as EventIcon,
  Logout as LogoutIcon,
  Dashboard as DashboardIcon,
  CalendarMonth as CalendarIcon,
} from '@mui/icons-material';
import { useAuth } from '../auth/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';

interface LayoutProps {
  children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const menuItems = [
    { label: 'Dashboard', path: '/dashboard', icon: <DashboardIcon /> },
    { label: 'Calendário', path: '/calendar', icon: <CalendarIcon /> },
  ];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: '#f4f6f8' }}>
      <AppBar position="static" color="primary" sx={{ mb: 4, boxShadow: '0 4px 20px rgba(0,0,0,0.1)' }}>
        <Toolbar>
          <EventIcon sx={{ mr: 2 }} />
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 600, letterSpacing: '0.5px' }}>
            Nossa Senhora de Fátima
          </Typography>
          
          <Box sx={{ display: 'flex', gap: 1, mr: 4 }}>
            {menuItems.map((item) => (
              <Button
                key={item.path}
                color="inherit"
                startIcon={item.icon}
                onClick={() => navigate(item.path)}
                sx={{
                  textTransform: 'none',
                  fontWeight: location.pathname === item.path ? 700 : 400,
                  bgcolor: location.pathname === item.path ? 'rgba(255,255,255,0.1)' : 'transparent',
                  '&:hover': { bgcolor: 'rgba(255,255,255,0.2)' },
                }}
              >
                {item.label}
              </Button>
            ))}
          </Box>

          {user && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Typography variant="body2" sx={{ display: { xs: 'none', sm: 'block' }, fontWeight: 500 }}>
                Olá, {user.name}
              </Typography>
              <IconButton color="inherit" onClick={handleLogout} title="Sair do sistema">
                <LogoutIcon />
              </IconButton>
            </Box>
          )}
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ pb: 6 }}>
        {children}
      </Container>
    </Box>
  );
};

export default Layout;
