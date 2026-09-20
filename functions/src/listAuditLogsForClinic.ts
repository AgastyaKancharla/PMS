import { HttpsError, onCall } from "firebase-functions/v2/https";
import { db } from "./admin";
import { REGION } from "./constants";

interface ListAuditLogsRequest {
  clinicId: string;
  limit?: number;
  startAfterId?: string | null;
}

const DEFAULT_LIMIT = 25;
const MAX_LIMIT = 100;

/**
 * Platform-admin-only. The Firestore rules deliberately give platform admins no
 * read access to a clinic's audit log (owner-only there, see firestore.rules) -
 * this callable uses the Admin SDK to read it anyway, for support/ops visibility,
 * without relaxing the client-side rules a clinic's own staff are bound by.
 */
export const listAuditLogsForClinic = onCall(
  { region: REGION },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Sign in first.");
    }
    if (request.auth.token.platformAdmin !== true) {
      throw new HttpsError(
        "permission-denied",
        "Only a platform admin can view a clinic's audit log."
      );
    }

    const data = request.data as ListAuditLogsRequest;
    const clinicId = typeof data.clinicId === "string" ? data.clinicId.trim() : "";
    if (!clinicId) {
      throw new HttpsError("invalid-argument", "clinicId is required.");
    }
    const requestedLimit = Number(data.limit);
    const limit = Math.min(
      Math.max(1, Number.isFinite(requestedLimit) ? requestedLimit : DEFAULT_LIMIT),
      MAX_LIMIT
    );

    const auditLogRef = db.collection("tenants").doc(clinicId).collection("auditLog");
    let query = auditLogRef.orderBy("timestamp", "desc").limit(limit);

    if (data.startAfterId) {
      const cursorSnap = await auditLogRef.doc(data.startAfterId).get();
      if (cursorSnap.exists) {
        query = query.startAfter(cursorSnap);
      }
    }

    const snap = await query.get();
    const entries = snap.docs.map((doc) => {
      const raw = doc.data();
      const timestamp = raw.timestamp;
      return {
        id: doc.id,
        ...raw,
        timestamp:
          timestamp && typeof timestamp.toDate === "function"
            ? timestamp.toDate().toISOString()
            : null,
      };
    });

    return { entries };
  }
);
