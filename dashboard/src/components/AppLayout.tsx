import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Building2, UserPlus, LogOut, Shield, Database, Activity } from 'lucide-react'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `inline-flex items-center gap-2 rounded-lg px-3 py-1.5 text-xs font-semibold tracking-wide transition ${
    isActive
      ? 'bg-slate-900 text-white shadow-xs'
      : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
  }`

export function AppLayout() {
  const { user, signOutUser } = useAuth()

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      {/* Top Header */}
      <header className="sticky top-0 z-30 border-b border-slate-200 bg-white shadow-2xs">
        <div className="mx-auto flex h-14 max-w-7xl items-center justify-between px-4 sm:px-6">
          {/* Left Brand & Nav */}
          <div className="flex items-center gap-8">
            <div className="flex items-center gap-2.5">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-teal-800 text-white shadow-xs">
                <Shield className="h-4 w-4" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-bold tracking-tight text-slate-900">AgastyaOne</span>
                  <span className="rounded bg-teal-50 px-1.5 py-0.5 text-[10px] font-semibold text-teal-800 border border-teal-200/60">
                    PLATFORM OPS
                  </span>
                </div>
              </div>
            </div>

            <nav className="flex items-center gap-1.5">
              <NavLink to="/clinics" className={navLinkClass}>
                <Building2 className="h-3.5 w-3.5" />
                <span>Clinics</span>
              </NavLink>
              <NavLink to="/provision-admin" className={navLinkClass}>
                <UserPlus className="h-3.5 w-3.5" />
                <span>Provision Admin</span>
              </NavLink>
            </nav>
          </div>

          {/* Right Status & User Menu */}
          <div className="flex items-center gap-4">
            {/* System Status Indicator */}
            <div className="hidden sm:flex items-center gap-1.5 text-xs text-slate-500 bg-slate-100/80 px-2.5 py-1 rounded-md border border-slate-200/60">
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
              </span>
              <Database className="h-3 w-3 text-slate-400 ml-1" />
              <span className="font-mono text-[11px] text-slate-600">asia-south1</span>
            </div>

            {/* User Pill */}
            <div className="flex items-center gap-3 border-l border-slate-200 pl-4">
              <div className="text-right">
                <div className="text-xs font-semibold text-slate-900 truncate max-w-[180px]">
                  {user?.email ?? 'Admin'}
                </div>
                <div className="text-[10px] font-medium text-slate-400 flex items-center justify-end gap-1">
                  <Activity className="h-2.5 w-2.5 text-teal-600" />
                  <span>Platform Superuser</span>
                </div>
              </div>

              <button
                type="button"
                onClick={() => void signOutUser()}
                title="Sign out of console"
                className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 text-xs font-medium text-slate-700 shadow-2xs hover:bg-slate-50 hover:text-slate-900 transition cursor-pointer"
              >
                <LogOut className="h-3.5 w-3.5 text-slate-500" />
                <span className="hidden sm:inline">Sign out</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-6 sm:px-6">
        <Outlet />
      </main>

      {/* Subtle Footer */}
      <footer className="border-t border-slate-200 bg-white py-3 text-center text-xs text-slate-400">
        <div className="mx-auto max-w-7xl px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>AgastyaOne Dental Practice Management • Enterprise Multi-Tenant Core</span>
          <span className="font-mono text-[11px]">Strict HIPAA / DPDP Isolation • Custom Claims Auth</span>
        </div>
      </footer>
    </div>
  )
}
