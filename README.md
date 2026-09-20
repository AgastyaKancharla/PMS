# AgastyaOne CRM — Phase 1: Foundation

Firebase wiring, authentication, role-based access, and tenant isolation for the
AgastyaOne dental clinic CRM. No patient management, scheduling, or other clinical
modules yet — this phase only builds the foundation those will sit on.

## What's here

- **`app/`** — Android app (Kotlin + Jetpack Compose). Auth screens, role-based
  dashboard shells, and the client side of the invite flow.
- **`dashboard/`** — AgastyaOne's own platform-admin web dashboard (React + Vite +
  TypeScript), separate from the clinic-facing Android app. See Phase 5 below.
- **`functions/`** — Cloud Functions (TypeScript) that own every custom-claims
  mutation: `createClinicAndOwner`, `inviteStaff`, `acceptInvite`, `updateStaffRole`,
  `onPlatformAdminWrite`, and Phase 5's `setTenantSuspended`, `listAuditLogsForClinic`,
  `provisionPlatformAdmin`.
- **`firestore.rules`**, **`storage.rules`**, **`firestore.indexes.json`** — security
  rules and indexes for this phase's collections.
- **`firebase.json`**, **`.firebaserc`** — Firebase CLI project config.

## 1. Create the Firebase project

1. Go to the [Firebase console](https://console.firebase.google.com) and create a new
   project (or use an existing GCP project).
2. **Firestore**: Build → Firestore Database → Create database → select
   **`asia-south1` (Mumbai)** as the location. This is a one-time, unchangeable choice
   made in the console (or via `gcloud firestore databases create --location=asia-south1`)
   — it cannot be set from `firebase.json`. Do not accept the default `nam5`/`us-central`
   location.
3. **Storage**: Build → Storage → Get started → also select **`asia-south1`**.
4. **Authentication**: Build → Authentication → Sign-in method → enable **Phone** and
   **Email/Password**.
5. **App Check**: Build → App Check → register your Android app → enable the
   **Play Integrity** provider. (You'll need a debug token for local development on an
   emulator/unregistered device — App Check → Apps → your app → "Manage debug tokens".)
6. Register an Android app in Project settings with package name
   `com.agastyaone.crmai`, and download `google-services.json`.

## 2. Place `google-services.json`

`app/google-services.json` is already committed, pointing at the `agastyaone-crm`
Firebase project - the API key in this file identifies the project to Google's
services but isn't a secret (Firebase security comes from Security Rules and App
Check, not from hiding this file), so it's safe to keep in the repo. If you point
this app at a different Firebase project, download your own from Project settings
and overwrite the file at:

```
app/google-services.json
```

(Next to `app/build.gradle.kts`.)

## 3. Wire up the Firebase CLI

```bash
npm install -g firebase-tools   # if you don't have it
firebase login
```

Edit `.firebaserc` and replace `REPLACE_WITH_YOUR_FIREBASE_PROJECT_ID` with your real
project ID.

## 4. Seed the first platform admin (manual, one-time)

Platform admin is deliberately not reachable from the clinic signup/invite flow. For
the very first admin, there's a chicken-and-egg problem (Phase 5's provisioning
callable is itself platform-admin-only), so grant it by hand exactly once: create a
Firebase Auth user for your AgastyaOne team member (console → Authentication → Add
user, email/password), then create a Firestore document at
`platformAdmins/{that user's uid}` with `{ name, email }`. The `onPlatformAdminWrite`
Cloud Function will pick that up and set the `platformAdmin` custom claim
automatically. They can then sign in via the app's "AgastyaOne team sign-in" entry
point, or via the Phase 5 web dashboard.

Every platform admin *after* the first one should go through the dashboard's
"Provision admin" screen instead (`provisionPlatformAdmin`, see Phase 5) - no more
by-hand console work needed from here on.

## 5. Deploy rules, indexes, and functions

