import React, { useState } from 'react';
import {
  AppBar,
  Box,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  Chip,
  Container,
  ButtonGroup,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import StorefrontIcon from '@mui/icons-material/Storefront';
import PointOfSaleIcon from '@mui/icons-material/PointOfSale';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import ShoppingBagIcon from '@mui/icons-material/ShoppingBag';
import AssignmentReturnIcon from '@mui/icons-material/AssignmentReturn';
import PeopleIcon from '@mui/icons-material/People';
import HistoryIcon from '@mui/icons-material/History';
import SecurityIcon from '@mui/icons-material/Security';
import ManageAccountsIcon from '@mui/icons-material/ManageAccounts';
import InsightsIcon from '@mui/icons-material/Insights';
import LogoutIcon from '@mui/icons-material/Logout';
import DashboardIcon from '@mui/icons-material/Dashboard';
import TranslateIcon from '@mui/icons-material/Translate';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../app/providers/AuthProvider';
import { useLanguage } from '../../app/providers/LanguageProvider';

export const MainLayout = ({ children }) => {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const { user, logout, hasRole } = useAuth();
  const { language, changeLanguage, t } = useLanguage();
  const navigate = useNavigate();
  const location = useLocation();

  const isAdmin = hasRole('ROLE_ADMINISTRADOR');

  const menuItems = [
    { text: t('dashboard'), icon: <DashboardIcon />, path: '/dashboard', show: true },
    { text: t('pos'), icon: <PointOfSaleIcon />, path: '/ventas/nueva', show: true },
    { text: t('salesHistory'), icon: <HistoryIcon />, path: '/ventas', show: true },
    { text: t('purchases'), icon: <ShoppingBagIcon />, path: '/compras/nueva', show: true },
    { text: t('purchasesHistory'), icon: <HistoryIcon />, path: '/compras', show: true },
    { text: t('inventory'), icon: <Inventory2Icon />, path: '/inventario', show: true },
    { text: t('catalog'), icon: <StorefrontIcon />, path: '/catalogo', show: true },
    { text: t('thirdParties'), icon: <PeopleIcon />, path: '/terceros', show: true },
    { text: t('returns'), icon: <AssignmentReturnIcon />, path: '/devoluciones', show: true },
    { text: t('observability'), icon: <InsightsIcon />, path: '/observabilidad', show: isAdmin },
    { text: t('audit'), icon: <SecurityIcon />, path: '/auditoria', show: isAdmin },
    { text: t('users'), icon: <ManageAccountsIcon />, path: '/usuarios', show: isAdmin },
  ];

  const handleNavigate = (path) => {
    navigate(path);
    setDrawerOpen(false);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="sticky" elevation={2}>
        <Toolbar>
          <IconButton
            color="inherit"
            edge="start"
            onClick={() => setDrawerOpen(true)}
            sx={{ mr: 2 }}
          >
            <MenuIcon />
          </IconButton>

          <StorefrontIcon sx={{ mr: 1 }} />
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 700 }}>
            {t('appName')}
          </Typography>

          {/* Selector de idioma bilingüe i18n (Área B) */}
          <Box sx={{ display: 'flex', alignItems: 'center', mr: 2 }}>
            <TranslateIcon fontSize="small" sx={{ mr: 0.5, opacity: 0.8 }} />
            <ButtonGroup size="small" variant="outlined" sx={{ bgcolor: 'rgba(255,255,255,0.1)' }}>
              <Button
                onClick={() => changeLanguage('es')}
                variant={language === 'es' ? 'contained' : 'outlined'}
                sx={{ color: '#fff', borderColor: 'rgba(255,255,255,0.4)', px: 1, minWidth: 32 }}
              >
                ES
              </Button>
              <Button
                onClick={() => changeLanguage('en')}
                variant={language === 'en' ? 'contained' : 'outlined'}
                sx={{ color: '#fff', borderColor: 'rgba(255,255,255,0.4)', px: 1, minWidth: 32 }}
              >
                EN
              </Button>
            </ButtonGroup>
          </Box>

          {user && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Chip
                label={isAdmin ? t('admin') : t('operative')}
                color={isAdmin ? 'secondary' : 'default'}
                size="small"
                sx={{ fontWeight: 'bold' }}
              />
              <Typography variant="body2" sx={{ display: { xs: 'none', md: 'block' } }}>
                {user.nombreCompleto}
              </Typography>
              <Button
                color="inherit"
                startIcon={<LogoutIcon />}
                onClick={logout}
                size="small"
              >
                {t('logout')}
              </Button>
            </Box>
          )}
        </Toolbar>
      </AppBar>

      <Drawer
        anchor="left"
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
      >
        <Box sx={{ width: 280 }} role="presentation">
          <Box sx={{ p: 2, bgcolor: 'primary.main', color: 'primary.contrastText' }}>
            <Typography variant="subtitle1" fontWeight="bold">
              {t('menuTitle')}
            </Typography>
            <Typography variant="caption">
              {t('systemSubtitle')} v1.0
            </Typography>
          </Box>
          <Divider />
          <List>
            {menuItems
              .filter((item) => item.show)
              .map((item) => (
                <ListItem key={item.text} disablePadding>
                  <ListItemButton
                    selected={location.pathname === item.path}
                    onClick={() => handleNavigate(item.path)}
                  >
                    <ListItemIcon sx={{ color: location.pathname === item.path ? 'primary.main' : 'inherit' }}>
                      {item.icon}
                    </ListItemIcon>
                    <ListItemText primary={item.text} />
                  </ListItemButton>
                </ListItem>
              ))}
          </List>
        </Box>
      </Drawer>

      <Container maxWidth="xl" sx={{ flexGrow: 1, py: 3 }}>
        {children}
      </Container>

      <Box component="footer" sx={{ py: 2, textAlign: 'center', bgcolor: 'background.paper', borderTop: '1px solid #e2e8f0' }}>
        <Typography variant="caption" color="text.secondary">
          Sistema de Inventario para Ferretería — Moneda: COP — Demostración Académica Sede Única — CI/CD Verificado
        </Typography>
      </Box>
    </Box>
  );
};
