# Multi-Tenant Subscription Management Platform

## Project Overview and Technical Specification

> **Modular Documentation Index:**
> - [01 Database Schema & Domain Specification](docs/01_database_schema_and_domain.md) — Detailed tables, types, constraints, and relationships.
> - [02 Product & Payment Architecture](docs/02_product_and_payment_architecture.md) — Business model, personas, BYOK payment routing, and headless API strategy.

## 1. Project purpose

Build a publicly accessible, multi-tenant subscription management
platform that demonstrates practical backend engineering using Java,
Spring Boot, PostgreSQL, Redis, Apache Kafka, scheduled jobs, payment
gateway integration, and an external email-delivery API.

The platform is offered free of charge to businesses. It does not
collect subscription revenue on behalf of those businesses. Each
business uses the platform to create its own subscription plans, define
the benefits included in those plans, manage its customers, and track
subscriptions and payments.

Examples of businesses that can use the platform:

-   A gym offering monthly and annual memberships.
-   A coaching institute offering access to courses and recorded
    lectures.
-   A SaaS company offering different software plans and usage limits.
-   A club or membership organisation offering different membership
    levels.

The platform must be industry-independent. It must not contain hardcoded
feature lists for gyms, coaching institutes, or any other business
category. Each business defines its own features and plan benefits
through the application.

The primary goal is to create a working, live portfolio project that can
be opened and tested publicly. The architecture should demonstrate the
technologies listed in this document without adding technologies that do
not solve a real requirement.

## 2. Core product model

There are three main concepts:

1.  **Business:** An organisation that registers and uses the platform.
2.  **Feature:** A benefit, capability, or limit that a business defines
    for its own plans.
3.  **Plan:** A priced subscription offering created by a business by
    selecting its features and assigning values to them.

A business can create multiple features and multiple plans. A plan can
contain multiple features, and a feature can be included in multiple
plans.

For example, ABC Gym may create:

  Feature                  Type
  ------------------------ ---------
  Gym access               Boolean
  Personal trainer         Boolean
  Locker access            Boolean
  Guest visits per month   Number
  Support level            Text

It can then create:

  -------------------------------------------------------------------------------
  Plan               Price Gym       Personal   Locker    Guest visits Support
                           access    trainer                           
  --------- -------------- --------- ---------- --------- ------------ ----------
  Basic       ₹1,000/month Yes       No         No                   2 Standard

  Premium     ₹2,000/month Yes       Yes        Yes                 10 Priority
  -------------------------------------------------------------------------------

A coaching institute can create a completely different set of features,
such as recorded lectures, live classes, course access, downloadable
materials, or doubt-clearing sessions.

The platform supplies the feature-building mechanism and supported data
types. The business supplies the actual feature names and values.

## 3. User roles and access

### 3.1 Platform administrator

The platform administrator operates the overall service.

Responsibilities:

-   View and manage registered businesses.
-   Activate, suspend, or deactivate business accounts.
-   View platform-level operational information.
-   Review platform-wide logs and system health.
-   Manage platform configuration.

The platform administrator is not the owner of each business's plans or
customer payments.

### 3.2 Business administrator

Each registered business receives its own business account and an
administrator user.

Responsibilities:

-   Manage the business profile.
-   Create and manage features.
-   Create and publish plans.
-   Configure payment gateway details.
-   View and manage customers.
-   View subscriptions and payment records.
-   Cancel or change subscriptions where permitted.
-   View renewal, expiry, and payment reports.

### 3.3 Customer

A customer is a person who subscribes to a business's plan.

Responsibilities:

-   Browse a business's public plans.
-   Register or log in.
-   Select a plan.
-   Complete payment.
-   View their subscription and payment history.
-   View the benefits associated with their subscription.
-   Request cancellation or perform permitted subscription actions.

A customer belongs to the relevant business context. A customer account
must not expose another business's customer information.

## 4. Application interfaces

The frontend can be implemented as a web application. It should provide
the following areas.

### 4.1 Public website

-   Project landing page explaining the platform.
-   Business registration and login.
-   Public business profile and plan pages.
-   Public plan details and subscription entry point.

Example public URL pattern:

`https://<platform-domain>/<business-slug>/plans`

### 4.2 Platform administrator dashboard

-   Registered business count.
-   Active and suspended businesses.
-   Platform-level activity.
-   Business details and account status.
-   Basic system and operational information.

### 4.3 Business dashboard

Suggested navigation:

-   Overview
-   Business Profile
-   Features
-   Subscription Plans
-   Customers
-   Subscriptions
-   Payments
-   Invoices
-   Notifications / Email History
-   Reports
-   Payment Gateway Settings

### 4.4 Customer portal

-   Customer profile.
-   Current subscription.
-   Plan benefits and limits.
-   Payment history.
-   Invoice access.
-   Renewal and expiry information.
-   Cancellation or other permitted subscription actions.

The frontend must render business-defined features dynamically. It must
not have a separate hardcoded form for every industry.

## 5. Dynamic feature management

### 5.1 Feature creation

A business administrator opens **Feature Management** and creates a
feature by entering:

-   Feature name.
-   Feature code, generated from the name or entered and validated.
-   Optional description.
-   Feature type.
-   Optional configuration or validation settings.
-   Active/inactive status.

The initial supported feature types are:

  Type      Example            Frontend control         Stored value
  --------- ------------------ ------------------------ --------------
  BOOLEAN   Personal trainer   Yes/No switch            Boolean
  NUMBER    Guest visits       Numeric input            Numeric
  TEXT      Support level      Text input               Text
  LIST      Included courses   Multi-value/list input   JSON array

