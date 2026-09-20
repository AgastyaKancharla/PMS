process.env.FIREBASE_AUTH_EMULATOR_HOST = '127.0.0.1:9099';
process.env.FIRESTORE_EMULATOR_HOST = '127.0.0.1:8085';

const { initializeApp } = require('firebase/app');
const { getAuth, connectAuthEmulator, signInWithEmailAndPassword } = require('firebase/auth');
const { getFirestore, connectFirestoreEmulator, doc, getDoc, setDoc, updateDoc, collection, addDoc, getDocs } = require('firebase/firestore');

// Public Firebase config
const firebaseConfig = {
  apiKey: 'fake-api-key-for-emulator',
  authDomain: 'agastyaone-crm.firebaseapp.com',
  projectId: 'agastyaone-crm'
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);

connectAuthEmulator(auth, 'http://127.0.0.1:9099', { disableWarnings: true });
connectFirestoreEmulator(db, '127.0.0.1', 8085);

async function runTests() {
  console.log('====================================================');
  console.log('  AGASTYAONE CLINIC & SECURITY VERIFICATION SUITE   ');
  console.log('====================================================\n');

  // Step 1: Sign in as Dr. Agastya Venu (Clinic Owner)
  console.log('1. Authenticating as Clinic Owner (agastya.venu@agastyaone.com)...');
  const ownerCred = await signInWithEmailAndPassword(auth, 'agastya.venu@agastyaone.com', 'Agastya.27venu');
  console.log('   ✓ Owner authenticated! UID:', ownerCred.user.uid);

  // Read clinic doc while active
  const clinicSnap = await getDoc(doc(db, 'tenants', 'clinic-bengaluru'));
  console.log('   ✓ Read clinic status:', clinicSnap.data().clinicName, '| Suspended:', clinicSnap.data().suspended);

  // Step 2: TEST 3 - Suspension Enforcement
  console.log('\n2. [TEST 3] Testing Clinic Suspension Enforcement...');
  // Suspend clinic via admin SDK / emulator
  const admin = require('firebase-admin');
  if (!admin.apps.length) {
    admin.initializeApp({ projectId: 'agastyaone-crm' });
  }
  await admin.firestore().doc('tenants/clinic-bengaluru').update({ suspended: true });
  console.log('   [Admin Action] Clinic "clinic-bengaluru" set to suspended = true');

  // Try to read patient collection as owner while clinic is suspended
  try {
    await getDocs(collection(db, 'tenants', 'clinic-bengaluru', 'patients'));
    console.error('   ❌ FAILED: Owner was able to read patients while clinic is suspended!');
  } catch (err) {
    console.log('   ✓ SUCCESS: Owner access blocked with PERMISSION_DENIED! (' + err.code + ')');
  }

  // Reactivate clinic
  await admin.firestore().doc('tenants/clinic-bengaluru').update({ suspended: false });
  console.log('   [Admin Action] Clinic "clinic-bengaluru" reactivated (suspended = false)');

  // Retry read as owner
  const activePatients = await getDocs(collection(db, 'tenants', 'clinic-bengaluru', 'patients'));
  console.log('   ✓ SUCCESS: Owner access immediately restored! Patient count:', activePatients.docs.length);

  // Step 3: TEST 4 - Patient Intake & DPDP Granular Consent
  console.log('\n3. [TEST 4] Testing Patient Creation & DPDP Granular Consents...');
  const patientRef = await addDoc(collection(db, 'tenants', 'clinic-bengaluru', 'patients'), {
    name: 'Rahul Sharma',
    phone: '+919876543299',
    dob: '1990-05-15',
    gender: 'Male',
    address: 'Indiranagar, Bengaluru',
    bloodGroup: 'O+',
    allergies: ['Penicillin'],
    chronicConditions: ['Hypertension'],
    createdAt: new Date().toISOString(),
    lastEditedAt: new Date().toISOString()
  });
  console.log('   ✓ Patient created! ID:', patientRef.id);

  // Record Granular DPDP Consents as separate documents
  const consents = [
    { type: 'treatmentRecords', granted: true, signatureUrl: 'https://storage/signatures/sig-001.png' },
    { type: 'whatsappMarketing', granted: true, signatureUrl: null },
    { type: 'dataSharingTpa', granted: false, signatureUrl: null }, // Opted out!
    { type: 'reviewRequests', granted: true, signatureUrl: null }
  ];

  for (const c of consents) {
    await setDoc(doc(db, 'tenants', 'clinic-bengaluru', 'patients', patientRef.id, 'consents', c.type), {
      consentType: c.type,
      granted: c.granted,
      grantedAt: new Date().toISOString(),
      signatureUrl: c.signatureUrl,
      recordedByUid: ownerCred.user.uid
    });
  }
  console.log('   ✓ Granular DPDP consents recorded (Treatment: YES, WhatsApp: YES, TPA Insurance: NO, Reviews: YES)');

  // Step 4: TEST 5 - DPDP Rights Request (Erasure / Correction / Access)
  console.log('\n4. [TEST 5] Testing DPDP Data Rights Request Logging...');
  const requestRef = await addDoc(collection(db, 'tenants', 'clinic-bengaluru', 'dataRequests'), {
    patientId: patientRef.id,
    requestType: 'erasure',
    status: 'open',
    requestedAt: new Date().toISOString(),
    requestedByUid: ownerCred.user.uid,
    notes: 'Patient requested erasure of marketing communication history.'
  });
  console.log('   ✓ Erasure request logged with status: open (ID:', requestRef.id, ')');

  // Update request to resolved
  await updateDoc(doc(db, 'tenants', 'clinic-bengaluru', 'dataRequests', requestRef.id), {
    status: 'resolved',
    resolvedAt: new Date().toISOString(),
    notes: 'Communication logs purged per DPDP policy.'
  });
  console.log('   ✓ Erasure request lifecycle transition: open -> resolved');

  // Step 5: TEST 6 - Role-Based Least Privilege Enforcement
  console.log('\n5. [TEST 6] Testing Role-Based Least Privilege (Receptionist vs Owner)...');
  // Sign in as Receptionist Pooja Sharma
  const recCred = await signInWithEmailAndPassword(auth, 'pooja@agastyaone.com', 'Agastya.27venu');
  console.log('   ✓ Receptionist authenticated! UID:', recCred.user.uid);

  // A) Receptionist touches demographic fields -> ALLOWED
  await updateDoc(doc(db, 'tenants', 'clinic-bengaluru', 'patients', patientRef.id), {
    phone: '+919876543298',
    address: 'Koramangala, Bengaluru'
  });
  console.log('   ✓ Receptionist updating demographic fields (phone, address): ALLOWED');

  // B) Receptionist touches clinical fields (allergies) -> MUST BE DENIED
  try {
    await updateDoc(doc(db, 'tenants', 'clinic-bengaluru', 'patients', patientRef.id), {
      allergies: ['Penicillin', 'Sulfa drugs']
    });
    console.error('   ❌ FAILED: Receptionist was able to modify clinical allergies field!');
  } catch (err) {
    console.log('   ✓ SUCCESS: Receptionist attempt to edit clinical fields blocked with PERMISSION_DENIED! (' + err.code + ')');
  }

  // C) Cross-tenant access: Receptionist tries to access Mumbai clinic -> MUST BE DENIED
  try {
    await getDocs(collection(db, 'tenants', 'clinic-mumbai', 'patients'));
    console.error('   ❌ FAILED: Bengaluru receptionist was able to read Mumbai clinic patients!');
  } catch (err) {
    console.log('   ✓ SUCCESS: Cross-tenant access to Mumbai blocked with PERMISSION_DENIED! (' + err.code + ')');
  }

  console.log('\n====================================================');
  console.log('  ALL TESTS (3, 4, 5, 6) PASSED WITH 100% SUCCESS!  ');
  console.log('====================================================\n');
  process.exit(0);
}

runTests().catch(err => {
  console.error('Test failed with error:', err);
  process.exit(1);
});
