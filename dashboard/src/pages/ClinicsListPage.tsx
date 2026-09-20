import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { collection, getDocs, orderBy, query } from 'firebase/firestore'
import { db } from '../firebase'
import type { Tenant } from '../types'

export function ClinicsListPage() {
  const [clinics, setClinics] = useState<Tenant[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    // Allowed by firestore.rules: isPlatformAdmin() grants read-only access
    // across every tenants/{clinicId} doc, no clinicId filter needed here.
    getDocs(query(collection(db, 'tenants'), orderBy('clinicName')))
      .then((snap) => {
        setClinics(snap.docs.map((d) => ({ id: d.id, ...d.data() }) as Tenant))
      })
      .catch(() => setError('Could not load clinics.'))
  }, [])

  return (
    <div>
      <h1 className="text-lg font-semibold text-slate-900">Clinics</h1>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}
      {!error && clinics === null && <p className="mt-4 text-sm text-slate-500">Loading…</p>}
      {clinics !== null && clinics.length === 0 && (
        <p className="mt-4 text-sm text-slate-500">No clinics yet.</p>
      )}

      {clinics !== null && clinics.length > 0 && (
        <div className="mt-4 overflow-hidden rounded-lg border border-slate-200 bg-white">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-left text-xs font-medium uppercase text-slate-500">
              <tr>
                <th className="px-4 py-2">Clinic</th>
                <th className="px-4 py-2">City / State</th>
                <th className="px-4 py-2">Subscription</th>
                <th className="px-4 py-2">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {clinics.map((clinic) => (
                <tr key={clinic.id} className="hover:bg-slate-50">
                  <td className="px-4 py-2">
                    <Link
                      to={`/clinics/${clinic.id}`}
                      className="font-medium text-slate-900 hover:underline"
                    >
                      {clinic.clinicName}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">
                    {[clinic.city, clinic.state].filter(Boolean).join(', ') || '—'}
                  </td>
                  <td className="px-4 py-2 text-slate-600">{clinic.subscriptionStatus ?? '—'}</td>
                  <td className="px-4 py-2">
                    {clinic.suspended ? (
                      <span className="rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700">
                        Suspended
                      </span>
                    ) : (
                      <span className="rounded-full bg-green-100 px-2 py-0.5 text-xs font-medium text-green-700">
                        Active
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
