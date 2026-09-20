/**
 * Seed script for Firebase Local Emulator.
 * Populates Auth and Firestore with initial Super Admin and Clinic accounts.
 *
 * Usage:
 *   node scripts/seed-emulator.js
 */

const admin = require('firebase-admin');

process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';
process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8085';

admin.initializeApp({ projectId: 'agastyaone-crm' });
const auth = admin.auth();
const db = admin.firestore();

const DEFAULT_PASSWORD = 'Agastya.27venu';

async function seed() {
  console.log('Seeding local emulator data...');

  // 1. Platform Admin (Web Dashboard)
  let adminUid;
  try {
    const existing = await auth.getUserByEmail('agastya@agastyaone.com');
    adminUid = existing.uid;
    console.log('Existing Super Admin found:', adminUid);
  } catch {
    const user = await auth.createUser({
      email: 'agastya@agastyaone.com',
      password: DEFAULT_PASSWORD,
      displayName: 'Super Admin',
    });
    adminUid = user.uid;
    console.log('Created Super Admin:', adminUid);
  }
  await auth.setCustomUserClaims(adminUid, { platformAdmin: true });
  await db.doc('platformAdmins/' + adminUid).set({
    name: 'Super Admin',
    email: 'agastya@agastyaone.com',
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  // 2. Clinic 1 Owner
  let owner1Uid;
  try {
    const existing = await auth.getUserByEmail('agastya.venu@agastyaone.com');
    owner1Uid = existing.uid;
    console.log('Existing Clinic 1 Owner found:', owner1Uid);
  } catch {
    const user = await auth.createUser({
      email: 'agastya.venu@agastyaone.com',
      password: DEFAULT_PASSWORD,
      displayName: 'Dr. Agastya Venu',
      phoneNumber: '+919876543210',
    });
    owner1Uid = user.uid;
    console.log('Created Clinic 1 Owner:', owner1Uid);
  }
  await auth.setCustomUserClaims(owner1Uid, { role: 'owner', clinicId: 'clinic-bengaluru' });

  // 3. Clinic 1 Receptionist
  let rec1Uid;
  try {
    const existing = await auth.getUserByEmail('pooja@agastyaone.com');
    rec1Uid = existing.uid;
    console.log('Existing Clinic 1 Receptionist found:', rec1Uid);
  } catch {
    const user = await auth.createUser({
      email: 'pooja@agastyaone.com',
      password: DEFAULT_PASSWORD,
      displayName: 'Pooja Sharma',
      phoneNumber: '+919876543211',
    });
    rec1Uid = user.uid;
    console.log('Created Clinic 1 Receptionist:', rec1Uid);
  }
  await auth.setCustomUserClaims(rec1Uid, { role: 'receptionist', clinicId: 'clinic-bengaluru' });

  // Seed Clinic 1 Tenant Doc & Staff Docs
  await db.doc('tenants/clinic-bengaluru').set({
    clinicName: 'Agastya Dental Care & Implant Center',
    address: '42, 100 Feet Road, Indiranagar',
    city: 'Bengaluru',
    state: 'Karnataka',
    gstin: '29AAAAA0000A1Z5',
    subscriptionStatus: 'active',
    suspended: false,
    ownerUid: owner1Uid,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  await db.doc('tenants/clinic-bengaluru/staff/' + owner1Uid).set({
    name: 'Dr. Agastya Venu',
    role: 'owner',
    email: 'agastya.venu@agastyaone.com',
    phone: '+919876543210',
    active: true,
  });

  await db.doc('tenants/clinic-bengaluru/staff/' + rec1Uid).set({
    name: 'Pooja Sharma',
    role: 'receptionist',
    email: 'pooja@agastyaone.com',
    phone: '+919876543211',
    active: true,
  });

  // 4. Clinic 2 (Mumbai)
  let owner2Uid;
  try {
    const existing = await auth.getUserByEmail('priya@apexdental.com');
    owner2Uid = existing.uid;
  } catch {
    const user = await auth.createUser({
      email: 'priya@apexdental.com',
      password: DEFAULT_PASSWORD,
      displayName: 'Dr. Priya Desai',
      phoneNumber: '+919876543220',
    });
    owner2Uid = user.uid;
  }
  await auth.setCustomUserClaims(owner2Uid, { role: 'owner', clinicId: 'clinic-mumbai' });

  await db.doc('tenants/clinic-mumbai').set({
    clinicName: 'Apex Multispeciality Dental',
    address: '15 Linking Road, Bandra West',
    city: 'Mumbai',
    state: 'Maharashtra',
    gstin: '27BBBBB1111B1Z2',
    subscriptionStatus: 'trial',
    suspended: false,
    ownerUid: owner2Uid,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  await db.doc('tenants/clinic-mumbai/staff/' + owner2Uid).set({
    name: 'Dr. Priya Desai',
    role: 'owner',
    email: 'priya@apexdental.com',
    phone: '+919876543220',
    active: true,
  });

  console.log('\n--- Emulator Credentials Ready ---');
  console.log('1. Platform Admin (Web Dashboard): agastya@agastyaone.com / ' + DEFAULT_PASSWORD);
  console.log('2. Clinic 1 Owner:                agastya.venu@agastyaone.com / ' + DEFAULT_PASSWORD);
  console.log('3. Clinic 1 Receptionist:         pooja@agastyaone.com / ' + DEFAULT_PASSWORD);
  console.log('4. Clinic 2 Owner:                priya@apexdental.com / ' + DEFAULT_PASSWORD);
  console.log('----------------------------------\n');
}

seed()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error('Seeding failed:', err);
    process.exit(1);
  });
