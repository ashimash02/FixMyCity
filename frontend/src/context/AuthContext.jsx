import { createContext, useContext, useEffect, useState } from 'react'
import { Loader2 } from 'lucide-react'
import keycloak from '@/keycloak'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState({
    initialized: false,
    authenticated: false,
    user: null,
    token: null,
  })

  useEffect(() => {
    keycloak
      .init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
      })
      .then((authenticated) => {
        setAuth({
          initialized: true,
          authenticated,
          user: authenticated ? keycloak.tokenParsed : null,
          token: authenticated ? keycloak.token : null,
        })
      })
      .catch(() => {
        // Keycloak unreachable — still mark initialized so the app renders
        setAuth({ initialized: true, authenticated: false, user: null, token: null })
      })

    // Refresh the token 60 seconds before it expires
    keycloak.onTokenExpired = () => {
      keycloak.updateToken(60).then((refreshed) => {
        if (refreshed) {
          setAuth((prev) => ({ ...prev, token: keycloak.token }))
        }
      })
    }
  }, [])

  const login = () => keycloak.login({ redirectUri: window.location.origin + '/home' })
  const logout = () => keycloak.logout({ redirectUri: window.location.origin + '/login' })

  if (!auth.initialized) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  return (
    <AuthContext.Provider value={{ ...auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
