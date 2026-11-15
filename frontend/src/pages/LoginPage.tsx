import { useState, useEffect, useRef, type FormEvent, type KeyboardEvent, type ClipboardEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, ArrowRight, ShieldCheck, KeyRound, AlertCircle, RefreshCw, UserPlus } from 'lucide-react';
import { request } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { AuthResponse, OtpSendResponse } from '../types';

export default function LoginPage() {
  const navigate = useNavigate();
  const { loginWithAuthResponse, isAuthenticated } = useAuth();

  const [step, setStep] = useState<'EMAIL' | 'OTP'>('EMAIL');
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Sign up prompt when account does not exist
  const [accountNotFound, setAccountNotFound] = useState(false);

  // OTP State: 6 individual digit boxes
  const [otpDigits, setOtpDigits] = useState(['', '', '', '', '', '']);
  const [cooldown, setCooldown] = useState(0);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard');
    }
  }, [isAuthenticated, navigate]);

  // Cooldown countdown timer
  useEffect(() => {
    if (cooldown <= 0) return;
    const timer = setInterval(() => {
      setCooldown((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldown]);

  const handleSendOtp = async (e?: FormEvent) => {
    if (e) e.preventDefault();
    if (!email.trim() || !email.includes('@')) {
      setError('Please enter a valid email address.');
      return;
    }

    setLoading(true);
    setError(null);
    setAccountNotFound(false);

    try {
      const response = await request<OtpSendResponse>('/auth/otp/send', {
        method: 'POST',
        body: JSON.stringify({ email: email.trim() }),
      });

      if (!response.exists) {
        setAccountNotFound(true);
        setError(response.message || 'No account found with this email.');
      } else {
        setStep('OTP');
        setCooldown(response.cooldownSeconds || 60);
        setOtpDigits(['', '', '', '', '', '']);
        setTimeout(() => inputRefs.current[0]?.focus(), 100);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to send verification code.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleDigitChange = (index: number, value: string) => {
    // Only accept numeric digit
    const char = value.replace(/\D/g, '').slice(-1);
    const newDigits = [...otpDigits];
    newDigits[index] = char;
    setOtpDigits(newDigits);
    setError(null);

    // Auto-advance to next input
    if (char && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }

    // Auto-submit if all 6 digits are populated
    const fullCode = newDigits.join('');
    if (fullCode.length === 6) {
      submitVerifyOtp(fullCode);
    }
  };

  const handleKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && !otpDigits[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (pasted.length > 0) {
      const newDigits = ['', '', '', '', '', ''];
      for (let i = 0; i < pasted.length; i++) {
        newDigits[i] = pasted[i];
      }
      setOtpDigits(newDigits);
      if (pasted.length === 6) {
        submitVerifyOtp(pasted);
      } else {
        inputRefs.current[pasted.length]?.focus();
      }
    }
  };

  const submitVerifyOtp = async (code: string) => {
    setLoading(true);
    setError(null);

    try {
      const authRes = await request<AuthResponse>('/auth/otp/verify', {
        method: 'POST',
        body: JSON.stringify({ email: email.trim(), otp: code }),
      });

      loginWithAuthResponse(authRes);
      navigate('/dashboard');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Invalid code.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-8rem)] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-xl p-8">
        {/* Top Icon & Badge */}
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-indigo-50 text-indigo-600 mb-4 ring-8 ring-indigo-50/50">
            {step === 'EMAIL' ? <Mail className="w-7 h-7" /> : <KeyRound className="w-7 h-7" />}
          </div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            {step === 'EMAIL' ? 'Welcome to SubPulse' : 'Check your email'}
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            {step === 'EMAIL'
              ? 'Enter your work email for instant passwordless OTP login.'
              : `We sent a 6-digit code to ${email}`}
          </p>
          {step === 'OTP' && (
            <button
              type="button"
              onClick={() => {
                setStep('EMAIL');
                setError(null);
              }}
              className="text-xs text-indigo-600 hover:text-indigo-800 font-medium mt-1 inline-block"
            >
              (Wrong email? Edit)
            </button>
          )}
        </div>

        {/* Error Alert */}
        {error && (
          <div className="mb-6 p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-start gap-3">
            <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
            <div className="flex-1">{error}</div>
          </div>
        )}

        {/* Account Not Found Banner - Directs to Sign Up */}
        {accountNotFound && (
          <div className="mb-6 p-4 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-sm">
            <div className="flex items-center gap-2 font-semibold mb-1">
              <UserPlus className="w-4 h-4 text-amber-700" />
              <span>Ready to start your business?</span>
            </div>
            <p className="text-xs text-amber-700 mb-3">
              No existing tenant account found with <strong>{email}</strong>. Create your business workspace in 60 seconds.
            </p>
            <Link
              to={`/register?email=${encodeURIComponent(email)}`}
              className="inline-flex items-center justify-center w-full gap-2 px-4 py-2.5 rounded-lg bg-amber-600 text-white font-medium text-xs hover:bg-amber-700 transition shadow-sm"
            >
              <span>Sign Up & Create Business</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        )}

        {/* STEP 1: EMAIL INPUT */}
        {step === 'EMAIL' && (
          <form onSubmit={handleSendOtp} className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
                Work Email Address
              </label>
              <div className="relative">
                <input
                  id="email"
                  type="email"
                  required
                  placeholder="alex@fitzone.com"
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value);
                    if (accountNotFound) setAccountNotFound(false);
                    if (error) setError(null);
                  }}
                  className="w-full px-4 py-3 pl-10 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-sm transition"
                />
                <Mail className="w-5 h-5 text-slate-400 absolute left-3 top-3.5" />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-sm transition flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
            >
              {loading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  <span>Checking account...</span>
                </>
              ) : (
                <>
                  <span>Send Login Code</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>

            <div className="pt-4 text-center border-t border-slate-100">
              <p className="text-xs text-slate-500">
                New to SubPulse?{' '}
                <Link to="/register" className="text-indigo-600 hover:text-indigo-800 font-semibold">
                  Register your business
                </Link>
              </p>
            </div>
          </form>
        )}

        {/* STEP 2: 6-DIGIT OTP INPUT */}
        {step === 'OTP' && (
          <div className="space-y-6">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-3 text-center">
                Enter 6-Digit Code
              </label>
              <div className="flex justify-between gap-2" onPaste={handlePaste}>
                {otpDigits.map((digit, i) => (
                  <input
                    key={i}
                    ref={(el) => (inputRefs.current[i] = el)}
                    type="text"
                    inputMode="numeric"
                    maxLength={1}
                    value={digit}
                    onChange={(e) => handleDigitChange(i, e.target.value)}
                    onKeyDown={(e) => handleKeyDown(i, e)}
                    className="w-12 h-14 text-center text-xl font-bold rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-600 focus:border-indigo-600 transition bg-slate-50/50"
                  />
                ))}
              </div>
            </div>

            <button
              type="button"
              disabled={loading || otpDigits.join('').length < 6}
              onClick={() => submitVerifyOtp(otpDigits.join(''))}
              className="w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-sm transition flex items-center justify-center gap-2 shadow-sm disabled:opacity-50"
            >
              {loading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  <span>Verifying code...</span>
                </>
              ) : (
                <>
                  <ShieldCheck className="w-4 h-4" />
                  <span>Verify & Sign In</span>
                </>
              )}
            </button>

            {/* Resend Cooldown Section */}
            <div className="text-center pt-2">
              {cooldown > 0 ? (
                <p className="text-xs text-slate-400">
                  Resend code in <span className="font-semibold text-slate-600">{cooldown}s</span>
                </p>
              ) : (
                <button
                  type="button"
                  onClick={() => handleSendOtp()}
                  disabled={loading}
                  className="text-xs text-indigo-600 hover:text-indigo-800 font-semibold inline-flex items-center gap-1"
                >
                  <RefreshCw className="w-3 h-3" />
                  <span>Didn't get code? Resend</span>
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
