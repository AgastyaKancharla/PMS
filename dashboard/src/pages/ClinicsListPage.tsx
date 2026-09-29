import { useEffect, useState, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { collection, getDocs, orderBy, query } from 'firebase/firestore'
import { db } from '../firebase'
import type { Tenant } from '../types'
import {
  Building2,
  Search,
  CheckCircle2,
  AlertTriangle,
  MapPin,
  ChevronRight,
  ShieldAlert,
  Filter,
  RefreshCw,
} from 'lucide-react'

export function ClinicsListPage() {
  const [clinics, setClinics] = useState<Tenant[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'suspended'>('all')
  const [refreshing, setRefreshing] = useState(false)

  async function loadClinics() {
    setRefreshing(true)
    setError(null)
    try {
      const snap = await getDocs(query(collection(db, 'tenants'), orderBy('clinicName')))
      setClinics(snap.docs.map((d) => ({ id: d.id, ...d.data() }) as Tenant))
    } catch {
      setError('Could not load clinics. Verify platform admin permissions.')
    } finally {
      setRefreshing(false)
    }
  }

  useEffect(() => {
    void loadClinics()
  }, [])

  const filteredClinics = useMemo(() => {
    if (!clinics) return []
    return clinics.filter((c) => {
      const matchesSearch =
        c.clinicName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        c.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (c.city && c.city.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (c.state && c.state.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (c.gstin && c.gstin.toLowerCase().includes(searchQuery.toLowerCase()))

      if (!matchesSearch) return false

      if (statusFilter === 'active') return !c.suspended
      if (statusFilter === 'suspended') return !!c.suspended
      return true
    })
  }, [clinics, searchQuery, statusFilter])

  const stats = useMemo(() => {
    if (!clinics) return { total: 0, active: 0, suspended: 0 }
    const total = clinics.length
    const suspended = clinics.filter((c) => c.suspended).length
    const active = total - suspended
    return { total, active, suspended }
  }, [clinics])

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
            <Building2 className="h-5 w-5 text-teal-700" />
            <span>Clinic Tenants</span>
          </h1>
          <p className="mt-0.5 text-xs text-slate-500">
            Registered dental practices and isolated multi-tenant workspaces.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => void loadClinics()}
            disabled={refreshing}
            className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 shadow-2xs hover:bg-slate-50 disabled:opacity-50 transition cursor-pointer"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${refreshing ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* KPI Ribbon */}
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-2xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-500">Total Clinics</span>
            <Building2 className="h-4 w-4 text-slate-400" />
          </div>
          <div className="mt-2 text-2xl font-bold text-slate-900 font-mono">
            {clinics ? stats.total : '—'}
          </div>
          <span className="text-[11px] text-slate-400">Tenants registered across platform</span>
        </div>

        <div className="rounded-xl border border-emerald-200/80 bg-white p-4 shadow-2xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-emerald-700">Active Practices</span>
            <CheckCircle2 className="h-4 w-4 text-emerald-600" />
          </div>
          <div className="mt-2 text-2xl font-bold text-emerald-700 font-mono">
            {clinics ? stats.active : '—'}
          </div>
          <span className="text-[11px] text-emerald-600/80">Operational & healthy</span>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-2xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-rose-600">Suspended Practices</span>
            <AlertTriangle className="h-4 w-4 text-rose-500" />
          </div>
          <div className="mt-2 text-2xl font-bold text-rose-600 font-mono">
            {clinics ? stats.suspended : '—'}
          </div>
          <span className="text-[11px] text-slate-400">Access paused / overdue</span>
        </div>
      </div>

      {/* Controls Bar: Search & Status Filters */}
      <div className="flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-3 shadow-2xs sm:flex-row sm:items-center sm:justify-between">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute inset-y-0 left-0 my-auto ml-3 h-4 w-4 text-slate-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by clinic name, city, state, or ID..."
            className="w-full rounded-lg border border-slate-200 bg-slate-50/60 pl-9 pr-3 py-1.5 text-xs text-slate-900 placeholder:text-slate-400 focus:border-teal-700 focus:bg-white focus:outline-none focus:ring-1 focus:ring-teal-700"
          />
        </div>

        <div className="flex items-center gap-1.5">
          <Filter className="h-3.5 w-3.5 text-slate-400 mr-1" />
          {(['all', 'active', 'suspended'] as const).map((filter) => (
            <button
              key={filter}
              type="button"
              onClick={() => setStatusFilter(filter)}
              className={`rounded-md px-2.5 py-1 text-xs font-medium capitalize transition cursor-pointer ${
                statusFilter === filter
                  ? 'bg-slate-900 text-white shadow-2xs'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {filter}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2.5 rounded-lg border border-red-200 bg-red-50 p-3 text-xs text-red-800">
          <ShieldAlert className="h-4 w-4 text-red-600 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {clinics === null ? (
        <div className="rounded-xl border border-slate-200 bg-white p-12 text-center shadow-2xs">
          <RefreshCw className="mx-auto h-6 w-6 animate-spin text-teal-700" />
          <p className="mt-3 text-xs font-medium text-slate-500">Loading clinic registries...</p>
        </div>
      ) : filteredClinics.length === 0 ? (
        <div className="rounded-xl border border-slate-200 bg-white p-12 text-center shadow-2xs">
          <Building2 className="mx-auto h-8 w-8 text-slate-300" />
          <p className="mt-2 text-sm font-semibold text-slate-800">No clinics match your criteria</p>
          <p className="mt-1 text-xs text-slate-400">
            {searchQuery ? 'Try adjusting your search terms or filters.' : 'No clinics enrolled yet.'}
          </p>
        </div>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-2xs">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-left text-xs">
              <thead className="bg-slate-50/80 font-semibold text-slate-600 uppercase tracking-wider text-[11px]">
                <tr>
                  <th className="px-4 py-3">Clinic & Identifier</th>
                  <th className="px-4 py-3">Location</th>
                  <th className="px-4 py-3">Subscription</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-normal text-slate-800">
                {filteredClinics.map((clinic) => {
                  const location = [clinic.city, clinic.state].filter(Boolean).join(', ') || '—'
                  return (
                    <tr
                      key={clinic.id}
                      className="hover:bg-slate-50/80 transition-colors group cursor-pointer"
                    >
                      <td className="px-4 py-3">
                        <Link to={`/clinics/${clinic.id}`} className="block">
                          <div className="flex items-center gap-3">
                            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-teal-50 text-teal-800 font-bold text-sm border border-teal-200/60">
                              {clinic.clinicName.charAt(0).toUpperCase()}
                            </div>
                            <div>
                              <div className="font-semibold text-slate-900 group-hover:text-teal-700 transition">
                                {clinic.clinicName}
                              </div>
                              <div className="font-mono text-[10px] text-slate-400 truncate max-w-[200px]">
                                {clinic.id}
                              </div>
                            </div>
                          </div>
                        </Link>
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-1.5 text-slate-600">
                          <MapPin className="h-3 w-3 text-slate-400 shrink-0" />
                          <span>{location}</span>
                        </div>
                        {clinic.gstin && (
                          <div className="font-mono text-[10px] text-slate-400 mt-0.5">
                            GSTIN: {clinic.gstin}
                          </div>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <span className="inline-flex items-center rounded-md bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-700 border border-slate-200">
                          {clinic.subscriptionStatus ?? 'Standard'}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        {clinic.suspended ? (
                          <span className="inline-flex items-center gap-1 rounded-full bg-rose-50 px-2.5 py-0.5 text-[11px] font-semibold text-rose-700 border border-rose-200">
                            <span className="h-1.5 w-1.5 rounded-full bg-rose-500" />
                            Suspended
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-0.5 text-[11px] font-semibold text-emerald-700 border border-emerald-200">
                            <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                            Active
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-3 text-right">
                        <Link
                          to={`/clinics/${clinic.id}`}
                          className="inline-flex items-center gap-1 rounded-md border border-slate-200 bg-white px-2.5 py-1 text-xs font-semibold text-slate-700 shadow-2xs hover:bg-slate-50 hover:text-teal-700 transition"
                        >
                          <span>Manage</span>
                          <ChevronRight className="h-3 w-3" />
                        </Link>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
