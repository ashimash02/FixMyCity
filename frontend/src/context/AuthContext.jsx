import { createContext, useContext, useEffect, useState } from 'react'
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

    // Refresh the token 60 seconds before it expires
    keycloak.onTokenExpired = () => {
      keycloak.updateToken(60).then((refreshed) => {
        if (refreshed) {
          setAuth((prev) => ({ ...prev, token: keycloak.token }))
        }
      })
    }
  }, [])

  const login = () => keycloak.login()
  const logout = () => keycloak.logout({ redirectUri: window.location.origin })

  return (
    <AuthContext.Provider value={{ ...auth, login, logout }}>
      {auth.initialized ? children : null}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
