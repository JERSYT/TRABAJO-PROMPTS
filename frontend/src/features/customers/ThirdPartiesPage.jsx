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
  FormControlLabel,
  Checkbox,
  Tabs,
  Tab,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import SearchIcon from '@mui/icons-material/Search';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import apiClient from '../../services/api/apiClient';
import { useLanguage } from '../../app/providers/LanguageProvider';

export const ThirdPartiesPage = () => {
  const { t } = useLanguage();
  const [terceros, setTerceros] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filtroTipo, setFiltroTipo] = useState('TODOS');
  const [openModal, setOpenModal] = useState(false);
  const [editingTercero, setEditingTercero] = useState(null);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  const fetchTerceros = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = new URLSearchParams({ size: 50 });
      if (search) params.append('search', search);
      if (filtroTipo !== 'TODOS') params.append('tipo', filtroTipo);

      const response = await apiClient.get(`/third-parties?${params.toString()}`);
      setTerceros(response.data.content || []);
    } catch (err) {
      setError('Error al cargar la lista de terceros');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTerceros();
  }, [search, filtroTipo]);

  const formik = useFormik({
    initialValues: {
      tipoDocumento: 'CC',
      numeroDocumento: '',
      razonSocial: '',
      tipoPersona: 'NATURAL',
      telefono: '',
      email: '',
      direccion: '',
      esCliente: true,
      esProveedor: false,
    },
    validationSchema: Yup.object({
      tipoDocumento: Yup.string().required('Obligatorio'),
      numeroDocumento: Yup.string()
        .matches(/^[0-9]+(-[0-9kK])?$/, 'El número de documento debe contener solo dígitos (o guión para NIT ej: 900123456-1)')
        .min(4, 'Mínimo 4 caracteres')
        .max(30, 'Máximo 30 caracteres')
        .required('Número de documento obligatorio'),
      razonSocial: Yup.string().required('Nombre o Razón Social obligatorio'),
      tipoPersona: Yup.string().required('Obligatorio'),
      telefono: Yup.string()
        .matches(/^[0-9+\s-]*$/, 'El teléfono debe contener únicamente números')
        .nullable(),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        if (!values.esCliente && !values.esProveedor) {
          setError('Debe seleccionar al menos un rol comercial: Cliente o Proveedor');
          return;
        }

        if (editingTercero) {
          await apiClient.put(`/third-parties/${editingTercero.id}`, values);
          setSuccessMsg('Tercero actualizado correctamente');
        } else {
          await apiClient.post('/third-parties', values);
          setSuccessMsg('Tercero registrado correctamente');
        }
        setOpenModal(false);
        resetForm();
        setEditingTercero(null);
        fetchTerceros();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al guardar tercero');
      }
    },
  });

  const handleOpenEdit = (tercero) => {
    setEditingTercero(tercero);
    formik.setValues({
      tipoDocumento: tercero.tipoDocumento,
      numeroDocumento: tercero.numeroDocumento,
      razonSocial: tercero.razonSocial,
      tipoPersona: tercero.tipoPersona,
      telefono: tercero.telefono || '',
      email: tercero.email || '',
      direccion: tercero.direccion || '',
      esCliente: tercero.esCliente,
      esProveedor: tercero.esProveedor,
    });
    setOpenModal(true);
  };

  const handleOpenCreate = () => {
    setEditingTercero(null);
    formik.resetForm();
    setOpenModal(true);
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Administración de Terceros (Clientes y Proveedores)
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {t('tpSubtitle')}
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
          {t('newThirdParty')}
        </Button>
      </Box>

      {successMsg && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMsg(null)}>{successMsg}</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      <Paper sx={{ mb: 3, p: 2 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="center">
          <TextField
            fullWidth
            size="small"
            placeholder={t('customerSearchPlaceholder')}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            InputProps={{
              startAdornment: <SearchIcon sx={{ color: 'action.active', mr: 1 }} />,
            }}
          />
          <Tabs
            value={filtroTipo}
            onChange={(e, val) => setFiltroTipo(val)}
            sx={{ minWidth: 320 }}
          >
            <Tab label={t('tabAll')} value="TODOS" />
            <Tab label={t('tabClients')} value="CLIENTE" />
            <Tab label={t('tabSuppliers')} value="PROVEEDOR" />
          </Tabs>
        </Stack>
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
                <TableCell><strong>{t('colId')}</strong></TableCell>
                <TableCell><strong>{t('colBusinessName')}</strong></TableCell>
                <TableCell><strong>{t('colPersonType')}</strong></TableCell>
                <TableCell><strong>{t('colContact')}</strong></TableCell>
                <TableCell align="center"><strong>{t('colRoles')}</strong></TableCell>
                <TableCell align="center"><strong>{t('colActions')}</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {terceros.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 3 }}>
                    No se encontraron registros de clientes o proveedores.
                  </TableCell>
                </TableRow>
              ) : (
                terceros.map((t) => (
                  <TableRow key={t.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>
                      {t.tipoDocumento} {t.numeroDocumento}
                    </TableCell>
                    <TableCell sx={{ fontWeight: 'medium' }}>
                      {t.razonSocial}
                    </TableCell>
                    <TableCell>{t.tipoPersona}</TableCell>
                    <TableCell>
                      <Typography variant="body2">{t.telefono || '-'}</Typography>
                      <Typography variant="caption" color="text.secondary">{t.email || '-'}</Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Stack direction="row" spacing={0.5} justifyContent="center">
                        {t.esCliente && <Chip label="CLIENTE" color="primary" size="small" />}
                        {t.esProveedor && <Chip label="PROVEEDOR" color="secondary" size="small" />}
                      </Stack>
                    </TableCell>
                    <TableCell align="center">
                      <IconButton size="small" color="primary" onClick={() => handleOpenEdit(t)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Modal Crear / Editar */}
      <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingTercero ? 'Editar Tercero' : 'Registrar Nuevo Tercero'}</DialogTitle>
        <form onSubmit={formik.handleSubmit}>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <Stack direction="row" spacing={2}>
                <FormControl sx={{ minWidth: 120 }}>
                  <InputLabel>Tipo Doc</InputLabel>
                  <Select
                    id="tipoDocumento"
                    name="tipoDocumento"
                    value={formik.values.tipoDocumento}
                    label="Tipo Doc"
                    onChange={formik.handleChange}
                  >
                    <MenuItem value="CC">CC (Cédula)</MenuItem>
                    <MenuItem value="NIT">NIT (Empresa)</MenuItem>
                    <MenuItem value="CE">CE (Extranjería)</MenuItem>
                    <MenuItem value="PASAPORTE">Pasaporte</MenuItem>
                  </Select>
                </FormControl>
                <TextField
                  fullWidth
                  id="numeroDocumento"
                  name="numeroDocumento"
                  label="Número de Documento (Solo dígitos)"
                  value={formik.values.numeroDocumento}
                  onChange={(e) => {
                    const clean = e.target.value.replace(/[^0-9-]/g, '');
                    formik.setFieldValue('numeroDocumento', clean);
                  }}
                  error={formik.touched.numeroDocumento && Boolean(formik.errors.numeroDocumento)}
                  helperText={formik.touched.numeroDocumento && formik.errors.numeroDocumento}
                />
              </Stack>

              <TextField
                fullWidth
                id="razonSocial"
                name="razonSocial"
                label="Nombre Completo o Razón Social"
                value={formik.values.razonSocial}
                onChange={formik.handleChange}
                error={formik.touched.razonSocial && Boolean(formik.errors.razonSocial)}
                helperText={formik.touched.razonSocial && formik.errors.razonSocial}
              />

              <FormControl fullWidth>
                <InputLabel>Tipo de Persona</InputLabel>
                <Select
                  id="tipoPersona"
                  name="tipoPersona"
                  value={formik.values.tipoPersona}
                  label="Tipo de Persona"
                  onChange={formik.handleChange}
                >
                  <MenuItem value="NATURAL">Persona Natural</MenuItem>
                  <MenuItem value="JURIDICA">Persona Jurídica</MenuItem>
                </Select>
              </FormControl>

              <Stack direction="row" spacing={2}>
                <TextField
                  fullWidth
                  id="telefono"
                  name="telefono"
                  label="Teléfono / Celular (Solo dígitos)"
                  value={formik.values.telefono}
                  onChange={(e) => {
                    const clean = e.target.value.replace(/[^0-9+\s-]/g, '');
                    formik.setFieldValue('telefono', clean);
                  }}
                  error={formik.touched.telefono && Boolean(formik.errors.telefono)}
                  helperText={formik.touched.telefono && formik.errors.telefono}
                />
                <TextField
                  fullWidth
                  id="email"
                  name="email"
                  label="Correo Electrónico"
                  type="email"
                  value={formik.values.email}
                  onChange={formik.handleChange}
                />
              </Stack>

              <TextField
                fullWidth
                id="direccion"
                name="direccion"
                label="Dirección Física"
                value={formik.values.direccion}
                onChange={formik.handleChange}
              />

              <Box sx={{ p: 1.5, bgcolor: 'background.default', borderRadius: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Roles Comerciales Habilitados (RN-10):
                </Typography>
                <Stack direction="row" spacing={3}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={formik.values.esCliente}
                        onChange={(e) => formik.setFieldValue('esCliente', e.target.checked)}
                      />
                    }
                    label="Es Cliente (Habilitado para Ventas)"
                  />
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={formik.values.esProveedor}
                        onChange={(e) => formik.setFieldValue('esProveedor', e.target.checked)}
                      />
                    }
                    label="Es Proveedor (Habilitado para Compras)"
                  />
                </Stack>
              </Box>
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenModal(false)}>Cancelar</Button>
            <Button type="submit" variant="contained">
              {editingTercero ? 'Guardar Cambios' : 'Registrar'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};
