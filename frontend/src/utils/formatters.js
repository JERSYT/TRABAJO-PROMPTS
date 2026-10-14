/**
 * Utilidades de formato para moneda colombiana (COP), cantidades físicas y fechas.
 */

// Formateador de moneda en pesos colombianos (COP) sin decimales visuales o con 2 según parámetro
export const formatCurrency = (amount, decimals = 0) => {
  if (amount === null || amount === undefined) return '$ 0';
  const num = Number(amount);
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(num);
};

// Formateador de cantidades físicas con hasta 3 decimales para unidades fraccionarias
export const formatQuantity = (quantity, maxDecimals = 3) => {
  if (quantity === null || quantity === undefined) return '0';
  const num = Number(quantity);
  return new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 0,
    maximumFractionDigits: maxDecimals,
  }).format(num);
};

// Formateador de fechas y horas estándar en Colombia
export const formatDateTime = (isoString) => {
  if (!isoString) return '-';
  const date = new Date(isoString);
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
};
