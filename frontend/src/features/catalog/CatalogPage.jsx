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
  Autocomplete,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import SearchIcon from '@mui/icons-material/Search';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import apiClient from '../../services/api/apiClient';
import { formatCurrency, formatQuantity } from '../../utils/formatters';
import { useAuth } from '../../app/providers/AuthProvider';
import { useLanguage } from '../../app/providers/LanguageProvider';

export const CatalogPage = () => {
  const { hasRole } = useAuth();
  const { t } = useLanguage();
  const isAdmin = hasRole('ROLE_ADMINISTRADOR');

  const [referencias, setReferencias] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Maestros para creación de referencias
  const [maestros, setMaestros] = useState({
    productos: [],
    marcas: [],
    presentaciones: [],
    unidadesMedida: [],
  });

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
      setError(t('noItemsFound'));
    } finally {
      setLoading(false);
    }
  };

  const fetchMasters = async () => {
    try {
      const response = await apiClient.get('/catalog/masters');
      setMaestros({
        productos: response.data.productos || [],
        marcas: response.data.marcas || [],
        presentaciones: response.data.presentaciones || [],
        unidadesMedida: response.data.unidadesMedida || [],
      });
    } catch (err) {
      // Si falla maestros no bloquea catálogo
    }
  };

  useEffect(() => {
    fetchCatalog();
  }, [search]);

  useEffect(() => {
    fetchMasters();
  }, []);

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

  // Formik para creación de nueva referencia con Producto + Marca + Presentación seleccionables o libres
  const createFormik = useFormik({
    initialValues: {
      codigoBarras: '',
      productoNombre: '',
      marcaNombre: '',
      presentacionNombre: '',
      unidadMedidaCodigo: 'UND',
      precioVenta: '',
    },
    validationSchema: Yup.object({
      codigoBarras: Yup.string().required('Código de barras obligatorio'),
      productoNombre: Yup.string().required('Debe ingresar o seleccionar el producto base'),
      marcaNombre: Yup.string().required('Debe ingresar o seleccionar la marca'),
      presentacionNombre: Yup.string().required('Debe ingresar o seleccionar la presentación'),
      unidadMedidaCodigo: Yup.string().required('Unidad de medida obligatoria'),
      precioVenta: Yup.number().positive('Precio debe ser positivo').required('Precio obligatorio'),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        await apiClient.post('/catalog/references', values);
        setSuccessMsg(`Referencia ${values.codigoBarras} creada en el catálogo exitosamente.`);
        setOpenCreateModal(false);
        resetForm();
        fetchCatalog();
        fetchMasters();
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

  // Activar o desactivar producto (soluciona error de reactivación)
  const handleToggleStatus = async (ref) => {
    const accion = ref.activo ? 'desactivar' : 'activar';
    if (window.confirm(`¿Está seguro de ${accion} la referencia ${ref.codigoBarras}?`)) {
      try {
        await apiClient.patch(`/catalog/references/${ref.id}/toggle-status`);
        setSuccessMsg(`Estado de referencia ${ref.codigoBarras} modificado a ${ref.activo ? 'INACTIVO' : 'ACTIVO'}.`);
        fetchCatalog();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al cambiar estado de la referencia');
      }
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            {t('catalogTitle')}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {t('catalogSubtitle')}
          </Typography>
        </Box>
        {isAdmin && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              createFormik.resetForm();
              setOpenCreateModal(true);
            }}
          >
            {t('newReference')}
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
          placeholder={t('searchPlaceholder')}
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
                <TableCell><strong>{t('colBarcode')}</strong></TableCell>
                <TableCell><strong>{t('colDescription')}</strong></TableCell>
                <TableCell><strong>{t('colUnit')}</strong></TableCell>
                <TableCell align="right"><strong>{t('colSalePrice')}</strong></TableCell>
                {isAdmin && <TableCell align="right"><strong>{t('colAvgCost')}</strong></TableCell>}
                <TableCell align="center"><strong>{t('colStock')}</strong></TableCell>
                <TableCell align="center"><strong>{t('colStatus')}</strong></TableCell>
                <TableCell align="center"><strong>{t('colActions')}</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {referencias.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 3 }}>
                    {t('noItemsFound')}
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
                        label={ref.activo ? t('active') : t('inactive')}
                        color={ref.activo ? 'success' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      <Stack direction="row" spacing={1} justifyContent="center">
                        <Tooltip title={t('modifyPrice')}>
                          <IconButton
                            size="small"
                            color="primary"
                            onClick={() => handleOpenPrice(ref)}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {isAdmin && (
                          <Tooltip title={ref.activo ? t('deactivateRef') : t('activateRef')}>
                            <IconButton
                              size="small"
                              color={ref.activo ? 'error' : 'success'}
                              onClick={() => handleToggleStatus(ref)}
                            >
                              {ref.activo ? <BlockIcon fontSize="small" /> : <CheckCircleOutlineIcon fontSize="small" />}
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
        <DialogTitle>{t('modifyPrice')}</DialogTitle>
        <form onSubmit={priceFormik.handleSubmit}>
          <DialogContent>
            <Typography variant="body2" sx={{ mb: 2 }}>
              Artículo: <strong>{selectedRef?.productoNombre}</strong> ({selectedRef?.codigoBarras})
            </Typography>
            <TextField
              fullWidth
              id="nuevoPrecio"
              name="nuevoPrecio"
              label={t('priceLabel')}
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
            <Button onClick={() => setOpenPriceModal(false)}>{t('cancel')}</Button>
            <Button type="submit" variant="contained">{t('savePrice')}</Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Modal Crear Referencia Completa (Corrige error de campos faltantes) */}
      <Dialog open={openCreateModal} onClose={() => setOpenCreateModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('createRefTitle')}</DialogTitle>
        <form onSubmit={createFormik.handleSubmit}>
          <DialogContent>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 2 }}>
              Una referencia comercial única se define combinando Producto + Marca + Presentación + Código de Barras (RN-02). Puede seleccionar existentes o escribir nuevos nombres.
            </Typography>

            <Stack spacing={2}>
              {/* Producto */}
              <Autocomplete
                freeSolo
                options={maestros.productos.map((p) => p.nombre)}
                value={createFormik.values.productoNombre}
                onChange={(event, newValue) => createFormik.setFieldValue('productoNombre', newValue || '')}
                onInputChange={(event, newInputValue) => createFormik.setFieldValue('productoNombre', newInputValue || '')}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label={`${t('productLabel')} (Ej: Martillo de Uña, Disco de Corte)`}
                    error={createFormik.touched.productoNombre && Boolean(createFormik.errors.productoNombre)}
                    helperText={createFormik.touched.productoNombre && createFormik.errors.productoNombre}
                  />
                )}
              />

              {/* Marca */}
              <Autocomplete
                freeSolo
                options={maestros.marcas.map((m) => m.nombre)}
                value={createFormik.values.marcaNombre}
                onChange={(event, newValue) => createFormik.setFieldValue('marcaNombre', newValue || '')}
                onInputChange={(event, newInputValue) => createFormik.setFieldValue('marcaNombre', newInputValue || '')}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label={`${t('brandLabel')} (Ej: Stanley, Herragro, Bellota, Generico)`}
                    error={createFormik.touched.marcaNombre && Boolean(createFormik.errors.marcaNombre)}
                    helperText={createFormik.touched.marcaNombre && createFormik.errors.marcaNombre}
                  />
                )}
              />

              {/* Presentación */}
              <Autocomplete
                freeSolo
                options={maestros.presentaciones.map((pr) => pr.nombre)}
                value={createFormik.values.presentacionNombre}
                onChange={(event, newValue) => createFormik.setFieldValue('presentacionNombre', newValue || '')}
                onInputChange={(event, newInputValue) => createFormik.setFieldValue('presentacionNombre', newInputValue || '')}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label={`${t('presentationLabel')} (Ej: Unidad Suelta, Caja x 100, Galón)`}
                    error={createFormik.touched.presentacionNombre && Boolean(createFormik.errors.presentacionNombre)}
                    helperText={createFormik.touched.presentacionNombre && createFormik.errors.presentacionNombre}
                  />
                )}
              />

              {/* Unidad de Medida */}
              <FormControl fullWidth>
                <InputLabel>{t('unitLabel')}</InputLabel>
                <Select
                  value={createFormik.values.unidadMedidaCodigo}
                  label={t('unitLabel')}
                  onChange={(e) => createFormik.setFieldValue('unidadMedidaCodigo', e.target.value)}
                >
                  <MenuItem value="UND">UND — Unidad</MenuItem>
                  <MenuItem value="MTR">MTR — Metro lineal</MenuItem>
                  <MenuItem value="KGM">KGM — Kilogramo</MenuItem>
                  <MenuItem value="GLN">GLN — Galón</MenuItem>
                  <MenuItem value="CJA">CJA — Caja</MenuItem>
                </Select>
              </FormControl>

              {/* Código de Barras */}
              <TextField
                fullWidth
                id="codigoBarras"
                name="codigoBarras"
                label={t('barcodeLabel')}
                value={createFormik.values.codigoBarras}
                onChange={createFormik.handleChange}
                error={createFormik.touched.codigoBarras && Boolean(createFormik.errors.codigoBarras)}
                helperText={createFormik.touched.codigoBarras && createFormik.errors.codigoBarras}
              />

              {/* Precio de Venta */}
              <TextField
                fullWidth
                id="precioVenta"
                name="precioVenta"
                label={t('priceLabel')}
                type="number"
                value={createFormik.values.precioVenta}
                onChange={createFormik.handleChange}
                error={createFormik.touched.precioVenta && Boolean(createFormik.errors.precioVenta)}
                helperText={createFormik.touched.precioVenta && createFormik.errors.precioVenta}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenCreateModal(false)}>{t('cancel')}</Button>
            <Button type="submit" variant="contained">{t('create')}</Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};
