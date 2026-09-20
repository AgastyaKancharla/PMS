import { randomBytes } from "node:crypto";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import { logger } from "firebase-functions/v2";
import { auth, db } from "./admin";
import { REGION } from "./constants";

interface ProvisionPlatformAdminRequest {
  email: string;
  name: string;
}

/**
 * Platform-admin-only. Replaces the by-hand process documented in the README
 * ("create the Auth user in the console, then write platformAdmins/{uid}
 * yourself"): looks up an existing Auth user by email, or creates one with a
 * one-time temporary password, then writes platformAdmins/{uid} - which is what
 * actually grants the platformAdmin custom claim, via the existing
 * onPlatformAdminWrite trigger (this function never sets that claim directly).
 *
 * Not logged through writeAuditLog() - that writer is shaped around a single
 * clinicId, and provisioning a platform admin isn't scoped to any one clinic.
 * Cloud Functions' own structured logging covers this for now rather than
 * introducing a separate platform-wide audit collection for one action type.
 */
export const provisionPlatformAdmin = onCall(
  { region: REGION },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Sign in first.");
    }
    if (request.auth.token.platformAdmin !== true) {
      throw new HttpsError(
        "permission-denied",
        "Only a platform admin can provision another platform admin."
      );
    }

    const data = request.data as ProvisionPlatformAdminRequest;
    const email = typeof data.email === "string" ? data.email.trim().toLowerCase() : "";
    const name = typeof data.name === "string" ? data.name.trim() : "";
    if (!email) {
      throw new HttpsError("invalid-argument", "email is required.");
    }
    if (!name) {
      throw new HttpsError("invalid-argument", "name is required.");
    }

    let uid: string;
    let temporaryPassword: string | null = null;

    try {
      const existing = await auth.getUserByEmail(email);
      uid = existing.uid;
    } catch (err) {
      const code = (err as { code?: string }).code;
      if (code !== "auth/user-not-found") {
        throw err;
      }
      temporaryPassword = randomBytes(12).toString("base64url");
      const created = await auth.createUser({ email, password: temporaryPassword, displayName: name });
      uid = created.uid;
    }

    await db.collection("platformAdmins").doc(uid).set({ name, email });

    logger.info(`Provisioned platform admin ${uid} (${email}) by ${request.auth.uid}`);

    return { uid, temporaryPassword };
  }
);
