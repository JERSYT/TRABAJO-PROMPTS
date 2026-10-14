import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  Alert,
  Stack,
  Tooltip,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import PrintIcon from '@mui/icons-material/Print';
import CancelIcon from '@mui/icons-material/Cancel';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../services/api/apiClient';
import { formatCurrency, formatQuantity, formatDateTime } from '../../utils/formatters';
import { useAuth } from '../../app/providers/AuthProvider';

export const SaleHistoryPage = () => {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');
  const navigate = useNavigate();

  const [ventas, setVentas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Modales
  const [selectedVenta, setSelectedVenta] = useState(null);
  const [openDetailModal, setOpenDetailModal] = useState(false);
  const [openCancelModal, setOpenCancelModal] = useState(false);
  const [motivoCancelacion, setMotivoCancelacion] = useState('');

  const fetchVentas = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get('/sales?size=50');
      setVentas(response.data.content || []);
    } catch (err) {
      setError('Error al consultar historial de ventas');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVentas();
  }, []);

  const handleOpenDetail = (venta) => {
    setSelectedVenta(venta);
    setOpenDetailModal(true);
  };

  const handleOpenCancel = (venta) => {
    setSelectedVenta(venta);
    setMotivoCancelacion('');
    setOpenCancelModal(true);
  };

  const handleConfirmCancel = async () => {
    if (!motivoCancelacion || motivoCancelacion.trim().length < 5) {
      setError('El motivo de cancelación debe tener al menos 5 caracteres');
      return;
    }

    try {
      await apiClient.post(`/sales/${selectedVenta.id}/cancel`, {
        motivo: motivoCancelacion,
      });
      setSuccessMsg(`Venta ${selectedVenta.consecutivo} cancelada exitosamente y stock reincorporado`);
      setOpenCancelModal(false);
      fetchVentas();
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al cancelar la venta');
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Historial de Ventas de Mostrador
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Consulta de comprobantes emitidos, líneas con costos históricos congelados y anulación autorizada
          </Typography>
        </Box>
        <Button variant="contained" color="secondary" onClick={() => navigate('/ventas/nueva')}>
          Nueva Venta
        </Button>
      </Box>

      {successMsg && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMsg(null)}>{successMsg}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} elevation={2}>
          <Table>
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                <TableCell><strong>Consecutivo</strong></TableCell>
                <TableCell><strong>Fecha / Hora</strong></TableCell>
                <TableCell><strong>Cliente</strong></TableCell>
                <TableCell align="right"><strong>Total (COP)</strong></TableCell>
                <TableCell align="center"><strong>Estado</strong></TableCell>
                <TableCell><strong>Vendedor</strong></TableCell>
                <TableCell align="center"><strong>Acciones</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {ventas.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                    No se registran ventas en el sistema.
                  </TableCell>
                </TableRow>
              ) : (
                ventas.map((v) => (
                  <TableRow key={v.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                      {v.consecutivo}
                    </TableCell>
                    <TableCell>{formatDateTime(v.fechaHora)}</TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium">{v.clienteNombre}</Typography>
                      <Typography variant="caption" color="text.secondary">{v.clienteDocumento}</Typography>
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                      {formatCurrency(v.total)}
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        label={v.estado}
                        color={v.estado === 'COMPLETADA' ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{v.usuarioNombre}</TableCell>
                    <TableCell align="center">
                      <Stack direction="row" spacing={1} justifyContent="center">
                        <Tooltip title="Ver Detalle y Costo Histórico">
                          <IconButton size="small" color="primary" onClick={() => handleOpenDetail(v)}>
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Imprimir Comprobante">
                          <IconButton
                            size="small"
                            color="info"
                            onClick={() => navigate(`/comprobantes/VENTA/${v.consecutivo}`)}
                          >
                            <PrintIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {isAdmin && v.estado === 'COMPLETADA' && (
                          <Tooltip title="Cancelar Venta (Reversión de Stock)">
                            <IconButton size="small" color="error" onClick={() => handleOpenCancel(v)}>
                              <CancelIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Modal Detalle de Venta */}
      <Dialog open={openDetailModal} onClose={() => setOpenDetailModal(false)} maxWidth="md" fullWidth>
        <DialogTitle>Detalle de Venta: {selectedVenta?.consecutivo}</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2"><strong>Cliente:</strong> {selectedVenta?.clienteNombre} ({selectedVenta?.clienteDocumento})</Typography>
            <Typography variant="body2"><strong>Fecha:</strong> {formatDateTime(selectedVenta?.fechaHora)}</Typography>
            <Typography variant="body2"><strong>Estado:</strong> {selectedVenta?.estado}</Typography>
            {selectedVenta?.motivoCancelacion && (
              <Alert severity="error" sx={{ mt: 1 }}>
                Motivo de Cancelación: {selectedVenta.motivoCancelacion}
              </Alert>
            )}
          </Box>

          <TableContainer>
            <Table size="small">
              <TableHead sx={{ bgcolor: 'action.hover' }}>
                <TableRow>
                  <TableCell>Cód. Barras</TableCell>
                  <TableCell>Producto</TableCell>
                  <TableCell align="center">Cantidad</TableCell>
                  <TableCell align="right">Precio Unit.</TableCell>
                  {isAdmin && <TableCell align="right">Costo Histórico Unit. (RN-04)</TableCell>}
                  <TableCell align="right">Subtotal</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {selectedVenta?.lineas?.map((l) => (
                  <TableRow key={l.id}>
                    <TableCell sx={{ fontFamily: 'monospace' }}>{l.codigoBarras}</TableCell>
                    <TableCell>{l.productoNombre}</TableCell>
                    <TableCell align="center">{formatQuantity(l.cantidad)}</TableCell>
                    <TableCell align="right">{formatCurrency(l.precioUnitario)}</TableCell>
                    {isAdmin && (
                      <TableCell align="right" sx={{ color: 'text.secondary' }}>
                        {formatCurrency(l.costoHistoricoUnitario, 2)}
                      </TableCell>
                    )}
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(l.subtotal)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Box sx={{ mt: 2, textAlign: 'right' }}>
            <Typography variant="h6" fontWeight="bold">Total: {formatCurrency(selectedVenta?.total)}</Typography>
            <Typography variant="body2" color="text.secondary">Efectivo Recibido: {formatCurrency(selectedVenta?.montoPagadoEfectivo)}</Typography>
            <Typography variant="body2" color="text.secondary">Cambio Devuelto: {formatCurrency(selectedVenta?.cambioDevuelto)}</Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDetailModal(false)}>Cerrar</Button>
          <Button
            variant="contained"
            startIcon={<PrintIcon />}
            onClick={() => navigate(`/comprobantes/VENTA/${selectedVenta?.consecutivo}`)}
          >
            Imprimir
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal Cancelar Venta */}
      <Dialog open={openCancelModal} onClose={() => setOpenCancelModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Cancelar Venta {selectedVenta?.consecutivo}</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            Esta operación anulará el comprobante y reincorporará las existencias al inventario.
          </Alert>
          <TextField
            fullWidth
            label="Motivo Obligatorio de Cancelación"
            multiline
            rows={3}
            value={motivoCancelacion}
            onChange={(e) => setMotivoCancelacion(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenCancelModal(false)}>Volver</Button>
          <Button variant="contained" color="error" onClick={handleConfirmCancel}>
            Confirmar Anulación
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