The feature count should not be fixed by the frontend. The application
can apply a configurable server-side limit, for example 50 active
features per business, to protect the service. This is an operational
limit, not a product-specific list.

A business can edit or deactivate its features. A feature already used
by plans or existing subscriptions should not be physically deleted in a
way that breaks historical records. Deactivation is preferred.

### 5.2 Dynamic plan creation

When a business creates a plan:

1.  The frontend requests that business's active features from the
    backend.
2.  The backend returns feature metadata, including each feature's type.
3.  The frontend renders the correct input control based on the type.
4.  The business enters the value for each feature in the plan.
5.  The frontend submits the plan and its feature values.
6.  The backend validates that every feature belongs to the same
    business as the plan.
7.  The backend validates that each value matches the feature type.
8.  The plan and its feature values are saved transactionally.

For example, a Boolean feature renders a switch, a Number feature
renders a numeric input, a Text feature renders a text field, and a List
feature renders a multi-value editor.

### 5.3 Feature entitlements

A plan's feature values describe what a subscriber is entitled to
receive.

Examples:

-   `gym_access = true`
-   `personal_trainer = false`
-   `guest_visits = 10`
-   `support_level = "Priority"`
-   `included_courses = ["Java", "Python"]`

For a SaaS business, numeric features can represent usage quotas such as
projects, team members, storage, or API calls.

The platform can store and expose entitlements through APIs. It cannot
automatically enforce a limit inside an unrelated third-party
application unless that application integrates with the platform's API
or SDK.

## 6. Database design

Use PostgreSQL as the primary relational database. The following is the
proposed logical schema. IDs are shown as `BIGINT`/identity values for
readability. Use foreign keys, indexes, unique constraints, and
transactions.

### 6.1 `businesses`

Stores registered organisations.

  Column          Type                       Notes
  --------------- -------------------------- ------------------------------
  id              BIGINT                     Primary key
  business_name   VARCHAR(150)               Required
  business_slug   VARCHAR(120)               Unique public URL identifier
  business_type   VARCHAR(80)                Optional category
  email           VARCHAR(255)               Business contact
  phone           VARCHAR(30)                Optional
  address         TEXT                       Optional
  currency        CHAR(3)                    Default INR
  timezone        VARCHAR(60)                Business timezone
  status          VARCHAR(20)                PENDING, ACTIVE, SUSPENDED
  created_at      TIMESTAMP WITH TIME ZONE   Creation time
  updated_at      TIMESTAMP WITH TIME ZONE   Update time

### 6.2 `users`

Stores platform users and business users.

  -----------------------------------------------------------------------
  Column                  Type                    Notes
  ----------------------- ----------------------- -----------------------
  id                      BIGINT                  Primary key

  business_id             BIGINT                  Nullable for
                                                  platform-level users;
                                                  FK to businesses

  full_name               VARCHAR(150)            User name

  email                   VARCHAR(255)            Login email; unique
                                                  according to chosen
                                                  account model

  password_hash           VARCHAR(255)            Store a secure password
                                                  hash, never plaintext

  role                    VARCHAR(30)             PLATFORM_ADMIN,
                                                  BUSINESS_ADMIN,
                                                  CUSTOMER

  status                  VARCHAR(20)             ACTIVE, BLOCKED,
                                                  PENDING

  email_verified          BOOLEAN                 Email verification
                                                  state

  created_at              TIMESTAMP WITH TIME     Creation time
                          ZONE                    

  updated_at              TIMESTAMP WITH TIME     Update time
                          ZONE                    
  -----------------------------------------------------------------------

If the same person must have separate customer accounts for multiple
businesses, model the global identity and business membership separately
rather than forcing one `business_id` onto the user. The initial
implementation can use a simpler account model, provided the ownership
rules are clear.

### 6.3 `business_features`

Stores the custom features defined by each business.

  Column         Type                       Notes
  -------------- -------------------------- ---------------------------------
  id             BIGINT                     Primary key
  business_id    BIGINT                     FK to businesses
  feature_name   VARCHAR(100)               Display name
  feature_code   VARCHAR(100)               Stable machine-readable code
  description    TEXT                       Optional
  feature_type   VARCHAR(20)                BOOLEAN, NUMBER, TEXT, LIST
  is_active      BOOLEAN                    Whether available for new plans
  created_at     TIMESTAMP WITH TIME ZONE   Creation time
  updated_at     TIMESTAMP WITH TIME ZONE   Update time

Constraints and rules:

-   Unique `(business_id, feature_code)`.
-   Index `business_id`.
-   A business can only access its own features.
-   Feature type should be validated against the supported types.
-   Avoid changing a feature's type after it has been used. Prefer
    creating a replacement feature and migrating deliberately.

### 6.4 `plans`

Stores subscription plans created by businesses.

  Column                   Type                       Notes
  ------------------------ -------------------------- -------------------------------------
  id                       BIGINT                     Primary key
  business_id              BIGINT                     FK to businesses
  plan_name                VARCHAR(100)               Required
  plan_code                VARCHAR(100)               Optional business-scoped code
  description              TEXT                       Optional
  price                    NUMERIC(12,2)              Non-negative
  currency                 CHAR(3)                    Usually inherited from business
  billing_interval         VARCHAR(20)                DAY, WEEK, MONTH, YEAR
  billing_interval_count   INTEGER                    For example, 1 month or 3 months
  trial_days               INTEGER                    Default 0
  is_active                BOOLEAN                    Whether new customers can subscribe
  created_at               TIMESTAMP WITH TIME ZONE   Creation time
  updated_at               TIMESTAMP WITH TIME ZONE   Update time

