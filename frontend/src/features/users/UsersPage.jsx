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
  Tooltip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import apiClient from '../../services/api/apiClient';
import { formatDateTime } from '../../utils/formatters';

export const UsersPage = () => {
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  const [openModal, setOpenModal] = useState(false);

  const fetchUsuarios = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await apiClient.get('/users?size=50');
      setUsuarios(response.data.content || []);
    } catch (err) {
      setError('Error al consultar usuarios del sistema');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsuarios();
  }, []);

  const formik = useFormik({
    initialValues: {
      username: '',
      password: '',
      nombreCompleto: '',
      email: '',
      rol: 'ROLE_USUARIO_OPERATIVO',
    },
    validationSchema: Yup.object({
      username: Yup.string().min(3, 'Mínimo 3 caracteres').required('Usuario obligatorio'),
      password: Yup.string().min(6, 'Mínimo 6 caracteres').required('Contraseña obligatoria'),
      nombreCompleto: Yup.string().required('Nombre completo obligatorio'),
      email: Yup.string().email('Email inválido'),
      rol: Yup.string().required('Rol obligatorio'),
    }),
    onSubmit: async (values, { resetForm }) => {
      try {
        await apiClient.post('/users', {
          username: values.username,
          password: values.password,
          nombreCompleto: values.nombreCompleto,
          email: values.email,
          roles: [values.rol],
        });
        setSuccessMsg(`Usuario ${values.username} creado exitosamente.`);
        setOpenModal(false);
        resetForm();
        fetchUsuarios();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al crear usuario');
      }
    },
  });

  const handleToggleStatus = async (user) => {
    const accion = user.activo ? 'desactivar' : 'activar';
    if (window.confirm(`¿Está seguro de ${accion} al usuario ${user.username}?`)) {
      try {
        await apiClient.patch(`/users/${user.id}/toggle-status`);
        setSuccessMsg(`Estado del usuario ${user.username} modificado correctamente.`);
        fetchUsuarios();
      } catch (err) {
        setError(err.response?.data?.detail || 'Error al modificar estado de usuario');
      }
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Gestión de Usuarios y Roles (RBAC)
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Administración de cuentas con acceso al sistema: ADMINISTRADOR y USUARIO_OPERATIVO
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpenModal(true)}>
          Nuevo Usuario
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
                <TableCell><strong>Usuario (Login)</strong></TableCell>
                <TableCell><strong>Nombre Completo</strong></TableCell>
                <TableCell><strong>Correo Electrónico</strong></TableCell>
                <TableCell align="center"><strong>Roles Asignados</strong></TableCell>
                <TableCell align="center"><strong>Estado</strong></TableCell>
                <TableCell><strong>Fecha de Alta</strong></TableCell>
                <TableCell align="center"><strong>Acciones</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {usuarios.map((u) => (
                <TableRow key={u.id} hover>
                  <TableCell sx={{ fontFamily: 'monospace', fontWeight: 'bold' }}>{u.username}</TableCell>
                  <TableCell>{u.nombreCompleto}</TableCell>
                  <TableCell>{u.email || '-'}</TableCell>
                  <TableCell align="center">
                    <Stack direction="row" spacing={0.5} justifyContent="center">
                      {u.roles?.map((r) => (
                        <Chip
                          key={r}
                          label={r.replace('ROLE_', '')}
                          size="small"
                          color={r === 'ROLE_ADMINISTRADOR' ? 'secondary' : 'default'}
                        />
                      ))}
                    </Stack>
                  </TableCell>
                  <TableCell align="center">
                    <Chip
                      label={u.activo ? 'ACTIVO' : 'INACTIVO'}
                      color={u.activo ? 'success' : 'default'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{formatDateTime(u.fechaCreacion)}</TableCell>
                  <TableCell align="center">
                    <Tooltip title={u.activo ? 'Desactivar Cuenta' : 'Activar Cuenta'}>
                      <IconButton
                        size="small"
                        color={u.activo ? 'error' : 'success'}
                        onClick={() => handleToggleStatus(u)}
                      >
                        {u.activo ? <BlockIcon fontSize="small" /> : <CheckCircleOutlineIcon fontSize="small" />}
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Modal Crear Usuario */}
      <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Registrar Nuevo Usuario</DialogTitle>
        <form onSubmit={formik.handleSubmit}>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                fullWidth
                id="username"
                name="username"
                label="Nombre de Usuario (Login)"
                value={formik.values.username}
                onChange={formik.handleChange}
                error={formik.touched.username && Boolean(formik.errors.username)}
                helperText={formik.touched.username && formik.errors.username}
              />
              <TextField
                fullWidth
                id="password"
                name="password"
                label="Contraseña"
                type="password"
                value={formik.values.password}
                onChange={formik.handleChange}
                error={formik.touched.password && Boolean(formik.errors.password)}
                helperText={formik.touched.password && formik.errors.password}
              />
              <TextField
                fullWidth
                id="nombreCompleto"
                name="nombreCompleto"
                label="Nombre y Apellidos"
                value={formik.values.nombreCompleto}
                onChange={formik.handleChange}
                error={formik.touched.nombreCompleto && Boolean(formik.errors.nombreCompleto)}
                helperText={formik.touched.nombreCompleto && formik.errors.nombreCompleto}
              />
              <TextField
                fullWidth
                id="email"
                name="email"
                label="Correo Electrónico"
                value={formik.values.email}
                onChange={formik.handleChange}
                error={formik.touched.email && Boolean(formik.errors.email)}
                helperText={formik.touched.email && formik.errors.email}
              />
              <FormControl fullWidth>
                <InputLabel>Rol de Autorización</InputLabel>
                <Select
                  id="rol"
                  name="rol"
                  value={formik.values.rol}
                  label="Rol de Autorización"
                  onChange={formik.handleChange}
                >
                  <MenuItem value="ROLE_USUARIO_OPERATIVO">Usuario Operativo (Mostrador)</MenuItem>
                  <MenuItem value="ROLE_ADMINISTRADOR">Administrador (Control Total)</MenuItem>
                </Select>
              </FormControl>
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpenModal(false)}>Cancelar</Button>
            <Button type="submit" variant="contained">Guardar Usuario</Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};
