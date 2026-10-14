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
  IconButton,
  Grid,
  Card,
  CardContent,
  Divider,
  Alert,
  CircularProgress,
  Stack,
  Autocomplete,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PrintIcon from '@mui/icons-material/Print';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../services/api/apiClient';
import { formatCurrency, formatQuantity } from '../../utils/formatters';

export const PurchaseCreatePage = () => {
  const navigate = useNavigate();

  const [proveedores, setProveedores] = useState([]);
  const [proveedorSeleccionado, setProveedorSeleccionado] = useState(null);
  const [referencias, setReferencias] = useState([]);
  const [referenciaSeleccionada, setReferenciaSeleccionada] = useState(null);
  const [cantidadInput, setCantidadInput] = useState(1);
  const [costoUnitarioInput, setCostoUnitarioInput] = useState('');

  const [lineas, setLineas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [compraExitosa, setCompraExitosa] = useState(null);

  useEffect(() => {
    const cargarMaestros = async () => {
      try {
        const [provRes, refRes] = await Promise.all([
          apiClient.get('/third-parties?tipo=PROVEEDOR&size=100'),
          apiClient.get('/catalog/references?activo=true&size=100'),
        ]);
        setProveedores(provRes.data.content || []);
        setReferencias(refRes.data.content || []);

        if (provRes.data.content?.length > 0) {
          setProveedorSeleccionado(provRes.data.content[0]);
        }
      } catch (err) {
        setError('Error al cargar proveedores o catálogo');
      }
    };

    cargarMaestros();
  }, []);

  const handleAgregarLinea = () => {
    if (!referenciaSeleccionada) return;
    const cant = Number(cantidadInput);
    const costo = Number(costoUnitarioInput);

    if (cant <= 0) {
      setError('La cantidad debe ser mayor a cero');
      return;
    }
    if (costo <= 0) {
      setError('El costo unitario debe ser mayor a cero');
      return;
    }

    setError(null);

    setLineas([
      ...lineas,
      {
        referenciaId: referenciaSeleccionada.id,
        codigoBarras: referenciaSeleccionada.codigoBarras,
        productoNombre: referenciaSeleccionada.productoNombre,
        marcaNombre: referenciaSeleccionada.marcaNombre,
        presentacionNombre: referenciaSeleccionada.presentacionNombre,
        unidadMedida: referenciaSeleccionada.unidadMedidaCodigo,
        cantidad: cant,
        costoUnitario: costo,
        subtotal: cant * costo,
      },
    ]);

    setReferenciaSeleccionada(null);
    setCantidadInput(1);
    setCostoUnitarioInput('');
  };

  const handleEliminarLinea = (index) => {
    setLineas(lineas.filter((_, i) => i !== index));
  };

  const totalCompra = lineas.reduce((sum, l) => sum + l.subtotal, 0);

  const handleConfirmarCompra = async () => {
    if (!proveedorSeleccionado) {
      setError('Debe seleccionar obligatoriamente un proveedor registrado (RN-10)');
      return;
    }
    if (lineas.length === 0) {
      setError('La compra debe contener al menos un artículo');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const payload = {
        proveedorId: proveedorSeleccionado.id,
        lineas: lineas.map((l) => ({
          referenciaId: l.referenciaId,
          cantidad: l.cantidad,
          costoUnitario: l.costoUnitario,
        })),
      };

      const response = await apiClient.post('/purchases', payload);
      setCompraExitosa(response.data);
      setLineas([]);

      // Recargar catálogo para actualizar existencias y CPP
      const refRes = await apiClient.get('/catalog/references?activo=true&size=100');
      setReferencias(refRes.data.content || []);
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al registrar la compra');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">
          Registro y Recepción de Compras a Proveedores
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Recepción completa en una sola operación (RN-03) y recálculo automático del Costo Promedio Ponderado (RN-04)
        </Typography>
      </Box>

      {compraExitosa && (
        <Alert
          severity="success"
          icon={<CheckCircleIcon />}
          sx={{ mb: 3 }}
          action={
            <Button
              color="inherit"
              size="small"
              startIcon={<PrintIcon />}
              onClick={() => navigate(`/comprobantes/COMPRA/${compraExitosa.consecutivo}`)}
            >
              Imprimir Comprobante {compraExitosa.consecutivo}
            </Button>
          }
        >
          ¡Compra registrada y recibida con éxito! Consecutivo: <strong>{compraExitosa.consecutivo}</strong>. El inventario y los costos promedio fueron actualizados.
        </Alert>
      )}

      {error && <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>{error}</Alert>}

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, mb: 3 }} elevation={2}>
            <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
              1. Proveedor
            </Typography>
            <Autocomplete
              options={proveedores}
              getOptionLabel={(option) => `${option.tipoDocumento} ${option.numeroDocumento} — ${option.razonSocial}`}
              value={proveedorSeleccionado}
              onChange={(event, newValue) => setProveedorSeleccionado(newValue)}
              renderInput={(params) => (
                <TextField {...params} label="Buscar Proveedor por NIT o Nombre" size="small" fullWidth />
              )}
            />
          </Paper>

          <Paper sx={{ p: 3, mb: 3 }} elevation={2}>
            <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
              2. Artículos a Recibir
            </Typography>
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={5}>
                <Autocomplete
                  options={referencias}
                  getOptionLabel={(option) => `[${option.codigoBarras}] ${option.productoNombre} (${option.marcaNombre})`}
                  value={referenciaSeleccionada}
                  onChange={(event, newValue) => setReferenciaSeleccionada(newValue)}
                  renderInput={(params) => (
                    <TextField {...params} label="Seleccionar Artículo de Catálogo" size="small" fullWidth />
                  )}
                />
              </Grid>
              <Grid item xs={6} sm={2}>
                <TextField
                  fullWidth
                  size="small"
                  label="Cantidad"
                  type="number"
                  value={cantidadInput}
                  onChange={(e) => setCantidadInput(e.target.value)}
                  inputProps={{ min: 1 }}
                />
              </Grid>
              <Grid item xs={6} sm={3}>
                <TextField
                  fullWidth
                  size="small"
                  label="Costo Unitario (COP)"
                  type="number"
                  placeholder="Ej: 15000"
                  value={costoUnitarioInput}
                  onChange={(e) => setCostoUnitarioInput(e.target.value)}
                />
              </Grid>
              <Grid item xs={12} sm={2}>
                <Button
                  fullWidth
                  variant="contained"
                  startIcon={<AddCircleOutlineIcon />}
                  onClick={handleAgregarLinea}
                  disabled={!referenciaSeleccionada}
                >
                  Añadir
                </Button>
              </Grid>
            </Grid>
          </Paper>

          <TableContainer component={Paper} elevation={2}>
            <Table size="small">
              <TableHead sx={{ bgcolor: 'action.hover' }}>
                <TableRow>
                  <TableCell><strong>Cód.</strong></TableCell>
                  <TableCell><strong>Descripción</strong></TableCell>
                  <TableCell align="center"><strong>Cantidad</strong></TableCell>
                  <TableCell align="right"><strong>Costo Unit. (COP)</strong></TableCell>
                  <TableCell align="right"><strong>Subtotal (COP)</strong></TableCell>
                  <TableCell align="center"><strong>Acción</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lineas.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                      No hay artículos agregados a la orden de compra.
                    </TableCell>
                  </TableRow>
                ) : (
                  lineas.map((linea, index) => (
                    <TableRow key={index} hover>
                      <TableCell sx={{ fontFamily: 'monospace' }}>{linea.codigoBarras}</TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">{linea.productoNombre}</Typography>
                        <Typography variant="caption" color="text.secondary">{linea.marcaNombre} - {linea.presentacionNombre}</Typography>
                      </TableCell>
                      <TableCell align="center">{formatQuantity(linea.cantidad)} {linea.unidadMedida}</TableCell>
                      <TableCell align="right">{formatCurrency(linea.costoUnitario, 2)}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(linea.subtotal)}</TableCell>
                      <TableCell align="center">
                        <IconButton size="small" color="error" onClick={() => handleEliminarLinea(index)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card elevation={3} sx={{ borderRadius: 2 }}>
            <CardContent>
              <Typography variant="h6" fontWeight="bold" gutterBottom>
                Resumen de Recepción
              </Typography>
              <Divider sx={{ my: 1.5 }} />

              <Stack spacing={2} sx={{ my: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body1" color="text.secondary">Total Ítems:</Typography>
                  <Typography variant="body1" fontWeight="bold">
                    {lineas.length}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body1" color="text.secondary">Unidades Físicas:</Typography>
                  <Typography variant="body1" fontWeight="bold">
                    {lineas.reduce((sum, l) => sum + Number(l.cantidad), 0)}
                  </Typography>
                </Box>

                <Divider />

                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                  <Typography variant="h6">Total Compra:</Typography>
                  <Typography variant="h4" fontWeight="bold" color="primary.main">
                    {formatCurrency(totalCompra)}
                  </Typography>
                </Box>

                <Alert severity="info" sx={{ mt: 2 }}>
                  Al confirmar, las existencias ingresarán de inmediato al almacén y el sistema recalculará el <strong>Costo Promedio Ponderado (CPP)</strong> de cada referencia.
                </Alert>

                <Button
                  fullWidth
                  variant="contained"
                  color="primary"
                  size="large"
                  disabled={loading || lineas.length === 0}
                  onClick={handleConfirmarCompra}
                  sx={{ py: 1.5, fontWeight: 'bold' }}
                >
                  {loading ? <CircularProgress size={26} color="inherit" /> : 'Confirmar y Recibir Todo'}
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
