import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Building2, UserCheck, Shield, ExternalLink, LogOut, CheckCircle2, Zap, Layers, RefreshCw } from 'lucide-react';
import { request } from '../services/api';
import { Plan } from '../types';

export default function DashboardPage() {
  const { user, business, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [plans, setPlans] = useState<Plan[]>([]);
  const [loadingPlans, setLoadingPlans] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    if (business?.businessSlug) {
      setLoadingPlans(true);
      request<Plan[]>(`/public/businesses/${business.businessSlug}/plans`)
        .then((data) => setPlans(data || []))
        .catch(() => setPlans([]))
        .finally(() => setLoadingPlans(false));
    }
  }, [business?.businessSlug]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) return null;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Top Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
              <CheckCircle2 className="w-3.5 h-3.5" />
              Authenticated via OTP
            </span>
            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-100 text-indigo-800">
              <Shield className="w-3.5 h-3.5" />
              Tenant ID #{business?.id || user.businessId}
            </span>
          </div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">
            {business?.businessName || 'Business Workspace'}
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Signed in as <strong>{user.fullName}</strong> ({user.email}) • Role: <code className="text-xs bg-slate-100 px-1.5 py-0.5 rounded">{user.role}</code>
          </p>
        </div>

        <div className="flex items-center gap-3">
          {business?.businessSlug && (
            <a
              href={`http://localhost:8080/api/public/businesses/${business.businessSlug}/plans`}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl border border-slate-200 bg-white text-xs font-semibold text-slate-700 hover:bg-slate-50 transition shadow-sm"
            >
              <ExternalLink className="w-3.5 h-3.5 text-slate-500" />
              <span>Public Plans API</span>
            </a>
          )}
          <button
            onClick={handleLogout}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-slate-100 text-xs font-semibold text-slate-700 hover:bg-slate-200 transition"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span>Sign Out</span>
          </button>
        </div>
      </div>

      {/* Main Stats / Overview */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 my-8">
        <div className="p-6 rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Tenant Profile</span>
            <Building2 className="w-5 h-5 text-indigo-600" />
          </div>
          <div className="text-xl font-bold text-slate-900">{business?.businessName}</div>
          <p className="text-xs text-slate-500 mt-1">Slug: <code className="text-indigo-600 font-semibold">{business?.businessSlug}</code></p>
          <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
            <span>Currency: <strong>{business?.currency}</strong></span>
            <span>Status: <strong className="text-emerald-600">{business?.status || 'ACTIVE'}</strong></span>
          </div>
        </div>

        <div className="p-6 rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Security & Session</span>
            <UserCheck className="w-5 h-5 text-emerald-600" />
          </div>
          <div className="text-xl font-bold text-slate-900">Passwordless OTP</div>
          <p className="text-xs text-slate-500 mt-1">Redis Ephemeral Storage • 5m TTL</p>
          <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
            <span>Verified: <strong className="text-emerald-600">Yes</strong></span>
            <span>Auth Type: <strong>JWT HMAC-SHA512</strong></span>
          </div>
        </div>

        <div className="p-6 rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Storefront Cache</span>
            <Zap className="w-5 h-5 text-amber-500" />
          </div>
          <div className="text-xl font-bold text-slate-900">Redis 7 Accelerated</div>
          <p className="text-xs text-slate-500 mt-1">Key: <code>public-plans::{business?.businessSlug}</code></p>
          <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
            <span>Plans Cached: <strong>{plans.length}</strong></span>
            <span>TTL: <strong>15 Minutes</strong></span>
          </div>
        </div>
      </div>

      {/* Subscription Plans Section */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 mb-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <Layers className="w-5 h-5 text-indigo-600" />
              <span>Active Subscription Plans</span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Configured dynamically using the Phase 3 feature engine.
            </p>
          </div>
          {loadingPlans && <RefreshCw className="w-4 h-4 text-slate-400 animate-spin" />}
        </div>

        {plans.length === 0 ? (
          <div className="text-center py-8 text-slate-400 text-sm">
            {loadingPlans ? 'Loading plans...' : 'No plans configured yet for this business.'}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {plans.map((p) => (
              <div key={p.id} className="p-5 rounded-xl border border-slate-200 bg-slate-50/50 hover:bg-slate-50 transition">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="font-bold text-slate-900 text-base">{p.planName}</h3>
                    <p className="text-xs text-slate-500 mt-0.5">{p.description}</p>
                  </div>
                  <div className="text-right">
                    <span className="text-lg font-extrabold text-slate-900">
                      {p.currency === 'INR' ? '₹' : '$'}{p.price}
                    </span>
                    <span className="text-xs text-slate-500">/{p.billingInterval.toLowerCase()}</span>
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-200/60 space-y-2">
                  <div className="text-xs font-semibold text-slate-600 uppercase tracking-wider mb-2">
                    Included Features:
                  </div>
                  {p.features?.map((f) => (
                    <div key={f.featureId} className="flex items-center justify-between text-xs text-slate-700">
                      <span className="flex items-center gap-1.5">
                        <CheckCircle2 className="w-3.5 h-3.5 text-indigo-600 flex-shrink-0" />
                        <span>{f.featureName}</span>
                      </span>
                      <span className="font-mono font-medium text-slate-900 bg-white px-2 py-0.5 rounded border border-slate-200">
                        {f.featureType === 'BOOLEAN' && (f.valueBoolean ? 'Enabled' : 'No')}
                        {f.featureType === 'NUMBER' && `${f.valueNumber} included`}
                        {f.featureType === 'TEXT' && f.valueText}
                        {f.featureType === 'LIST' && `${f.valueJson?.length || 0} classes`}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
