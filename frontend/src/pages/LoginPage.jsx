import { Navigate } from 'react-router-dom'
import { MapPin } from 'lucide-react'
import { useAuth } from '@/context/AuthContext'

export default function LoginPage() {
  const { authenticated, login } = useAuth()

  if (authenticated) {
    return <Navigate to="/home" replace />
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="w-full max-w-sm rounded-xl border bg-white p-8 shadow-sm text-center space-y-6">
        <div className="flex flex-col items-center gap-2">
          <div className="flex items-center justify-center rounded-full bg-primary/10 p-3">
            <MapPin className="h-6 w-6 text-primary" />
          </div>
          <h1 className="text-xl font-bold">LocalIssues</h1>
          <p className="text-sm text-muted-foreground">
            Sign in to report issues and vote in your community
          </p>
        </div>

        <button
          onClick={login}
          className="w-full rounded-md bg-primary px-4 py-2.5 text-sm font-medium text-primary-foreground hover:bg-primary/90 transition-colors"
        >
          Log in with Keycloak
        </button>
      </div>
    </div>
  )
}
