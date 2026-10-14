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
  CircularProgress,
  Alert,
  TextField,
  Stack,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import SecurityIcon from '@mui/icons-material/Security';
import apiClient from '../../services/api/apiClient';
import { formatDateTime } from '../../utils/formatters';

export const AuditPage = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [moduloFiltro, setModuloFiltro] = useState('');

  const fetchLogs = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = new URLSearchParams({ size: 50 });
      if (moduloFiltro) params.append('modulo', moduloFiltro);

      const response = await apiClient.get(`/audit/logs?${params.toString()}`);
      setLogs(response.data.content || []);
    } catch (err) {
      setError('Error al consultar bitácora de auditoría');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [moduloFiltro]);

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">
          Bitácora Básica de Auditoría del Sistema (RN-11)
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Registro inmutable de trazabilidad operativa: Usuario responsable, Fecha y hora, y Descripción de la acción
        </Typography>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack direction="row" spacing={2} alignItems="center">
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Filtrar por Módulo</InputLabel>
            <Select
              value={moduloFiltro}
              label="Filtrar por Módulo"
              onChange={(e) => setModuloFiltro(e.target.value)}
            >
              <MenuItem value="">Todos los Módulos</MenuItem>
              <MenuItem value="VENTAS">Ventas</MenuItem>
              <MenuItem value="COMPRAS">Compras</MenuItem>
              <MenuItem value="CATALOGO">Catálogo</MenuItem>
              <MenuItem value="INVENTARIO">Inventario</MenuItem>
              <MenuItem value="AJUSTES">Ajustes</MenuItem>
              <MenuItem value="DEVOLUCIONES">Devoluciones</MenuItem>
              <MenuItem value="SEGURIDAD">Seguridad</MenuItem>
              <MenuItem value="USUARIOS">Usuarios</MenuItem>
            </Select>
          </FormControl>
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
                <TableCell><strong>Fecha / Hora</strong></TableCell>
                <TableCell><strong>Módulo</strong></TableCell>
                <TableCell><strong>Acción</strong></TableCell>
                <TableCell><strong>Usuario Responsable</strong></TableCell>
                <TableCell><strong>Descripción Detallada</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {logs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                    No hay eventos registrados en la bitácora de auditoría.
                  </TableCell>
                </TableRow>
              ) : (
                logs.map((log) => (
                  <TableRow key={log.id} hover>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDateTime(log.fechaHora)}</TableCell>
                    <TableCell>
                      <Chip label={log.modulo} size="small" color="primary" variant="outlined" />
                    </TableCell>
                    <TableCell sx={{ fontWeight: 'medium' }}>{log.accion}</TableCell>
                    <TableCell sx={{ fontWeight: 'bold' }}>{log.usuarioNombre}</TableCell>
                    <TableCell sx={{ maxWidth: 400 }}>{log.descripcion}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
};
