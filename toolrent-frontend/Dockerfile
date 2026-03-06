# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Layer cache: install deps first
COPY package*.json ./
RUN if [ -f package-lock.json ]; then npm ci --silent; else npm install --silent; fi

# Copy source and build
COPY . .
RUN npm run build

# ── Stage 2: Nginx serving ───────────────────────────────────────────────────
FROM nginx:alpine AS runtime

LABEL maintainer="ToolRent <hello@toolrent.in>"
LABEL description="ToolRent SaaS Frontend — React + Vite served by Nginx"

# Remove default config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/toolrent.conf

# Copy built assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
