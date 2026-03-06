-- ToolRent enhancements schema updates

-- Additional inventory attributes
ALTER TABLE tools
    ADD COLUMN IF NOT EXISTS sku VARCHAR(100),
    ADD COLUMN IF NOT EXISTS barcode VARCHAR(100),
    ADD COLUMN IF NOT EXISTS replacement_cost NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS purchase_date DATE;

-- Booking lifecycle/analytics fields
ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS pickup_time TIMESTAMP,
    ADD COLUMN IF NOT EXISTS return_time TIMESTAMP,
    ADD COLUMN IF NOT EXISTS booking_source VARCHAR(50) DEFAULT 'web';

-- Accounting fields on payments
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS gateway_fee NUMERIC(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS platform_fee NUMERIC(10,2) NOT NULL DEFAULT 0;

-- Multi-branch support
CREATE TABLE IF NOT EXISTS branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    location VARCHAR(500) NOT NULL,
    manager_name VARCHAR(200),
    phone VARCHAR(30),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_branches_tenant_id ON branches(tenant_id);

-- Delivery workflow
CREATE TABLE IF NOT EXISTS deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    delivery_type VARCHAR(30) NOT NULL, -- self_pickup | store_delivery | third_party
    driver_name VARCHAR(200),
    status VARCHAR(30) NOT NULL DEFAULT 'scheduled',
    scheduled_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_deliveries_tenant_id ON deliveries(tenant_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_booking_id ON deliveries(booking_id);

-- Dynamic pricing rules
CREATE TABLE IF NOT EXISTS pricing_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    tool_id UUID NOT NULL REFERENCES tools(id) ON DELETE CASCADE,
    min_days INTEGER NOT NULL DEFAULT 1,
    discount_percent NUMERIC(5,2) NOT NULL DEFAULT 0,
    weekend_rate NUMERIC(10,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_pricing_rules_tenant_tool ON pricing_rules(tenant_id, tool_id);

-- Damage reporting
CREATE TABLE IF NOT EXISTS damage_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    image_url VARCHAR(500),
    deduction_amount NUMERIC(10,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_damage_reports_tenant_id ON damage_reports(tenant_id);
CREATE INDEX IF NOT EXISTS idx_damage_reports_booking_id ON damage_reports(booking_id);

-- Tool maintenance tracking
CREATE TABLE IF NOT EXISTS maintenance_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    tool_id UUID NOT NULL REFERENCES tools(id) ON DELETE CASCADE,
    service_date DATE NOT NULL,
    description TEXT,
    cost NUMERIC(10,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_maintenance_logs_tenant_tool ON maintenance_logs(tenant_id, tool_id);

-- Basic fraud/risk events
CREATE TABLE IF NOT EXISTS risk_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    booking_id UUID REFERENCES bookings(id) ON DELETE SET NULL,
    event_type VARCHAR(100) NOT NULL,
    risk_score INTEGER NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_risk_events_tenant_id ON risk_events(tenant_id);

-- Expanded audit logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(100),
    ip_address VARCHAR(64),
    user_agent TEXT,
    payload JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_audit_logs_tenant_id ON audit_logs(tenant_id);
