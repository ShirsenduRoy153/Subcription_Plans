import { BrowserRouter, Routes, Route, Link, useNavigate } from 'react-router-dom';
import { Layers, ShieldCheck, Zap, LogOut, Building2, Plus, Sparkles, UserCheck } from 'lucide-react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { WorkspaceProvider, useWorkspace } from './context/WorkspaceContext';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CreateWorkspacePage from './pages/CreateWorkspacePage';
import WorkspacesDashboardPage from './pages/WorkspacesDashboardPage';

export default function App() {
  return (
    <AuthProvider>
      <WorkspaceProvider>
        <BrowserRouter>
          <AppShell />
        </BrowserRouter>
      </WorkspaceProvider>
    </AuthProvider>
  );
}

function AppShell() {
  const { isAuthenticated, user, logout } = useAuth();
  const { activeWorkspace } = useWorkspace();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-800">
      {/* Navigation Bar */}
      <header className="border-b border-slate-200 bg-white sticky top-0 z-50 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 text-indigo-600 font-bold text-xl">
            <Layers className="w-6 h-6" />
            <span>SubPulse</span>
          </Link>

          <nav className="flex items-center gap-5 text-sm font-medium">
            <Link to="/" className="text-slate-600 hover:text-indigo-600 transition">
              Home
            </Link>

            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noreferrer"
              className="text-slate-600 hover:text-indigo-600 transition hidden sm:inline"
            >
              Swagger
            </a>

            {isAuthenticated ? (
              <div className="flex items-center gap-3">
                {activeWorkspace && (
                  <Link
                    to="/workspaces"
                    className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-indigo-50 text-indigo-700 hover:bg-indigo-100 transition text-xs font-semibold"
                  >
                    <Building2 className="w-4 h-4" />
                    <span>{activeWorkspace.businessName}</span>
                    <span className="bg-indigo-200/80 text-indigo-800 text-[10px] px-1.5 py-0.2 rounded font-extrabold uppercase">
                      {activeWorkspace.userRole}
                    </span>
                  </Link>
                )}

                <Link
                  to="/create-workspace"
                  title="Create New Business"
                  className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 transition text-xs font-semibold"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span className="hidden md:inline">New Business</span>
                </Link>

                <div className="flex items-center gap-2 pl-2 border-l border-slate-200">
                  <span className="text-xs text-slate-600 font-medium hidden lg:inline">
                    {user?.fullName}
                  </span>
                  <button
                    onClick={handleLogout}
                    title="Sign Out"
                    className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition"
                  >
                    <LogOut className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ) : (
              <div className="flex items-center gap-3">
                <Link
                  to="/login"
                  className="px-4 py-2 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition text-xs font-semibold shadow-sm"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="px-3 py-2 rounded-lg text-slate-600 hover:text-indigo-600 transition text-xs font-semibold hidden sm:inline"
                >
                  Sign Up
                </Link>
              </div>
            )}
          </nav>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<HomeView />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/workspaces" element={<WorkspacesDashboardPage />} />
          <Route path="/create-workspace" element={<CreateWorkspacePage />} />
          <Route path="*" element={<HomeView />} />
        </Routes>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-200 bg-white py-6 text-center text-xs text-slate-500">
        <p>SubPulse Platform • Many-to-Many Multi-Tenant Architecture (Java 17, Spring Boot 3, PostgreSQL 16)</p>
      </footer>
    </div>
  );
}

function HomeView() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="max-w-5xl mx-auto px-4 py-16 text-center">
      <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-50 text-indigo-700 text-xs font-semibold mb-6">
        <Zap className="w-4 h-4" />
        Multi-Tenant Architecture • Dynamic RBAC & Many-to-Many Workspaces
      </div>

      <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-slate-900 leading-tight">
        Multi-Tenant Subscription Management Platform
      </h1>
      <p className="mt-4 text-lg text-slate-600 max-w-2xl mx-auto">
        Global user accounts, many-to-many business workspaces, dynamic domain reference data, and hierarchical role-based permissions.
      </p>

      <div className="mt-8 flex items-center justify-center gap-4">
        {isAuthenticated ? (
          <Link
            to="/workspaces"
            className="px-6 py-3 rounded-xl bg-indigo-600 text-white font-semibold text-sm hover:bg-indigo-700 transition shadow-md flex items-center gap-2"
          >
            <Building2 className="w-4 h-4" />
            <span>Go to Workspaces Dashboard</span>
          </Link>
        ) : (
          <>
            <Link
              to="/register"
              className="px-6 py-3 rounded-xl bg-indigo-600 text-white font-semibold text-sm hover:bg-indigo-700 transition shadow-md flex items-center gap-2"
            >
              <UserCheck className="w-4 h-4" />
              <span>Create Account</span>
            </Link>
            <Link
              to="/login"
              className="px-6 py-3 rounded-xl bg-white border border-slate-300 text-slate-700 font-semibold text-sm hover:bg-slate-50 transition shadow-sm"
            >
              Sign In
            </Link>
          </>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-16 text-left">
        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow transition">
          <Building2 className="w-8 h-8 text-indigo-600 mb-3" />
          <h3 className="font-semibold text-slate-900">Many-to-Many Workspaces</h3>
          <p className="text-sm text-slate-600 mt-2">
            One user can belong to multiple businesses with different roles (Owner at Gym A, Staff at Studio B).
          </p>
        </div>

        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow transition">
          <ShieldCheck className="w-8 h-8 text-emerald-600 mb-3" />
          <h3 className="font-semibold text-slate-900">Dynamic RBAC & Hierarchy</h3>
          <p className="text-sm text-slate-600 mt-2">
            Zero hardcoded roles. Dedicated security table with numerical hierarchy levels (100, 80, 40, 10) and JSON permissions.
          </p>
        </div>

        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow transition">
          <Sparkles className="w-8 h-8 text-amber-500 mb-3" />
          <h3 className="font-semibold text-slate-900">Unified Domain Lookups</h3>
          <p className="text-sm text-slate-600 mt-2">
            Centralized reference data table for Business Types and Currencies with custom sort ordering.
          </p>
        </div>
      </div>
    </div>
  );
}
