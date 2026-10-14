# ------------------------------------------------------------------------------
# ETAPA 1: Construcción (Node 20+ con Vite)
# ------------------------------------------------------------------------------
FROM node:20-alpine AS builder
WORKDIR /app

# Instalar dependencias
COPY package*.json ./
RUN npm ci

# Copiar código fuente y compilar
COPY . ./
RUN npm run build

# ------------------------------------------------------------------------------
# ETAPA 2: Servidor Nginx Optimizado para Producción
# ------------------------------------------------------------------------------
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Eliminar configuración por defecto de Nginx
RUN rm -rf ./* /etc/nginx/conf.d/default.conf

# Copiar bundle compilado desde la etapa builder
COPY --from=builder /app/dist ./

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Health check del frontend
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q --spider http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
