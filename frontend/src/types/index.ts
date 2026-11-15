// Core entity and API types

export type UserRole = 'PLATFORM_ADMIN' | 'BUSINESS_ADMIN' | 'CUSTOMER';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: UserRole;
  businessId?: number;
  emailVerified?: boolean;
}

export interface Business {
  id: number;
  businessName: string;
  businessSlug: string;
  businessType?: string;
  email: string;
  phone?: string;
  currency: string;
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED';
}

export interface AuthResponse {
  token: string;
  user: User;
  business?: Business | null;
}

export interface OtpSendResponse {
  exists: boolean;
  message: string;
  cooldownSeconds?: number;
}

export type FeatureType = 'BOOLEAN' | 'NUMBER' | 'TEXT' | 'LIST';

export interface BusinessFeature {
  id: number;
  businessId: number;
  featureName: string;
  featureCode: string;
  description?: string;
  featureType: FeatureType;
  isActive: boolean;
}

export interface PlanFeatureValue {
  featureId: number;
  featureCode: string;
  featureName: string;
  featureType: FeatureType;
  valueBoolean?: boolean;
  valueNumber?: number;
  valueText?: string;
  valueJson?: string[];
}

export interface Plan {
  id: number;
  businessId: number;
  planName: string;
  planCode?: string;
  description?: string;
  price: number;
  currency: string;
  billingInterval: 'DAY' | 'WEEK' | 'MONTH' | 'YEAR';
  billingIntervalCount: number;
  trialDays: number;
  isActive: boolean;
  features: PlanFeatureValue[];
}

export interface Subscription {
  id: number;
  businessId: number;
  customerId: number;
  planId: number;
  status: 'PENDING' | 'ACTIVE' | 'PAST_DUE' | 'CANCELLED' | 'EXPIRED';
  currentPeriodStart: string;
  currentPeriodEnd: string;
  cancelAtPeriodEnd: boolean;
  planName?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}
