import { HttpsError, onCall } from "firebase-functions/v2/https";
import { db } from "./admin";
import { REGION } from "./constants";
import { writeAuditLog } from "./audit";

interface SetTenantSuspendedRequest {
  clinicId: string;
  suspended: boolean;
}

/**
 * Platform-admin-only. Suspending a clinic blocks all of that clinic's own staff
 * from reading or writing their tenant's data (see isTenantSuspended() in
 * firestore.rules, wired into belongsToTenant()) - it only ever touches the
 * `suspended` field on the tenant doc and never affects a platform admin's own
 * read access, which doesn't route through that check.
 */
export const setTenantSuspended = onCall(
  { region: REGION },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Sign in first.");
    }
    if (request.auth.token.platformAdmin !== true) {
      throw new HttpsError("permission-denied", "Only a platform admin can suspend a clinic.");
    }

    const data = request.data as SetTenantSuspendedRequest;
    const clinicId = typeof data.clinicId === "string" ? data.clinicId.trim() : "";
    if (!clinicId) {
      throw new HttpsError("invalid-argument", "clinicId is required.");
    }
    if (typeof data.suspended !== "boolean") {
      throw new HttpsError("invalid-argument", "suspended must be a boolean.");
    }

    const clinicRef = db.collection("tenants").doc(clinicId);
    const clinicSnap = await clinicRef.get();
    if (!clinicSnap.exists) {
      throw new HttpsError("not-found", "No clinic with that clinicId.");
    }

    await clinicRef.update({ suspended: data.suspended });

    await writeAuditLog({
      clinicId,
      actionType: data.suspended ? "clinic_suspended" : "clinic_reactivated",
      performedByUid: request.auth.uid,
      targetCollection: "tenants",
      targetDocId: clinicId,
      details: {},
    });

    return { ok: true };
  }
);
