-- ==============================================================================
-- V1: Initialize Domain Lookups, Roles, Users, Businesses, and Business Members
-- ==============================================================================

-- 1. Unified Reference / Domain Lookups Table
CREATE TABLE domain_lookups (
    id BIGSERIAL PRIMARY KEY,
    domain_type VARCHAR(50) NOT NULL,
    lookup_code VARCHAR(50) NOT NULL,
    lookup_value VARCHAR(150) NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_domain_type_code UNIQUE (domain_type, lookup_code)
);

CREATE INDEX idx_domain_lookups_type ON domain_lookups(domain_type, is_active, sort_order);

-- Pre-seed Domain Data
INSERT INTO domain_lookups (domain_type, lookup_code, lookup_value, description, sort_order) VALUES
('BUSINESS_TYPE', 'GYM', 'Fitness Club & Gym', 'Gyms, martial arts, yoga, and fitness centers', 1),
('BUSINESS_TYPE', 'SAAS', 'SaaS & Software', 'Software as a service, developer tools, digital platforms', 2),
('BUSINESS_TYPE', 'COACHING', 'Coaching & Education', 'Tuition centers, recorded courses, educational institutes', 3),
('BUSINESS_TYPE', 'CLUB', 'Club & Community', 'Social clubs, mastermind communities, membership spaces', 4),
('BUSINESS_TYPE', 'CLINIC', 'Healthcare & Wellness', 'Clinics, dietitians, wellness centers', 5),
('BUSINESS_TYPE', 'OTHER', 'Other Business', 'General subscription businesses', 6),

('CURRENCY', 'INR', 'Indian Rupee (₹)', 'Indian Rupee currency', 1),
('CURRENCY', 'USD', 'US Dollar ($)', 'United States Dollar currency', 2),
('CURRENCY', 'EUR', 'Euro (€)', 'European Union Euro currency', 3),
('CURRENCY', 'GBP', 'British Pound (£)', 'British Pound Sterling currency', 4);

-- 2. Global Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);

-- 3. Businesses (Workspaces / Tenants) Table
CREATE TABLE businesses (
    id BIGSERIAL PRIMARY KEY,
    business_name VARCHAR(150) NOT NULL,
    business_slug VARCHAR(120) NOT NULL UNIQUE,
    business_type_id BIGINT NOT NULL REFERENCES domain_lookups(id),
    currency_id BIGINT NOT NULL REFERENCES domain_lookups(id),
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    address TEXT,
    timezone VARCHAR(60) NOT NULL DEFAULT 'Asia/Kolkata',
    logo_url VARCHAR(500),
    cover_image_url VARCHAR(500),
    primary_color VARCHAR(10) DEFAULT '#4F46E5',
    tagline VARCHAR(255),
    razorpay_key_id VARCHAR(100),
    razorpay_key_secret VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_businesses_slug ON businesses(business_slug);
CREATE INDEX idx_businesses_status ON businesses(status);

-- 4. Dedicated Roles Table (Dynamic RBAC & Hierarchy)
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT REFERENCES businesses(id) ON DELETE CASCADE,
    role_name VARCHAR(100) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    hierarchy_level INTEGER NOT NULL DEFAULT 10,
    permissions JSONB NOT NULL DEFAULT '[]',
    is_system_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_role_code_per_business UNIQUE (business_id, role_code)
);

CREATE UNIQUE INDEX uq_global_system_roles ON roles(role_code) WHERE business_id IS NULL;
CREATE INDEX idx_roles_business_id ON roles(business_id);

-- Pre-seed Global System Roles
INSERT INTO roles (business_id, role_name, role_code, hierarchy_level, permissions, is_system_default) VALUES
(NULL, 'Workspace Owner', 'OWNER', 100, '["*"]'::jsonb, TRUE),
(NULL, 'Administrator', 'ADMIN', 80, '["plans:write", "members:write", "analytics:read"]'::jsonb, TRUE),
(NULL, 'Staff Member', 'STAFF', 40, '["members:read", "checkin:write"]'::jsonb, TRUE),
(NULL, 'Customer', 'CUSTOMER', 10, '["my_subscription:read", "invoices:read"]'::jsonb, TRUE);

-- 5. Business Members (User <-> Business Many-to-Many Join Table)
CREATE TABLE business_members (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_business_member UNIQUE (business_id, user_id)
);

CREATE INDEX idx_business_members_business_id ON business_members(business_id);
CREATE INDEX idx_business_members_user_id ON business_members(user_id);
CREATE INDEX idx_business_members_role_id ON business_members(role_id);

