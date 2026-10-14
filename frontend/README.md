# Frontend - Sistema de Gestión de Inventario para Ferretería

## Descripción General
Aplicación web Single Page Application (SPA) desarrollada con **React 18**, **Vite**, **Material UI (MUI v5)**, **React Router v6**, **Formik**, **Yup** y **Axios**. Se conecta con el backend Spring Boot manteniendo autenticación basada en sesiones de servidor con cookies `JSESSIONID` y protección automática contra CSRF (`X-XSRF-TOKEN`).

---

## 1. Requisitos de Entorno
* **Node.js:** Versión 18+ (probado y verificado en Node v24 LTS).
* **npm:** Versión 9+.

---

## 2. Variables de Entorno
El proyecto utiliza variables de entorno mediante Vite (prefijo `VITE_`):

| Variable | Valor Predeterminado | Descripción |
| :--- | :--- | :--- |
| `VITE_API_BASE_URL` | `/api/v1` | URL base del backend REST para peticiones HTTP. |

En desarrollo local, Vite redirige las llamadas `/api` a `http://localhost:8080` a través del proxy configurado en `vite.config.js`.

---

## 3. Instalación y Ejecución

### 3.1 Instalar Dependencias
```bash
npm install
```

### 3.2 Iniciar Servidor de Desarrollo
```bash
npm run dev
```
La aplicación estará accesible en: `http://localhost:5173`.

### 3.3 Construir para Producción (Build)
```bash
npm run build
```
Los artefactos estáticos optimizados se generarán en la carpeta `dist/`.

### 3.4 Previsualizar el Build Localmente
```bash
npm run preview
```

---

## 4. Estructura Modular del Proyecto
* `src/app/`: Configuración global (enrutador, proveedores de contexto y tema de Material UI).
* `src/components/`: Componentes transversales reutilizables (layout, feedback, formularios, tablas).
* `src/features/`: Módulos verticales de negocio (auth, catalog, customers, suppliers, inventory, purchases, sales, returns, documents, reports, health).
* `src/services/api/`: Instancia centralizada de Axios con soporte de cookies y CSRF.
* `src/utils/`: Formateadores estándar de moneda COP, fechas y cantidades.
