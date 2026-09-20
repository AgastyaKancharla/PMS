# AgastyaOne platform admin dashboard

AgastyaOne's own back-office web app (clinics/tenants, staff rosters, audit log,
suspend/reactivate, platform-admin provisioning) - separate from the clinic-facing
Android app. See the repo root [`README.md`](../README.md#phase-5--platform-admin-web-dashboard)
("Phase 5") for setup, deployment, and what this does and doesn't have access to.

```bash
npm install
cp .env.example .env.local   # fill in your Firebase project's Web SDK config
npm run dev
```
