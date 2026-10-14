import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Alert,
  Stack,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tooltip,
} from '@mui/material';
import HistoryIcon from '@mui/icons-material/History';
import TuneIcon from '@mui/icons-material/Tune';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import SearchIcon from '@mui/icons-material/Search';
import WarningIcon from '@mui/icons-material/Warning';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import apiClient from '../../services/api/apiClient';
import { formatCurrency, formatQuantity, formatDateTime } from '../../utils/formatters';
import { useAuth } from '../../app/providers/AuthProvider';

export const InventoryPage = () => {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');

  const [saldos, setSaldos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [soloBajoStock, setSoloBajoStock] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Modales
  const [openKardexModal, setOpenKardexModal] = useState(false);
  const [kardexMovements, setKardexMovements] = useState([]);
  const [kardexLoading, setKardexLoading] = useState(false);
  const [selectedRef, setSelectedRef] = useState(null);

  const [openInitialModal, setOpenInitialModal] = useState(false);
  const [openAdjustmentModal, setOpenAdjustmentModal] = useState(false);

  const fetchSaldos = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = new URLSearchParams({ size: 50 });
      if (search) params.append('search', search);
      if (soloBajoStock) params.append('bajoStock', 'true');

      const response = await apiClient.get(`/inventory/stocks?${params.toString()}`);
      setSaldos(response.data.content || []);
    } catch (err) {
      setError('Error al consultar existencias de inventario');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSaldos();
  }, [search, soloBajoStock]);

  const handleOpenKardex = async (item) => {
    setSelectedRef(item);
    setOpenKardexModal(true);
    setKardexLoading(true);
    try {
      const response = await apiClient.get(`/inventory/references/${item.referenciaId}/movements?size=50`);
      setKardexMovements(response.data.content || []);
    } catch (err) {
      setError('Error al consultar Kárdex');
    } finally {
      setKardexLoading(false);
    }
  };

  // Formik Ajuste de Inventario (Solo Administrador - RN-08)
  const adjustmentFormik = useFormik({
    initialValues: {
      tipoAjuste: 'INCREMENTO',
      cantidad: '',
      motivo: '',
    },
    validationSchema: Yup.object({
      tipoAjuste: Yup.string().required(),
      cantidad: Yup.number().positive('Cantidad debe ser positiva').required('Cantidad obligatoria'),
      motivo: Yup.string().min(5, 'Mínimo 5 caracteres de justificación').required('Motivo obligatorio (RN-08)'),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        await apiClient.post('/inventory/adjustments', {
          referenciaId: selectedRef.referenciaId,
          tipoAjuste: values.tipoAjuste,
          cantidad: values.cantidad,
          motivo: values.motivo,
        });
        setSuccessMsg(`Ajuste de inventario ejecutado correctamente para ${selectedRef.codigoBarras}`);
        setOpenAdjustmentModal(false);
        resetForm();
        fetchSaldos();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al ejecutar ajuste');
      }
    },
  });

  // Formik Inventario Inicial (Solo Administrador - RN-04)
  const initialStockFormik = useFormik({
    initialValues: {
      cantidadInicial: '',
      costoUnitario: '',
    },
    validationSchema: Yup.object({
      cantidadInicial: Yup.number().positive('Cantidad debe ser positiva').required('Cantidad inicial requerida'),
      costoUnitario: Yup.number().positive('Costo unitario requerido').required('Costo requerido'),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        await apiClient.post('/inventory/initial', {
          referenciaId: selectedRef.referenciaId,
          cantidadInicial: values.cantidadInicial,
          costoUnitario: values.costoUnitario,
        });
        setSuccessMsg(`Inventario inicial registrado para ${selectedRef.codigoBarras}`);
        setOpenInitialModal(false);
        resetForm();
        fetchSaldos();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al registrar inventario inicial');
      }
    },
  });

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Control de Existencias e Inventario
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Consulta de saldos en tiempo real, valoración por Costo Promedio Ponderado (CPP) y Kárdex inmutable
          </Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button
            variant={soloBajoStock ? 'contained' : 'outlined'}
            color="warning"
            startIcon={<WarningIcon />}
            onClick={() => setSoloBajoStock(!soloBajoStock)}
          >
            {soloBajoStock ? 'Mostrando Bajo Stock' : 'Filtrar Bajo Stock (<=5)'}
          </Button>
        </Stack>
      </Box>

      {successMsg && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMsg(null)}>{successMsg}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          size="small"
          placeholder="Buscar por código de barras o producto..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          InputProps={{
            startAdornment: <SearchIcon sx={{ color: 'action.active', mr: 1 }} />,
          }}
        />
      </Paper>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <TableContainer component={Paper} elevation={2}>
          <Table>
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                <TableCell><strong>Cód. Barras</strong></TableCell>
                <TableCell><strong>Artículo Comercial</strong></TableCell>
                <TableCell><strong>Unidad</strong></TableCell>
                <TableCell align="right"><strong>Stock Disponible</strong></TableCell>
                {isAdmin && <TableCell align="right"><strong>Costo Promedio (CPP)</strong></TableCell>}
                {isAdmin && <TableCell align="right"><strong>Valor Total Stock</strong></TableCell>}
                <TableCell align="center"><strong>Última Actualización</strong></TableCell>
                <TableCell align="center"><strong>Operaciones</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {saldos.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                    No se encontraron registros de inventario.
                  </TableCell>
                </TableRow>
              ) : (
                saldos.map((item) => {
                  const cant = Number(item.cantidadDisponible || 0);
                  const cpp = Number(item.costoPromedioPonderado || 0);
                  const totalVal = cant * cpp;
                  return (
                    <TableRow key={item.id} hover>
                      <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                        {item.codigoBarras}
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">
                          {item.productoNombre}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {item.marcaNombre} — {item.presentacionNombre}
                        </Typography>
                      </TableCell>
                      <TableCell>{item.unidadMedida}</TableCell>
                      <TableCell align="right">
                        <Chip
                          label={formatQuantity(cant)}
                          color={cant <= 0 ? 'error' : cant <= 5 ? 'warning' : 'success'}
                          variant={cant <= 5 ? 'filled' : 'outlined'}
                          sx={{ fontWeight: 'bold' }}
                        />
                      </TableCell>
                      {isAdmin && (
                        <TableCell align="right" sx={{ fontWeight: 'medium' }}>
                          {formatCurrency(cpp, 2)}
                        </TableCell>
                      )}
                      {isAdmin && (
                        <TableCell align="right" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                          {formatCurrency(totalVal)}
                        </TableCell>
                      )}
                      <TableCell align="center">
                        <Typography variant="caption">{formatDateTime(item.fechaActualizacion)}</Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Stack direction="row" spacing={1} justifyContent="center">
                          {isAdmin && (
                            <Tooltip title="Ver Kárdex (Movimientos Inmutables)">
                              <IconButton
                                size="small"
                                color="info"
                                onClick={() => handleOpenKardex(item)}
                              >
                                <HistoryIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}
                          {isAdmin && (
                            <Tooltip title="Ajuste Manual de Stock (RN-08)">
                              <IconButton
                                size="small"
                                color="warning"
                                onClick={() => {
                                  setSelectedRef(item);
                                  adjustmentFormik.resetForm();
                                  setOpenAdjustmentModal(true);
                                }}
                              >
                                <TuneIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}
                          {isAdmin && cant === 0 && (
                            <Tooltip title="Registrar Inventario Inicial (RN-04)">
                              <IconButton
                                size="small"
                                color="success"
                                onClick={() => {
                                  setSelectedRef(item);
                                  initialStockFormik.resetForm();
                                  setOpenInitialModal(true);
                                }}
                              >
                                <PlayArrowIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}
                        </Stack>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Modal Kárdex */}
      <Dialog open={openKardexModal} onClose={() => setOpenKardexModal(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Kárdex de Movimientos — {selectedRef?.productoNombre} ({selectedRef?.codigoBarras})
        </DialogTitle>
        <DialogContent dividers>
          {kardexLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : kardexMovements.length === 0 ? (
            <Typography variant="body2" color="text.secondary" align="center" sx={{ py: 3 }}>
              No existen movimientos registrados para este artículo.
            </Typography>
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead sx={{ bgcolor: 'action.hover' }}>
                  <TableRow>
                    <TableCell>Fecha / Hora</TableCell>
                    <TableCell>Tipo Flujo</TableCell>
                    <TableCell align="right">Cantidad</TableCell>
                    <TableCell align="right">Costo Unit.</TableCell>
                    <TableCell align="right">Saldo Resultante</TableCell>
                    <TableCell>Documento / Motivo</TableCell>
                    <TableCell>Usuario</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {kardexMovements.map((mov) => (
                    <TableRow key={mov.id}>
                      <TableCell>{formatDateTime(mov.fechaHora)}</TableCell>
                      <TableCell>
                        <Chip
                          label={mov.tipoMovimiento}
                          size="small"
                          color={
                            mov.tipoMovimiento.includes('COMPRA') || mov.tipoMovimiento.includes('ENTRADA') || mov.tipoMovimiento.includes('INICIALIZACION')
                              ? 'success'
                              : mov.tipoMovimiento.includes('VENTA') || mov.tipoMovimiento.includes('SALIDA')
                              ? 'error'
                              : 'default'
                          }
                        />
                      </TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>
                        {formatQuantity(mov.cantidad)}
                      </TableCell>
                      <TableCell align="right">{formatCurrency(mov.costoUnitarioMovimiento, 2)}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatQuantity(mov.saldoResultante)}</TableCell>
                      <TableCell>
                        <Typography variant="caption" display="block">{mov.documentoTipo} #{mov.documentoId}</Typography>
                        <Typography variant="caption" color="text.secondary">{mov.motivo}</Typography>
                      </TableCell>
                      <TableCell>{mov.usuarioNombre}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenKardexModal(false)}>Cerrar</Button>
        </DialogActions>
      </Dialog>

      {/* Modal Ajuste de Inventario (RN-08) */}
      <Dialog open={openAdjustmentModal} onClose={() => setOpenAdjustmentModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Ajuste Manual de Inventario (Solo Admin)</DialogTitle>
        <form onSubmit={adjustmentFormik.handleSubmit}>
          <DialogContent>
            <Typography variant="body2" sx={{ mb: 2 }}>
              Artículo: <strong>{selectedRef?.productoNombre}</strong> (Stock Actual: {selectedRef?.cantidadDisponible})
            </Typography>
            <FormControl fullWidth margin="normal">
              <InputLabel>Sentido del Ajuste</InputLabel>
              <Select
                id="tipoAjuste"
                name="tipoAjuste"
                value={adjustmentFormik.values.tipoAjuste}
                label="Sentido del Ajuste"
                onChange={adjustmentFormik.handleChange}
              >
                <MenuItem value="INCREMENTO">Incremento (+) Sobrante en conteo físico</MenuItem>
                <MenuItem value="DISMINUCION">Disminución (-) Merma, rotura o pérdida</MenuItem>
              </Select>
            </FormControl>
            <TextField
              fullWidth
              id="cantidad"
              name="cantidad"
              label="Cantidad a Ajustar"
              type="number"
              margin="normal"
              value={adjustmentFormik.values.cantidad}
              onChange={adjustmentFormik.handleChange}
              error={adjustmentFormik.touched.cantidad && Boolean(adjustmentFormik.errors.cantidad)}
              helperText={adjustmentFormik.touched.cantidad && adjustmentFormik.errors.cantidad}
            />
            <TextField
              fullWidth
              id="motivo"
              name="motivo"
              label="Motivo Documentado Obligatorio (RN-08)"
              multiline
              rows={2}
              margin="normal"
              value={adjustmentFormik.values.motivo}
              onChange={adjustmentFormik.handleChange}
              error={adjustmentFormik.touched.motivo && Boolean(adjustmentFormik.errors.motivo)}
              helperText={adjustmentFormik.touched.motivo && adjustmentFormik.errors.motivo}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenAdjustmentModal(false)}>Cancelar</Button>
            <Button type="submit" variant="contained" color="warning">Aplicar Ajuste</Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Modal Inventario Inicial (RN-04) */}
      <Dialog open={openInitialModal} onClose={() => setOpenInitialModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Carga de Inventario Inicial</DialogTitle>
        <form onSubmit={initialStockFormik.handleSubmit}>
          <DialogContent>
            <Typography variant="body2" sx={{ mb: 2 }}>
              Artículo: <strong>{selectedRef?.productoNombre}</strong> ({selectedRef?.codigoBarras})
            </Typography>
            <TextField
              fullWidth
              id="cantidadInicial"
              name="cantidadInicial"
              label="Cantidad Inicial de Arranque"
              type="number"
              margin="normal"
              value={initialStockFormik.values.cantidadInicial}
              onChange={initialStockFormik.handleChange}
              error={initialStockFormik.touched.cantidadInicial && Boolean(initialStockFormik.errors.cantidadInicial)}
              helperText={initialStockFormik.touched.cantidadInicial && initialStockFormik.errors.cantidadInicial}
            />
            <TextField
              fullWidth
              id="costoUnitario"
              name="costoUnitario"
              label="Costo Unitario de Referencia (COP)"
              type="number"
              margin="normal"
              value={initialStockFormik.values.costoUnitario}
              onChange={initialStockFormik.handleChange}
              error={initialStockFormik.touched.costoUnitario && Boolean(initialStockFormik.errors.costoUnitario)}
              helperText={initialStockFormik.touched.costoUnitario && initialStockFormik.errors.costoUnitario}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenInitialModal(false)}>Cancelar</Button>
            <Button type="submit" variant="contained" color="success">Asentar Inicial</Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};
