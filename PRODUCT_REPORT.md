# ToolRent Product Report

## 1. Executive Summary

ToolRent is a multi-tenant SaaS platform for tool rental businesses. It digitizes the full rental workflow from inventory listing to booking and payment, while isolating each tenant's data and operations.

Current version delivers a working MVP plus foundational enhancement schema for future scaling.

## 2. Problem Statement

Rental businesses often depend on manual booking registers, fragmented payment collection, and no real-time inventory visibility. This causes revenue leakage, scheduling conflicts, and poor customer experience.

## 3. Solution

ToolRent provides:

- Tenant onboarding with isolated storefront context
- Online tool catalog and booking workflows
- Date overlap availability validation
- Digital payment collection via Razorpay (rental + deposit)
- Admin dashboard for bookings and operational control

## 4. Target Users

- Tenant Admin: rental business owner/manager
- Customer: renter booking tools online
- Platform Team (future): centralized operational and growth controls

## 5. Product Scope Delivered

### Authentication & Access

- Tenant registration
- Customer registration under tenant context
- Login and JWT access/refresh flow
- Frontend automatic token refresh on expiry

### Multi-Tenant Architecture

- Tenant resolution from `X-Tenant-ID` header or subdomain
- Tenant-scoped data access in services/repositories

### Inventory

- Tool CRUD APIs
- Admin UI for tool management
- Additional inventory fields: SKU, barcode, replacement cost, purchase date

### Booking

- Create booking with date range and amount calculation
- Overlap conflict prevention
- Booking status management (confirm/cancel/complete)
- Admin and customer booking UIs

### Payments

- Razorpay order creation
- Payment signature verification
- Webhook receiver for async updates
- Deposit-inclusive totals

## 6. Architecture Snapshot

- Frontend: React + Vite + Tailwind
- Backend: Spring Boot modular services (`auth`, `tenant`, `tool`, `booking`, `payment`)
- Database: PostgreSQL with Flyway migrations
- Cache/Support: Redis
- Containerization: Docker Compose

## 7. Database Status

### Core Tables (MVP)

- `tenants`, `users`, `storefronts`, `tools`, `tool_images`, `bookings`, `payments`, `refresh_tokens`

### Enhancement-Ready Tables

- `branches`
- `deliveries`
- `pricing_rules`
- `damage_reports`
- `maintenance_logs`
- `risk_events`
- `audit_logs`

## 8. Frontend Coverage

### Public

- Storefront listing
- Tool detail and booking initiation

### Tenant Admin

- Dashboard with top metrics
- Tool management
- Booking operations

### Customer

- My bookings
- Checkout and payment completion

## 9. Deployment & Operations

- Local and staging-friendly setup via `docker compose`
- Services: Postgres, Redis, backend, frontend
- Health checks configured for database, cache, and backend API

## 10. Security Highlights

- Password hashing using BCrypt
- JWT-based auth with refresh support
- Role-based admin route protection
- Razorpay signature validation

## 11. Validation Performed

- Backend Docker image build successful
- Frontend Docker image build successful
- Full stack startup successful via `docker compose up -d`
- Runtime status healthy for all services

## 12. Known Gaps (Roadmap)

- Chat and messaging module
- Reviews and ratings
- Notification integrations (SMS/email)
- Super admin control panel
- Analytics and business reports
- Complete APIs for newly added enhancement tables

## 13. Business Positioning

ToolRent is positioned as an India-first rental digitization platform with SaaS revenue potential through subscription tiers, transaction-linked add-ons, and future marketplace expansion.

## 14. Demo Narrative (Suggested)

1. Register a tenant
2. Add tools with pricing/deposit details
3. Open public storefront and create a booking
4. Complete checkout flow through payment order + verification
5. Review booking lifecycle changes in admin dashboard

## 15. Conclusion

The product is currently in a strong MVP state with production-oriented architecture choices and a clear path to post-MVP expansion. Core flows are functional, containerized, and ready for pilot demonstrations.