Use a check constraint to prevent negative prices and invalid interval
counts. A plan should not be hard-deleted if subscriptions refer to it.

### 6.5 `plan_features`

Joins plans to business-defined features and stores the value of each
feature for that plan.

  Column          Type                       Notes
  --------------- -------------------------- --------------------------
  id              BIGINT                     Primary key
  plan_id         BIGINT                     FK to plans
  feature_id      BIGINT                     FK to business_features
  value_boolean   BOOLEAN                    Boolean feature value
  value_number    NUMERIC(14,2)              Numeric feature value
  value_text      TEXT                       Text feature value
  value_json      JSONB                      List or structured value
  created_at      TIMESTAMP WITH TIME ZONE   Creation time
  updated_at      TIMESTAMP WITH TIME ZONE   Update time

Constraints and rules:

-   Unique `(plan_id, feature_id)`.
-   Validate that the plan and feature belong to the same business.
-   Only the value column corresponding to the feature's type should be
    populated.
-   Validate the value type in the service layer; use database
    constraints or triggers where appropriate.
-   `value_json` can store a JSON array for the initial LIST type.

An alternative is a single JSONB `feature_value` column. The
typed-column design above makes simple values more explicit and easier
to query. Choose one representation consistently; do not store the same
value in both representations.

### 6.6 `customers`

Stores the customer relationship for a business.

  -----------------------------------------------------------------------
  Column                  Type                    Notes
  ----------------------- ----------------------- -----------------------
  id                      BIGINT                  Primary key

  business_id             BIGINT                  FK to businesses

  user_id                 BIGINT                  Optional FK to users,
                                                  if customers use login
                                                  accounts

  customer_name           VARCHAR(150)            Required

  email                   VARCHAR(255)            Customer email

  phone                   VARCHAR(30)             Optional

  status                  VARCHAR(20)             ACTIVE, INACTIVE

  created_at              TIMESTAMP WITH TIME     Creation time
                          ZONE                    

  updated_at              TIMESTAMP WITH TIME     Update time
                          ZONE                    
  -----------------------------------------------------------------------

Add a business-scoped uniqueness rule for customer email if that is the
desired account policy. Do not assume an email is globally unique across
all businesses unless the identity model requires it.

### 6.7 `subscriptions`

Stores each customer's subscription to a business plan.

  -----------------------------------------------------------------------
  Column                  Type                    Notes
  ----------------------- ----------------------- -----------------------
  id                      BIGINT                  Primary key

  business_id             BIGINT                  FK to businesses

  customer_id             BIGINT                  FK to customers

  plan_id                 BIGINT                  FK to plans

  status                  VARCHAR(25)             PENDING, ACTIVE,
                                                  PAST_DUE, CANCELLED,
                                                  EXPIRED

  start_at                TIMESTAMP WITH TIME     Start time
                          ZONE                    

  current_period_start    TIMESTAMP WITH TIME     Current billing period
                          ZONE                    start

  current_period_end      TIMESTAMP WITH TIME     Current billing period
                          ZONE                    end

  trial_end_at            TIMESTAMP WITH TIME     Nullable
                          ZONE                    

  cancel_at_period_end    BOOLEAN                 Whether cancellation is
                                                  scheduled

  cancelled_at            TIMESTAMP WITH TIME     Nullable
                          ZONE                    

  ended_at                TIMESTAMP WITH TIME     Nullable
                          ZONE                    

  created_at              TIMESTAMP WITH TIME     Creation time
                          ZONE                    

  updated_at              TIMESTAMP WITH TIME     Update time
                          ZONE                    
  -----------------------------------------------------------------------

The subscription must preserve the plan and entitlement terms that
applied when the customer subscribed. Do not rely only on the current
`plans` and `plan_features` rows, because a business may edit a plan
later.

### 6.8 `subscription_entitlements`

Stores a snapshot of the feature values granted to a customer when a
subscription is created or changed.

  -----------------------------------------------------------------------
  Column                  Type                    Notes
  ----------------------- ----------------------- -----------------------
  id                      BIGINT                  Primary key

  subscription_id         BIGINT                  FK to subscriptions

  feature_id              BIGINT                  Reference to original
                                                  feature; nullable if
                                                  historical retention
                                                  policy requires it

  feature_name            VARCHAR(100)            Snapshot name

  feature_code            VARCHAR(100)            Snapshot code

  feature_type            VARCHAR(20)             Snapshot type

  value_boolean           BOOLEAN                 Boolean entitlement

  value_number            NUMERIC(14,2)           Numeric entitlement

  value_text              TEXT                    Text entitlement

  value_json              JSONB                   List/structured
                                                  entitlement

  created_at              TIMESTAMP WITH TIME     Snapshot time
                          ZONE                    
  -----------------------------------------------------------------------

This table preserves what the customer actually purchased, even if the
business later edits or deactivates a feature or changes a plan.

### 6.9 `payments`

