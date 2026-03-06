# ToolRent — Multi-Tenant Tool Rental SaaS

> **Rent the tools you need, when you need them.**
> A full-stack SaaS platform where rental businesses can onboard, list tools, and accept bookings with online payments.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 3.2, Java 17 |
| Database | PostgreSQL + Flyway Migrations |
| Cache | Redis |
| Security | Spring Security + JWT (JJWT) |
| Payments | Razorpay |
| Frontend | React 18 + Vite + Tailwind CSS |
| Routing | React Router v6 |
| HTTP Client | Axios |

---

## Architecture

### Multi-Tenant Model

Every request is scoped to a tenant (rental business) via subdomain resolution:

```
abc.toolrent.in  →  TenantFilter extracts "abc"  →  TenantContext.setCurrentTenant("abc")
```

Or using the `X-Tenant-ID` header for API clients / mobile apps.

### Database Tables (8 total)

```
tenants          Users (businesses)
users            Admins + customers per tenant
storefronts      Public store profile
tools            Tool inventory per tenant
tool_images      Tool photo gallery
bookings         Rental bookings with status
payments         Razorpay payment records
refresh_tokens   JWT refresh token store
```

### Backend Modules (5)

```
auth/       → Register tenant, login, JWT
tenant/     → TenantContext, TenantFilter, Tenant entity
tool/       → CRUD for tool inventory
booking/    → Booking lifecycle + availability engine
payment/    → Razorpay create-order, verify, webhook
```

---

## Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Redis

### 1. Database Setup

```sql
CREATE DATABASE toolrent;
CREATE USER toolrent WITH PASSWORD 'toolrent';
GRANT ALL PRIVILEGES ON DATABASE toolrent TO toolrent;
```

### 2. Backend

```bash
cd toolrent-backend

# Set environment variables (or edit application.yml)
export DB_USERNAME=toolrent
export DB_PASSWORD=toolrent
export JWT_SECRET=your-super-secret-key-32-chars-min
export RAZORPAY_KEY_ID=rzp_test_xxxx
export RAZORPAY_KEY_SECRET=your_razorpay_secret

# Run (Flyway will auto-create tables on first start)
./mvnw spring-boot:run
```

Backend runs at: `http://localhost:8080/api`

### 3. Frontend

```bash
cd toolrent-frontend
npm install
npm run dev
```

Frontend runs at: `http://localhost:5173`

---

## API Reference

### Auth

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register-tenant` | Public | Register a new rental business |
| POST | `/api/auth/login` | Public | Login and get JWT tokens |
| POST | `/api/auth/refresh` | Public | Refresh access token |

### Tools

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/tools` | Public | List available tools (storefront) |
| GET | `/api/tools/{id}` | Public | Get tool details |
| POST | `/api/tools` | Admin | Create a new tool |
| PUT | `/api/tools/{id}` | Admin | Update tool details |
| DELETE | `/api/tools/{id}` | Admin | Delete a tool |

### Bookings

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/bookings` | Auth | List all bookings (admin) |
| GET | `/api/bookings?myBookings=true` | Auth | My bookings (customer) |
| POST | `/api/bookings` | Auth | Create new booking |
| PUT | `/api/bookings/{id}/confirm` | Admin | Confirm a booking |
| PUT | `/api/bookings/{id}/cancel` | Auth | Cancel a booking |
| PUT | `/api/bookings/{id}/complete` | Admin | Mark booking completed |

### Payments

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/payments/create-order` | Auth | Create Razorpay order |
| POST | `/api/payments/verify` | Auth | Verify payment signature |
| POST | `/api/payments/webhook` | Public | Razorpay webhook handler |

---

## Frontend Pages (10)

| Page | Route | Who |
|------|-------|-----|
| `RegisterPage` | `/register` | New tenants |
| `LoginPage` | `/login` | All users |
| `TenantDashboard` | `/dashboard` | Admin |
| `ToolsPage` | `/tools` | Admin |
| `AddToolPage` | `/tools/new`, `/tools/:id/edit` | Admin |
| `BookingsPage` | `/bookings` | Admin |
| `StorefrontPage` | `/store/:subdomain` | Public |
| `ToolDetail` | `/store/:subdomain/tools/:toolId` | Public |
| `MyBookingsPage` | `/my-bookings` | Customer |
| `CheckoutPage` | `/checkout/:bookingId` | Customer |

