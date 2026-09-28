/** Cloud Functions region. Must match the Firestore/Storage location (asia-south1, Mumbai). */
export const REGION = "asia-south1";

/**
 * Indicates whether functions are executing in the local Firebase emulator suite.
 */
export const IS_EMULATOR =
  process.env.FUNCTIONS_EMULATOR === "true" ||
  Boolean(process.env.FIREBASE_AUTH_EMULATOR_HOST) ||
  Boolean(process.env.FIRESTORE_EMULATOR_HOST);

/**
 * Enforce App Check for mobile callable functions.
 * In local emulator environments, enforcement is disabled (false) so developer testing,
 * seed scripts, and local emulators run without needing live Play Integrity attestation.
 * In production, it defaults to true unless explicitly opted out via ENFORCE_APP_CHECK=false.
 */
export const ENFORCE_APP_CHECK_MOBILE =
  !IS_EMULATOR && process.env.ENFORCE_APP_CHECK === "true";

/**
 * Mirrors app/src/main/java/com/agastyaone/crmai/core/Role.kt - keep both in sync,
 * these are the only values the `role` custom claim and Firestore rules recognize.
 */
export const CLINIC_ROLES = [
  "owner",
  "receptionist",
  "assistant",
  "labCoordinator",
] as const;

export type ClinicRole = (typeof CLINIC_ROLES)[number];

export const INVITABLE_ROLES: ClinicRole[] = [
  "receptionist",
  "assistant",
  "labCoordinator",
];

export function isClinicRole(value: unknown): value is ClinicRole {
  return typeof value === "string" && (CLINIC_ROLES as readonly string[]).includes(value);
}
