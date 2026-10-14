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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Autocomplete,
  Tooltip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PrintIcon from '@mui/icons-material/Print';
import AssignmentReturnIcon from '@mui/icons-material/AssignmentReturn';
import { useNavigate } from 'react-router-dom';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import apiClient from '../../services/api/apiClient';
import { formatCurrency, formatQuantity, formatDateTime } from '../../utils/formatters';

export const ReturnsPage = () => {
  const navigate = useNavigate();

  const [devoluciones, setDevoluciones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Estados para modal de nueva devolución
  const [openModal, setOpenModal] = useState(false);
  const [tipoDevolucion, setTipoDevolucion] = useState('NO_VINCULADA');
  const [referencias, setReferencias] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [ventas, setVentas] = useState([]);

  const [selectedRef, setSelectedRef] = useState(null);
  const [selectedCli, setSelectedCli] = useState(null);
  const [selectedVenta, setSelectedVenta] = useState(null);
  const [cantidadDevuelta, setCantidadDevuelta] = useState(1);
  const [motivo, setMotivo] = useState('');

  const fetchDevoluciones = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get('/returns?size=50');
      setDevoluciones(response.data.content || []);
    } catch (err) {
      setError('Error al consultar devoluciones');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDevoluciones();
  }, []);

  const handleOpenCreate = async () => {
    try {
      const [refRes, cliRes, vtaRes] = await Promise.all([
        apiClient.get('/catalog/references?activo=true&size=100'),
        apiClient.get('/third-parties?tipo=CLIENTE&size=100'),
        apiClient.get('/sales?estado=COMPLETADA&size=50'),
      ]);
      setReferencias(refRes.data.content || []);
      setClientes(cliRes.data.content || []);
      setVentas(vtaRes.data.content || []);
      setOpenModal(true);
    } catch (err) {
      setError('Error al preparar formulario de devolución');
    }
  };

  const handleConfirmarDevolucion = async () => {
    if (!selectedRef) {
      setError('Debe seleccionar un producto');
      return;
    }
    if (Number(cantidadDevuelta) <= 0) {
      setError('La cantidad devuelta debe ser mayor a cero');
      return;
    }
    if (!motivo || motivo.trim().length < 5) {
      setError('Debe ingresar un motivo de al menos 5 caracteres');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const payload = {
        tipoDevolucion,
        ventaId: tipoDevolucion === 'VINCULADA' ? selectedVenta?.id : null,
        clienteId: selectedCli?.id || null,
        motivo,
        lineas: [
          {
            referenciaId: selectedRef.id,
            cantidad: Number(cantidadDevuelta),
          },
        ],
      };

      const response = await apiClient.post('/returns', payload);
      setSuccessMsg(`Devolución ${response.data.consecutivo} registrada. Las existencias fueron reincorporadas.`);
      setOpenModal(false);
      setSelectedRef(null);
      setSelectedVenta(null);
      setMotivo('');
      fetchDevoluciones();
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al procesar la devolución');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Devoluciones de Mercancía de Clientes
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Reincorporación automática de mercancía al stock disponible valorada a costo histórico o CPP (RN-07)
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
          Nueva Devolución
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
                <TableCell><strong>Tipo</strong></TableCell>
                <TableCell><strong>Venta Original</strong></TableCell>
                <TableCell><strong>Cliente</strong></TableCell>
                <TableCell><strong>Motivo</strong></TableCell>
                <TableCell align="center"><strong>Estado</strong></TableCell>
                <TableCell align="center"><strong>Imprimir</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {devoluciones.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                    No se registran devoluciones de clientes en el sistema.
                  </TableCell>
                </TableRow>
              ) : (
                devoluciones.map((d) => (
                  <TableRow key={d.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                      {d.consecutivo}
                    </TableCell>
                    <TableCell>{formatDateTime(d.fechaHora)}</TableCell>
                    <TableCell>
                      <Chip
                        label={d.tipoDevolucion}
                        size="small"
                        color={d.tipoDevolucion === 'VINCULADA' ? 'primary' : 'default'}
                      />
                    </TableCell>
                    <TableCell>{d.ventaConsecutivo || 'N/A'}</TableCell>
                    <TableCell>{d.clienteNombre || 'Sin asociar'}</TableCell>
                    <TableCell sx={{ maxWidth: 200, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {d.motivo}
                    </TableCell>
                    <TableCell align="center">
                      <Chip label={d.estado} color="success" size="small" />
                    </TableCell>
                    <TableCell align="center">
                      <Tooltip title="Imprimir Comprobante de Devolución">
                        <IconButton
                          size="small"
                          color="info"
                          onClick={() => navigate(`/comprobantes/DEVOLUCION/${d.consecutivo}`)}
                        >
                          <PrintIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Modal Registrar Devolución */}
      <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Registrar Devolución de Cliente</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <FormControl fullWidth size="small">
              <InputLabel>Modalidad de Devolución</InputLabel>
              <Select
                value={tipoDevolucion}
                label="Modalidad de Devolución"
                onChange={(e) => setTipoDevolucion(e.target.value)}
              >
                <MenuItem value="NO_VINCULADA">No Vinculada (Reingresa a Costo Promedio Vigente)</MenuItem>
                <MenuItem value="VINCULADA">Vinculada a Venta Original (Reingresa a Costo Histórico)</MenuItem>
              </Select>
            </FormControl>

            {tipoDevolucion === 'VINCULADA' && (
              <Autocomplete
                options={ventas}
                getOptionLabel={(option) => `${option.consecutivo} — ${option.clienteNombre} (${formatCurrency(option.total)})`}
                value={selectedVenta}
                onChange={(event, newValue) => setSelectedVenta(newValue)}
                renderInput={(params) => (
                  <TextField {...params} label="Buscar y Seleccionar Venta Original" size="small" fullWidth />
                )}
              />
            )}

            <Autocomplete
              options={clientes}
              getOptionLabel={(option) => `${option.numeroDocumento} — ${option.razonSocial}`}
              value={selectedCli}
              onChange={(event, newValue) => setSelectedCli(newValue)}
              renderInput={(params) => (
                <TextField {...params} label="Cliente que devuelve" size="small" fullWidth />
              )}
            />

            <Autocomplete
              options={referencias}
              getOptionLabel={(option) => `[${option.codigoBarras}] ${option.productoNombre} (${option.marcaNombre})`}
              value={selectedRef}
              onChange={(event, newValue) => setSelectedRef(newValue)}
              renderInput={(params) => (
                <TextField {...params} label="Artículo que reingresa a almacén" size="small" fullWidth />
              )}
            />

            <TextField
              fullWidth
              size="small"
              label="Cantidad a Devolver"
              type="number"
              value={cantidadDevuelta}
              onChange={(e) => setCantidadDevuelta(e.target.value)}
              inputProps={{ min: 1 }}
            />

            <TextField
              fullWidth
              label="Motivo o Justificación Obligatoria"
              multiline
              rows={2}
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenModal(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleConfirmarDevolucion} disabled={loading}>
            Reintegrar al Inventario
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
