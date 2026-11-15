import { useState, useEffect, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Building2, ArrowRight, AlertCircle, RefreshCw, CheckCircle2, XCircle } from 'lucide-react';
import { request } from '../services/api';
import { useWorkspace } from '../context/WorkspaceContext';
import { DomainLookupItem, BusinessDetail, SlugAvailability } from '../types';

export default function CreateWorkspacePage() {
  const navigate = useNavigate();
  const { reloadWorkspaces, setActiveWorkspace } = useWorkspace();

  const [businessName, setBusinessName] = useState('');
  const [businessSlug, setBusinessSlug] = useState('');
  const [isSlugManuallyEdited, setIsSlugManuallyEdited] = useState(false);

  // Slug check states
  const [slugChecking, setSlugChecking] = useState(false);
  const [slugAvailable, setSlugAvailable] = useState<boolean | null>(null);

  // Dynamic dropdowns
  const [businessTypes, setBusinessTypes] = useState<DomainLookupItem[]>([]);
  const [currencies, setCurrencies] = useState<DomainLookupItem[]>([]);
  const [selectedTypeId, setSelectedTypeId] = useState<number | ''>('');
  const [selectedCurrencyId, setSelectedCurrencyId] = useState<number | ''>('');

  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [tagline, setTagline] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch dynamic domain lookups for Business Types and Currencies
  useEffect(() => {
    request<DomainLookupItem[]>('/public/domains?type=BUSINESS_TYPE')
      .then((data) => {
        setBusinessTypes(data || []);
        if (data && data.length > 0) setSelectedTypeId(data[0].id);
      })
      .catch(() => {});

    request<DomainLookupItem[]>('/public/domains?type=CURRENCY')
      .then((data) => {
        setCurrencies(data || []);
        if (data && data.length > 0) setSelectedCurrencyId(data[0].id);
      })
      .catch(() => {});
  }, []);

  // Auto-generate slug from name
  const handleNameChange = (val: string) => {
    setBusinessName(val);
    if (!isSlugManuallyEdited) {
      const generated = val
        .toLowerCase()
        .trim()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-+|-+$/g, '');
      setBusinessSlug(generated);
    }
  };

  // Real-time debounced slug availability check
  useEffect(() => {
    if (!businessSlug || businessSlug.length < 2) {
      setSlugAvailable(null);
      return;
    }

    setSlugChecking(true);
    const timer = setTimeout(async () => {
      try {
        const res = await request<SlugAvailability>(`/public/businesses/check-slug?slug=${encodeURIComponent(businessSlug)}`);
        setSlugAvailable(res.available);
      } catch {
        setSlugAvailable(null);
      } finally {
        setSlugChecking(false);
      }
    }, 350);

    return () => clearTimeout(timer);
  }, [businessSlug]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!selectedTypeId || !selectedCurrencyId) {
      setError('Please select a business type and currency.');
      return;
    }
    if (slugAvailable === false) {
      setError('Please choose an available URL slug.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const payload = {
        businessName: businessName.trim(),
        businessSlug: businessSlug.trim().toLowerCase(),
        businessTypeId: Number(selectedTypeId),
        currencyId: Number(selectedCurrencyId),
        email: email.trim().toLowerCase(),
        phone: phone.trim() || undefined,
        tagline: tagline.trim() || undefined,
      };

      const newBiz = await request<BusinessDetail>('/businesses', {
        method: 'POST',
        body: JSON.stringify(payload),
      });

      await reloadWorkspaces();
      setActiveWorkspace({
        id: newBiz.id,
        businessName: newBiz.businessName,
        businessSlug: newBiz.businessSlug,
        businessType: newBiz.businessType,
        currency: newBiz.currency,
        userRole: newBiz.userRole || 'OWNER',
        hierarchyLevel: newBiz.hierarchyLevel || 100,
        status: newBiz.status,
      });

      navigate('/workspaces');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to create workspace.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-12">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xl p-8">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-indigo-50 text-indigo-600 mb-4 ring-8 ring-indigo-50/50">
            <Building2 className="w-7 h-7" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Create Business Workspace
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Provision a new isolated tenant. You will automatically be designated as the <strong>OWNER</strong>.
          </p>
        </div>

        {error && (
          <div className="mb-6 p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-start gap-3">
            <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
            <div className="flex-1">{error}</div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Business Name *
              </label>
              <input
                type="text"
                required
                placeholder="FitZone Gym"
                value={businessName}
                onChange={(e) => handleNameChange(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Storefront Slug *
              </label>
              <div className="relative">
                <input
                  type="text"
                  required
                  placeholder="fitzone-gym"
                  value={businessSlug}
                  onChange={(e) => {
                    setBusinessSlug(e.target.value);
                    setIsSlugManuallyEdited(true);
                  }}
                  className={`w-full px-3.5 py-2.5 pr-10 rounded-xl border focus:outline-none focus:ring-2 text-sm font-mono text-xs ${
                    slugAvailable === true
                      ? 'border-emerald-300 focus:ring-emerald-500 bg-emerald-50/30'
                      : slugAvailable === false
                      ? 'border-rose-300 focus:ring-rose-500 bg-rose-50/30'
                      : 'border-slate-200 focus:ring-indigo-500'
                  }`}
                />
                <div className="absolute right-3 top-3">
                  {slugChecking && <RefreshCw className="w-4 h-4 text-slate-400 animate-spin" />}
                  {!slugChecking && slugAvailable === true && <CheckCircle2 className="w-4 h-4 text-emerald-600" />}
                  {!slugChecking && slugAvailable === false && <XCircle className="w-4 h-4 text-rose-500" />}
                </div>
              </div>
              <p className="text-[11px] text-slate-400 mt-1">
                {slugAvailable === true && <span className="text-emerald-600 font-medium">✓ Available</span>}
                {slugAvailable === false && <span className="text-rose-600 font-medium">✗ Already taken</span>}
                {slugAvailable === null && 'Lowercase letters, numbers, and hyphens'}
              </p>
            </div>
          </div>

          {/* Dynamic Dropdowns from domain_lookups */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Business Type (Domain Lookup)
              </label>
              <select
                value={selectedTypeId}
                onChange={(e) => setSelectedTypeId(Number(e.target.value))}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm bg-white"
              >
                {businessTypes.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.value}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Currency (Domain Lookup)
              </label>
              <select
                value={selectedCurrencyId}
                onChange={(e) => setSelectedCurrencyId(Number(e.target.value))}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm bg-white"
              >
                {currencies.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.value} ({c.code})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Business Email *
              </label>
              <input
                type="email"
                required
                placeholder="contact@fitzone.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
                Business Phone
              </label>
              <input
                type="tel"
                placeholder="+91 98765 43210"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">
              Storefront Tagline (Optional)
            </label>
            <input
              type="text"
              placeholder="e.g. Elevate your fitness journey"
              value={tagline}
              onChange={(e) => setTagline(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
            />
          </div>

          <div className="pt-4">
            <button
              type="submit"
              disabled={loading || slugAvailable === false}
              className="w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-sm transition flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
            >
              {loading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  <span>Provisioning workspace...</span>
                </>
              ) : (
                <>
                  <Building2 className="w-4 h-4" />
                  <span>Create Workspace & Assign as OWNER</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

