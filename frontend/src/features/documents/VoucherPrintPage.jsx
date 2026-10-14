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
  Button,
  Divider,
  CircularProgress,
  Alert,
  Stack,
  Container,
  Grid,
} from '@mui/material';
import PrintIcon from '@mui/icons-material/Print';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../../services/api/apiClient';
import { formatCurrency, formatQuantity, formatDateTime } from '../../utils/formatters';

/**
 * Vista de Comprobante Imprimible:
 * Diseñada para simular un ticket / factura de mostrador de una ferretería en Colombia (RN-12).
 * Formateada con estilos CSS limpios para impresión directa mediante window.print().
 */
export const VoucherPrintPage = () => {
  const { tipo, consecutivo } = useParams();
  const navigate = useNavigate();

  const [documento, setDocumento] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchComprobante = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await apiClient.get(`/documents/${tipo}/${consecutivo}`);
        setDocumento(response.data);
      } catch (err) {
        setError(err.response?.data?.detail || 'No se pudo cargar el comprobante solicitado');
      } finally {
        setLoading(false);
      }
    };

    fetchComprobante();
  }, [tipo, consecutivo]);

  const handlePrint = () => {
    window.print();
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error || !documento) {
    return (
      <Container maxWidth="sm" sx={{ py: 6 }}>
        <Alert severity="error" sx={{ mb: 2 }}>{error || 'Comprobante no encontrado'}</Alert>
        <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)}>
          Volver
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 3 }}>
      {/* Botones de acción (ocultos en la impresión mediante CSS @media print) */}
      <Box
        sx={{
          mb: 3,
          display: 'flex',
          justifyContent: 'space-between',
          '@media print': { display: 'none' },
        }}
      >
        <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)}>
          Volver a Historial
        </Button>
        <Button variant="contained" startIcon={<PrintIcon />} onClick={handlePrint} size="large">
          Imprimir Comprobante
        </Button>
      </Box>

      {/* Recibo estructurado */}
      <Paper
        elevation={3}
        sx={{
          p: 4,
          borderRadius: 2,
          bgcolor: 'background.paper',
          border: '1px solid #cbd5e1',
          '@media print': {
            boxShadow: 'none',
            border: 'none',
            p: 1,
          },
        }}
      >
        {/* Encabezado Ferretería */}
        <Box sx={{ textAlign: 'center', mb: 3 }}>
          <Typography variant="h5" fontWeight="bold">
            {documento.ferreteriaNombre}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            NIT: {documento.ferreteriaNit} — Régimen Simplificado (Demostración Académica)
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {documento.ferreteriaDireccion} | Tel: {documento.ferreteriaTelefono}
          </Typography>
          <Typography variant="caption" sx={{ display: 'block', mt: 0.5, fontStyle: 'italic', color: 'text.secondary' }}>
            * Facturación Electrónica Simulada — Sin integración real con la DIAN (RN-12) *
          </Typography>
        </Box>

        <Divider sx={{ my: 2 }} />

        {/* Datos del Documento y Tercero */}
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={6}>
            <Typography variant="subtitle2" color="text.secondary">COMPROBANTE INTERNO:</Typography>
            <Typography variant="h6" fontWeight="bold" sx={{ fontFamily: 'monospace' }}>
              {documento.tipoDocumento} #{documento.consecutivo}
            </Typography>
            <Typography variant="body2">
              <strong>Fecha / Hora:</strong> {formatDateTime(documento.fechaHora)}
            </Typography>
            <Typography variant="body2">
              <strong>Estado:</strong> {documento.estado}
            </Typography>
            <Typography variant="body2">
              <strong>Atendido por:</strong> {documento.usuarioResponsable}
            </Typography>
          </Grid>

          <Grid item xs={12} sm={6}>
            <Typography variant="subtitle2" color="text.secondary">
              {documento.tipoDocumento === 'COMPRA' ? 'DATOS DEL PROVEEDOR:' : 'DATOS DEL CLIENTE:'}
            </Typography>
            <Typography variant="body1" fontWeight="bold">
              {documento.terceroNombre || 'Consumidor Final'}
            </Typography>
            <Typography variant="body2">
              <strong>Identificación:</strong> {documento.terceroDocumento || '222222222222'}
            </Typography>
            {documento.terceroDireccion && (
              <Typography variant="body2"><strong>Dirección:</strong> {documento.terceroDireccion}</Typography>
            )}
            {documento.terceroTelefono && (
              <Typography variant="body2"><strong>Teléfono:</strong> {documento.terceroTelefono}</Typography>
            )}
          </Grid>
        </Grid>

        {documento.motivo && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            <strong>Motivo Documentado:</strong> {documento.motivo}
          </Alert>
        )}

        {/* Tabla de Renglones de Comprobante */}
        <TableContainer sx={{ mb: 3 }}>
          <Table size="small">
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                <TableCell><strong>Cód.</strong></TableCell>
                <TableCell><strong>Descripción del Artículo</strong></TableCell>
                <TableCell align="center"><strong>Cant.</strong></TableCell>
                <TableCell align="right"><strong>Precio Unit.</strong></TableCell>
                <TableCell align="right"><strong>Subtotal (COP)</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {documento.lineas?.map((l, index) => (
                <TableRow key={index}>
                  <TableCell sx={{ fontFamily: 'monospace' }}>{l.codigoBarras}</TableCell>
                  <TableCell>{l.descripcion}</TableCell>
                  <TableCell align="center">{formatQuantity(l.cantidad)} {l.unidad}</TableCell>
                  <TableCell align="right">{formatCurrency(l.valorUnitario)}</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(l.subtotal)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Totales y Liquidación en Efectivo */}
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 3 }}>
          <Box sx={{ width: 280 }}>
            <Stack spacing={1}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2">Subtotal:</Typography>
                <Typography variant="body2" fontWeight="bold">{formatCurrency(documento.subtotal || documento.total)}</Typography>
              </Box>
              <Divider />
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                <Typography variant="h6" fontWeight="bold">TOTAL COP:</Typography>
                <Typography variant="h5" fontWeight="bold" color="primary.main">
                  {formatCurrency(documento.total)}
                </Typography>
              </Box>

              {documento.montoPagado && (
                <>
                  <Divider />
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2">Efectivo Recibido:</Typography>
                    <Typography variant="body2">{formatCurrency(documento.montoPagado)}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" fontWeight="bold">Cambio Entregado:</Typography>
                    <Typography variant="body2" fontWeight="bold">{formatCurrency(documento.cambio)}</Typography>
                  </Box>
                </>
              )}
            </Stack>
          </Box>
        </Box>

        {/* Pie de Recibo */}
        <Box sx={{ textAlign: 'center', mt: 4, pt: 2, borderTop: '1px dashed #94a3b8' }}>
          <Typography variant="caption" display="block">
            ¡Gracias por su compra en Ferretería El Tornillo Central!
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Conserve este comprobante para cualquier trámite o devolución de mercancía.
          </Typography>
        </Box>
      </Paper>
    </Container>
  );
};
