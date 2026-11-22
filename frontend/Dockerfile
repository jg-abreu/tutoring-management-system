# Frontend image. Stage 1 builds for production; stage 2 serves it with nginx.

FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# The browser calls /api on nginx itself, which forwards to the API (see default.conf.template).
# That way frontend and API share the same origin and CORS isn't involved.
ENV VITE_API_URL=/api
RUN npm run build

FROM nginx:1.27-alpine
# Where nginx forwards /api/. The default reaches an API running on the Docker host.
ENV API_URL=http://host.docker.internal:8080
COPY default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