```bash
cd functions && npm install && cd ..
firebase deploy --only firestore:rules,firestore:indexes,storage,functions
```

## 6. Build the Android app

Open the repo root in Android Studio (Iguana+) and let it sync, or from the CLI:

```bash
./gradlew :app:assembleDebug
```

> **Note on this PR:** the sandbox this was built in has no Android SDK and its
> network policy blocks `dl.google.com` (the Android Gradle Plugin's Maven repo), so
> the Android module could not be compiled here. The Cloud Functions were compiled
> (`tsc`) and linted (`eslint`) successfully. Please run a full `./gradlew build` (or
> open in Android Studio) as part of reviewing this PR.

## Data model (this phase)

```
tenants/{clinicId}
  clinicName, address, city, state, gstin, subscriptionStatus, createdAt, ownerUid

tenants/{clinicId}/staff/{uid}
  name, role, phone, email, active, invitedAt, joinedAt, invitedByUid

tenants/{clinicId}/invites/{inviteId}
  inviteId, role, name, phone, email, status, invitedByUid, invitedAt,
  acceptedAt, acceptedByUid

tenants/{clinicId}/auditLog/{logId}
  actionType, performedByUid, targetCollection, targetDocId, timestamp, details

platformAdmins/{uid}
  name, email
```

`role` and `clinicId` (and `platformAdmin`) live as Firebase Auth **custom claims**,
not just Firestore fields — that's what both the security rules and the app's routing
trust. Only the Cloud Functions in `functions/src` ever set them.

## Auth flows

- **Owner/Dentist signup** (`OwnerSignupScreen`): collects clinic details + a
  phone-OTP or email/password credential, then calls `createClinicAndOwner`, which
  creates the `tenants/{clinicId}` doc and the owner's own `staff` doc in one
  transaction and sets `{ role: "owner", clinicId }` claims.
- **Staff invite**: an owner invites a Receptionist/Assistant/Lab Coordinator by
  phone or email from the "Staff" tile on their dashboard (`InviteStaffScreen` →
  `inviteStaff`). The invited person signs in with that phone/email
  (`StaffSignInScreen`) and lands on "Waiting for clinic setup", where "I have a staff
  invite" calls `acceptInvite` — it looks up the pending invite by their verified
  phone/email, sets their claims, creates their `staff` doc, and marks the invite
  accepted.
- **Platform admin**: a separate "AgastyaOne team sign-in" entry point
  (`PlatformAdminSignInScreen`), email/password only, checked against the
  `platformAdmin` claim after sign-in — never reachable from the owner-signup or
  invite flow.

## Role-based routing

After sign-in, `AgastyaOneRoot` routes purely off the resolved custom claims:

| Session state | Screen |
|---|---|
| `owner` | Full dashboard: Schedule, Patients, Clinical, Billing, Reports, Inventory, Lab, Staff |
| `receptionist` | Schedule, Billing only |
| `assistant` | Patients (view-only placeholder) only |
| `labCoordinator` | Lab Orders only |
| Signed in, no `role`/`clinicId` claim yet | "Waiting for clinic setup" |
| Platform admin | Separate platform-admin shell |

Hidden tiles are absent from the grid, not just disabled.

## Manual test plan ("done" criteria)

1. Sign up as an Owner → land on the full dashboard shell.
2. From the Owner dashboard's Staff tile, invite a Receptionist by phone or email.
3. Sign in as that Receptionist (same phone/email) → "Waiting for clinic setup" → tap
   "I have a staff invite" → land on the Receptionist shell (Schedule + Billing only).
4. As the Receptionist, try to read another tenant's `tenants/{otherClinicId}` doc
   directly — the security rules reject it (`PERMISSION_DENIED`).
5. Check `tenants/{clinicId}/auditLog` in the console — entries exist for
   `clinic_created` and `staff_invite_sent`/`staff_invite_accepted`.

## Phase 3a — Odontogram, periodontal charting & treatment planning

- **Chartings** (`tenants/{clinicId}/chartings`): the odontogram (`toothConditions`) is
  dentist/owner-only to write; the periodontal chart (`periodontalChart`) can be written
  by the dentist/owner *or* an assistant/hygienist (routine pocket-depth/bleeding/mobility
  readings are their job) - enforced with the same field-level rule pattern as Phase 2a's
  patient-record split. Receptionist and Lab Coordinator have no access at all.
- **Treatment plans** (`tenants/{clinicId}/treatmentPlans`): dentist/owner-only for every
  write (create, edit, and patient-approval signing); assistant gets read-only access,
  everyone else none.
- **Tooth numbering**: chart data is always stored keyed by FDI tooth number. Display as
  Universal instead is a per-clinic, per-tenant setting (`tenants/{clinicId}.toothNumberingSystem`,
  `"FDI"` or `"Universal"`) - a display-layer conversion only, so switching it later never
  renumbers or orphans existing chart data.

> **⚠️ Billing caveat**: the treatment-plan line-item picker's seed procedure list
> (`ProcedureCatalog.SEED_PROCEDURES`) uses internal placeholder codes (e.g. `"RCT"`,
> `"SCALING"`) that are **not** GST HSN/SAC codes and carry no default pricing. Before
> this ever feeds a real invoice (a later phase), the clinic's own accountant must verify
> and map every procedure to its correct HSN/SAC code and tax rate. Treating the seed list
> as billing-ready as-is would be a compliance mistake.

## Phase 3b — X-ray & imaging

- **Imaging** (`tenants/{clinicId}/imaging`): owner/dentist and assistant/hygienist can
  upload and tag (tooth number, type, notes); only the owner/dentist can delete a record,
  same real-world split as periodontal charting in Phase 3a. Receptionist and Lab
  Coordinator have no Firestore or Storage access at all - not even to a raw file URL, since
  Storage rules (`tenants/{clinicId}/patients/{patientId}/imaging/...`) mirror the same
  role/tenant restrictions as the Firestore document.
- **Format scope**: JPEG/PNG only (camera capture and gallery/Photo Picker selection).
  **DICOM is an explicit stretch goal, not built in this phase** - CBCT/RVG/OPG hardware
  that exports DICOM natively would need a separate ingestion and viewer path (windowing,
  multi-frame series) that this phase's simple image pipeline doesn't attempt.
- **Before/after comparison**: side-by-side view of any two images with independent
  pinch-zoom per pane. A slider/overlay comparison mode was considered and deliberately
  left out - side-by-side is the spec's minimum bar, and overlay alignment (registration
  between two photos taken at different times/angles) is a meaningfully bigger feature than
  this phase's scope.
