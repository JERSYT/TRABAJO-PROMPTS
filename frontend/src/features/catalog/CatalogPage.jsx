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
  Tooltip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import BlockIcon from '@mui/icons-material/Block';
import SearchIcon from '@mui/icons-material/Search';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import apiClient from '../../services/api/apiClient';
import { formatCurrency, formatQuantity } from '../../utils/formatters';
import { useAuth } from '../../app/providers/AuthProvider';

export const CatalogPage = () => {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');

  const [referencias, setReferencias] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Modales
  const [openPriceModal, setOpenPriceModal] = useState(false);
  const [selectedRef, setSelectedRef] = useState(null);
  const [openCreateModal, setOpenCreateModal] = useState(false);

  const fetchCatalog = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = new URLSearchParams({ size: 50 });
      if (search) params.append('search', search);

      const response = await apiClient.get(`/catalog/references?${params.toString()}`);
      setReferencias(response.data.content || []);
    } catch (err) {
      setError('Error al cargar referencias del catálogo');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCatalog();
  }, [search]);

  // Formik para cambio de precio con auditoría (RN-09, RN-11)
  const priceFormik = useFormik({
    initialValues: {
      nuevoPrecio: '',
      motivo: '',
    },
    validationSchema: Yup.object({
      nuevoPrecio: Yup.number()
        .positive('El precio debe ser positivo')
        .required('El precio es obligatorio'),
      motivo: Yup.string().required('Debe indicar la justificación del cambio para la auditoría'),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        await apiClient.patch(`/catalog/references/${selectedRef.id}/price`, {
          nuevoPrecio: values.nuevoPrecio,
          motivo: values.motivo,
        });
        setSuccessMsg(`Precio de ${selectedRef.codigoBarras} actualizado exitosamente.`);
        setOpenPriceModal(false);
        resetForm();
        fetchCatalog();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al actualizar el precio');
      }
    },
  });

  // Formik para creación de nueva referencia
  const createFormik = useFormik({
    initialValues: {
      codigoBarras: '',
      productoId: 1,
      marcaId: 1,
      presentacionId: 1,
      unidadMedidaId: 1,
      precioVenta: '',
    },
    validationSchema: Yup.object({
      codigoBarras: Yup.string().required('Código de barras obligatorio'),
      precioVenta: Yup.number().positive('Precio debe ser positivo').required('Precio obligatorio'),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        await apiClient.post('/catalog/references', values);
        setSuccessMsg('Referencia creada en el catálogo exitosamente.');
        setOpenCreateModal(false);
        resetForm();
        fetchCatalog();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al crear la referencia en el catálogo');
      }
    },
  });

  const handleOpenPrice = (ref) => {
    setSelectedRef(ref);
    priceFormik.setValues({
      nuevoPrecio: ref.precioVenta,
      motivo: '',
    });
    setOpenPriceModal(true);
  };

  const handleDeactivate = async (ref) => {
    if (window.confirm(`¿Está seguro de desactivar la referencia ${ref.codigoBarras}? Si tiene historial no se borrará físicamente (RN-02).`)) {
      try {
        await apiClient.delete(`/catalog/references/${ref.id}`);
        setSuccessMsg(`Referencia ${ref.codigoBarras} desactivada correctamente.`);
        fetchCatalog();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al desactivar referencia');
      }
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Catálogo de Artículos y Referencias
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Administración de referencias comerciales (Producto + Marca + Presentación), códigos de barra y precios en COP
          </Typography>
        </Box>
        {isAdmin && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setOpenCreateModal(true)}
          >
            Nueva Referencia
          </Button>
        )}
      </Box>

      {successMsg && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMsg(null)}>
          {successMsg}
        </Alert>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          size="small"
          placeholder="Buscar por código de barras o nombre del artículo..."
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
                <TableCell><strong>Descripción Comercial</strong></TableCell>
                <TableCell><strong>Unidad</strong></TableCell>
                <TableCell align="right"><strong>Precio Venta (COP)</strong></TableCell>
                {isAdmin && <TableCell align="right"><strong>Costo Promedio (CPP)</strong></TableCell>}
                <TableCell align="center"><strong>Stock</strong></TableCell>
                <TableCell align="center"><strong>Estado</strong></TableCell>
                <TableCell align="center"><strong>Acciones</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {referencias.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                    No se encontraron artículos en el catálogo.
                  </TableCell>
                </TableRow>
              ) : (
                referencias.map((ref) => (
                  <TableRow key={ref.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                      {ref.codigoBarras}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium">
                        {ref.productoNombre}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {ref.marcaNombre} — {ref.presentacionNombre}
                      </Typography>
                    </TableCell>
                    <TableCell>{ref.unidadMedidaCodigo}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                      {formatCurrency(ref.precioVenta)}
                    </TableCell>
                    {isAdmin && (
                      <TableCell align="right" sx={{ color: 'text.secondary' }}>
                        {formatCurrency(ref.costoPromedio, 2)}
                      </TableCell>
                    )}
                    <TableCell align="center">
                      <Chip
                        label={formatQuantity(ref.stockDisponible)}
                        color={Number(ref.stockDisponible) <= 5 ? 'warning' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        label={ref.activo ? 'ACTIVO' : 'INACTIVO'}
                        color={ref.activo ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      <Stack direction="row" spacing={1} justifyContent="center">
                        <Tooltip title="Modificar Precio (Auditado)">
                          <IconButton
                            size="small"
                            color="primary"
                            onClick={() => handleOpenPrice(ref)}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {isAdmin && ref.activo && (
                          <Tooltip title="Desactivar Referencia">
                            <IconButton
                              size="small"
                              color="error"
                              onClick={() => handleDeactivate(ref)}
                            >
                              <BlockIcon fontSize="small" />
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

      {/* Modal Modificar Precio */}
      <Dialog open={openPriceModal} onClose={() => setOpenPriceModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Modificar Precio de Venta</DialogTitle>
        <form onSubmit={priceFormik.handleSubmit}>
          <DialogContent>
            <Typography variant="body2" sx={{ mb: 2 }}>
              Artículo: <strong>{selectedRef?.productoNombre}</strong> ({selectedRef?.codigoBarras})
            </Typography>
            <TextField
              fullWidth
              id="nuevoPrecio"
              name="nuevoPrecio"
              label="Nuevo Precio de Venta (COP)"
              type="number"
              margin="normal"
              value={priceFormik.values.nuevoPrecio}
              onChange={priceFormik.handleChange}
              error={priceFormik.touched.nuevoPrecio && Boolean(priceFormik.errors.nuevoPrecio)}
              helperText={priceFormik.touched.nuevoPrecio && priceFormik.errors.nuevoPrecio}
            />
            <TextField
              fullWidth
              id="motivo"
              name="motivo"
              label="Motivo o Justificación (Auditoría RN-09/11)"
              margin="normal"
              multiline
              rows={2}
              value={priceFormik.values.motivo}
              onChange={priceFormik.handleChange}
              error={priceFormik.touched.motivo && Boolean(priceFormik.errors.motivo)}
              helperText={priceFormik.touched.motivo && priceFormik.errors.motivo}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenPriceModal(false)}>Cancelar</Button>
            <Button type="submit" variant="contained">Guardar Precio</Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Modal Crear Referencia */}
      <Dialog open={openCreateModal} onClose={() => setOpenCreateModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Registrar Nueva Referencia en Catálogo</DialogTitle>
        <form onSubmit={createFormik.handleSubmit}>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                fullWidth
                id="codigoBarras"
                name="codigoBarras"
                label="Código de Barras Único (EAN-13 o Interno)"
                value={createFormik.values.codigoBarras}
                onChange={createFormik.handleChange}
                error={createFormik.touched.codigoBarras && Boolean(createFormik.errors.codigoBarras)}
                helperText={createFormik.touched.codigoBarras && createFormik.errors.codigoBarras}
              />
              <TextField
                fullWidth
                id="precioVenta"
                name="precioVenta"
                label="Precio de Venta al Público (COP)"
                type="number"
                value={createFormik.values.precioVenta}
                onChange={createFormik.handleChange}
                error={createFormik.touched.precioVenta && Boolean(createFormik.errors.precioVenta)}
                helperText={createFormik.touched.precioVenta && createFormik.errors.precioVenta}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenCreateModal(false)}>Cancelar</Button>
            <Button type="submit" variant="contained">Crear Referencia</Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};
