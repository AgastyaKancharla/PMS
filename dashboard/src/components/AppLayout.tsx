import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-1.5 text-sm font-medium ${
    isActive ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
  }`

export function AppLayout() {
  const { user, signOutUser } = useAuth()

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
          <div className="flex items-center gap-6">
            <span className="text-sm font-semibold text-slate-900">AgastyaOne platform admin</span>
            <nav className="flex gap-1">
              <NavLink to="/clinics" className={navLinkClass}>
                Clinics
              </NavLink>
              <NavLink to="/provision-admin" className={navLinkClass}>
                Provision admin
              </NavLink>
            </nav>
          </div>
          <div className="flex items-center gap-3 text-sm text-slate-500">
            <span>{user?.email}</span>
            <button
              type="button"
              onClick={() => void signOutUser()}
              className="rounded-md border border-slate-300 px-2.5 py-1 text-slate-700 hover:bg-slate-100"
            >
              Sign out
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