- This closes out **Core Clinical Tools**. Next module: **Billing & Financial Management**.

## Phase 4a — GST invoicing

- **Invoices** (`tenants/{clinicId}/invoices`): owner/dentist and receptionist have full
  access (create, edit, void, record payments) - the spec is explicit that billing is a
  receptionist-level duty here, same as the owner. Assistant/hygienist and Lab Coordinator
  get a **clean full exclusion**: no partial-field carve-out like the clinical modules,
  both their read and write are rejected outright.
- **Sequential invoice numbers** (`INV-0001`, `INV-0002`, ...) are assigned inside the same
  Firestore transaction that creates the invoice, bumping an `invoiceCounter` field on the
  tenant doc - race-safe under concurrent staff members, unlike a naive "count existing
  invoices + 1". firestore.rules gives the receptionist a narrow, field-scoped allowance
  (`touchesOnly(['invoiceCounter'])`) to touch just that one field on the otherwise
  owner-only tenant document.
- **GST split**: same state as the clinic's own `tenants/{clinicId}.state` → CGST + SGST
  (each half the total rate); different state → IGST, full rate. Most patients are local to
  the clinic, so `billingState` simply defaults to the clinic's state and is editable per
  invoice, rather than building a full address-collection flow.
- **⚠️ HSN/SAC codes and GST rate are placeholders, not verified for filing**:
  `ProcedureCatalog.Procedure.hsnSacCode` defaults every seed procedure to SAC 999319
  ("Other human health services") as a stand-in, and the GST calculation uses an 18%
  placeholder rate (`DEFAULT_GST_RATE_PERCENT`) since the spec doesn't name a rate and many
  dental/medical services are actually GST-exempt under Indian law. Both are flagged here,
  in the `ProcedureCatalog` doc comment, and as a footnote printed on every generated
  invoice PDF - the clinic's own accountant must confirm the correct code and rate per
  procedure before this is used for real GST filing.
