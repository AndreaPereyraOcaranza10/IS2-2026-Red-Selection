# Dockerfile multi-stage del frontend. Una imagen por app:
#   docker build --build-arg APP=preventista   -t dulcemayor-preventista .
#   docker build --build-arg APP=transportista -t dulcemayor-transportista .

# ---------- 1) build ----------
FROM node:20-alpine AS build
ARG APP
ARG VITE_USE_MOCK=false
ARG VITE_API_URL=
ENV VITE_USE_MOCK=$VITE_USE_MOCK VITE_API_URL=$VITE_API_URL
WORKDIR /repo

# Capa de dependencias (cacheable): solo los package.json
COPY package.json ./
COPY shared/package.json shared/
COPY preventista/package.json preventista/
COPY transportista/package.json transportista/
RUN npm install --no-audit --no-fund

COPY shared shared
COPY ${APP} ${APP}
RUN npm run build -w ${APP}

# ---------- 2) runtime ----------
FROM nginx:1.27-alpine
ARG APP
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /repo/${APP}/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost/ >/dev/null || exit 1
