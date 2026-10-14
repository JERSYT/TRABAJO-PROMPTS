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

export const PurchaseHistoryPage = () => {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');
  const navigate = useNavigate();

  const [compras, setCompras] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Modales
  const [selectedCompra, setSelectedCompra] = useState(null);
  const [openDetailModal, setOpenDetailModal] = useState(false);
  const [openCancelModal, setOpenCancelModal] = useState(false);
  const [motivoCancelacion, setMotivoCancelacion] = useState('');

  const fetchCompras = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get('/purchases?size=50');
      setCompras(response.data.content || []);
    } catch (err) {
      setError('Error al consultar historial de compras');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCompras();
  }, []);

  const handleOpenDetail = (compra) => {
    setSelectedCompra(compra);
    setOpenDetailModal(true);
  };

  const handleOpenCancel = (compra) => {
    setSelectedCompra(compra);
    setMotivoCancelacion('');
    setOpenCancelModal(true);
  };

  const handleConfirmCancel = async () => {
    if (!motivoCancelacion || motivoCancelacion.trim().length < 5) {
      setError('El motivo de cancelación debe tener al menos 5 caracteres');
      return;
    }

    try {
      await apiClient.post(`/purchases/${selectedCompra.id}/cancel`, {
        motivo: motivoCancelacion,
      });
      setSuccessMsg(`Compra ${selectedCompra.consecutivo} cancelada exitosamente y stock revertido`);
      setOpenCancelModal(false);
      fetchCompras();
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al cancelar la compra. Verifique existencias.');
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Historial de Compras y Recepción de Mercancía
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Consulta de compras recibidas, líneas de abastecimiento y costos unitarios de adquisición
          </Typography>
        </Box>
        <Button variant="contained" onClick={() => navigate('/compras/nueva')}>
          Nueva Compra
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
                <TableCell><strong>Proveedor</strong></TableCell>
                <TableCell align="right"><strong>Total (COP)</strong></TableCell>
                <TableCell align="center"><strong>Estado</strong></TableCell>
                <TableCell><strong>Responsable</strong></TableCell>
                <TableCell align="center"><strong>Acciones</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {compras.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                    No se registran compras en el sistema.
                  </TableCell>
                </TableRow>
              ) : (
                compras.map((c) => (
                  <TableRow key={c.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                      {c.consecutivo}
                    </TableCell>
                    <TableCell>{formatDateTime(c.fechaHora)}</TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium">{c.proveedorRazonSocial}</Typography>
                      <Typography variant="caption" color="text.secondary">{c.proveedorDocumento}</Typography>
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                      {formatCurrency(c.total)}
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        label={c.estado}
                        color={c.estado === 'RECIBIDA' ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{c.usuarioNombre}</TableCell>
                    <TableCell align="center">
                      <Stack direction="row" spacing={1} justifyContent="center">
                        <Tooltip title="Ver Detalle de Compra">
                          <IconButton size="small" color="primary" onClick={() => handleOpenDetail(c)}>
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Imprimir Comprobante">
                          <IconButton
                            size="small"
                            color="info"
                            onClick={() => navigate(`/comprobantes/COMPRA/${c.consecutivo}`)}
                          >
                            <PrintIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {isAdmin && c.estado === 'RECIBIDA' && (
                          <Tooltip title="Cancelar Compra (Reversión de Stock)">
                            <IconButton size="small" color="error" onClick={() => handleOpenCancel(c)}>
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

      {/* Modal Detalle de Compra */}
      <Dialog open={openDetailModal} onClose={() => setOpenDetailModal(false)} maxWidth="md" fullWidth>
        <DialogTitle>Detalle de Compra: {selectedCompra?.consecutivo}</DialogTitle>
        <DialogContent dividers>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2"><strong>Proveedor:</strong> {selectedCompra?.proveedorRazonSocial} ({selectedCompra?.proveedorDocumento})</Typography>
            <Typography variant="body2"><strong>Fecha de Recepción:</strong> {formatDateTime(selectedCompra?.fechaHora)}</Typography>
            <Typography variant="body2"><strong>Estado:</strong> {selectedCompra?.estado}</Typography>
            {selectedCompra?.motivoCancelacion && (
              <Alert severity="error" sx={{ mt: 1 }}>
                Motivo de Cancelación: {selectedCompra.motivoCancelacion}
              </Alert>
            )}
          </Box>

          <TableContainer>
            <Table size="small">
              <TableHead sx={{ bgcolor: 'action.hover' }}>
                <TableRow>
                  <TableCell>Cód. Barras</TableCell>
                  <TableCell>Producto Recibido</TableCell>
                  <TableCell align="center">Cantidad</TableCell>
                  <TableCell align="right">Costo Unit. Compra</TableCell>
                  <TableCell align="right">Subtotal</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {selectedCompra?.lineas?.map((l) => (
                  <TableRow key={l.id}>
                    <TableCell sx={{ fontFamily: 'monospace' }}>{l.codigoBarras}</TableCell>
                    <TableCell>{l.productoNombre}</TableCell>
                    <TableCell align="center">{formatQuantity(l.cantidad)}</TableCell>
                    <TableCell align="right">{formatCurrency(l.costoUnitario, 2)}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(l.subtotal)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Box sx={{ mt: 2, textAlign: 'right' }}>
            <Typography variant="h6" fontWeight="bold">Total Compra: {formatCurrency(selectedCompra?.total)}</Typography>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDetailModal(false)}>Cerrar</Button>
          <Button
            variant="contained"
            startIcon={<PrintIcon />}
            onClick={() => navigate(`/comprobantes/COMPRA/${selectedCompra?.consecutivo}`)}
          >
            Imprimir
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal Cancelar Compra */}
      <Dialog open={openCancelModal} onClose={() => setOpenCancelModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Cancelar Compra {selectedCompra?.consecutivo}</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            Esta operación verificará que las existencias no hayan sido consumidas o vendidas para revertir el stock.
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
            Confirmar Reversión
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
