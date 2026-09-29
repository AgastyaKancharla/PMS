import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { collection, doc, getDoc, getDocs } from 'firebase/firestore'
import { db } from '../firebase'
import { listAuditLogsForClinic, setTenantSuspended } from '../lib/adminFunctions'
import type { AuditLogEntry, StaffMember, Tenant } from '../types'
import {
  ArrowLeft,
  Building2,
  Users,
  History,
  AlertTriangle,
  CheckCircle2,
  MapPin,
  Clock,
  ShieldAlert,
  FileText,
  Copy,
  Check,
} from 'lucide-react'

export function ClinicDetailPage() {
  const { clinicId } = useParams<{ clinicId: string }>()
  const [tenant, setTenant] = useState<Tenant | null>(null)
  const [staff, setStaff] = useState<StaffMember[]>([])
  const [auditLog, setAuditLog] = useState<AuditLogEntry[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [suspending, setSuspending] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const [copiedId, setCopiedId] = useState(false)
  const [activeTab, setActiveTab] = useState<'staff' | 'audit'>('staff')

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
    return () => {
      ignore = true
    }
  }, [clinicId])

  useEffect(() => {
    if (!clinicId) return
    listAuditLogsForClinic(clinicId)
      .then(setAuditLog)
      .catch((err) => {
        console.error('Failed to load audit logs:', err)
        setAuditLog([])
      })
  }, [clinicId])

  async function handleToggleSuspended() {
    if (!clinicId || !tenant) return
    const nextSuspended = !tenant.suspended
    const promptMessage = nextSuspended
      ? `Are you sure you want to suspend "${tenant.clinicName}"? Staff will be blocked from accessing clinic data.`
      : `Reactivate "${tenant.clinicName}"? Staff access will be immediately restored.`
    if (!window.confirm(promptMessage)) return

    setSuspending(true)
    setActionError(null)
    try {
      await setTenantSuspended(clinicId, nextSuspended)
      setTenant({ ...tenant, suspended: nextSuspended })
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

  function copyTenantId() {
    if (!clinicId) return
    navigator.clipboard.writeText(clinicId).catch(() => {})
    setCopiedId(true)
    setTimeout(() => setCopiedId(false), 2000)
  }

  if (loadError) {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center">
        <ShieldAlert className="mx-auto h-8 w-8 text-red-600" />
        <h2 className="mt-2 text-sm font-bold text-red-900">{loadError}</h2>
        <Link
          to="/clinics"
          className="mt-4 inline-flex items-center gap-1.5 text-xs font-semibold text-red-800 underline hover:text-red-900"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          <span>Back to Clinics</span>
        </Link>
      </div>
    )
  }

  if (!tenant) {
    return (
      <div className="rounded-xl border border-slate-200 bg-white p-12 text-center shadow-2xs">
        <Clock className="mx-auto h-6 w-6 animate-spin text-teal-700" />
        <p className="mt-3 text-xs font-medium text-slate-500">Loading clinic profile...</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <Link
          to="/clinics"
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-900 transition"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          <span>Back to Clinics</span>
        </Link>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-2xs">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div className="flex items-start gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-teal-800 text-white font-bold text-lg shadow-xs">
              <Building2 className="h-6 w-6" />
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h1 className="text-xl font-bold tracking-tight text-slate-900">
                  {tenant.clinicName}
                </h1>
                {tenant.suspended ? (
                  <span className="inline-flex items-center gap-1 rounded-full bg-rose-50 px-2.5 py-0.5 text-xs font-semibold text-rose-700 border border-rose-200">
                    <span className="h-1.5 w-1.5 rounded-full bg-rose-500" />
                    Suspended
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-semibold text-emerald-700 border border-emerald-200">
                    <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                    Active
                  </span>
                )}
                <span className="rounded-md bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-600 border border-slate-200">
                  {tenant.toothNumberingSystem ?? 'FDI System'}
                </span>
              </div>

              <div className="mt-1 flex flex-wrap items-center gap-4 text-xs text-slate-500">
                <div className="flex items-center gap-1">
                  <MapPin className="h-3.5 w-3.5 text-slate-400" />
                  <span>
                    {[tenant.address, tenant.city, tenant.state].filter(Boolean).join(', ') ||
                      'No physical address registered'}
                  </span>
                </div>
                {tenant.gstin && (
                  <div className="flex items-center gap-1 font-mono text-[11px] text-slate-600 bg-slate-100 px-2 py-0.5 rounded">
                    <span>GSTIN:</span>
                    <span className="font-semibold">{tenant.gstin}</span>
                  </div>
                )}
              </div>

              <div className="mt-2 flex items-center gap-2">
                <span className="text-[11px] text-slate-400 font-mono">Tenant ID: {tenant.id}</span>
                <button
                  type="button"
                  onClick={copyTenantId}
                  className="inline-flex items-center gap-1 text-[11px] text-slate-500 hover:text-slate-800 cursor-pointer"
                  title="Copy Tenant ID"
                >
                  {copiedId ? (
                    <Check className="h-3 w-3 text-emerald-600" />
                  ) : (
                    <Copy className="h-3 w-3" />
                  )}
                  <span>{copiedId ? 'Copied' : 'Copy'}</span>
                </button>
              </div>
            </div>
          </div>

          <div>
            <button
              type="button"
              disabled={suspending}
              onClick={() => void handleToggleSuspended()}
              className={`inline-flex items-center gap-1.5 rounded-lg px-3.5 py-2 text-xs font-semibold text-white shadow-xs transition disabled:opacity-50 cursor-pointer ${
                tenant.suspended
                  ? 'bg-emerald-600 hover:bg-emerald-500'
                  : 'bg-rose-600 hover:bg-rose-500'
              }`}
            >
              {tenant.suspended ? (
                <>
                  <CheckCircle2 className="h-4 w-4" />
                  <span>Reactivate Clinic</span>
                </>
              ) : (
                <>
                  <AlertTriangle className="h-4 w-4" />
                  <span>Suspend Access</span>
                </>
              )}
            </button>
          </div>
        </div>

        {actionError && (
          <div className="mt-4 flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 p-3 text-xs text-red-800">
            <AlertTriangle className="h-4 w-4 text-red-600 shrink-0" />
            <span>{actionError}</span>
          </div>
        )}
      </div>

      <div className="flex border-b border-slate-200">
        <button
          type="button"
          onClick={() => setActiveTab('staff')}
          className={`flex items-center gap-2 border-b-2 px-4 py-2.5 text-xs font-semibold transition cursor-pointer ${
            activeTab === 'staff'
              ? 'border-slate-900 text-slate-900'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          <Users className="h-4 w-4" />
          <span>Staff Registry ({staff.length})</span>
        </button>
        <button
          type="button"
          onClick={() => setActiveTab('audit')}
          className={`flex items-center gap-2 border-b-2 px-4 py-2.5 text-xs font-semibold transition cursor-pointer ${
            activeTab === 'audit'
              ? 'border-slate-900 text-slate-900'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          <History className="h-4 w-4" />
          <span>Audit Log {auditLog ? `(${auditLog.length})` : ''}</span>
        </button>
      </div>

      {activeTab === 'staff' && (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-2xs">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-left text-xs">
              <thead className="bg-slate-50/80 font-semibold text-slate-600 uppercase tracking-wider text-[11px]">
                <tr>
                  <th className="px-4 py-3">Member</th>
                  <th className="px-4 py-3">Assigned Role</th>
                  <th className="px-4 py-3">Contact</th>
                  <th className="px-4 py-3">Account UID</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-normal text-slate-800">
                {staff.map((member) => {
                  const roleColors =
                    member.role === 'owner'
                      ? 'bg-purple-50 text-purple-700 border-purple-200'
                      : member.role === 'receptionist'
                      ? 'bg-blue-50 text-blue-700 border-blue-200'
                      : member.role === 'assistant'
                      ? 'bg-teal-50 text-teal-700 border-teal-200'
                      : 'bg-amber-50 text-amber-700 border-amber-200'

                  return (
                    <tr key={member.uid} className="hover:bg-slate-50/75 transition-colors">
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2.5">
                          <div className="flex h-7 w-7 items-center justify-center rounded-full bg-slate-100 text-slate-700 font-semibold text-xs border border-slate-200">
                            {member.name.charAt(0).toUpperCase()}
                          </div>
                          <span className="font-semibold text-slate-900">{member.name}</span>
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <span
                          className={`inline-flex items-center rounded-md px-2 py-0.5 text-[11px] font-semibold border ${roleColors}`}
                        >
                          {member.role.toUpperCase()}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-slate-600 font-mono text-[11px]">
                        {member.email ?? member.phone ?? '—'}
                      </td>
                      <td className="px-4 py-3 font-mono text-[10px] text-slate-400">
                        {member.uid}
                      </td>
                    </tr>
                  )
                })}
                {staff.length === 0 && (
                  <tr>
                    <td colSpan={4} className="px-4 py-8 text-center text-slate-400">
                      No staff members on record for this practice.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'audit' && (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-2xs">
          {auditLog === null ? (
            <div className="p-8 text-center text-xs text-slate-500">
              <Clock className="mx-auto h-5 w-5 animate-spin text-teal-700 mb-2" />
              Loading security audit trail...
            </div>
          ) : auditLog.length === 0 ? (
            <div className="p-8 text-center text-xs text-slate-400">
              <FileText className="mx-auto h-6 w-6 text-slate-300 mb-2" />
              No audit log entries recorded for this clinic.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-left text-xs">
                <thead className="bg-slate-50/80 font-semibold text-slate-600 uppercase tracking-wider text-[11px]">
                  <tr>
                    <th className="px-4 py-3">Timestamp</th>
                    <th className="px-4 py-3">Action Event</th>
                    <th className="px-4 py-3">Performed By</th>
                    <th className="px-4 py-3">Target Doc</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 font-normal text-slate-800">
                  {auditLog.map((entry) => (
                    <tr key={entry.id} className="hover:bg-slate-50/75 transition-colors">
                      <td className="px-4 py-3 font-mono text-[11px] text-slate-500 whitespace-nowrap">
                        {entry.timestamp ? new Date(entry.timestamp).toLocaleString() : '—'}
                      </td>
                      <td className="px-4 py-3">
                        <span className="inline-flex items-center rounded bg-slate-100 px-2 py-0.5 font-mono text-[11px] font-semibold text-slate-800 border border-slate-200">
                          {entry.actionType}
                        </span>
                      </td>
                      <td className="px-4 py-3 font-mono text-[11px] text-slate-600">
                        {entry.performedByUid}
                      </td>
                      <td className="px-4 py-3 font-mono text-[11px] text-slate-500 truncate max-w-[200px]">
                        {entry.targetCollection}/{entry.targetDocId}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
