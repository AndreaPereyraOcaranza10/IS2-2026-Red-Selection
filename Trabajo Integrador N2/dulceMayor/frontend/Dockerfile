# Dockerfile multi-stage del frontend. Una imagen por app:
#   docker build --build-arg APP=preventista   -t dulcemayor-preventista .
#   docker build --build-arg APP=transportista -t dulcemayor-transportista .
#   docker build --build-arg APP=cliente       -t dulcemayor-cliente .
#   docker build --build-arg APP=admin         -t dulcemayor-admin .
# nginx sirve la app y reenvía /api al servicio "backend" (ver nginx.conf y ../docker-compose.yml).

# ---------- 1) build ----------
FROM node:20-alpine AS build
ARG APP
ARG VITE_USE_MOCK=false
ARG VITE_API_URL=
ARG VITE_CLIENT_URL=
ARG VITE_ADMIN_URL=
ARG VITE_AUTH0_DOMAIN=
ARG VITE_AUTH0_CLIENT_ID=
ENV VITE_USE_MOCK=$VITE_USE_MOCK VITE_API_URL=$VITE_API_URL \
    VITE_CLIENT_URL=$VITE_CLIENT_URL VITE_ADMIN_URL=$VITE_ADMIN_URL \
    VITE_AUTH0_DOMAIN=$VITE_AUTH0_DOMAIN VITE_AUTH0_CLIENT_ID=$VITE_AUTH0_CLIENT_ID
WORKDIR /repo

# Capa de dependencias (cacheable): solo los package.json y el lockfile
COPY package.json package-lock.json ./
COPY shared/package.json shared/
COPY preventista/package.json preventista/
COPY transportista/package.json transportista/
COPY cliente/package.json cliente/
COPY admin/package.json admin/
RUN npm ci --no-audit --no-fund

COPY shared shared
COPY ${APP} ${APP}
RUN npm run build -w ${APP}

# ---------- 2) runtime ----------
FROM nginx:1.27-alpine
ARG APP
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /repo/${APP}/dist /usr/share/nginx/html
EXPOSE 80
# 127.0.0.1 y no localhost: en Alpine localhost resuelve primero a IPv6 y nginx escucha en IPv4
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q --spider http://127.0.0.1/ || exit 1
