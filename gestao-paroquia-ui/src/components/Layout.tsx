import React from 'react';
import {
    alpha,
    AppBar,
    Box,
    Container,
    Divider,
    Drawer,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Paper,
    Toolbar,
    Tooltip,
    Typography,
    useMediaQuery,
    useTheme,
} from '@mui/material';
import {
    AccountTree as AccountTreeIcon,
    Assessment as AssessmentIcon,
    Assignment as WorkIcon,
    CalendarMonth as CalendarIcon,
    Dashboard as DashboardIcon,
    Description as DescriptionIcon,
    FactCheck as FactCheckIcon,
    Inventory as InventoryIcon,
    Logout as LogoutIcon,
    Menu as MenuIcon,
    People as PeopleIcon,
    Settings as SettingsIcon,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { canApprove, canManageAnoParoquial, canManageLocais, isUnlinkedUser } from '../auth/permissions';
import ActionButton from './ActionButton';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
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
    navigate('/login');
  };

  const menuItems = [
    { label: 'Dashboard', path: '/dashboard', icon: <DashboardIcon /> },
    { label: 'Calendário', path: '/calendar', icon: <CalendarIcon /> },
    { label: 'Fiéis', path: '/fieis', icon: <PeopleIcon /> },
    { label: 'Projetos', path: '/projects', icon: <WorkIcon /> },
    { label: 'Organograma', path: '/organograma', icon: <AccountTreeIcon /> },
    { label: 'Conselhos & Atas', path: '/conselhos', icon: <DescriptionIcon /> },
    { label: 'Ativos Patrimoniais', path: '/ativos', icon: <InventoryIcon /> },
    { label: 'Relatórios', path: '/reports', icon: <AssessmentIcon /> },
  ];

  if (canApprove(user)) {
    menuItems.push({ label: 'Aprovações', path: '/approvals', icon: <FactCheckIcon /> });
  }

  if (canManageAnoParoquial(user) || canManageLocais(user)) {
    menuItems.push({ label: 'Configurações', path: '/configuracoes', icon: <SettingsIcon /> });
  }

  const drawerWidth = isMdUp ? (drawerOpen ? 240 : 72) : 240;

  const drawerContent = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: 'background.paper' }}>
      <Box sx={{ 
        p: 2, 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: drawerOpen || !isMdUp ? 'space-between' : 'center', 
        minHeight: 64,
        width: '100%',
        boxSizing: 'border-box'
      }}>
        {(drawerOpen || !isMdUp) ? (
          <>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, overflow: 'hidden' }}>
              <Box sx={{ bgcolor: 'background.paper', p: 0.5, display: 'flex', alignItems: 'center', justifyContent: 'center', width: 40, height: 40, flexShrink: 0 }}>
                <img 
                  src="/logo-paroquia.png" 
                  alt="Logo da Paróquia" 
                  style={{ width: '90%', height: '90%', objectFit: 'contain' }} 
                />
              </Box>
              <Box sx={{ overflow: 'hidden' }}>
                <Typography variant="subtitle1" noWrap sx={{ fontWeight: 700, color: 'primary.main', lineHeight: 1.2 }}>
                  N. S. de Fátima
                </Typography>
                <Typography variant="caption" noWrap sx={{ color: 'text.secondary', display: 'block' }}>
                  Gestão Paroquial
                </Typography>
              </Box>
            </Box>
            <ActionButton
              title={drawerOpen ? "Recolher menu lateral" : "Expandir menu lateral"}
              ariaLabel={drawerOpen ? "recolher menu" : "expandir menu"}
              icon={<MenuIcon />}
              color="primary"
              onClick={handleDrawerToggle}
              sx={{ p: 0.5 }}
            />
          </>
        ) : (
          <ActionButton
            title={drawerOpen ? "Recolher menu lateral" : "Expandir menu lateral"}
            ariaLabel={drawerOpen ? "recolher menu" : "expandir menu"}
            icon={<MenuIcon />}
            color="primary"
            onClick={handleDrawerToggle}
          />
        )}
      </Box>
      <Divider />
      
      <List sx={{ px: 1, py: 2, flexGrow: 1 }}>
        {menuItems.map((item) => {
          const isSelected = location.pathname === item.path;
          const button = (
            <ListItemButton
              onClick={() => {
                navigate(item.path);
                setMobileOpen(false);
              }}
              selected={isSelected}
              sx={{
                
                mb: 1,
                justifyContent: drawerOpen || !isMdUp ? 'initial' : 'center',
                px: 2,
                py: 1,
                '&.Mui-selected': {
                  bgcolor: (theme) => alpha(theme.palette.primary.main, 0.08),
                  color: 'primary.main',
                  '&:hover': {
                    bgcolor: (theme) => alpha(theme.palette.primary.main, 0.12),
                  },
                  '& .MuiListItemIcon-root': {
                    color: 'primary.main',
                  }
                }
              }}
            >
              <ListItemIcon sx={{ minWidth: 0, mr: drawerOpen || !isMdUp ? 3 : 'auto', justifyContent: 'center', color: isSelected ? 'primary.main' : 'text.secondary' }}>
                {item.icon}
              </ListItemIcon>
              {(drawerOpen || !isMdUp) && (
                <ListItemText 
                  primary={
                    <Typography sx={{ fontWeight: isSelected ? 700 : 500, fontSize: '0.9rem' }}>
                      {item.label}
                    </Typography>
                  } 
                />
              )}
            </ListItemButton>
          );

          if (!drawerOpen && isMdUp) {
            return (
              <Tooltip key={item.path} title={item.label} placement="right">
                <ListItem disablePadding sx={{ display: 'block' }}>
                  {button}
                </ListItem>
              </Tooltip>
            );
          }

          return (
            <ListItem key={item.path} disablePadding sx={{ display: 'block' }}>
              {button}
            </ListItem>
          );
        })}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
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
        <AppBar position="static" color="primary" sx={{ mb: 4 }}>
          <Toolbar>
            {!isMdUp && (
              <ActionButton
                title="Abrir menu de navegação"
                ariaLabel="abrir menu"
                icon={<MenuIcon />}
                color="inherit"
                onClick={handleDrawerToggle}
                sx={{ mr: 2 }}
              />
            )}
            
            <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 600, letterSpacing: '0.5px' }}>
              Nossa Senhora de Fátima
            </Typography>

            {user && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Typography variant="body2" sx={{ display: { xs: 'none', sm: 'block' }, fontWeight: 500 }}>
                  Olá, {user.name}
                </Typography>
                <ActionButton title="Sair do sistema" icon={<LogoutIcon />} color="inherit" onClick={handleLogout} />
              </Box>
            )}
          </Toolbar>
        </AppBar>

        <Container maxWidth="xl" sx={{ pb: 6, flexGrow: 1 }}>
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
