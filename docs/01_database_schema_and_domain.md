# Database Schema & Domain Architecture Specification

This document defines the core relational database schema, tables, data types, constraints, and relationships for the Multi-Tenant Subscription Management Platform.

---

## 1. Architectural Philosophy

1. **PostgreSQL as the Single Source of Truth**: All critical business state (workspaces, users, memberships, packages, plans, and subscriptions) is enforced via ACID transactions and foreign keys.
2. **Unified Reference Data Pattern (`domain_lookups`)**: Dropdown options, categories, currencies, and roles are managed centrally in a single reference table with customizable sort orders, rather than hardcoded enums.
3. **Many-to-Many Tenant Model (`business_members`)**: A user account exists globally and can belong to multiple businesses with different roles (e.g. Owner in Workspace A, Staff in Workspace B).
4. **Hybrid Relational & JSONB Modeling**:
   - Master feature definitions and plan pricing are stored relationally for validation and querying.
   - Package feature values and customer subscription entitlements are stored as frozen `JSONB` snapshots for $O(1)$ fast reads and complete protection against upstream plan modifications.

---

## 2. Table Specifications

### Section A: Domain Reference Data

#### `domain_lookups`
Stores system categories, business types, currencies, and member roles dynamically.

| Column | Type | Constraints | Description & Example |
|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Unique ID |
| `domain_type` | `VARCHAR(50)` | `NOT NULL` | Category identifier (e.g. `'BUSINESS_TYPE'`, `'CURRENCY'`, `'MEMBER_ROLE'`) |
| `lookup_code` | `VARCHAR(50)` | `NOT NULL` | Machine-readable code (e.g. `'GYM'`, `'INR'`, `'OWNER'`) |
| `lookup_value` | `VARCHAR(150)` | `NOT NULL` | Human-readable label (e.g. `'Fitness Club & Gym'`, `'Indian Rupee (₹)'`) |
| `description` | `TEXT` | *Nullable* | Optional notes describing the option |
| `sort_order` | `INTEGER` | `NOT NULL DEFAULT 0` | Display sorting priority for UI dropdowns (`1, 2, 3...`) |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE`| Active flag (allows deactivation without hard deletion) |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Creation timestamp |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Last modification timestamp |

* **Constraints:** `UNIQUE(domain_type, lookup_code)`
* **Indexes:** `CREATE INDEX idx_domain_lookups_type ON domain_lookups(domain_type, is_active);`

---

### Section B: Identity & Multi-Tenancy (Many-to-Many)

#### `users`
Global identity table. A single person account can access multiple business workspaces.

| Column | Type | Constraints | Description & Example |
|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Unique User ID |
| `email` | `VARCHAR(255)` | `NOT NULL UNIQUE` | Global login email (e.g. `alex@example.com`) |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | BCrypt password hash |
| `full_name` | `VARCHAR(150)` | `NOT NULL` | Person's display name (e.g. `"Alex Mercer"`) |
| `phone` | `VARCHAR(30)` | *Nullable* | Phone number (e.g. `"+91 9876543210"`) |
| `email_verified`| `BOOLEAN` | `NOT NULL DEFAULT FALSE` | Email verification flag (via OTP) |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'ACTIVE'` | Account status (`ACTIVE`, `SUSPENDED`) |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Account creation date |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Last update timestamp |

* **Indexes:** `CREATE INDEX idx_users_email ON users(email);`

---

#### `businesses`
Workspace/tenant table representing individual companies using the platform.

| Column | Type | Constraints | Description & Example |
|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Unique Business ID |
| `business_name` | `VARCHAR(150)` | `NOT NULL` | Legal / Brand Name (e.g. `"FitZone Gym"`) |
| `business_slug` | `VARCHAR(120)` | `NOT NULL UNIQUE` | URL-safe slug (e.g. `"fitzone-gym"`) |
| `business_type_id`| `BIGINT` | `NOT NULL REFERENCES domain_lookups(id)` | Foreign key to business type lookup |
| `currency_id` | `BIGINT` | `NOT NULL REFERENCES domain_lookups(id)` | Foreign key to default currency lookup |
| `email` | `VARCHAR(255)` | `NOT NULL` | Contact / support email |
| `phone` | `VARCHAR(30)` | *Nullable* | Business telephone |
| `address` | `TEXT` | *Nullable* | Physical facility address |
| `timezone` | `VARCHAR(60)` | `NOT NULL DEFAULT 'Asia/Kolkata'` | Workspace timezone |
| `logo_url` | `VARCHAR(500)` | *Nullable* | Custom branding: logo image URL |
| `cover_image_url` | `VARCHAR(500)` | *Nullable* | Custom branding: hero banner image URL |
| `primary_color` | `VARCHAR(10)` | *Nullable* | Custom branding: brand hex color (e.g. `"#FF5722"`) |
| `tagline` | `VARCHAR(255)` | *Nullable* | Custom branding: storefront slogan |
| `razorpay_key_id` | `VARCHAR(100)` | *Nullable* | BYOK: Business's own Razorpay Key ID |
| `razorpay_key_secret`| `VARCHAR(100)` | *Nullable* | BYOK: Business's own Razorpay Secret |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'ACTIVE'` | Workspace status (`ACTIVE`, `SUSPENDED`) |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Creation timestamp |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Last update timestamp |