Stores payment attempts and confirmed payment information.

  Column                Type                       Notes
  --------------------- -------------------------- ---------------------------------------------
  id                    BIGINT                     Primary key
  business_id           BIGINT                     FK to businesses
  customer_id           BIGINT                     FK to customers
  subscription_id       BIGINT                     Nullable FK to subscriptions
  amount                NUMERIC(12,2)              Payment amount
  currency              CHAR(3)                    Currency
  payment_provider      VARCHAR(30)                RAZORPAY initially
  provider_order_id     VARCHAR(150)               Gateway order identifier
  provider_payment_id   VARCHAR(150)               Gateway payment identifier
  status                VARCHAR(25)                CREATED, PENDING, SUCCESS, FAILED, REFUNDED
  idempotency_key       VARCHAR(150)               Prevents duplicate payment creation
  paid_at               TIMESTAMP WITH TIME ZONE   Nullable
  created_at            TIMESTAMP WITH TIME ZONE   Creation time
  updated_at            TIMESTAMP WITH TIME ZONE   Update time

Use unique constraints for provider identifiers where appropriate. Never
mark a payment successful based only on a frontend redirect or callback.
Verify it with the backend and trusted gateway response/webhook.

### 6.10 `business_payment_settings`

Stores payment gateway configuration per business.

  -----------------------------------------------------------------------
  Column                  Type                    Notes
  ----------------------- ----------------------- -----------------------
  id                      BIGINT                  Primary key

  business_id             BIGINT                  FK; unique for one
                                                  active configuration
                                                  per provider

  provider                VARCHAR(30)             RAZORPAY

  provider_account_id     VARCHAR(150)            Optional account
                                                  identifier

  key_id                  VARCHAR(255)            Public gateway key
                                                  identifier

  secret_reference        VARCHAR(255)            Reference to encrypted
                                                  secret storage

  environment             VARCHAR(20)             TEST or LIVE

  is_active               BOOLEAN                 Active configuration

  created_at              TIMESTAMP WITH TIME     Creation time
                          ZONE                    

  updated_at              TIMESTAMP WITH TIME     Update time
                          ZONE                    
  -----------------------------------------------------------------------

Do not store raw gateway secrets in plaintext. Use environment secrets
or an encrypted secret-management approach. For the portfolio
demonstration, use test mode and clearly label it.

### 6.11 `webhook_events`

Stores gateway webhook events for verification, idempotency, and
troubleshooting.

  -----------------------------------------------------------------------
  Column                  Type                    Notes
  ----------------------- ----------------------- -----------------------
  id                      BIGINT                  Primary key

  business_id             BIGINT                  FK to businesses, if
                                                  resolved

  provider                VARCHAR(30)             RAZORPAY

  provider_event_id       VARCHAR(150)            Unique provider event
                                                  identifier where
                                                  available

  event_type              VARCHAR(100)            Event name

  payload                 JSONB                   Received payload

  signature_valid         BOOLEAN                 Signature verification
                                                  result

  processing_status       VARCHAR(20)             RECEIVED, PROCESSED,
                                                  FAILED

  received_at             TIMESTAMP WITH TIME     Receipt time
                          ZONE                    

  processed_at            TIMESTAMP WITH TIME     Nullable
                          ZONE                    
  -----------------------------------------------------------------------

Store only the information needed for processing and audit. Apply
appropriate retention and access controls to webhook payloads.

### 6.12 `email_logs`

Stores the status of application email requests.

  -----------------------------------------------------------------------
  Column                  Type                    Notes
  ----------------------- ----------------------- -----------------------
  id                      BIGINT                  Primary key

  business_id             BIGINT                  Nullable FK to
                                                  businesses

  recipient_email         VARCHAR(255)            Destination

  subject                 VARCHAR(255)            Email subject

  template_code           VARCHAR(80)             Template identifier

  provider                VARCHAR(30)             Selected email provider

  provider_message_id     VARCHAR(150)            Provider response ID

  status                  VARCHAR(20)             QUEUED, SENT, FAILED

  failure_reason          TEXT                    Sanitised error detail

  related_entity_type     VARCHAR(40)             SUBSCRIPTION, PAYMENT,
                                                  USER, etc.

  related_entity_id       BIGINT                  Related record

  created_at              TIMESTAMP WITH TIME     Request time
                          ZONE                    

  sent_at                 TIMESTAMP WITH TIME     Nullable
                          ZONE                    
  -----------------------------------------------------------------------

Do not store sensitive payment credentials or unnecessary personal
information in email logs.

### 6.13 `invoices` (optional but useful)

  Column            Type                       Notes
  ----------------- -------------------------- -------------------------------
  id                BIGINT                     Primary key
  business_id       BIGINT                     FK to businesses
  customer_id       BIGINT                     FK to customers
  subscription_id   BIGINT                     FK to subscriptions
  payment_id        BIGINT                     Nullable FK to payments
  invoice_number    VARCHAR(80)                Business-scoped unique number
  amount            NUMERIC(12,2)              Invoice total
  currency          CHAR(3)                    Currency
  status            VARCHAR(20)                DRAFT, ISSUED, VOID
  issued_at         TIMESTAMP WITH TIME ZONE   Nullable
  created_at        TIMESTAMP WITH TIME ZONE   Creation time

Invoices should be generated only when the relevant payment or billing
event has been confirmed according to the business rules.

### 6.14 Database relationship summary

-   `businesses` has many `users`, `business_features`, `plans`,
    `customers`, `subscriptions`, `payments`, and
    `business_payment_settings`.
-   `plans` has many `plan_features`.
-   `business_features` has many `plan_features`.
-   `customers` has many `subscriptions` and `payments`.
-   `subscriptions` has many `subscription_entitlements` and can have
    multiple payment records over time.