---

## Customer Booking Flow

```
Customer visits abc.toolrent.in
→ Browses tool grid
→ Selects tool → picks dates → total auto-calculated
→ Clicks "Book Now" → creates booking (PENDING)
→ Redirected to /checkout/:bookingId
→ Razorpay modal opens → customer pays
→ Backend verifies signature → booking status → CONFIRMED
→ Redirected to /my-bookings
```

## Availability Engine

Prevents double booking using an overlap query:

```sql
SELECT COUNT(*) FROM bookings
WHERE tool_id = ?
  AND status IN ('PENDING', 'CONFIRMED')
  AND start_date <= requested_end_date
  AND end_date   >= requested_start_date
```

If `count >= tool.quantity` → booking rejected.

---

## Payment Flow (Razorpay)

```
POST /payments/create-order
  → Creates Razorpay order (amount in paise)
  → Stores Payment record (status: CREATED)

Frontend opens Razorpay modal

POST /payments/verify
  → Validates HMAC-SHA256 signature
  → Updates Payment (status: CAPTURED)
  → Auto-confirms Booking (status: CONFIRMED)

POST /payments/webhook (async)
  → Handles payment.captured / payment.failed / refund.created
```

---

## Security Notes

- All passwords hashed with BCrypt
- JWT access token: 1 hour expiry
- JWT refresh token: 7 days expiry
- Razorpay signature verified server-side using HMAC-SHA256
- All admin APIs guarded with `@PreAuthorize("hasRole('TENANT_ADMIN')")`
- CORS configured for `*.toolrent.in` + localhost

---

## Deployment (Week 3 Target)

```
AWS EC2        → Spring Boot JAR + Nginx reverse proxy
PostgreSQL RDS → Managed database
Cloudflare     → Wildcard DNS *.toolrent.in → EC2
Docker         → Containerized deployment
SSL            → Let's Encrypt via Certbot
```

Sample Nginx config for multi-tenant subdomain routing:

```nginx
server {
    server_name *.toolrent.in;

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header X-Tenant-ID $subdomain;
    }

    location / {
        proxy_pass http://localhost:5173;
    }
}
```

---

## MVP Scope (3 Weeks)

### ✅ Included
- Multi-tenant SaaS architecture
- Tenant registration + JWT authentication
- Tool inventory management
- Booking system with availability engine
- Razorpay payment + security deposit
- Admin dashboard (stats, bookings, tools)
- Public storefront per tenant

### 🔜 Post-MVP
- In-app chat
- SMS notifications (Twilio)
- Customer reviews
- Analytics & reports
- Super admin panel
- AI-based pricing suggestions

---

## Project Structure

```
tool-rent/
├── toolrent-backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/in/toolrent/
│       │   ├── ToolRentApplication.java
│       │   ├── auth/          # JWT, User, AuthService, AuthController
│       │   ├── tenant/        # Tenant, TenantContext, TenantFilter
│       │   ├── tool/          # Tool, ToolService, ToolController
│       │   ├── booking/       # Booking, BookingService, BookingController
│       │   ├── payment/       # Payment, PaymentService (Razorpay)
│       │   └── config/        # SecurityConfig
│       └── resources/
│           ├── application.yml
│           └── db/migration/V1__init.sql
│
├── toolrent-frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── tailwind.config.js
│   └── src/
│       ├── api/api.js         # Axios client + all API calls
│       ├── context/AuthContext.jsx
│       ├── App.jsx            # React Router setup
│       ├── main.jsx
│       └── pages/
│           ├── RegisterPage.jsx
│           ├── LoginPage.jsx
│           ├── TenantDashboard.jsx
│           ├── ToolsPage.jsx
│           ├── AddToolPage.jsx
│           ├── BookingsPage.jsx
│           ├── StorefrontPage.jsx
│           ├── ToolDetail.jsx
│           ├── MyBookingsPage.jsx
│           └── CheckoutPage.jsx
│
├── plan/
│   └── toolrent_mvp_3weeks.txt
└── README.md
```

---

*Built with ❤️ for Indian rental businesses · ToolRent MVP v0.1*
