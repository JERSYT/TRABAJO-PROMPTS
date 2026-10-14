import React, { useState, useEffect } from 'react';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Button,
  Stack,
  Alert,
  CircularProgress,
} from '@mui/material';
import PointOfSaleIcon from '@mui/icons-material/PointOfSale';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import ShoppingBagIcon from '@mui/icons-material/ShoppingBag';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import StorefrontIcon from '@mui/icons-material/Storefront';
import PeopleIcon from '@mui/icons-material/People';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../app/providers/AuthProvider';
import apiClient from '../../services/api/apiClient';
import { formatCurrency, formatQuantity } from '../../utils/formatters';

export const DashboardPage = () => {
  const { user, hasRole } = useAuth();
  const navigate = useNavigate();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');

  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalReferencias: 0,
    articulosBajoStock: 0,
    totalExistencias: 0,
  });

  useEffect(() => {
    const fetchDashboardStats = async () => {
      try {
        setLoading(true);
        const [stocksRes, lowStocksRes] = await Promise.all([
          apiClient.get('/inventory/stocks?size=100'),
          apiClient.get('/inventory/stocks?bajoStock=true&size=100'),
        ]);

        const allStocks = stocksRes.data.content || [];
        const lowStocks = lowStocksRes.data.content || [];

        const totalQty = allStocks.reduce((sum, item) => sum + Number(item.cantidadDisponible || 0), 0);

        setStats({
          totalReferencias: stocksRes.data.totalElements || allStocks.length,
          articulosBajoStock: lowStocksRes.data.totalElements || lowStocks.length,
          totalExistencias: totalQty,
        });
      } catch (err) {
        console.error('Error cargando métricas:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, []);

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h4" component="h1" fontWeight="bold">
            Panel de Control
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Bienvenido, {user?.nombreCompleto} ({isAdmin ? 'Administrador' : 'Usuario Operativo'})
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="secondary"
          size="large"
          startIcon={<PointOfSaleIcon />}
          onClick={() => navigate('/ventas/nueva')}
          sx={{ py: 1.2, px: 3, fontWeight: 'bold' }}
        >
          Nueva Venta de Mostrador
        </Button>
      </Box>

      {stats.articulosBajoStock > 0 && (
        <Alert
          severity="warning"
          icon={<WarningAmberIcon />}
          sx={{ mb: 3 }}
          action={
            <Button color="inherit" size="small" onClick={() => navigate('/inventario?bajoStock=true')}>
              Ver Artículos
            </Button>
          }
        >
          Hay <strong>{stats.articulosBajoStock} artículos</strong> con existencias bajas (5 unidades o menos).
        </Alert>
      )}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={4}>
            <Card elevation={2} sx={{ borderRadius: 2 }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: 'primary.light', color: 'primary.contrastText', mr: 2 }}>
                  <StorefrontIcon fontSize="large" />
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Referencias en Catálogo
                  </Typography>
                  <Typography variant="h5" fontWeight="bold">
                    {stats.totalReferencias}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={4}>
            <Card elevation={2} sx={{ borderRadius: 2 }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: 'success.light', color: 'success.contrastText', mr: 2 }}>
                  <Inventory2Icon fontSize="large" />
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Total Unidades en Stock
                  </Typography>
                  <Typography variant="h5" fontWeight="bold">
                    {formatQuantity(stats.totalExistencias)}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={4}>
            <Card elevation={2} sx={{ borderRadius: 2 }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center' }}>
                <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: 'warning.light', color: 'warning.contrastText', mr: 2 }}>
                  <WarningAmberIcon fontSize="large" />
                </Box>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Artículos en Nivel Crítico
                  </Typography>
                  <Typography variant="h5" fontWeight="bold" color="error.main">
                    {stats.articulosBajoStock}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      <Typography variant="h6" fontWeight="bold" sx={{ mb: 2 }}>
        Accesos Rápidos de Operación
      </Typography>

      <Grid container spacing={2}>
        <Grid item xs={12} sm={6} md={3}>
          <Card
            elevation={1}
            sx={{ p: 2, cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
            onClick={() => navigate('/ventas/nueva')}
          >
            <Stack direction="row" spacing={2} alignItems="center">
              <PointOfSaleIcon color="primary" fontSize="large" />
              <Box>
                <Typography variant="subtitle1" fontWeight="bold">Facturación</Typography>
                <Typography variant="caption" color="text.secondary">Cobro en efectivo y despacho</Typography>
              </Box>
            </Stack>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            elevation={1}
            sx={{ p: 2, cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
            onClick={() => navigate('/compras/nueva')}
          >
            <Stack direction="row" spacing={2} alignItems="center">
              <ShoppingBagIcon color="primary" fontSize="large" />
              <Box>
                <Typography variant="subtitle1" fontWeight="bold">Nueva Compra</Typography>
                <Typography variant="caption" color="text.secondary">Recepción y Costo Promedio</Typography>
              </Box>
            </Stack>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            elevation={1}
            sx={{ p: 2, cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
            onClick={() => navigate('/inventario')}
          >
            <Stack direction="row" spacing={2} alignItems="center">
              <Inventory2Icon color="primary" fontSize="large" />
              <Box>
                <Typography variant="subtitle1" fontWeight="bold">Inventario / Kárdex</Typography>
                <Typography variant="caption" color="text.secondary">Saldos y movimientos</Typography>
              </Box>
            </Stack>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            elevation={1}
            sx={{ p: 2, cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
            onClick={() => navigate('/terceros')}
          >
            <Stack direction="row" spacing={2} alignItems="center">
              <PeopleIcon color="primary" fontSize="large" />
              <Box>
                <Typography variant="subtitle1" fontWeight="bold">Terceros</Typography>
                <Typography variant="caption" color="text.secondary">Clientes y Proveedores</Typography>
              </Box>
            </Stack>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