-   `payments` can be associated with invoices.
-   `webhook_events` records incoming gateway events.
-   `email_logs` records email delivery attempts.

Use database constraints and service-layer validation together. Foreign
keys alone do not guarantee tenant isolation.

## 7. Multi-tenancy and data security

This is a multi-tenant application using a shared PostgreSQL database
and shared tables.

Every business-owned record must be associated with a `business_id`,
directly or through a validated parent relationship.

For every business API request:

1.  Authenticate the user.
2.  Read the user's identity and role from the verified JWT.
3.  Resolve the user's permitted business context on the backend.
4.  Check that the user is allowed to perform the requested operation.
5.  Filter database access by the resolved `business_id`.
6.  Validate ownership of referenced records such as plans, features,
    customers, and subscriptions.

Do not trust a `business_id` sent by the browser as proof of access. A
user must not be able to change an ID in the request and access another
business's records.

Use:

-   Spring Security and JWT authentication.
-   Role-based authorization.
-   Password hashing using a strong password encoder.
-   Input validation.
-   Rate limiting where appropriate.
-   CORS configuration for the deployed frontend.
-   Secure secret management.
-   Webhook signature verification.
-   Audit logging for sensitive actions.

## 8. Technology stack and responsibilities

### Backend

**Java 17 or 21 + Spring Boot**

The backend exposes REST APIs and contains the business logic.

Suggested Spring modules/libraries:

-   Spring Web: REST controllers.
-   Spring Security: authentication and authorization.
-   Spring Data JPA / Hibernate: relational persistence.
-   PostgreSQL JDBC driver.
-   Bean Validation: request validation.
-   Spring Boot Actuator: health and operational endpoints.
-   Spring Mail is optional if using SMTP; an HTTP client can call an
    email provider API.
-   Spring Kafka: Apache Kafka distributed event streaming integration.
-   Spring Scheduling: scheduled jobs.
-   OpenAPI / Swagger: API documentation.
-   Flyway or Liquibase: database schema migrations.

Use a consistent persistence approach. JPA is suitable for normal entity
operations; use JdbcTemplate or native SQL only where a specific query
benefits from it.

### Database

**PostgreSQL**

Stores the permanent application data:

-   Businesses and users.
-   Features and plans.
-   Customers and subscriptions.
-   Entitlement snapshots.
-   Payments and gateway events.
-   Email logs and invoices.

PostgreSQL is the source of truth. Redis and Apache Kafka must not replace
it.

### Cache

**Redis**

Use Redis for data that is safe to rebuild or expire:

-   Public plan listing cache.
-   Frequently requested business profile data.
-   Selected subscription status lookups.
-   Short-lived verification or temporary tokens, if appropriate.
-   Rate-limit counters.

Use sensible TTLs and invalidate relevant cache entries when plans or
business settings change. Do not use Redis as the only store for
payments, subscriptions, or other critical records.

### Distributed Event Streaming

**Apache Kafka (KRaft Mode)**

Apache Kafka decouples the main API requests from work that can be performed
asynchronously, offering distributed event streaming, consumer groups, partitions,
and message persistence. Running in KRaft mode eliminates ZooKeeper for lightweight,
100% free forever operation both locally and on serverless free tiers (e.g., Upstash Kafka).

Examples:

-   Send subscription confirmation email.
-   Send payment receipt email.
-   Send renewal reminder email.
-   Send payment failure email.
-   Generate an invoice.
-   Process verified payment events.
-   Retry temporary failures with Dead Letter Topics (DLT).

The API saves the business transaction in PostgreSQL first, then
publishes an event for background processing. For reliability,
we use the transactional outbox pattern: write an outbox event in the same
database transaction as the business change, then have a publisher send
it to Kafka topics (`business.events`, `subscription.events`, `payment.events`)
and mark it as published. This guarantees at-least-once delivery even if Kafka
is temporarily unreachable.

### Email delivery provider

**External email provider API (initial provider: Mailpit locally, Brevo / Resend Free Tier)**

Kafka does not send emails. It streams events to background consumer groups.
An email consumer consumes the message and calls an email provider's HTTP API (or Mailpit SMTP locally).

The email flow is:

1.  An application event occurs, such as a successful payment.
2.  The backend saves the payment and relevant records in PostgreSQL.
3.  The backend creates an outbox event.
4.  The event is published to the Kafka topic (e.g. `payment.events`).
5.  An email consumer group (`email-service-group`) consumes the event.
6.  The consumer loads the appropriate email template and required data.
7.  The consumer calls the configured provider API (or Mailpit locally).
8.  The provider accepts or rejects the request.
9.  The consumer updates `email_logs` with the result and provider message ID.
10. If an error occurs, Spring Kafka retry handling attempts delivery.
    Repeated failures are forwarded to a Dead Letter Topic (`.DLT`) for inspection.

Suggested email types:

-   Business registration verification.
-   Welcome email.
-   Customer subscription confirmation.
-   Payment receipt.
-   Payment failure notification.
-   Upcoming renewal reminder.
-   Subscription expiry notice.
-   Cancellation confirmation.
-   Invoice email.
-   Password reset email.

Keep email templates in the application, with variables such as customer
name, business name, plan name, amount, and renewal date. Use a verified
sender domain for production. The provider choice should be configurable
so it can be replaced without changing subscription business logic.

### Payment gateway

**Razorpay**

