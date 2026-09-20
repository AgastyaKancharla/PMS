import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import {
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signOut as firebaseSignOut,
  type User,
} from 'firebase/auth'
import { auth } from '../firebase'

interface AuthState {
  /** null while the initial auth check is still running. */
  user: User | null
  loading: boolean
  isPlatformAdmin: boolean
  /** Set when a sign-in succeeded but the account isn't a platform admin. */
  notAdminError: string | null
  signIn: (email: string, password: string) => Promise<void>
  signOutUser: () => Promise<void>
}

const AuthContext = createContext<AuthState | null>(null)

/**
 * Same pattern as the Android PlatformAdminSignInScreen: after sign-in, force-
 * refresh the ID token so a just-granted platformAdmin claim (set by the
 * onPlatformAdminWrite trigger, which can lag a sign-in by a moment) is picked
 * up, then check it. An account that signs in successfully but isn't a
 * platform admin is immediately signed back out - there is no "regular user"
 * mode for this app, only platform-admin-or-nothing.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [isPlatformAdmin, setIsPlatformAdmin] = useState(false)
  const [notAdminError, setNotAdminError] = useState<string | null>(null)

  useEffect(() => {
    return onAuthStateChanged(auth, async (nextUser) => {
      if (!nextUser) {
        setUser(null)
        setIsPlatformAdmin(false)
        setLoading(false)
        return
      }

      const tokenResult = await nextUser.getIdTokenResult(true)
      const admin = tokenResult.claims.platformAdmin === true
      if (!admin) {
        await firebaseSignOut(auth)
        setNotAdminError('This account is not a platform admin.')
        setUser(null)
        setIsPlatformAdmin(false)
        setLoading(false)
        return
      }

      setUser(nextUser)
      setIsPlatformAdmin(true)
      setLoading(false)
    })
  }, [])

  async function signIn(email: string, password: string): Promise<void> {
    setNotAdminError(null)
    await signInWithEmailAndPassword(auth, email, password)
    // onAuthStateChanged above does the claim check and any resulting sign-out.
  }

  async function signOutUser(): Promise<void> {
    await firebaseSignOut(auth)
  }

  return (
    <AuthContext.Provider
      value={{ user, loading, isPlatformAdmin, notAdminError, signIn, signOutUser }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}
