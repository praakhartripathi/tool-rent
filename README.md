# ToolRent - Multi-Tenant Tool Rental SaaS

ToolRent is a full-stack SaaS platform for rental businesses to onboard, publish inventory, accept bookings, and collect online payments with deposit handling.

## Current Status

- Backend: Spring Boot APIs for auth, tenant-scoped tools, bookings, and payments
- Frontend: React app with admin and customer flows
- Infra: Docker Compose (Postgres + Redis + backend + frontend)
- Multi-tenancy: `TenantFilter` via `X-Tenant-ID` or subdomain resolution

## Tech Stack

- Backend: Java 17, Spring Boot 3.2, Spring Security, JPA, Flyway
- Frontend: React 18, Vite, Tailwind CSS, Axios
- DB/Cache: PostgreSQL, Redis
- Payments: Razorpay
- Runtime: Docker, Nginx

## Quick Start (Docker)

```bash
docker compose up --build -d
docker compose ps
```

- Frontend: `http://localhost:80`
- Backend: `http://localhost:8080/api`

## API Overview

Base URL: `/api`

### Auth

- `POST /auth/register-tenant`
- `POST /auth/register-customer`
- `POST /auth/login`
- `POST /auth/refresh`
- `GET /auth/health`

### Tools

- `GET /tools?adminView=false`
- `GET /tools/{id}`
- `GET /tools/{id}/availability?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- `POST /tools` (admin)
- `PUT /tools/{id}` (admin)
- `DELETE /tools/{id}` (admin)

### Bookings

- `GET /bookings`
- `GET /bookings?myBookings=true`
- `POST /bookings`
- `PUT /bookings/{id}/confirm`
- `PUT /bookings/{id}/cancel`
- `PUT /bookings/{id}/complete`

### Payments

- `POST /payments/create-order`
- `POST /payments/verify`
- `POST /payments/webhook`

## Implemented MVP Flows

- Tenant registration and tenant admin login
- Customer registration/login under tenant context
- Tool CRUD with tenant isolation
- Availability overlap checks for booking dates
- Booking lifecycle: pending, confirmed, completed, cancelled
- Razorpay order creation and signature verification
- Admin dashboard + booking management UI
- Customer booking + checkout + my bookings UI
- Token refresh integration in frontend Axios interceptor

## Data Model

### Core migration

- `V1__init.sql`: tenants, users, storefronts, tools, tool_images, bookings, payments, refresh_tokens

### Enhancement migration

- `V2__enhancements.sql`:
  - Added fields: tool inventory (`sku`, `barcode`, `replacement_cost`, `purchase_date`)
  - Added fields: bookings (`pickup_time`, `return_time`, `booking_source`)
  - Added fields: payments (`gateway_fee`, `platform_fee`)
  - Added tables: `branches`, `deliveries`, `pricing_rules`, `damage_reports`, `maintenance_logs`, `risk_events`, `audit_logs`

## Frontend Routes

- Public: `/register`, `/login`, `/store/:subdomain`, `/store/:subdomain/tools/:toolId`
- Admin: `/dashboard`, `/tools`, `/tools/new`, `/tools/:id/edit`, `/bookings`
- Customer: `/my-bookings`, `/checkout/:bookingId`

## Project Report

A presentation-ready product report is available here:

- `PRODUCT_REPORT.md`

## Repository Structure

```text
tool-rent/
  toolrent-backend/
  toolrent-frontend/
  plan/
  PRODUCT_REPORT.md
  README.md
```
