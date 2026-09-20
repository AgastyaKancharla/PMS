/** Mirrors the tenants/{clinicId} doc shape written by functions/src/createClinicAndOwner.ts. */
export interface Tenant {
  id: string
  clinicName: string
  address?: string
  city?: string
  state?: string
  gstin?: string | null
  subscriptionStatus?: string
  ownerUid?: string
  suspended?: boolean
  invoiceCounter?: number
  toothNumberingSystem?: 'FDI' | 'Universal'
}

/** Mirrors app/src/main/java/com/agastyaone/crmai/core/Role.kt - the only clinic-role claim values. */
export type ClinicRole = 'owner' | 'receptionist' | 'assistant' | 'labCoordinator'

/** Mirrors tenants/{clinicId}/staff/{uid} doc shape. */
export interface StaffMember {
  uid: string
  name: string
  role: ClinicRole
  phone?: string | null
  email?: string | null
  active?: boolean
}

/** Mirrors the shape returned by the listAuditLogsForClinic callable. */
export interface AuditLogEntry {
  id: string
  actionType: string
  performedByUid: string
  targetCollection: string
  targetDocId: string
  timestamp: string | null
  details?: Record<string, unknown>
}