* **Indexes:** `CREATE INDEX idx_businesses_slug ON businesses(business_slug);`

---

#### `business_members`
Many-to-Many join table linking users to business workspaces.

| Column | Type | Constraints | Description & Example |
|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Unique Membership ID |
| `business_id` | `BIGINT` | `NOT NULL REFERENCES businesses(id) ON DELETE CASCADE` | Workspace reference |
| `user_id` | `BIGINT` | `NOT NULL REFERENCES users(id) ON DELETE CASCADE` | User reference |
| `role_id` | `BIGINT` | `NOT NULL REFERENCES domain_lookups(id)` | Foreign key to member role lookup (`OWNER`, `ADMIN`, `STAFF`, `CUSTOMER`) |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'ACTIVE'` | Membership status (`ACTIVE`, `INVITED`, `DEACTIVATED`) |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Joined timestamp |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Last update timestamp |

* **Constraints:** `UNIQUE(business_id, user_id)` (A user has exactly one membership record per business)
* **Indexes:** `CREATE INDEX idx_business_members_user ON business_members(user_id);`
* **Indexes:** `CREATE INDEX idx_business_members_business ON business_members(business_id);`

---

### Section C: Catalog (Features, Packages, Plans)

#### `features`
The catalog of raw capabilities, limits, and perks defined by a business.

| Column | Type | Constraints | Description & Example |
|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Unique Feature ID |
| `business_id` | `BIGINT` | `NOT NULL REFERENCES businesses(id) ON DELETE CASCADE` | Business owning this feature |
| `feature_name` | `VARCHAR(100)` | `NOT NULL` | Display title (e.g. `"Guest Passes"`) |
| `feature_code` | `VARCHAR(100)` | `NOT NULL` | Programmatic code (e.g. `"guest_passes"`) |
| `feature_type` | `VARCHAR(20)` | `NOT NULL` | `BOOLEAN`, `NUMBER`, `TEXT`, `LIST` |
| `description` | `TEXT` | *Nullable* | Detailed description |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Active toggle |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Creation timestamp |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Last update timestamp |

* **Constraints:** `UNIQUE(business_id, feature_code)`

---

#### `packages`
A named tier bundling features together. Feature values are stored as a structured `JSONB` array for clean management and instant snapshotting.

| Column | Type | Constraints | Description & Example |
|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Unique Package ID |
| `business_id` | `BIGINT` | `NOT NULL REFERENCES businesses(id) ON DELETE CASCADE` | Business workspace |
| `package_name` | `VARCHAR(100)` | `NOT NULL` | Title (e.g. `"Gold VIP Tier"`) |
| `description` | `TEXT` | *Nullable* | Overview of the package |
| `features` | `JSONB` | `NOT NULL` | Array of feature codes and values (e.g. `[{"code": "gym_access", "value": true}, {"code": "guest_passes", "value": 5}]`) |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Can this package be sold? |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Creation timestamp |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Last update timestamp |

---

#### `plans`
Priced offerings attaching billing cycles and currency to a Package.

| Column | Type | Constraints | Description & Example |
|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Unique Plan ID |
| `business_id` | `BIGINT` | `NOT NULL REFERENCES businesses(id) ON DELETE CASCADE` | Business workspace |
| `package_id` | `BIGINT` | `NOT NULL REFERENCES packages(id) ON DELETE CASCADE` | Foreign key to package bundle |
| `plan_name` | `VARCHAR(100)` | `NOT NULL` | Title (e.g. `"Gold VIP - Monthly"`) |
| `price` | `NUMERIC(12, 2)`| `NOT NULL CHECK (price >= 0)` | Recurring price (e.g. `1999.00`) |
| `currency_id` | `BIGINT` | `NOT NULL REFERENCES domain_lookups(id)` | Foreign key to currency lookup (`INR`, `USD`) |
| `billing_interval` | `VARCHAR(20)` | `NOT NULL` | `DAY`, `WEEK`, `MONTH`, `YEAR` |
| `billing_interval_count` | `INTEGER` | `NOT NULL DEFAULT 1 CHECK (billing_interval_count > 0)` | Interval multiplier |
| `trial_days` | `INTEGER` | `NOT NULL DEFAULT 0 CHECK (trial_days >= 0)` | Free trial days |
| `is_active` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` | Active toggle |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Creation timestamp |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT NOW()` | Last update timestamp |

---

## 3. Entity-Relationship Overview

```text
 domain_lookups (Types, Currencies, Roles)
       │              │               │
       │ (type, curr) │ (role)        │ (currency)
       ▼              ▼               ▼
  businesses ──< business_members >── users (Global Accounts)
       │
       ├───< features
       │
       └───< packages (JSONB Features)
                 │
                 └───< plans (Pricing & Intervals)
```

