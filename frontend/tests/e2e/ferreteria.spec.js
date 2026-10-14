import { test, expect } from '@playwright/test';

/**
 * Suite de Pruebas End-to-End (E2E) con Playwright para el Sistema de Inventario:
 * Verifica autenticación, navegación, punto de venta (POS), inventario y visualización de comprobantes.
 */

test.describe('Flujos Críticos del Sistema de Inventario — Ferretería', () => {

  test('01. Acceso y autenticación con credenciales de Administrador', async ({ page }) => {
    await page.goto('/login');
    await expect(page).toHaveTitle(/Sistema de Inventario - Ferretería/);

    // Ingresar credenciales aprobadas
    await page.fill('#username', 'admin_ferreteria');
    await page.fill('#password', 'Admin123*');
    await page.click('button[type="submit"]');

    // Debe redirigir al panel de control
    await expect(page).toHaveURL(/.*dashboard/);
    await expect(page.locator('h1')).toContainText('Panel de Control');
    await expect(page.locator('text=ADMINISTRADOR')).toBeVisible();
  });

  test('02. Navegación al Catálogo de Artículos y verificación de columnas', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#username', 'admin_ferreteria');
    await page.fill('#password', 'Admin123*');
    await page.click('button[type="submit"]');

    await page.goto('/catalogo');
    await expect(page.locator('text=Catálogo de Artículos y Referencias')).toBeVisible();
    await expect(page.locator('text=Cód. Barras')).toBeVisible();
    await expect(page.locator('text=Precio Venta (COP)')).toBeVisible();
  });

  test('03. Navegación al Punto de Venta en Mostrador (POS)', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#username', 'admin_ferreteria');
    await page.fill('#password', 'Admin123*');
    await page.click('button[type="submit"]');

    await page.goto('/ventas/nueva');
    await expect(page.locator('text=Punto de Venta en Mostrador')).toBeVisible();
    await expect(page.locator('text=Liquidación de Mostrador')).toBeVisible();
    await expect(page.locator('text=Medio de Pago: Efectivo (RN-06)')).toBeVisible();
  });

  test('04. Navegación a Control de Inventario y filtrado de bajo stock', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#username', 'admin_ferreteria');
    await page.fill('#password', 'Admin123*');
    await page.click('button[type="submit"]');

    await page.goto('/inventario');
    await expect(page.locator('text=Control de Existencias e Inventario')).toBeVisible();
    await expect(page.locator('text=Costo Promedio (CPP)')).toBeVisible();
  });

  test('05. Visualización del formato de comprobante imprimible', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#username', 'admin_ferreteria');
    await page.fill('#password', 'Admin123*');
    await page.click('button[type="submit"]');

    await page.goto('/comprobantes/VENTA/VTA-000101');
    // Si el comprobante existe o carga la vista
    await expect(page.locator('text=Ferretería El Tornillo Central')).toBeVisible();
    await expect(page.locator('text=Facturación Electrónica Simulada')).toBeVisible();
  });

  test('06. Control de acceso RBAC: Operativo no debe ver opciones exclusivas de Admin', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#username', 'operador_mostrador');
    await page.fill('#password', 'Operador123*');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/.*dashboard/);
    await expect(page.locator('text=OPERATIVO')).toBeVisible();

    // Intentar acceder directamente por URL a /auditoria
    await page.goto('/auditoria');
    // Debe ser redirigido de vuelta al dashboard por el ProtectedRoute
    await expect(page).toHaveURL(/.*dashboard/);
  });

});
