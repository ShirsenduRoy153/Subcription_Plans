import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import { Layers, ShieldCheck, Zap, CreditCard } from 'lucide-react';

export default function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-800">
        {/* Navigation Bar */}
        <header className="border-b border-slate-200 bg-white sticky top-0 z-50 shadow-sm">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
            <Link to="/" className="flex items-center gap-2 text-indigo-600 font-bold text-xl">
              <Layers className="w-6 h-6" />
              <span>SubPulse Platform</span>
            </Link>
            <nav className="flex items-center gap-6 text-sm font-medium">
              <Link to="/" className="text-slate-600 hover:text-indigo-600 transition">Home</Link>
              <Link to="/demo" className="text-slate-600 hover:text-indigo-600 transition">Sample Business</Link>
              <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer" className="text-slate-600 hover:text-indigo-600 transition">
                API Docs (Swagger)
              </a>
              <Link to="/login" className="px-4 py-2 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition">
                Sign In
              </Link>
            </nav>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1">
          <Routes>
            <Route path="/" element={<HomeView />} />
            <Route path="*" element={<HomeView />} />
          </Routes>
        </main>

        {/* Footer */}
        <footer className="border-t border-slate-200 bg-white py-6 text-center text-xs text-slate-500">
          <p>Multi-Tenant Dynamic Subscription Platform • 100% Free Sandbox & Local Environment Architecture</p>
        </footer>
      </div>
    </BrowserRouter>
  );
}

function HomeView() {
  return (
    <div className="max-w-5xl mx-auto px-4 py-16 text-center">
      <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-50 text-indigo-700 text-xs font-semibold mb-6">
        <Zap className="w-4 h-4" />
        Zero-Cost Portfolio Architecture (Java 17, Spring Boot 3, PostgreSQL, Redis, RabbitMQ)
      </div>
      <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-slate-900 leading-tight">
        Multi-Tenant Subscription Management Platform
      </h1>
      <p className="mt-4 text-lg text-slate-600 max-w-2xl mx-auto">
        Dynamic schema-agnostic plan engine, transactional outbox messaging, Spring Scheduler, and Razorpay sandbox payments.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-12 text-left">
        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow transition">
          <Layers className="w-8 h-8 text-indigo-600 mb-3" />
          <h3 className="font-semibold text-slate-900">Dynamic Feature Engine</h3>
          <p className="text-sm text-slate-600 mt-2">
            No hardcoded industries. Businesses define custom features (Boolean, Number, Text, List) and assemble dynamic plans.
          </p>
        </div>

        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow transition">
          <ShieldCheck className="w-8 h-8 text-emerald-600 mb-3" />
          <h3 className="font-semibold text-slate-900">Tenant Isolation & Security</h3>
          <p className="text-sm text-slate-600 mt-2">
            Spring Security with JWT authentication. Strict tenant scoping ensures complete data privacy across businesses.
          </p>
        </div>

        <div className="p-6 rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow transition">
          <CreditCard className="w-8 h-8 text-blue-600 mb-3" />
          <h3 className="font-semibold text-slate-900">Razorpay Sandbox (100% Free)</h3>
          <p className="text-sm text-slate-600 mt-2">
            Simulated test checkouts, webhook signature verification, and frozen entitlement snapshots upon payment.
          </p>
        </div>
      </div>
    </div>
  );
}