Each business connects its own Razorpay account or uses a supported
account-onboarding/connected-account model. The exact live payment
routing model must be confirmed against the gateway's current product
and account requirements.

For the portfolio demonstration:

-   Start with Razorpay test mode.
-   Keep the gateway integration behind a payment service interface.
-   Create orders on the backend.
-   Return the order information and required public checkout details to
    the frontend.
-   Complete checkout using the supported Razorpay frontend flow.
-   Verify payment signatures on the backend.
-   Process gateway webhooks securely.
-   Update payment and subscription state only after trusted
    verification.
-   Make payment processing idempotent so duplicate callbacks/webhooks
    do not create duplicate payments or subscriptions.

The platform is not intended to collect all business revenue into one
platform-owned account. Payment should be associated with the relevant
business's gateway configuration and account arrangement.

For the first version, use one-time Razorpay orders and manage
subscription periods in the platform database. Native recurring Razorpay
Subscriptions can be added only after the business-specific gateway
account model is confirmed.

### Frontend

A web frontend such as React can provide:

-   Public landing and plan pages.
-   Registration and login.
-   Platform administrator dashboard.
-   Business dashboard.
-   Dynamic feature builder.
-   Dynamic plan creation forms.
-   Customer portal.
-   Payment checkout integration.
-   Subscription and payment history.

The frontend must obtain feature definitions from the backend and render
controls based on `feature_type`.

### Deployment and supporting tools

-   Docker: package the backend and supporting services.
-   Docker Compose: local development environment.
-   Git and GitHub: source control and public project repository.
-   Swagger UI: interactive API documentation.
-   Postman: API testing.
-   Environment variables: configuration and secrets.
-   A public frontend host and public backend host.
-   Managed PostgreSQL, Redis, and Apache Kafka services where practical.

Free hosting availability, resource limits, sleeping behaviour, and
persistence policies change. Choose providers after checking their
current free-tier terms. The project should be designed so services can
be replaced without rewriting business logic.

## 9. End-to-end business workflow

### 9.1 Business registration

1.  A business visits the platform.
2.  It submits business name, owner name, email, password, and contact
    details.
3.  The backend validates the request.
4.  The backend creates a `businesses` record.
5.  The backend creates the initial `BUSINESS_ADMIN` user linked to that
    business.
6.  The backend publishes an email verification event to Kafka (`business.events`).
7.  The email worker calls the email provider API.
8.  The business verifies its email and logs in.
9.  The backend issues a JWT.

### 9.2 Business configuration

1.  The business admin updates the business profile.
2.  The admin selects currency and timezone.
3.  The admin configures Razorpay test credentials or completes the
    supported account connection process.
4.  The backend securely stores or references the credentials.
5.  The business is ready to configure its plans.

### 9.3 Feature creation

1.  The business admin opens Feature Management.
2.  The frontend requests the business's existing features.
3.  The admin creates a feature name and chooses a type.
4.  The frontend submits the feature definition.
5.  The backend validates the type, name, code, and business ownership.
6.  The backend saves the record in `business_features`.
7.  The feature appears in the business's feature list.

### 9.4 Plan creation and publication

1.  The admin opens Subscription Plans.
2.  The frontend fetches active business features.
3.  The admin enters plan name, price, billing interval, and duration.
4.  The frontend dynamically renders an input for every feature.
5.  The admin configures each feature's value for this plan.
6.  The backend validates ownership and value types.
7.  The backend saves the plan and its `plan_features` rows in one
    transaction.
8.  The admin publishes the plan.
9.  The plan becomes available on the business's public plan page.

### 9.5 Customer registration and subscription

1.  A customer opens the business's public plan page.
2.  The customer selects a plan.
3.  The customer registers or logs in.
4.  The backend creates or finds the customer record for that business.
5.  The backend creates a pending subscription or checkout record as
    appropriate.
6.  The backend creates a Razorpay order using that business's
    configured gateway account arrangement.
7.  The frontend opens the payment checkout.
8.  The customer completes payment.
9.  The frontend sends the payment response to the backend.
10. The backend verifies the payment signature and/or obtains trusted
    gateway confirmation.
11. The backend processes the webhook safely and idempotently.
12. The payment record is marked successful only after verification.
13. The subscription becomes active.
14. The backend snapshots the plan's current feature values into
    `subscription_entitlements`.
15. A subscription confirmation and receipt event is sent through
    Apache Kafka (`subscription.events`).
16. The email worker calls the email provider API.
17. The customer can view the active subscription in the portal.

### 9.6 Subscription lifecycle

A subscription can move through states such as:

-   PENDING: checkout or confirmation not completed.
-   ACTIVE: valid subscription period.
-   PAST_DUE: payment or renewal is overdue.
-   CANCELLED: cancellation has taken effect or is scheduled, according
    to the chosen status model.
-   EXPIRED: subscription period has ended without renewal.

Keep scheduled cancellation separate from immediate cancellation. The
`cancel_at_period_end` field indicates that a subscription should remain
active until the end of the paid period.

When a subscription changes plan, define whether the change is immediate
or takes effect at the next renewal. Update entitlement snapshots
accordingly and record the change.

## 10. Scheduled jobs (cron / Spring Scheduler)

Scheduled jobs handle time-based work that should not depend on a user
opening the application.

Examples:

### Expiry check

-   Runs periodically.
-   Finds subscriptions whose paid period has ended and which have not
    renewed.
-   Updates their status to EXPIRED.
-   Publishes an expiry notification event.

### Renewal reminders

