import { initializeApp } from 'firebase/app'
import { getAuth, connectAuthEmulator } from 'firebase/auth'
import { getFirestore, connectFirestoreEmulator } from 'firebase/firestore'
import { getFunctions, connectFunctionsEmulator } from 'firebase/functions'

// Firebase Web SDK config is a public client identifier, not a secret - see
// https://firebase.google.com/docs/projects/api-keys - but it's still supplied
// via env vars (not hardcoded) so this codebase never bakes in one specific
// Firebase project. Fallbacks are provided for local emulator development.
const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || 'fake-api-key-for-emulator',
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || 'agastyaone-crm.firebaseapp.com',
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || 'agastyaone-crm',
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || 'agastyaone-crm.appspot.com',
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || '123456789',
  appId: import.meta.env.VITE_FIREBASE_APP_ID || '1:123456789:web:abcdef',
}

export const app = initializeApp(firebaseConfig)
export const auth = getAuth(app)
export const db = getFirestore(app)

// Must match functions/src/constants.ts REGION - the platform-admin callables
// are deployed there, not the SDK default (us-central1).
export const functions = getFunctions(app, 'asia-south1')

// Connect to local Firebase emulators when served on localhost / 127.0.0.1
if (
  typeof window !== 'undefined' &&
  (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
) {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  ;(self as any).FIREBASE_APPCHECK_DEBUG_TOKEN = true
  connectAuthEmulator(auth, 'http://127.0.0.1:9099', { disableWarnings: true })
  connectFirestoreEmulator(db, '127.0.0.1', 8085)
  connectFunctionsEmulator(functions, '127.0.0.1', 5001)
}

