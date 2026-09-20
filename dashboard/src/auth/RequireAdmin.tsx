import type { ReactNode } from 'react'
import { useAuth } from './AuthContext'
import { SignInPage } from './SignInPage'

/** Route guard: renders children only once signed in as a verified platform admin. */
export function RequireAdmin({ children }: { children: ReactNode }) {
  const { loading, isPlatformAdmin } = useAuth()

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center text-sm text-slate-500">
        Loading…
      </div>
    )
  }

  if (!isPlatformAdmin) {
    return <SignInPage />
  }

  return <>{children}</>
}
