import React from 'react';
import {
  alpha,
  AppBar,
  Avatar,
  Box,
  Collapse,
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
import AccountTreeIcon from '@mui/icons-material/AccountTree';
import AssessmentIcon from '@mui/icons-material/Assessment';
import AssignmentIcon from '@mui/icons-material/Assignment';
import CalendarIcon from '@mui/icons-material/CalendarMonth';
import ChurchIcon from '@mui/icons-material/Church';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ReservasIcon from '@mui/icons-material/EventAvailable';
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import FactCheckIcon from '@mui/icons-material/FactCheck';
import GroupsIcon from '@mui/icons-material/Groups';
import InventoryIcon from '@mui/icons-material/Inventory';
import LocationIcon from '@mui/icons-material/LocationOn';
import LogoutIcon from '@mui/icons-material/Logout';
import ManageAccountsIcon from '@mui/icons-material/ManageAccounts';
import MenuIcon from '@mui/icons-material/Menu';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import AutoStoriesIcon from '@mui/icons-material/AutoStories';
import NotificationsIcon from '@mui/icons-material/Notifications';
import PeopleIcon from '@mui/icons-material/People';
import SettingsIcon from '@mui/icons-material/Settings';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { canApprove, canManageAnoParoquial, canManageLocais, canManageOrganizacoes, canManageUnidadesTerritoriais, canViewSacramentos, isUnlinkedUser } from '@/contextos/iam/presentation/auth/permissions';
import { useThemeMode } from '@/shared/presentation/context/ThemeModeContext';
import ActionButton from '@/shared/presentation/components/ActionButton';
import IconActionButton from './IconActionButton';

interface NavigationItem {
  label: string;
  path: string;
  icon: React.ReactElement;
}

interface NavigationCategory {
  title: string;
  items: NavigationItem[];
}

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const { mode, toggleMode } = useThemeMode();
  const theme = useTheme();
  const isMdUp = useMediaQuery(theme.breakpoints.up('md'));
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const [drawerOpen, setDrawerOpen] = React.useState(true);
  const [openCategories, setOpenCategories] = React.useState<Record<string, boolean>>({
    'Gestão pastoral': true,
    'Administração': true,
    'Análises': true,
  });

  const toggleCategory = (title: string) => {
    setOpenCategories((prev) => ({
      ...prev,
      [title]: prev[title] !== undefined ? !prev[title] : false,
    }));
  };

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

  const dashboardItem: NavigationItem = {
    label: 'Painel',
    path: '/painel',
    icon: <DashboardIcon sx={{ fontSize: '1.25rem' }} />,
  };

  const pastoralItems: NavigationItem[] = [
    { label: 'Calendário', path: '/calendario', icon: <CalendarIcon sx={{ fontSize: '1.25rem' }} /> },
    { label: 'Clero', path: '/clerigos', icon: <ChurchIcon sx={{ fontSize: '1.25rem' }} /> },
    { label: 'Conselhos & Atas', path: '/conselhos', icon: <MenuBookIcon sx={{ fontSize: '1.25rem' }} /> },
    { label: 'Fiéis', path: '/fieis', icon: <PeopleIcon sx={{ fontSize: '1.25rem' }} /> },
    { label: 'Notificações', path: '/notificacoes', icon: <NotificationsIcon sx={{ fontSize: '1.25rem' }} /> },
    ...(canManageOrganizacoes(user)
      ? [{ label: 'Pastorais e Movimentos', path: '/organizacoes', icon: <GroupsIcon sx={{ fontSize: '1.25rem' }} /> }]
      : []),
    { label: 'Projetos', path: '/projetos', icon: <AssignmentIcon sx={{ fontSize: '1.25rem' }} /> },
    { label: 'Reservas', path: '/reservas', icon: <ReservasIcon sx={{ fontSize: '1.25rem' }} /> },
    ...(canViewSacramentos(user)
      ? [{ label: 'Sacramentos', path: '/sacramentos', icon: <AutoStoriesIcon sx={{ fontSize: '1.25rem' }} /> }]
      : []),
  ].sort((a, b) => a.label.localeCompare(b.label, 'pt-BR'));

  const administrativeItems: NavigationItem[] = [
    ...(canApprove(user)
      ? [
          { label: 'Ajustes cadastrais', path: '/ajustes-cadastrais', icon: <ManageAccountsIcon sx={{ fontSize: '1.25rem' }} /> },
          { label: 'Aprovações', path: '/aprovacoes', icon: <FactCheckIcon sx={{ fontSize: '1.25rem' }} /> },
        ]
      : []),
    ...(canManageAnoParoquial(user) || canManageOrganizacoes(user)
      ? [{ label: 'Configurações', path: '/configuracoes', icon: <SettingsIcon sx={{ fontSize: '1.25rem' }} /> }]
      : []),
    ...(canManageLocais(user)
      ? [{ label: 'Locais', path: '/locais', icon: <LocationIcon sx={{ fontSize: '1.25rem' }} /> }]
      : []),
    ...(canManageUnidadesTerritoriais(user)
      ? [{ label: 'Governo & Território', path: '/governo-territorio', icon: <ChurchIcon sx={{ fontSize: '1.25rem' }} /> }]
      : []),
    { label: 'Organograma', path: '/organograma', icon: <AccountTreeIcon sx={{ fontSize: '1.25rem' }} /> },
    { label: 'Patrimônio', path: '/ativos', icon: <InventoryIcon sx={{ fontSize: '1.25rem' }} /> },
  ].sort((a, b) => a.label.localeCompare(b.label, 'pt-BR'));

  const analisesItems: NavigationItem[] = [
    { label: 'Relatórios', path: '/relatorios', icon: <AssessmentIcon sx={{ fontSize: '1.25rem' }} /> },
  ].sort((a, b) => a.label.localeCompare(b.label, 'pt-BR'));

  const categories: NavigationCategory[] = React.useMemo(() => [
    { title: 'Gestão pastoral', items: pastoralItems },
    { title: 'Administração', items: administrativeItems },
    { title: 'Análises', items: analisesItems },
  ].filter((cat) => cat.items.length > 0), [pastoralItems, administrativeItems, analisesItems]);

  React.useEffect(() => {
    for (const cat of categories) {
      if (cat.items.some((item) => item.path === location.pathname)) {
        setOpenCategories((prev) => {
          if (prev[cat.title] === false) {
            return { ...prev, [cat.title]: true };
          }
          return prev;
        });
      }
    }
  }, [location.pathname, categories]);

  const drawerWidth = isMdUp ? (drawerOpen ? 240 : 72) : 240;

  const renderNavigationItem = (item: NavigationItem) => {
    const isSelected = location.pathname === item.path;
    const button = (
      <ListItemButton
        onClick={() => {
          navigate(item.path);
          setMobileOpen(false);
        }}
        selected={isSelected}
        sx={{
          mb: 0.25,
          minHeight: 36,
          justifyContent: drawerOpen || !isMdUp ? 'initial' : 'center',
          px: 1.5,
          py: 0.5,
          borderRadius: '8px',
          '&.Mui-selected': {
            bgcolor: theme.custom?.navigationSelectedBg ?? alpha(theme.palette.primary.main, 0.10),
            color: 'primary.main',
            '&:hover': {
              bgcolor: alpha(theme.palette.primary.main, 0.14),
            },
            '& .MuiListItemIcon-root': {
              color: 'primary.main',
            },
          },
        }}
      >
        <ListItemIcon
          sx={{
            minWidth: 0,
            mr: drawerOpen || !isMdUp ? 1.5 : 'auto',
            justifyContent: 'center',
            color: isSelected ? 'primary.main' : 'text.secondary',
          }}
        >
          {item.icon}
        </ListItemIcon>
        {(drawerOpen || !isMdUp) && (
          <ListItemText
            primary={
              <Typography
                sx={{
                  fontWeight: isSelected ? 600 : 500,
                  fontSize: '0.8125rem',
                  lineHeight: 1.3,
                }}
              >
                {item.label}
              </Typography>
            }
          />
        )}
      </ListItemButton>
    );

    return !drawerOpen && isMdUp ? (
      <Tooltip key={item.path} title={item.label} placement="right">
        <ListItem disablePadding sx={{ display: 'block' }}>
          {button}
        </ListItem>
      </Tooltip>
    ) : (
      <ListItem key={item.path} disablePadding sx={{ display: 'block' }}>
        {button}
      </ListItem>
    );
  };

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

      <List component="nav" sx={{ px: 1, py: 1.5, flexGrow: 1, overflowY: 'auto' }} aria-label="Navegação principal">
        {/* Item raiz Painel */}
        {renderNavigationItem(dashboardItem)}

        {/* Categorias colapsáveis */}
        {drawerOpen || !isMdUp
          ? categories.map((category) => {
              const isExpanded = openCategories[category.title] ?? true;

              return (
                <Box key={category.title} sx={{ mt: 1 }}>
                  <ListItemButton
                    onClick={() => toggleCategory(category.title)}
                    sx={{
                      py: 0.5,
                      px: 1.5,
                      borderRadius: '8px',
                      color: 'text.secondary',
                      minHeight: 28,
                      '&:hover': {
                        bgcolor: alpha(theme.palette.primary.main, 0.04),
                        color: 'primary.main',
                        '& .MuiSvgIcon-root': { color: 'primary.main' },
                      },
                    }}
                  >
                    <ListItemText
                      primary={
                        <Typography
                          variant="caption"
                          sx={{
                            fontWeight: 700,
                            fontSize: '0.6875rem',
                            textTransform: 'uppercase',
                            letterSpacing: '0.06em',
                            lineHeight: '20px',
                            color: 'inherit',
                          }}
                        >
                          {category.title}
                        </Typography>
                      }
                    />
                    {isExpanded ? (
                      <ExpandLess sx={{ fontSize: '1rem', color: 'inherit' }} />
                    ) : (
                      <ExpandMore sx={{ fontSize: '1rem', color: 'inherit' }} />
                    )}
                  </ListItemButton>

                  <Collapse in={isExpanded} timeout="auto" unmountOnExit={false}>
                    <List component="div" disablePadding sx={{ pl: 0 }}>
                      {category.items.map((item) => renderNavigationItem(item))}
                    </List>
                  </Collapse>
                </Box>
              );
            })
          : categories.map((category, idx) => (
              <React.Fragment key={category.title}>
                <Divider sx={{ my: 1 }} />
                {category.items.map((item) => renderNavigationItem(item))}
              </React.Fragment>
            ))}
      </List>
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
