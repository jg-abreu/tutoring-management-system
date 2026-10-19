# Imagem do frontend. Etapa 1 gera o build de produção; etapa 2 serve com nginx.

FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# O navegador chama /api no próprio nginx, que repassa para a API (ver nginx.conf).
# Assim frontend e API ficam na mesma origem e não há CORS envolvido.
ENV VITE_API_URL=/api
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
