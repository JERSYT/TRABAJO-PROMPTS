import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../providers/AuthProvider';
import { MainLayout } from '../../components/layout/MainLayout';
import { LoginPage } from '../../features/auth/LoginPage';
import { DashboardPage } from '../../features/dashboard/DashboardPage';
import { CatalogPage } from '../../features/catalog/CatalogPage';
import { ThirdPartiesPage } from '../../features/customers/ThirdPartiesPage';
import { InventoryPage } from '../../features/inventory/InventoryPage';
import { POSSalePage } from '../../features/sales/POSSalePage';
import { SaleHistoryPage } from '../../features/sales/SaleHistoryPage';
import { PurchaseCreatePage } from '../../features/purchases/PurchaseCreatePage';
import { PurchaseHistoryPage } from '../../features/purchases/PurchaseHistoryPage';
import { ReturnsPage } from '../../features/returns/ReturnsPage';
import { VoucherPrintPage } from '../../features/documents/VoucherPrintPage';
import { AuditPage } from '../../features/audit/AuditPage';
import { UsersPage } from '../../features/users/UsersPage';
import { ObservabilityDashboard } from '../../features/reports/ObservabilityDashboard';
import { Box, CircularProgress } from '@mui/material';

const ProtectedRoute = ({ children, requiredRole }) => {
  const { user, loading, hasRole } = useAuth();

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to="/dashboard" replace />;
  }

  return <MainLayout>{children}</MainLayout>;
};

export const AppRouter = () => {
  const { user, loading } = useAuth();

  return (
    <BrowserRouter>
      <Routes>
        {/* Ruta pública de login */}
        <Route
          path="/login"
          element={!loading && user ? <Navigate to="/dashboard" replace /> : <LoginPage />}
        />

        {/* Rutas protegidas para todos los usuarios autenticados */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/ventas/nueva"
          element={
            <ProtectedRoute>
              <POSSalePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/ventas"
          element={
            <ProtectedRoute>
              <SaleHistoryPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/compras/nueva"
          element={
            <ProtectedRoute>
              <PurchaseCreatePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/compras"
          element={
            <ProtectedRoute>
              <PurchaseHistoryPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/inventario"
          element={
            <ProtectedRoute>
              <InventoryPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/catalogo"
          element={
            <ProtectedRoute>
              <CatalogPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/terceros"
          element={
            <ProtectedRoute>
              <ThirdPartiesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/devoluciones"
          element={
            <ProtectedRoute>
              <ReturnsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/comprobantes/:tipo/:consecutivo"
          element={
            <ProtectedRoute>
              <VoucherPrintPage />
            </ProtectedRoute>
          }
        />

        {/* Rutas exclusivas para ADMINISTRADOR */}
        <Route
          path="/observabilidad"
          element={
            <ProtectedRoute requiredRole="ROLE_ADMINISTRADOR">
              <ObservabilityDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/auditoria"
          element={
            <ProtectedRoute requiredRole="ROLE_ADMINISTRADOR">
              <AuditPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/usuarios"
          element={
            <ProtectedRoute requiredRole="ROLE_ADMINISTRADOR">
              <UsersPage />
            </ProtectedRoute>
          }
        />

        {/* Ruta por defecto */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};
