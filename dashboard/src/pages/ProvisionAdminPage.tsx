import { useState, type FormEvent } from 'react'
import { provisionPlatformAdmin } from '../lib/adminFunctions'

export function ProvisionAdminPage() {
  const [email, setEmail] = useState('')
  const [name, setName] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<{ uid: string; temporaryPassword: string | null } | null>(
    null
  )

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
        err instanceof Error ? err.message : 'Could not provision that admin. Check the email and try again.'
      setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-md">
      <h1 className="text-lg font-semibold text-slate-900">Provision a platform admin</h1>
      <p className="mt-1 text-sm text-slate-500">
        Looks up an existing Auth account by email, or creates one, then grants the
        platformAdmin claim. Replaces doing this by hand in the Firebase console.
      </p>

      <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-slate-700">
            Name
          </label>
          <input
            id="name"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-slate-700">
            Email
          </label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
        >
          {submitting ? 'Provisioning…' : 'Provision admin'}
        </button>
      </form>

      {result && (
        <div className="mt-6 rounded-md border border-green-200 bg-green-50 p-4 text-sm text-green-900">
          <p>
            Admin provisioned (uid <code>{result.uid}</code>).
          </p>
          {result.temporaryPassword && (
            <p className="mt-2">
              New account created with a one-time temporary password — share it out of band and
              have them change it after first sign-in:
              <br />
              <code className="mt-1 inline-block rounded bg-white px-2 py-1">
                {result.temporaryPassword}
              </code>
            </p>
          )}
          {!result.temporaryPassword && (
            <p className="mt-2">
              An existing account was reused — they can sign in with their current password.
            </p>
          )}
        </div>
      )}
    </div>
  )
}