- **PDF + sharing**: a real, single-page PDF is rendered with `android.graphics.pdf.PdfDocument`
  (part of the Android SDK, no new dependency) and handed to the standard Android share
  sheet (`Intent.ACTION_SEND`) - WhatsApp, email, Drive etc. all show up there naturally.
  This is explicitly *not* the Meta Cloud API WhatsApp integration (a later
  Communication & Automation phase); staff tap send manually from the share sheet.
- Payment status (`unpaid` / `partial` / `paid`) and `amountPaid` are recorded manually,
  independent of Phase 4b's future Razorpay integration - most patients still pay by cash
  or card in person.
- Razorpay payment links (Phase 4b) and insurance/TPA claims (Phase 4c) are explicitly out
  of scope for this phase.

## Phase 5 — Platform admin web dashboard

A new, separate web app (`dashboard/`) for AgastyaOne's own back office - clinic/tenant
support and ops, not a clinic-facing product. Until this phase, "platform admin" was
just an Android sign-in screen that verified the `platformAdmin` claim and then showed
two dead placeholder tiles; this phase is the first real functionality behind it,
built as a web app rather than more Android screens since it's an internal tool with
no reason to live on a phone.

- **Stack**: React + Vite + TypeScript + Tailwind CSS, using the Firebase Web SDK
  directly (no separate backend of its own - reads Firestore per the rules below, and
  calls the three new Cloud Functions for everything the rules don't allow a platform
  admin to do directly).
- **Scope, deliberately kept narrow**: clinics list/detail (reading `tenants/*` and
  `tenants/*/staff/*`, both already allowed for a platform admin by the existing
  rules), clinic suspend/reactivate, cross-tenant audit-log viewing, and platform-admin
  account provisioning. **Patient records, clinical data, and billing/invoice data are
  explicitly out of scope** - the existing rules never gave platform admins visibility
  into those (see the PII/DPDP-conscious rules comments from Phase 1 onward), and this
  phase doesn't relax that; a platform admin's job here is support/ops, not clinical
  access.
- **Clinic suspension** (new): `tenants/{clinicId}.suspended` (boolean, defaults to
  false/absent). Set only through the `setTenantSuspended` callable
  (platform-admin-only). Enforced in `firestore.rules` inside `belongsToTenant()` -
  every one of that clinic's own staff (owner, receptionist, assistant, lab
  coordinator) loses all read *and* write access to their tenant's data the moment
  it's suspended, since every role-check function in the rules file is built on top
  of `belongsToTenant()`. A platform admin's own read access is unaffected (it doesn't
  route through that function).
- **New platform-admin-only Cloud Functions** (all guarded by
  `request.auth.token.platformAdmin === true`, checked server-side - never trust a
  client claim to be self-reported):
  - `setTenantSuspended({ clinicId, suspended })` - toggles the field above and writes
    a `clinic_suspended`/`clinic_reactivated` audit log entry.
  - `listAuditLogsForClinic({ clinicId, limit?, startAfterId? })` - the Firestore rules
    give a platform admin no read access to a clinic's audit log (owner-only there);
    this callable reads it with the Admin SDK instead, without relaxing the rules a
    clinic's own staff are bound by.
  - `provisionPlatformAdmin({ email, name })` - looks up an existing Auth user by
    email, or creates one with a one-time temporary password, then writes
    `platformAdmins/{uid}` (which is what actually grants the claim, via the existing
    `onPlatformAdminWrite` trigger - this function never sets it directly). Replaces
    the by-hand console process from step 4 above for every admin after the first.