-   Finds subscriptions approaching their renewal date.
-   Avoids sending duplicate reminders.
-   Publishes reminder events to Apache Kafka (`subscription.events`).
-   The email consumer group sends the emails.

### Payment reconciliation

-   Finds payments that remain pending for too long.
-   Checks their status with the payment provider where supported.
-   Updates local records only after trusted confirmation.
-   Handles missed or delayed webhook events.

### Cleanup

-   Removes expired temporary records where retention policy permits.
-   Cleans up stale idempotency or temporary data.
-   Does not delete financial or subscription records that must be
    retained for audit.

Scheduled jobs must be safe to run more than once. Use database
conditions, locking, unique event keys, or idempotency records to
prevent duplicate processing when multiple application instances are
running.

## 11. Apache Kafka event streaming design

Suggested initial topics, event types, and consumer groups:

  -----------------------------------------------------------------------------------------
  Event                     Kafka Topic              Event Key        Consumer Group
  ------------------------- ------------------------ ---------------- ---------------------
  Business registered       `business.events`        `businessId`     `email-service-group`

  Subscription activated    `subscription.events`    `subscriptionId` `email-service-group`
                                                                      `invoice-service-group`

  Payment succeeded         `payment.events`         `paymentId`      `email-service-group`
                                                                      `invoice-service-group`

  Payment failed            `payment.events`         `paymentId`      `email-service-group`

  Renewal reminder due      `subscription.events`    `subscriptionId` `email-service-group`

  Subscription expired      `subscription.events`    `subscriptionId` `email-service-group`

  Invoice requested         `payment.events`         `paymentId`      `invoice-service-group`
  -----------------------------------------------------------------------------------------

Events contain entity IDs, event timestamps, and minimal metadata. Consumer
services load authoritative, tenant-scoped records from PostgreSQL using those IDs.

Recommended reliability features:

-   KRaft Mode: Fast, self-contained metadata management without ZooKeeper.
-   Partitioning: Keyed by `businessId` or `subscriptionId` to ensure ordered processing per tenant.
-   Consumer Groups: Multiple consumer groups (`email-service-group`, `invoice-service-group`) independently process streams.
-   Idempotent Consumers: Consumers track processed event IDs to avoid duplicate actions.
-   Dead Letter Topic (DLT): Failed messages automatically route to topic `.DLT` (e.g. `payment.events.DLT`) after bounded retries.
-   Transactional Outbox: Database transaction writes outbox events, guaranteed at-least-once publishing to Kafka.

Do not publish a Kafka event before the corresponding database transaction has committed.

## 12. Redis caching strategy

Initial cache candidates:

-   Public plan list by business slug.
-   Business public profile.
-   Frequently requested plan details.
-   Selected subscription entitlement lookups.

Example cache key patterns:

-   `business:{businessId}:public-profile`
-   `business:{businessId}:plans`
-   `plan:{planId}:details`
-   `subscription:{subscriptionId}:entitlements`

Use TTLs and invalidate keys when the underlying data changes. Do not
cache payment confirmation as the source of truth. Avoid caching
sensitive customer data unless there is a clear requirement and suitable
controls.

## 13. Suggested backend package / project structure

A feature-oriented package structure keeps related business logic
together.

``` text
subscription-platform/
├── backend/
│   └── src/main/java/com/example/subscriptionplatform/
│       ├── SubscriptionPlatformApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── RedisConfig.java
│       │   ├── KafkaConfig.java
│       │   ├── OpenApiConfig.java
│       │   └── WebConfig.java
│       ├── security/
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── JwtService.java
│       │   └── CustomUserDetailsService.java
│       ├── common/
│       │   ├── exception/
│       │   ├── response/
│       │   ├── validation/
│       │   └── util/
│       ├── auth/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── dto/
│       │   └── entity/
│       ├── business/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── dto/
│       │   └── entity/
│       ├── feature/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── dto/
│       │   └── entity/
│       ├── plan/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── dto/
│       │   └── entity/
│       ├── customer/
│       ├── subscription/
│       ├── entitlement/
│       ├── payment/
│       ├── webhook/
│       ├── invoice/
│       ├── notification/
│       │   ├── email/
│       │   ├── messaging/
│       │   └── template/
│       ├── scheduler/
│       ├── outbox/
│       └── audit/
│
├── frontend/
│   └── src/
│       ├── app/
│       ├── components/
│       ├── layouts/
│       ├── pages/
│       │   ├── public/
│       │   ├── platform-admin/
│       │   ├── business/
│       │   └── customer/
│       ├── features/
│       │   ├── auth/
│       │   ├── business/
│       │   ├── feature-management/
│       │   ├── plans/
│       │   ├── customers/
│       │   ├── subscriptions/
│       │   └── payments/
│       ├── services/
│       ├── hooks/
│       └── types/
│
├── database/
│   └── migrations/
├── docker/
├── docker-compose.yml
├── .env.example
├── README.md
└── docs/
    └── architecture.md
```

The exact package names can change, but keep controllers, services,
repositories, DTOs, and entities organised consistently. Do not put all
application logic into controllers.

## 14. API surface (indicative)

Use REST APIs with consistent response formats and validation errors.

### Authentication

-   `POST /api/auth/register-business`
-   `POST /api/auth/login`
-   `POST /api/auth/refresh` (if refresh-token flow is implemented)
-   `POST /api/auth/verify-email`
-   `POST /api/auth/forgot-password`
-   `POST /api/auth/reset-password`

### Business

