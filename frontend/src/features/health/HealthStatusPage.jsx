import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  Chip,
  Button,
  CircularProgress,
  Divider,
  Stack,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import RefreshIcon from '@mui/icons-material/Refresh';
import ConstructionIcon from '@mui/icons-material/Construction';
import apiClient from '../../services/api/apiClient';

/**
 * Pantalla inicial neutra de verificación técnica para comprobar que el frontend
 * se ejecuta correctamente y que existe conectividad con la API del backend.
 */
export const HealthStatusPage = () => {
  const [healthData, setHealthData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchBackendHealth = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get('/health');
      setHealthData(response.data);
    } catch (err) {
      setError(err.message || 'Error al conectar con el backend');
      setHealthData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBackendHealth();
  }, []);

  return (
    <Container maxWidth="md" sx={{ py: 6 }}>
      <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <ConstructionIcon sx={{ fontSize: 40, color: 'primary.main', mr: 2 }} />
          <Box>
            <Typography variant="h5" component="h1">
              Sistema de Gestión de Inventario — Ferretería
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Etapa 3: Preparación Técnica y Scaffolding de Repositorios
            </Typography>
          </Box>
        </Box>

        <Divider sx={{ my: 2 }} />

        <Typography variant="h6" gutterBottom>
          Estado de la Infraestructura Técnica
        </Typography>

        <Stack spacing={2} sx={{ my: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="body1">
              <strong>Frontend (React 18 + Vite + Material UI):</strong>
            </Typography>
            <Chip
              icon={<CheckCircleIcon />}
              label="En ejecución (Puerto 5173)"
              color="success"
              variant="outlined"
            />
          </Box>

          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="body1">
              <strong>Backend REST API (/api/v1/health):</strong>
            </Typography>
            {loading ? (
              <CircularProgress size={24} />
            ) : healthData ? (
              <Chip
                icon={<CheckCircleIcon />}
                label={`Conectado (${healthData.status})`}
                color="success"
              />
            ) : (
              <Chip
                icon={<ErrorOutlineIcon />}
                label={error ? 'Sin Conexión' : 'Desconocido'}
                color="warning"
              />
            )}
          </Box>
        </Stack>

        {healthData && (
          <Paper variant="outlined" sx={{ p: 2, bgcolor: 'background.default', mb: 3 }}>
            <Typography variant="caption" display="block" color="text.secondary">
              Respuesta del Endpoint Técnico:
            </Typography>
            <pre style={{ margin: 0, fontSize: '0.85rem' }}>
              {JSON.stringify(healthData, null, 2)}
            </pre>
          </Paper>
        )}

        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={fetchBackendHealth}
            disabled={loading}
          >
            Reintentar Conexión
          </Button>
          <Button variant="contained" href="/login">
            Ir a Iniciar Sesión
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};