- **Running it locally**:
  ```bash
  cd dashboard
  npm install
  cp .env.example .env.local   # fill in your Firebase project's Web SDK config
  npm run dev
  ```
  The `VITE_FIREBASE_*` values are a public client identifier, not a secret (same as
  `app/google-services.json`'s API key) - found in the Firebase console under Project
  settings → General → Your apps → Web app.
- **Deploying**: `firebase deploy --only hosting` (builds `dashboard/` via the
  `predeploy` hook in `firebase.json` first). The new Cloud Functions deploy the same
  way as any other: `firebase deploy --only functions`.
- **What this sandbox could not verify**: no live Firebase project or deployed Hosting
  site here, so the dashboard's actual sign-in flow, Firestore reads, and callable
  functions were never exercised end-to-end against a real backend - only built and
  typechecked. `dashboard`'s CI job (typecheck + lint + build) and the
  `rules-test` job (including new suspension test cases, run against the real local
  Firestore/Storage emulators) are the authoritative signal for this PR; a live-deploy
  smoke test (sign in, suspend a test clinic, provision a second admin) is the user's
  next step, same division of labor as Phase 4b's Razorpay integration.

### Continuous deployment

`.github/workflows/deploy.yml` auto-deploys Hosting, Functions, and Firestore rules to
the `agastyaone-crm` project (see `.firebaserc`) on every push to `main` or
`claude/firebase-foundation-setup-qvdyzl`, or via a manual "Run workflow" trigger. It's
deliberately a separate workflow from `ci.yml` - `ci.yml` needs no credentials and runs
on every push/PR; this one deploys to the real, live project and only runs where
that's actually wanted.

It needs these repository secrets (Settings → Secrets and variables → Actions → New
repository secret) before it can deploy anything - without them the workflow fails
cleanly at the auth step, it doesn't silently skip:

- **`FIREBASE_SERVICE_ACCOUNT`** - the full JSON key of a service account with deploy
  rights on the `agastyaone-crm` project. Create one: Google Cloud console → IAM & Admin
  → Service Accounts (select the `agastyaone-crm` project) → Create Service Account →
  grant it the **Firebase Admin** role (`roles/firebase.admin` - broad, but Cloud
  Functions gen2 deploys touch enough distinct services - Cloud Run, Cloud Build,
  Artifact Registry, Eventarc - that hand-picking a minimal role set is its own
  maintenance burden; tighten it later if you want) → Keys → Add key → JSON, then paste
  the entire downloaded file's contents as the secret value.
- **`VITE_FIREBASE_API_KEY`**, **`VITE_FIREBASE_AUTH_DOMAIN`**, **`VITE_FIREBASE_PROJECT_ID`**,
  **`VITE_FIREBASE_STORAGE_BUCKET`**, **`VITE_FIREBASE_MESSAGING_SENDER_ID`**,
  **`VITE_FIREBASE_APP_ID`** - the dashboard's own Web app config, baked into the build
  at build time (Vite env vars aren't available at runtime like a server app's would
  be). Firebase console → Project settings → General → Your apps. If there's no Web
  app registered yet under `agastyaone-crm` (only the existing Android app), add one
  there first (`</>` icon) - registering it doesn't touch the Android app's config at
  all, they're independent per-platform entries in the same project. These six values
  are a public client identifier, not a secret (same as `google-services.json`'s API
  key) - they're stored as Actions secrets here only because that's the standard place
  to inject build-time env vars, not because they need hiding.

Once both are set, push to either branch above (or use "Run workflow" on the Actions
tab) and the dashboard is live at `https://agastyaone-crm.web.app` (or `.firebaseapp.com`)
within a couple of minutes.