-   `GET /api/businesses/me`
-   `PUT /api/businesses/me`
-   `GET /api/public/businesses/{slug}`

### Features

-   `POST /api/businesses/me/features`
-   `GET /api/businesses/me/features`
-   `GET /api/businesses/me/features/{featureId}`
-   `PUT /api/businesses/me/features/{featureId}`
-   `PATCH /api/businesses/me/features/{featureId}/status`

### Plans

-   `POST /api/businesses/me/plans`
-   `GET /api/businesses/me/plans`
-   `GET /api/businesses/me/plans/{planId}`
-   `PUT /api/businesses/me/plans/{planId}`
-   `PATCH /api/businesses/me/plans/{planId}/publish`
-   `GET /api/public/businesses/{slug}/plans`

### Customers and subscriptions

-   `GET /api/businesses/me/customers`
-   `GET /api/businesses/me/customers/{customerId}`
-   `GET /api/businesses/me/subscriptions`
-   `GET /api/businesses/me/subscriptions/{subscriptionId}`
-   `POST /api/public/businesses/{slug}/subscriptions/checkout`
-   `POST /api/payments/verify`
-   `POST /api/webhooks/razorpay`

### Payments and invoices

-   `GET /api/businesses/me/payments`
-   `GET /api/businesses/me/payments/{paymentId}`
-   `GET /api/businesses/me/invoices`
-   `GET /api/customer/me/subscriptions`
-   `GET /api/customer/me/payments`

The backend must derive the authenticated business context securely. The
`/me` routes are illustrative and must not accept arbitrary business IDs
as an authorization shortcut.

## 15. Important implementation rules

-   Use DTOs for API requests and responses; do not expose JPA entities
    directly.
-   Validate all input on the backend.
-   Use transactions for operations that update related records.
-   Use database migrations rather than relying on automatic schema
    creation in production.
-   Use global exception handling and consistent API error responses.
-   Add pagination and sorting to customer, subscription, and payment
    lists.
-   Add structured logs with correlation IDs.
-   Never log passwords, gateway secrets, access tokens, or full
    sensitive payment data.
-   Verify webhook signatures and make webhook processing idempotent.
-   Use idempotency keys for payment creation and other retryable
    operations.
-   Snapshot plan entitlements when a subscription is activated or
    changed.
-   Ensure every business-owned query is tenant-scoped.
-   Add tests for cross-business access attempts.
-   Provide health endpoints without exposing sensitive operational
    information.
-   Document APIs using Swagger/OpenAPI.

## 16. Public portfolio demonstration

The project should be deployed so a recruiter or reviewer can open the
frontend and try the application.

The demonstration should include:

-   A public landing page.
-   A clearly explained sample business.
-   A business dashboard with sample plans and features.
-   A customer-facing plan page.
-   A working registration/login flow.
-   Dynamic feature creation and plan configuration.
-   A Razorpay test-mode checkout demonstration, where supported by the
    deployed account configuration.
-   Visible subscription and payment status.
-   Email event logs or a clearly labelled email-delivery demonstration.
-   Swagger API documentation.
-   Health/status information that does not reveal secrets.

Because this is a portfolio demo, a small amount of sample data is
sufficient. Data loss on a free hosting tier may be acceptable for the
demo, but the application should handle empty databases and reinitialise
sample data safely. Do not promise permanent data retention if the
hosting provider does not provide it.

Keep credentials and secrets in deployment environment variables. Never
commit `.env` files, API keys, database passwords, Razorpay secrets, or
email-provider API keys to GitHub.

## 17. What the project demonstrates

This project provides practical examples of:

-   Java and Spring Boot REST API development.
-   Authentication and role-based authorisation using JWT.
-   Multi-tenant data isolation.
-   PostgreSQL relational modelling and constraints.
-   Dynamic, metadata-driven frontend forms.
-   Redis caching and invalidation.
-   Apache Kafka distributed event streaming, topics, partitions, and consumer groups.
-   External email-provider API integration.
-   Razorpay order creation, payment verification, and webhooks.
-   Scheduled jobs for expiry and renewal reminders.
-   Idempotency, retries, and Dead Letter Topics (DLT).
-   Transaction management and reliable event publication.
-   Docker-based packaging and deployment.
-   API documentation, health checks, logging, and testing.

## 18. Final architecture summary

``` text
                         CUSTOMER / BUSINESS ADMIN
                                    |
                                    v
                              WEB FRONTEND
                                    |
                                    v
                         SPRING BOOT REST API
                                    |
             +----------------------+----------------------+
             |                      |                      |
             v                      v                      v
        PostgreSQL                Redis                 Razorpay
      Source of truth             Cache              Payment gateway
             |
             v
       Transactional Outbox
             |
             v
        Apache Kafka (KRaft)
             |
       +-----+-------------------+
       |                         |
       v                         v
   Email Worker              Invoice Worker
       |                         |
       v                         v
 Email Provider API         Invoice generation
 (Brevo / Resend)                 |
       |                         |
       v                         v
  Email delivered            Invoice stored/sent

Spring Scheduler:
- Checks subscription expiry.
- Finds upcoming renewals.
- Initiates reminder events.
- Reconciles pending payment records.
- Publishes events through the reliable messaging flow.
```

**Core principle:** PostgreSQL stores the truth, Spring Boot enforces
the business rules, Redis accelerates suitable reads, Apache Kafka streams
background work, the email provider delivers email, Razorpay processes
payments, and scheduled jobs handle time-based subscription operations.
