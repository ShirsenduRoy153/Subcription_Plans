# Product & Payment Architecture

This document defines the high-level product design, user personas, payment processing models, and distribution channels for the Multi-Tenant Subscription Management Platform.

---

## 1. Product Purpose & Value Proposition

### The Problem
Small and mid-sized businesses (gyms, martial arts dojos, coaching centers, indie SaaS apps, and clubs) require subscription management:
- Managing recurring plans (Monthly, Quarterly, Annual).
- Defining dynamic perks and usage limits (e.g. 5 guest passes, sauna access, API limits).
- Payment checkout, expiry tracking, and renewal reminders.

Building these systems custom costs thousands of dollars in development time, while commercial billing engines (like Chargebee or Recurly) charge high recurring fees and transaction cuts.

### The Solution: "Shopify for Subscriptions"
SubPulse provides an all-in-one, multi-tenant platform where **any business** can register, define their catalog of benefits in minutes, set up packages and pricing plans, and immediately start selling subscriptions to their customers.

---

## 2. Real-World Market Equivalents

This architecture is modeled after multi-billion-dollar SaaS and fintech systems:

| Company | Model | How SubPulse Compares |
|---|---|---|
| **Chargebee** | B2B Subscription Engine | SubPulse provides the same dynamic plan and feature builder with multi-tenant isolation. |
| **Stripe Billing** | Recurring Payments & Customer Portal | SubPulse implements dynamic hosted customer portals with custom tenant branding. |
| **Lemon Squeezy** | Developer Checkout & Licensing | SubPulse provides headless REST APIs for external integration. |
| **Mindbody / Gymdesk** | Vertical SaaS for Fitness & Studios | SubPulse supports gym, coaching, and studio business types out of the box. |

---

## 3. The Three Personas

```text
 ┌────────────────────────────────────────────────────────┐
 │ 1. BUSINESS OWNER / ADMIN (e.g., Alex, Gym Owner)       │
 │    • Logs into the Admin Dashboard                     │
 │    • Defines features: Gym access, Guest passes         │
 │    • Sets up pricing: ₹999/mo, ₹2,499/mo               │
 │    • Configures payment keys & branding                │
 └───────────────────────────┬────────────────────────────┘
                             │
                             ▼
                 [ SUBPULSE PLATFORM ]
                             ▲
                             │
 ┌───────────────────────────┴────────────────────────────┐
 │ 2. BUSINESS CUSTOMER (e.g., John, the Gym Member)      │
 │    • Opens public storefront: /fitzone-gym/plans       │
 │    • Chooses a plan and pays via Razorpay              │
 │    • Receives instant confirmation & receipt email     │
 │    • Consumes perks (app verifies active entitlement)  │
 └────────────────────────────────────────────────────────┘
                             │
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │ 3. AUTOMATION & BACKGROUND WORKERS (The System Itself) │
 │    • Scheduled jobs check for expiring subscriptions   │
 │    • Kafka workers deliver reminder & invoice emails   │
 └────────────────────────────────────────────────────────┘
```

---

## 4. Payment Architecture: "Bring Your Own Key" (BYOK)

To avoid legal liability, money custody issues, and financial aggregation licenses, the platform implements the **Bring Your Own Key (BYOK)** pattern:

```text
                 [ CUSTOMER (John) ]
                         │
                         ▼ (Pays ₹1,999)
             [ RAZORPAY CHECKOUT POPUP ]
                         │
             ┌───────────┴───────────┐
             │                       │
      (Direct Transfer)       (Webhook Proof)
             │                       │
             ▼                       ▼
 [ FitZone Gym's Bank ]     [ SubPulse Backend ]
  (Money arrives directly)   (Activates subscription)
```

1. **Merchant-Direct Routing**:
   - FitZone Gym configures their own `razorpay_key_id` and `razorpay_key_secret` in their workspace settings.
   - When John checks out, the transaction is processed directly through FitZone's merchant account.
   - **The platform never touches customer funds.**
2. **Platform Sandbox Fallback**:
   - If a business has not yet configured keys (or for development/demonstration purposes), the platform falls back to the master Razorpay sandbox credentials (`rzp_test_...`), charging ₹0.00 and enabling full simulated checkouts.

---

## 5. Distribution: Hosted White-Label vs. Headless REST API

Businesses vary widely in their aesthetic and technological needs (e.g. high-energy gym themes vs. minimalist clinic branding). The platform satisfies both:

### A. Hosted White-Label Storefront
A business without a software development team can share their unique SubPulse URL:
```
http://localhost:5173/businesses/fitzone-gym/plans
```
The page dynamically loads the business profile:
- Custom **Logo** and **Hero Cover Banner**.
- Dynamic **Primary Brand Color** (e.g. orange buttons for gyms, blue for software).
- Custom **Tagline** and **Currency**.

### B. Headless REST APIs
For tech companies with existing websites (WordPress, Webflow, React, or Mobile Apps):
- They call: `GET /api/public/businesses/{slug}/plans`
- They render the returned JSON in whatever custom design, animations, or layout they want.
- They invoke the Razorpay checkout modal directly on their own domain.

