import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { collection, doc, getDoc, getDocs } from 'firebase/firestore'
import { db } from '../firebase'
import { listAuditLogsForClinic, setTenantSuspended } from '../lib/adminFunctions'
import type { AuditLogEntry, StaffMember, Tenant } from '../types'

export function ClinicDetailPage() {
  const { clinicId } = useParams<{ clinicId: string }>()
  const [tenant, setTenant] = useState<Tenant | null>(null)
  const [staff, setStaff] = useState<StaffMember[]>([])
  const [auditLog, setAuditLog] = useState<AuditLogEntry[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [suspending, setSuspending] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)

  useEffect(() => {
    let ignore = false
    async function load() {
      if (!clinicId) return
      try {
        const tenantSnap = await getDoc(doc(db, 'tenants', clinicId))
        if (ignore) return
        if (!tenantSnap.exists()) {
          setLoadError('Clinic not found.')
          return
        }
        setTenant({ id: tenantSnap.id, ...tenantSnap.data() } as Tenant)

        const staffSnap = await getDocs(collection(db, 'tenants', clinicId, 'staff'))
        if (ignore) return
        setStaff(staffSnap.docs.map((d) => ({ uid: d.id, ...d.data() }) as StaffMember))
      } catch {
        if (!ignore) setLoadError('Could not load this clinic.')
      }
    }
    load()
    return () => { ignore = true }
  }, [clinicId])

  useEffect(() => {
    if (!clinicId) return
    // Firestore rules give a platform admin no read access to a clinic's audit
    // log (owner-only there) - this goes through the admin-only callable
    // instead, which reads it with the Admin SDK. See listAuditLogsForClinic.ts.
    listAuditLogsForClinic(clinicId)
      .then(setAuditLog)
      .catch((err) => {
        console.error('Failed to load audit logs:', err)
        setAuditLog([])
      })
  }, [clinicId])

  async function handleToggleSuspended() {
    if (!clinicId || !tenant) return
    setSuspending(true)
    setActionError(null)
    try {
      const nextSuspended = !tenant.suspended
      await setTenantSuspended(clinicId, nextSuspended)
      setTenant({ ...tenant, suspended: nextSuspended })
      // Refresh audit logs so the suspension/reactivation event is visible immediately
      listAuditLogsForClinic(clinicId)
        .then(setAuditLog)
        .catch(() => {})
    } catch (err: unknown) {
      console.error('Failed to update suspension status:', err)
      const message = err instanceof Error ? err.message : 'Could not update suspension status.'
      setActionError(message)
    } finally {
      setSuspending(false)
    }
  }

  if (loadError) {
    return <p className="text-sm text-red-600">{loadError}</p>
  }
  if (!tenant) {
    return <p className="text-sm text-slate-500">Loading…</p>
  }

  return (
    <div>
      <Link to="/clinics" className="text-sm text-slate-500 hover:underline">
        ← Clinics
      </Link>

      <div className="mt-2 flex items-start justify-between">
        <div>
          <h1 className="text-lg font-semibold text-slate-900">{tenant.clinicName}</h1>
          <p className="text-sm text-slate-500">
            {[tenant.city, tenant.state].filter(Boolean).join(', ')}
            {tenant.gstin ? ` · GSTIN ${tenant.gstin}` : ''}
          </p>
        </div>
        <button
          type="button"
          disabled={suspending}
          onClick={() => void handleToggleSuspended()}
          className={`rounded-md px-3 py-1.5 text-sm font-medium disabled:opacity-50 ${
            tenant.suspended
              ? 'bg-green-600 text-white hover:bg-green-500'
              : 'bg-red-600 text-white hover:bg-red-500'
          }`}
        >
          {tenant.suspended ? 'Reactivate clinic' : 'Suspend clinic'}
        </button>
      </div>
      {actionError && <p className="mt-2 text-sm text-red-600">{actionError}</p>}

      <section className="mt-6">
        <h2 className="text-sm font-semibold text-slate-900">Staff</h2>
        <div className="mt-2 overflow-hidden rounded-lg border border-slate-200 bg-white">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-left text-xs font-medium uppercase text-slate-500">
              <tr>
                <th className="px-4 py-2">Name</th>
                <th className="px-4 py-2">Role</th>
                <th className="px-4 py-2">Contact</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {staff.map((member) => (
                <tr key={member.uid}>
                  <td className="px-4 py-2 text-slate-900">{member.name}</td>
                  <td className="px-4 py-2 text-slate-600">{member.role}</td>
                  <td className="px-4 py-2 text-slate-600">
                    {member.email ?? member.phone ?? '—'}
                  </td>
                </tr>
              ))}
              {staff.length === 0 && (
                <tr>
                  <td className="px-4 py-2 text-slate-500" colSpan={3}>
                    No staff on record.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="mt-6">
        <h2 className="text-sm font-semibold text-slate-900">Audit log</h2>
        <div className="mt-2 overflow-hidden rounded-lg border border-slate-200 bg-white">
          {auditLog === null && (
            <p className="px-4 py-2 text-sm text-slate-500">Loading…</p>
          )}
          {auditLog !== null && auditLog.length === 0 && (
            <p className="px-4 py-2 text-sm text-slate-500">No entries yet.</p>
          )}
          {auditLog !== null && auditLog.length > 0 && (
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50 text-left text-xs font-medium uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-2">When</th>
                  <th className="px-4 py-2">Action</th>
                  <th className="px-4 py-2">Performed by</th>
                  <th className="px-4 py-2">Target</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {auditLog.map((entry) => (
                  <tr key={entry.id}>
                    <td className="px-4 py-2 text-slate-600">
                      {entry.timestamp ? new Date(entry.timestamp).toLocaleString() : '—'}
                    </td>
                    <td className="px-4 py-2 text-slate-900">{entry.actionType}</td>
                    <td className="px-4 py-2 text-slate-600">{entry.performedByUid}</td>
                    <td className="px-4 py-2 text-slate-600">
                      {entry.targetCollection}/{entry.targetDocId}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>
    </div>
  )
}
