import { useState, type FormEvent } from 'react'
import { provisionPlatformAdmin } from '../lib/adminFunctions'
import {
  UserPlus,
  Shield,
  User,
  Mail,
  CheckCircle2,
  Copy,
  Check,
  AlertCircle,
  KeyRound,
  ArrowRight,
} from 'lucide-react'

export function ProvisionAdminPage() {
  const [email, setEmail] = useState('')
  const [name, setName] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<{ uid: string; temporaryPassword: string | null } | null>(
    null
  )
  const [copiedPass, setCopiedPass] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    setResult(null)
    try {
      const outcome = await provisionPlatformAdmin(email, name)
      setResult(outcome)
      setEmail('')
      setName('')
    } catch (err: unknown) {
      console.error('Failed to provision platform admin:', err)
      const message =
        err instanceof Error
          ? err.message
          : 'Could not provision that admin. Check the email and try again.'
      setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  function copyPassword(pw: string) {
    navigator.clipboard.writeText(pw).catch(() => {})
    setCopiedPass(true)
    setTimeout(() => setCopiedPass(false), 2000)
  }

  return (
    <div className="max-w-2xl space-y-6">
      <div>
        <h1 className="text-xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
          <UserPlus className="h-5 w-5 text-teal-700" />
          <span>Provision Platform Administrator</span>
        </h1>
        <p className="mt-0.5 text-xs text-slate-500">
          Grant root-level operational privileges to AgastyaOne platform engineers.
        </p>
      </div>

      <div className="flex items-start gap-3 rounded-xl border border-teal-200/80 bg-teal-50/50 p-4 text-xs text-teal-900 shadow-2xs">
        <Shield className="h-4 w-4 shrink-0 text-teal-700 mt-0.5" />
        <div>
          <span className="font-semibold">Automated Claims Provisioning:</span> This callable sets the
          immutable <code className="rounded bg-teal-100/80 px-1 py-0.5 font-mono font-medium">platformAdmin: true</code> custom
          claim. If the user already has an account, existing credentials will remain active. If not, an account with a temporary password is generated.
        </div>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-2xs">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              htmlFor="name"
              className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5"
            >
              Full Name
            </label>
            <div className="relative">
              <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                <User className="h-4 w-4" />
              </div>
              <input
                id="name"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Dr. Agastya Kancharla"
                className="block w-full rounded-lg border border-slate-300 bg-slate-50/50 pl-9 pr-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-teal-700 focus:bg-white focus:outline-none focus:ring-1 focus:ring-teal-700"
              />
            </div>
          </div>

          <div>
            <label
              htmlFor="email"
              className="block text-xs font-semibold uppercase tracking-wider text-slate-600 mb-1.5"
            >
              Administrator Email
            </label>
            <div className="relative">
              <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                <Mail className="h-4 w-4" />
              </div>
              <input
                id="email"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="ops@agastyaone.com"
                className="block w-full rounded-lg border border-slate-300 bg-slate-50/50 pl-9 pr-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-teal-700 focus:bg-white focus:outline-none focus:ring-1 focus:ring-teal-700"
              />
            </div>
          </div>

          {error && (
            <div className="flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 p-3 text-xs text-red-800">
              <AlertCircle className="h-4 w-4 text-red-600 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="pt-2">
            <button
              type="submit"
              disabled={submitting || !email.trim() || !name.trim()}
              className="inline-flex items-center gap-2 rounded-lg bg-slate-900 px-4 py-2.5 text-xs font-semibold text-white shadow-xs hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:ring-offset-2 disabled:opacity-50 cursor-pointer transition"
            >
              {submitting ? (
                <>
                  <svg className="h-4 w-4 animate-spin text-white" viewBox="0 0 24 24" fill="none">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                  </svg>
                  <span>Granting Privileges...</span>
                </>
              ) : (
                <>
                  <span>Grant Platform Admin</span>
                  <ArrowRight className="h-3.5 w-3.5" />
                </>
              )}
            </button>
          </div>
        </form>
      </div>

      {result && (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50/70 p-5 text-xs text-emerald-950 shadow-2xs space-y-3">
          <div className="flex items-center gap-2 font-bold text-emerald-900 text-sm">
            <CheckCircle2 className="h-5 w-5 text-emerald-600 shrink-0" />
            <span>Platform Admin Successfully Provisioned</span>
          </div>

          <div className="font-mono text-emerald-800">
            Account UID: <span className="font-bold">{result.uid}</span>
          </div>

          {result.temporaryPassword ? (
            <div className="rounded-lg border border-emerald-300 bg-white p-3.5 space-y-2">
              <div className="flex items-center gap-1.5 font-semibold text-slate-800">
                <KeyRound className="h-4 w-4 text-amber-600" />
                <span>One-Time Temporary Password</span>
              </div>
              <p className="text-[11px] text-slate-500">
                Share this credential out-of-band. The administrator will be prompted to reset upon first sign-in:
              </p>
              <div className="flex items-center justify-between rounded-md bg-slate-50 border border-slate-200 px-3 py-1.5 font-mono text-sm font-bold text-slate-900">
                <span>{result.temporaryPassword}</span>
                <button
                  type="button"
                  onClick={() => copyPassword(result.temporaryPassword!)}
                  className="inline-flex items-center gap-1 rounded bg-white px-2 py-0.5 text-xs font-medium text-slate-700 shadow-2xs border border-slate-200 hover:bg-slate-50 cursor-pointer"
                >
                  {copiedPass ? (
                    <Check className="h-3 w-3 text-emerald-600" />
                  ) : (
                    <Copy className="h-3 w-3" />
                  )}
                  <span>{copiedPass ? 'Copied' : 'Copy'}</span>
                </button>
              </div>
            </div>
          ) : (
            <div className="text-emerald-800">
              An existing Firebase Auth user was found with this email. Their password remains unchanged.
            </div>
          )}
        </div>
      )}
    </div>
  )
}
