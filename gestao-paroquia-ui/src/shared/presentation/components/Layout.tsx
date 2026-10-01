import React from 'react';
import {
  AppBar,
  Avatar,
  Box,
  Container,
  Divider,
  Drawer,
  Paper,
  Toolbar,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import MenuIcon from '@mui/icons-material/Menu';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { isUnlinkedUser } from '@/contextos/iam/presentation/auth/permissions';
import SidebarNavigation from './navigation/SidebarNavigation';
import { useThemeMode } from '@/shared/presentation/context/ThemeModeContext';
import ActionButton from '@/shared/presentation/components/ActionButton';
import IconActionButton from './IconActionButton';

/** Shell responsivo; agrupamento e visibilidade dos links pertencem à navegação compartilhada. */
export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const { mode, toggleMode } = useThemeMode();
  const theme = useTheme();
  const isMdUp = useMediaQuery(theme.breakpoints.up('md'));
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const [drawerOpen, setDrawerOpen] = React.useState(true);
  const handleMobileDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleDrawerToggle = () => {
    if (isMdUp) {
      setDrawerOpen(!drawerOpen);
    } else {
      setMobileOpen(!mobileOpen);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/acesso');
  };

  const drawerWidth = isMdUp ? (drawerOpen ? 240 : 72) : 240;

  const drawerContent = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: 'background.paper' }}>
      <Box
        sx={{
          p: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: drawerOpen || !isMdUp ? 'space-between' : 'center',
          minHeight: 64,
          width: '100%',
          boxSizing: 'border-box',
        }}
      >
        {(drawerOpen || !isMdUp) ? (
          <>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, overflow: 'hidden' }}>
              <Box
                sx={{
                  bgcolor: 'background.paper',
                  p: 0.5,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: 36,
                  height: 36,
                  flexShrink: 0,
                }}
              >
                <img
                  src="/logo-paroquia.png"
                  alt="Logo da Paróquia"
                  style={{ width: '90%', height: '90%', objectFit: 'contain' }}
                />
              </Box>
              <Box sx={{ overflow: 'hidden' }}>
                <Typography variant="subtitle2" noWrap sx={{ fontWeight: 700, color: 'primary.main', lineHeight: 1.2 }}>
                  N. S. de Fátima
                </Typography>
                <Typography variant="caption" noWrap sx={{ color: 'text.secondary', display: 'block' }}>
                  Gestão Paroquial
                </Typography>
              </Box>
            </Box>
            <IconActionButton
              title={drawerOpen ? "Recolher menu lateral" : "Expandir menu lateral"}
              icon={<MenuIcon />}
              color="primary"
              onClick={handleDrawerToggle}
              sx={{ p: 0.5 }}
            />
          </>
        ) : (
          <IconActionButton
            title={drawerOpen ? "Recolher menu lateral" : "Expandir menu lateral"}
            icon={<MenuIcon />}
            color="primary"
            onClick={handleDrawerToggle}
          />
        )}
      </Box>
      <Divider />

      <SidebarNavigation user={user} pathname={location.pathname} expanded={drawerOpen || !isMdUp}
        onNavigate={() => setMobileOpen(false)} />
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <Box
        component="a"
        href="#main-content"
        sx={{
          position: 'absolute',
          left: 8,
          top: -48,
          zIndex: (t) => t.zIndex.tooltip + 1,
          bgcolor: 'background.paper',
          color: 'primary.main',
          px: 2,
          py: 1,
          borderRadius: 1,
          boxShadow: 3,
          transition: 'top 0.15s ease-in',
          '&:focus-visible': { top: 8 },
        }}
      >
        Pular para o conteúdo principal
      </Box>
      <Drawer
        variant={isMdUp ? 'permanent' : 'temporary'}
        open={isMdUp ? true : mobileOpen}
        onClose={handleMobileDrawerToggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': { 
            boxSizing: 'border-box', 
            width: drawerWidth,
            transition: theme.transitions.create('width', {
              easing: theme.transitions.easing.sharp,
              duration: theme.transitions.duration.enteringScreen,
            }),
            overflowX: 'hidden',
            borderRight: '1px solid',
            borderColor: 'divider'
          },
        }}
      >
        {drawerContent}
      </Drawer>

      {/* Main Content Area */}
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        <AppBar position="static" color="primary" elevation={0} sx={{ mb: 4, borderBottom: '1px solid', borderColor: 'rgba(255, 255, 255, 0.16)' }}>
          <Toolbar sx={{ minHeight: { xs: 60, sm: 68 } }}>
            {!isMdUp && (
              <IconActionButton
                title="Abrir menu de navegação"
                ariaLabel="abrir menu"
                icon={<MenuIcon />}
                color="inherit"
                onClick={handleDrawerToggle}
                sx={{ mr: 2 }}
              />
            )}
            
            <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 700, letterSpacing: '0.01em' }}>
              Nossa Senhora de Fátima
            </Typography>

            <IconActionButton
              title={mode === 'dark' ? 'Ativar tema claro' : 'Ativar tema escuro'}
              icon={mode === 'dark' ? <LightModeIcon /> : <DarkModeIcon />}
              color="inherit"
              onClick={toggleMode}
              sx={{ mr: user ? 1 : 0 }}
            />

            {user && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main', color: 'primary.main', fontSize: '0.8rem', fontWeight: 700 }}>
                  {user.name.slice(0, 1).toLocaleUpperCase('pt-BR')}
                </Avatar>
                <Typography variant="body2" sx={{ display: { xs: 'none', sm: 'block' }, fontWeight: 500 }}>
                  Olá, {user.name}
                </Typography>
                <ActionButton
                  label="Sair"
                  title="Sair do sistema"
                  icon={<LogoutIcon />}
                  onClick={handleLogout}
                  sx={{
                    bgcolor: 'rgba(255,255,255,0.16)',
                    color: 'common.white',
                    '&:hover': { bgcolor: 'rgba(255,255,255,0.28)' },
                  }}
                />
              </Box>
            )}
          </Toolbar>
        </AppBar>

        <Container id="main-content" component="main" tabIndex={-1} maxWidth="xl" sx={{ pb: 6, flexGrow: 1, px: { xs: 2, sm: 3, lg: 4 }, outline: 'none' }}>
          {user && isUnlinkedUser(user) ? (
            <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
              <Paper elevation={2} sx={{ p: 4, maxWidth: 600, width: '100%', borderRadius: 2 }}>
                <Box sx={{ textAlign: 'center', mb: 2 }}>
                  <Typography variant="h5" color="primary" gutterBottom sx={{ fontWeight: 600 }}>
                    Conta Ativa
                  </Typography>
                  <Typography variant="body1" color="text.secondary" sx={{ mt: 2, mb: 2 }}>
                    Sua conta está ativa, porém aguardando vínculo a uma pastoral ou movimento da sua comunidade por um coordenador ou administrador.
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Assim que seu vínculo for adicionado, as funcionalidades do sistema serão liberadas automaticamente.
                  </Typography>
                </Box>
              </Paper>
            </Box>
          ) : (
            children
          )}
        </Container>
      </Box>
    </Box>
  );
};

export default Layout;
