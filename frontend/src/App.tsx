import { BrowserRouter, Routes, Route, Link, useNavigate } from 'react-router-dom';
import { Layers, ShieldCheck, Zap, CreditCard, LogOut, LayoutDashboard, UserCheck } from 'lucide-react';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppShell />
      </BrowserRouter>
    </AuthProvider>
  );
}

function AppShell() {
  const { isAuthenticated, user, business, logout } = useAuth();
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
            <span>SubPulse Platform</span>
          </Link>

          <nav className="flex items-center gap-6 text-sm font-medium">
            <Link to="/" className="text-slate-600 hover:text-indigo-600 transition">
              Home
            </Link>

            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noreferrer"
              className="text-slate-600 hover:text-indigo-600 transition hidden sm:inline"
            >
              Swagger Docs
            </a>

            <a
              href="http://localhost:8025"
              target="_blank"
              rel="noreferrer"
              className="text-slate-600 hover:text-indigo-600 transition hidden sm:inline"
            >
              Mailpit Inbox
            </a>

            {isAuthenticated ? (
              <div className="flex items-center gap-4">
                <Link
                  to="/dashboard"
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-indigo-50 text-indigo-700 hover:bg-indigo-100 transition text-xs font-semibold"
                >
                  <LayoutDashboard className="w-4 h-4" />
                  <span>{business?.businessName || 'Dashboard'}</span>
                </Link>

                <div className="flex items-center gap-2">
                  <span className="text-xs text-slate-500 hidden md:inline">
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
                  Sign In (OTP)
                </Link>
                <Link
                  to="/register"
                  className="px-3 py-2 rounded-lg text-slate-600 hover:text-indigo-600 transition text-xs font-semibold hidden sm:inline"
                >
                  Register Business
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
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="*" element={<HomeView />} />
        </Routes>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-200 bg-white py-6 text-center text-xs text-slate-500">
        <p>SubPulse Platform • 100% Free Sandbox & Local Architecture (Java 17, Spring Boot 3, Redis 7, Kafka KRaft, Mailpit)</p>
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
        Zero-Cost Portfolio Architecture (Java 17, Spring Boot 3, PostgreSQL, Redis, Kafka KRaft)
      </div>

      <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-slate-900 leading-tight">
        Multi-Tenant Subscription Management Platform
      </h1>
      <p className="mt-4 text-lg text-slate-600 max-w-2xl mx-auto">
        Passwordless OTP authentication, dynamic schema-agnostic plan engine, Redis caching, and transactional outbox messaging.
      </p>

      <div className="mt-8 flex items-center justify-center gap-4">
        {isAuthenticated ? (
          <Link
            to="/dashboard"
            className="px-6 py-3 rounded-xl bg-indigo-600 text-white font-semibold text-sm hover:bg-indigo-700 transition shadow-md flex items-center gap-2"
          >
            <LayoutDashboard className="w-4 h-4" />
            <span>Go to Workspace Dashboard</span>
          </Link>
        ) : (
          <>
            <Link
              to="/login"
              className="px-6 py-3 rounded-xl bg-indigo-600 text-white font-semibold text-sm hover:bg-indigo-700 transition shadow-md flex items-center gap-2"
            >
              <UserCheck className="w-4 h-4" />
              <span>Test Passwordless OTP Login</span>
            </Link>
            <Link
              to="/register"
              className="px-6 py-3 rounded-xl bg-white border border-slate-300 text-slate-700 font-semibold text-sm hover:bg-slate-50 transition shadow-sm"
            >
              Register New Business
            </Link>
          </>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-16 text-left">
        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow transition">
          <ShieldCheck className="w-8 h-8 text-indigo-600 mb-3" />
          <h3 className="font-semibold text-slate-900">Passwordless OTP & Redis Rate Limiting</h3>
          <p className="text-sm text-slate-600 mt-2">
            6-digit crypto-secure code, 5-minute ephemeral Redis storage, 60-second cooldown rate limit, and HTML email delivery via Mailpit.
          </p>
        </div>

        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow transition">
          <Layers className="w-8 h-8 text-emerald-600 mb-3" />
          <h3 className="font-semibold text-slate-900">Dynamic Feature & Plan Engine</h3>
          <p className="text-sm text-slate-600 mt-2">
            Multi-tenant schema agnostic plan builder. Custom features (Boolean, Number, Text, List) stored in PostgreSQL and cached in Redis.
          </p>
        </div>

        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow transition">
          <CreditCard className="w-8 h-8 text-blue-600 mb-3" />
          <h3 className="font-semibold text-slate-900">Tenant Isolation & Security</h3>
          <p className="text-sm text-slate-600 mt-2">
            Spring Security 6 stateless filter chain, JWT HMAC-SHA512 token verification, and ThreadLocal tenant scoping.
          </p>
        </div>
      </div>
    </div>
  );
}
