import { httpsCallable } from 'firebase/functions'
import { functions } from '../firebase'
import type { AuditLogEntry } from '../types'

/** Thin, typed wrappers around the platform-admin-only Cloud Functions callables. */

export async function setTenantSuspended(clinicId: string, suspended: boolean): Promise<void> {
  const call = httpsCallable<{ clinicId: string; suspended: boolean }, { ok: true }>(
    functions,
    'setTenantSuspended'
  )
  await call({ clinicId, suspended })
}

export async function listAuditLogsForClinic(
  clinicId: string,
  limit = 25
): Promise<AuditLogEntry[]> {
  const call = httpsCallable<
    { clinicId: string; limit?: number },
    { entries: AuditLogEntry[] }
  >(functions, 'listAuditLogsForClinic')
  const result = await call({ clinicId, limit })
  return result.data.entries
}

export async function provisionPlatformAdmin(
  email: string,
  name: string
): Promise<{ uid: string; temporaryPassword: string | null }> {
  const call = httpsCallable<
    { email: string; name: string },
    { uid: string; temporaryPassword: string | null }
  >(functions, 'provisionPlatformAdmin')
  const result = await call({ email, name })
  return result.data
}
