-- ==============================================================================
-- V1: Create Businesses (Tenants) and Users Tables
-- ==============================================================================

-- 1. Businesses Table (Tenants)
CREATE TABLE businesses (
    id BIGSERIAL PRIMARY KEY,
    business_name VARCHAR(150) NOT NULL,
    business_slug VARCHAR(120) NOT NULL UNIQUE,
    business_type VARCHAR(80),
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    address TEXT,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    timezone VARCHAR(60) NOT NULL DEFAULT 'Asia/Kolkata',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_businesses_slug ON businesses(business_slug);
CREATE INDEX idx_businesses_status ON businesses(status);

-- 2. Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT REFERENCES businesses(id) ON DELETE CASCADE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_business_id ON users(business_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
