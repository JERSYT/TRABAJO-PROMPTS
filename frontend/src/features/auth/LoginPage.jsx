import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Alert,
  CircularProgress,
  Container,
} from '@mui/material';
import StorefrontIcon from '@mui/icons-material/Storefront';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { useAuth } from '../../app/providers/AuthProvider';
import { useNavigate, useLocation } from 'react-router-dom';

const validationSchema = Yup.object({
  username: Yup.string().required('El usuario es obligatorio'),
  password: Yup.string().required('La contraseña es obligatoria'),
});

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [errorMessage, setErrorMessage] = useState(null);

  const queryParams = new URLSearchParams(location.search);
  const isExpired = queryParams.get('expired') === 'true';

  const formik = useFormik({
    initialValues: {
      username: '',
      password: '',
    },
    validationSchema,
    onSubmit: async (values, { setSubmitting }) => {
      setErrorMessage(null);
      try {
        await login(values.username, values.password);
        navigate('/dashboard');
      } catch (err) {
        if (err.response && err.response.data && err.response.data.detail) {
          setErrorMessage(err.response.data.detail);
        } else if (err.response && err.response.status === 401) {
          setErrorMessage('Nombre de usuario o contraseña incorrectos');
        } else {
          setErrorMessage('No fue posible conectar con el servidor. Verifique que el backend esté activo.');
        }
      } finally {
        setSubmitting(false);
      }
    },
  });

  return (
    <Container maxWidth="xs" sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <Card elevation={4} sx={{ width: '100%', borderRadius: 3, p: 2 }}>
        <CardContent sx={{ textAlign: 'center' }}>
          <Box sx={{ display: 'inline-flex', p: 1.5, borderRadius: '50%', bgcolor: 'primary.light', color: 'primary.contrastText', mb: 2 }}>
            <StorefrontIcon fontSize="large" />
          </Box>
          <Typography variant="h5" component="h1" fontWeight="bold" gutterBottom>
            Ferretería El Tornillo
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Sistema de Gestión de Inventario — Iniciar Sesión
          </Typography>

          {isExpired && (
            <Alert severity="warning" sx={{ mb: 2 }}>
              Su sesión ha expirado por inactividad. Ingrese nuevamente.
            </Alert>
          )}

          {errorMessage && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {errorMessage}
            </Alert>
          )}

          <form onSubmit={formik.handleSubmit}>
            <TextField
              fullWidth
              id="username"
              name="username"
              label="Nombre de Usuario"
              variant="outlined"
              margin="normal"
              value={formik.values.username}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={formik.touched.username && Boolean(formik.errors.username)}
              helperText={formik.touched.username && formik.errors.username}
              autoFocus
            />

            <TextField
              fullWidth
              id="password"
              name="password"
              label="Contraseña"
              type="password"
              variant="outlined"
              margin="normal"
              value={formik.values.password}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={formik.touched.password && Boolean(formik.errors.password)}
              helperText={formik.touched.password && formik.errors.password}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              startIcon={<LockOutlinedIcon />}
              disabled={formik.isSubmitting}
              sx={{ mt: 3, mb: 1, py: 1.2 }}
            >
              {formik.isSubmitting ? <CircularProgress size={24} color="inherit" /> : 'Entrar al Sistema'}
            </Button>
          </form>

          <Box sx={{ mt: 3, p: 1.5, bgcolor: 'background.default', borderRadius: 2, textAlign: 'left' }}>
            <Typography variant="caption" display="block" fontWeight="bold">
              Credenciales de prueba aprobadas:
            </Typography>
            <Typography variant="caption" display="block">
              • <strong>Admin:</strong> admin_ferreteria / Admin123*
            </Typography>
            <Typography variant="caption" display="block">
              • <strong>Operativo:</strong> operador_mostrador / Operador123*
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Container>
  );
};
