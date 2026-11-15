import { useState, useEffect, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { Building2, Shield, Users, Plus, CheckCircle2, AlertCircle, RefreshCw, Sparkles, ExternalLink } from 'lucide-react';
import { useWorkspace } from '../context/WorkspaceContext';
import { useAuth } from '../context/AuthContext';
import { request } from '../services/api';
import { BusinessDetail, Member } from '../types';

export default function WorkspacesDashboardPage() {
  const { user } = useAuth();
  const { workspaces, activeWorkspace, setActiveWorkspace, loading: loadingWorkspaces } = useWorkspace();

  const [detail, setDetail] = useState<BusinessDetail | null>(null);
  const [members, setMembers] = useState<Member[]>([]);
  const [loadingDetails, setLoadingDetails] = useState(false);

  // Settings update state
  const [tagline, setTagline] = useState('');
  const [primaryColor, setPrimaryColor] = useState('#4F46E5');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [updating, setUpdating] = useState(false);
  const [updateMsg, setUpdateMsg] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Fetch detailed info and members whenever active workspace changes
  useEffect(() => {
    if (!activeWorkspace) {
      setDetail(null);
      setMembers([]);
      return;
    }

    setLoadingDetails(true);
    setUpdateMsg(null);
    setError(null);

    Promise.all([
      request<BusinessDetail>(`/businesses/${activeWorkspace.id}`),
      request<Member[]>(`/businesses/${activeWorkspace.id}/members`),
    ])
      .then(([bizData, membersData]) => {
        setDetail(bizData);
        setTagline(bizData.tagline || '');
        setPrimaryColor(bizData.primaryColor || '#4F46E5');
        setPhone(bizData.phone || '');
        setAddress(bizData.address || '');
        setMembers(membersData || []);
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : 'Failed to load details.');
      })
      .finally(() => setLoadingDetails(false));
  }, [activeWorkspace]);

  const handleUpdateSettings = async (e: FormEvent) => {
    e.preventDefault();
    if (!activeWorkspace) return;

    setUpdating(true);
    setUpdateMsg(null);
    setError(null);

    try {
      const updated = await request<BusinessDetail>(`/businesses/${activeWorkspace.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          tagline: tagline.trim() || undefined,
          primaryColor: primaryColor.trim() || undefined,
          phone: phone.trim() || undefined,
          address: address.trim() || undefined,
        }),
      });

      setDetail(updated);
      setUpdateMsg('Business settings updated successfully!');
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Update failed.');
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">
            Workspaces & Multi-Tenancy
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Logged in as <strong>{user?.fullName}</strong> ({user?.email}) • Global User Account
          </p>
        </div>

        <Link
          to="/create-workspace"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs transition shadow-sm self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>Create New Business</span>
        </Link>
      </div>

      {/* Workspaces List Grid */}
      <div className="my-8">
        <h2 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4">
          Your Business Memberships ({workspaces.length})
        </h2>

        {loadingWorkspaces ? (
          <div className="flex items-center gap-2 text-slate-400 text-sm py-4">
            <RefreshCw className="w-4 h-4 animate-spin" />
            <span>Loading workspaces...</span>
          </div>
        ) : workspaces.length === 0 ? (
          <div className="p-8 rounded-2xl border-2 border-dashed border-slate-200 bg-white text-center">
            <Building2 className="w-10 h-10 text-slate-300 mx-auto mb-3" />
            <h3 className="text-base font-semibold text-slate-900">No workspaces yet</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1 mb-4">
              You haven't created or been invited to any business workspace yet.
            </p>
            <Link
              to="/create-workspace"
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-indigo-600 text-white text-xs font-semibold hover:bg-indigo-700 transition"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Create Your First Business</span>
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {workspaces.map((w) => {
              const isSelected = activeWorkspace?.id === w.id;
              return (
                <div
                  key={w.id}
                  onClick={() => setActiveWorkspace(w)}
                  className={`cursor-pointer p-5 rounded-2xl border transition relative ${
                    isSelected
                      ? 'border-indigo-600 bg-white shadow-md ring-2 ring-indigo-600/20'
                      : 'border-slate-200 bg-white hover:border-slate-300 hover:shadow-sm'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="font-bold text-slate-900 text-base">{w.businessName}</h3>
                      <p className="text-xs font-mono text-indigo-600 mt-0.5">{w.businessSlug}</p>
                    </div>
                    <span
                      className={`inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold tracking-wide uppercase ${
                        w.userRole === 'OWNER'
                          ? 'bg-emerald-100 text-emerald-800'
                          : w.userRole === 'ADMIN'
                          ? 'bg-indigo-100 text-indigo-800'
                          : 'bg-amber-100 text-amber-800'
                      }`}
                    >
                      {w.userRole}
                    </span>
                  </div>

                  <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
                    <span>{w.businessType}</span>
                    <span>Currency: <strong className="text-slate-800">{w.currency}</strong></span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Selected Workspace Deep-Dive: Details, Settings & Members */}
      {activeWorkspace && (
        <div className="mt-10 pt-8 border-t border-slate-200">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-semibold px-2 py-0.5 rounded bg-indigo-50 text-indigo-700">
                  Workspace Active
                </span>
                <span className="text-xs text-slate-400">• Tenant ID #{activeWorkspace.id}</span>
              </div>
              <h2 className="text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
                <span>{detail?.businessName || activeWorkspace.businessName}</span>
                {detail?.primaryColor && (
                  <span
                    className="w-3.5 h-3.5 rounded-full inline-block border border-white shadow-sm"
                    style={{ backgroundColor: detail.primaryColor }}
                    title={`Brand Color: ${detail.primaryColor}`}
                  />
                )}
              </h2>
              {detail?.tagline && <p className="text-sm text-slate-500 italic mt-0.5">"{detail.tagline}"</p>}
            </div>

            <a
              href={`http://localhost:8080/api/businesses/${activeWorkspace.businessSlug}`}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl border border-slate-200 bg-white text-xs font-semibold text-slate-700 hover:bg-slate-50 transition shadow-sm self-start"
            >
              <ExternalLink className="w-3.5 h-3.5 text-slate-400" />
              <span>Public JSON API</span>
            </a>
          </div>

          {error && (
            <div className="mb-6 p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-start gap-3">
              <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
              <div className="flex-1">{error}</div>
            </div>
          )}

          {updateMsg && (
            <div className="mb-6 p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
              <span>{updateMsg}</span>
            </div>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left 2 Cols: Team Members with Hierarchy & Dynamic Permissions */}
            <div className="lg:col-span-2 space-y-6">
              <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="font-bold text-slate-900 text-base flex items-center gap-2">
                    <Users className="w-5 h-5 text-indigo-600" />
                    <span>Workspace Members ({members.length})</span>
                  </h3>
                  <span className="text-xs text-slate-400">Many-to-Many Tenant Model</span>
                </div>

                <div className="divide-y divide-slate-100">
                  {members.map((m) => (
                    <div key={m.membershipId} className="py-3.5 flex items-center justify-between gap-4">
                      <div>
                        <div className="font-semibold text-sm text-slate-900 flex items-center gap-2">
                          <span>{m.fullName}</span>
                          <span
                            className={`px-2 py-0.5 rounded text-[10px] font-extrabold uppercase tracking-wider ${
                              m.roleCode === 'OWNER'
                                ? 'bg-emerald-100 text-emerald-800'
                                : m.roleCode === 'ADMIN'
                                ? 'bg-indigo-100 text-indigo-800'
                                : 'bg-slate-100 text-slate-700'
                            }`}
                          >
                            {m.roleName} (Level {m.hierarchyLevel})
                          </span>
                        </div>
                        <p className="text-xs text-slate-500 mt-0.5">{m.email}</p>
                        <div className="mt-1 flex items-center gap-1.5 flex-wrap">
                          {m.permissions?.map((p, idx) => (
                            <span key={idx} className="font-mono text-[10px] bg-slate-50 text-slate-600 px-1.5 py-0.5 rounded border border-slate-100">
                              {p}
                            </span>
                          ))}
                        </div>
                      </div>

                      <div className="text-right">
                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700">
                          {m.status}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Right Col: Workspace Profile & Branding Settings */}
            <div>
              <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
                <h3 className="font-bold text-slate-900 text-base mb-1 flex items-center gap-2">
                  <Shield className="w-5 h-5 text-indigo-600" />
                  <span>Workspace Settings</span>
                </h3>
                <p className="text-xs text-slate-400 mb-4">
                  {activeWorkspace.userRole === 'OWNER' || activeWorkspace.userRole === 'ADMIN'
                    ? 'Manage your white-label branding and contact info.'
                    : 'Read-only view for your role.'}
                </p>

                <form onSubmit={handleUpdateSettings} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                      Storefront Tagline
                    </label>
                    <input
                      type="text"
                      disabled={activeWorkspace.hierarchyLevel < 80}
                      value={tagline}
                      onChange={(e) => setTagline(e.target.value)}
                      placeholder="e.g. Elevate your strength"
                      className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-indigo-500 disabled:bg-slate-50"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                      Brand Primary Color
                    </label>
                    <div className="flex items-center gap-2">
                      <input
                        type="color"
                        disabled={activeWorkspace.hierarchyLevel < 80}
                        value={primaryColor}
                        onChange={(e) => setPrimaryColor(e.target.value)}
                        className="w-9 h-9 p-0.5 rounded-lg border border-slate-200 cursor-pointer disabled:cursor-not-allowed"
                      />
                      <input
                        type="text"
                        disabled={activeWorkspace.hierarchyLevel < 80}
                        value={primaryColor}
                        onChange={(e) => setPrimaryColor(e.target.value)}
                        className="flex-1 px-3 py-2 rounded-xl border border-slate-200 font-mono text-xs focus:ring-2 focus:ring-indigo-500 disabled:bg-slate-50"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                      Phone Number
                    </label>
                    <input
                      type="tel"
                      disabled={activeWorkspace.hierarchyLevel < 80}
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      placeholder="+91 98765 43210"
                      className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-indigo-500 disabled:bg-slate-50"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                      Physical Address
                    </label>
                    <textarea
                      rows={2}
                      disabled={activeWorkspace.hierarchyLevel < 80}
                      value={address}
                      onChange={(e) => setAddress(e.target.value)}
                      placeholder="Floor 2, Fitness Towers, Mumbai"
                      className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-indigo-500 disabled:bg-slate-50"
                    />
                  </div>

                  {activeWorkspace.hierarchyLevel >= 80 && (
                    <button
                      type="submit"
                      disabled={updating || loadingDetails}
                      className="w-full py-2.5 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs transition flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
                    >
                      {updating ? (
                        <>
                          <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                          <span>Saving...</span>
                        </>
                      ) : (
                        <>
                          <Sparkles className="w-3.5 h-3.5" />
                          <span>Save Settings</span>
                        </>
                      )}
                    </button>
                  )}
                </form>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

