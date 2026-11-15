// Core entity and API types for Identity, Domain Lookups, and Multi-Tenant Workspaces

export interface DomainLookupItem {
  id: number;
  domainType: string;
  code: string;
  value: string;
  description?: string;
  sortOrder: number;
}

export interface User {
  id: number;
  email: string;
  fullName: string;
  phone?: string;
  emailVerified: boolean;
  status: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface CompactBusiness {
  id: number;
  businessName: string;
  businessSlug: string;
  businessType: string;
  currency: string;
  userRole: string;
  hierarchyLevel: number;
  status: string;
}

export interface BusinessDetail {
  id: number;
  businessName: string;
  businessSlug: string;
  businessTypeId: number;
  businessType: string;
  currencyId: number;
  currency: string;
  email: string;
  phone?: string;
  address?: string;
  timezone: string;
  logoUrl?: string;
  coverImageUrl?: string;
  primaryColor?: string;
  tagline?: string;
  status: string;
  userRole?: string;
  hierarchyLevel?: number;
  createdAt: string;
  updatedAt: string;
}

export interface Member {
  membershipId: number;
  userId: number;
  fullName: string;
  email: string;
  phone?: string;
  roleCode: string;
  roleName: string;
  hierarchyLevel: number;
  permissions: string[];
  status: string;
  joinedAt: string;
}

export interface SlugAvailability {
  slug: string;
  available: boolean;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}
