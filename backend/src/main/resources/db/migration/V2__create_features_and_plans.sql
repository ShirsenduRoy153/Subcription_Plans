-- ==============================================================================
-- V2: Create Business Features, Subscription Plans, and Plan Features
-- ==============================================================================

-- 1. Business Features (Dynamic benefit/entitlement definition per business)
CREATE TABLE business_features (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    feature_name VARCHAR(100) NOT NULL,
    feature_code VARCHAR(100) NOT NULL,
    description TEXT,
    feature_type VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_business_feature_code UNIQUE (business_id, feature_code)
);

CREATE INDEX idx_business_features_business_id ON business_features(business_id);
CREATE INDEX idx_business_features_is_active ON business_features(is_active);

-- 2. Subscription Plans
CREATE TABLE plans (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    plan_name VARCHAR(100) NOT NULL,
    plan_code VARCHAR(100),
    description TEXT,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    billing_interval VARCHAR(20) NOT NULL,
    billing_interval_count INTEGER NOT NULL DEFAULT 1 CHECK (billing_interval_count > 0),
    trial_days INTEGER NOT NULL DEFAULT 0 CHECK (trial_days >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_plans_business_id ON plans(business_id);
CREATE INDEX idx_plans_is_active ON plans(is_active);

-- 3. Plan Features Join Table (Stores value for each feature in a plan)
CREATE TABLE plan_features (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    feature_id BIGINT NOT NULL REFERENCES business_features(id) ON DELETE RESTRICT,
    value_boolean BOOLEAN,
    value_number NUMERIC(14, 2),
    value_text TEXT,
    value_json JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_plan_feature UNIQUE (plan_id, feature_id)
);

CREATE INDEX idx_plan_features_plan_id ON plan_features(plan_id);
CREATE INDEX idx_plan_features_feature_id ON plan_features(feature_id);

