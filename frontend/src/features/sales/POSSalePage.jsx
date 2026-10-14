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
  Tooltip,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddShoppingCartIcon from '@mui/icons-material/AddShoppingCart';
import PrintIcon from '@mui/icons-material/Print';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../services/api/apiClient';
import { formatCurrency, formatQuantity } from '../../utils/formatters';

export const POSSalePage = () => {
  const navigate = useNavigate();

  // Estados de clientes y catálogo
  const [clientes, setClientes] = useState([]);
  const [clienteSeleccionado, setClienteSeleccionado] = useState(null);
  const [referencias, setReferencias] = useState([]);
  const [referenciaSeleccionada, setReferenciaSeleccionada] = useState(null);
  const [cantidadInput, setCantidadInput] = useState(1);

  // Carrito de venta
  const [lineas, setLineas] = useState([]);
  const [montoEfectivo, setMontoEfectivo] = useState('');

  // Estados UI
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [ventaExitosa, setVentaExitosa] = useState(null);

  useEffect(() => {
    const cargarMaestros = async () => {
      try {
        const [cliRes, refRes] = await Promise.all([
          apiClient.get('/third-parties?tipo=CLIENTE&size=100'),
          apiClient.get('/catalog/references?activo=true&size=100'),
        ]);
        setClientes(cliRes.data.content || []);
        setReferencias(refRes.data.content || []);

        // Seleccionar por defecto el primer cliente si existe
        if (cliRes.data.content?.length > 0) {
          setClienteSeleccionado(cliRes.data.content[0]);
        }
      } catch (err) {
        setError('Error al cargar clientes o artículos de catálogo');
      }
    };

    cargarMaestros();
  }, []);

  const handleAgregarLinea = () => {
    if (!referenciaSeleccionada) return;
    const cant = Number(cantidadInput);
    if (cant <= 0) {
      setError('La cantidad debe ser mayor a cero');
      return;
    }

    const stockActual = Number(referenciaSeleccionada.stockDisponible || 0);
    const yaEnCarrito = lineas.find((l) => l.referenciaId === referenciaSeleccionada.id);
    const cantTotal = (yaEnCarrito ? yaEnCarrito.cantidad : 0) + cant;

    if (cantTotal > stockActual) {
      setError(`Stock insuficiente para ${referenciaSeleccionada.codigoBarras}. Disponible: ${stockActual}, solicitado: ${cantTotal}`);
      return;
    }

    setError(null);

    if (yaEnCarrito) {
      setLineas(
        lineas.map((l) =>
          l.referenciaId === referenciaSeleccionada.id
            ? { ...l, cantidad: cantTotal, subtotal: cantTotal * l.precioUnitario }
            : l
        )
      );
    } else {
      setLineas([
        ...lineas,
        {
          referenciaId: referenciaSeleccionada.id,
          codigoBarras: referenciaSeleccionada.codigoBarras,
          productoNombre: referenciaSeleccionada.productoNombre,
          marcaNombre: referenciaSeleccionada.marcaNombre,
          presentacionNombre: referenciaSeleccionada.presentacionNombre,
          unidadMedida: referenciaSeleccionada.unidadMedidaCodigo,
          precioUnitario: Number(referenciaSeleccionada.precioVenta),
          cantidad: cant,
          subtotal: cant * Number(referenciaSeleccionada.precioVenta),
        },
      ]);
    }

    setReferenciaSeleccionada(null);
    setCantidadInput(1);
  };

  const handleEliminarLinea = (refId) => {
    setLineas(lineas.filter((l) => l.referenciaId !== refId));
  };

  const totalVenta = lineas.reduce((sum, l) => sum + l.subtotal, 0);
  const efectivoNum = Number(montoEfectivo) || 0;
  const cambio = efectivoNum >= totalVenta ? efectivoNum - totalVenta : 0;

  const handleConfirmarVenta = async () => {
    if (!clienteSeleccionado) {
      setError('Debe seleccionar obligatoriamente un cliente registrado (RN-05)');
      return;
    }
    if (lineas.length === 0) {
      setError('El carrito no contiene ningún producto');
      return;
    }
    if (efectivoNum < totalVenta) {
      setError('El monto en efectivo recibido es inferior al total a pagar (RN-06)');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const payload = {
        clienteId: clienteSeleccionado.id,
        montoPagadoEfectivo: efectivoNum,
        lineas: lineas.map((l) => ({
          referenciaId: l.referenciaId,
          cantidad: l.cantidad,
        })),
      };

      const response = await apiClient.post('/sales', payload);
      setVentaExitosa(response.data);
      setLineas([]);
      setMontoEfectivo('');

      // Recargar catálogo para actualizar existencias visibles
      const refRes = await apiClient.get('/catalog/references?activo=true&size=100');
      setReferencias(refRes.data.content || []);
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al procesar la venta. Verifique existencias.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">
          Punto de Venta en Mostrador (Facturación de Contado)
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Cobro en efectivo (COP), cliente registrado obligatorio y descuento transaccional atómico contra stock negativo (RN-01, RN-05, RN-06)
        </Typography>
      </Box>

      {ventaExitosa && (
        <Alert
          severity="success"
          icon={<CheckCircleIcon />}
          sx={{ mb: 3 }}
          action={
            <Button
              color="inherit"
              size="small"
              startIcon={<PrintIcon />}
              onClick={() => navigate(`/comprobantes/VENTA/${ventaExitosa.consecutivo}`)}
            >
              Imprimir Recibo {ventaExitosa.consecutivo}
            </Button>
          }
        >
          ¡Venta completada con éxito! Comprobante emitido: <strong>{ventaExitosa.consecutivo}</strong>. Cambio a entregar: <strong>{formatCurrency(ventaExitosa.cambioDevuelto)}</strong>.
        </Alert>
      )}

      {error && <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>{error}</Alert>}

      <Grid container spacing={3}>
        {/* Panel Izquierdo: Selección de Cliente y Productos */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, mb: 3 }} elevation={2}>
            <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
              1. Cliente Registrado Obligatorio (RN-05)
            </Typography>
            <Autocomplete
              options={clientes}
              getOptionLabel={(option) => `${option.tipoDocumento} ${option.numeroDocumento} — ${option.razonSocial}`}
              value={clienteSeleccionado}
              onChange={(event, newValue) => setClienteSeleccionado(newValue)}
              renderInput={(params) => (
                <TextField {...params} label="Buscar Cliente por Cédula / NIT o Nombre" size="small" fullWidth />
              )}
            />
          </Paper>

          <Paper sx={{ p: 3, mb: 3 }} elevation={2}>
            <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
              2. Búsqueda y Adición de Artículos
            </Typography>
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={7}>
                <Autocomplete
                  options={referencias}
                  getOptionLabel={(option) => `[${option.codigoBarras}] ${option.productoNombre} (${option.marcaNombre}) — Stock: ${formatQuantity(option.stockDisponible)}`}
                  value={referenciaSeleccionada}
                  onChange={(event, newValue) => setReferenciaSeleccionada(newValue)}
                  renderInput={(params) => (
                    <TextField {...params} label="Escanear Código de Barras o Escribir Producto" size="small" fullWidth />
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
                <Button
                  fullWidth
                  variant="contained"
                  startIcon={<AddShoppingCartIcon />}
                  onClick={handleAgregarLinea}
                  disabled={!referenciaSeleccionada}
                >
                  Agregar
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
                  <TableCell align="center"><strong>Cant.</strong></TableCell>
                  <TableCell align="right"><strong>Precio Unit.</strong></TableCell>
                  <TableCell align="right"><strong>Subtotal (COP)</strong></TableCell>
                  <TableCell align="center"><strong>Quitar</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lineas.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                      El carrito está vacío. Agregue productos para iniciar la venta.
                    </TableCell>
                  </TableRow>
                ) : (
                  lineas.map((linea) => (
                    <TableRow key={linea.referenciaId} hover>
                      <TableCell sx={{ fontFamily: 'monospace' }}>{linea.codigoBarras}</TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">{linea.productoNombre}</Typography>
                        <Typography variant="caption" color="text.secondary">{linea.marcaNombre} - {linea.presentacionNombre}</Typography>
                      </TableCell>
                      <TableCell align="center">{formatQuantity(linea.cantidad)} {linea.unidadMedida}</TableCell>
                      <TableCell align="right">{formatCurrency(linea.precioUnitario)}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(linea.subtotal)}</TableCell>
                      <TableCell align="center">
                        <IconButton size="small" color="error" onClick={() => handleEliminarLinea(linea.referenciaId)}>
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

        {/* Panel Derecho: Liquidación de Pago en Efectivo */}
        <Grid item xs={12} md={4}>
          <Card elevation={3} sx={{ borderRadius: 2 }}>
            <CardContent>
              <Typography variant="h6" fontWeight="bold" gutterBottom>
                Liquidación de Mostrador
              </Typography>
              <Divider sx={{ my: 1.5 }} />

              <Stack spacing={2} sx={{ my: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body1" color="text.secondary">Total Artículos:</Typography>
                  <Typography variant="body1" fontWeight="bold">
                    {lineas.reduce((sum, l) => sum + Number(l.cantidad), 0)}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                  <Typography variant="h6">Total a Pagar:</Typography>
                  <Typography variant="h4" fontWeight="bold" color="primary.main">
                    {formatCurrency(totalVenta)}
                  </Typography>
                </Box>

                <Divider />

                <Typography variant="subtitle2" fontWeight="bold">
                  Medio de Pago: Efectivo (RN-06)
                </Typography>

                <TextField
                  fullWidth
                  label="Efectivo Recibido (COP)"
                  type="number"
                  value={montoEfectivo}
                  onChange={(e) => setMontoEfectivo(e.target.value)}
                  placeholder="Ej: 50000"
                  helperText={
                    efectivoNum > 0 && efectivoNum < totalVenta
                      ? `Faltan: ${formatCurrency(totalVenta - efectivoNum)}`
                      : ''
                  }
                  error={efectivoNum > 0 && efectivoNum < totalVenta}
                />

                <Box sx={{ p: 2, bgcolor: 'background.default', borderRadius: 2, textAlign: 'center' }}>
                  <Typography variant="caption" color="text.secondary" display="block">
                    Cambio / Vueltas a Devolver:
                  </Typography>
                  <Typography variant="h5" fontWeight="bold" color={cambio > 0 ? 'success.main' : 'text.primary'}>
                    {formatCurrency(cambio)}
                  </Typography>
                </Box>

                <Button
                  fullWidth
                  variant="contained"
                  color="secondary"
                  size="large"
                  disabled={loading || lineas.length === 0 || efectivoNum < totalVenta}
                  onClick={handleConfirmarVenta}
                  sx={{ py: 1.5, fontWeight: 'bold', fontSize: '1.05rem' }}
                >
                  {loading ? <CircularProgress size={26} color="inherit" /> : 'Confirmar y Despachar'}
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
