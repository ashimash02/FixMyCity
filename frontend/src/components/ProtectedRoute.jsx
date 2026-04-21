import { useAuth } from '@/context/AuthContext'

export default function ProtectedRoute({ children }) {
  const { authenticated, login } = useAuth()

  if (!authenticated) {
    return (
      <div className="mx-auto max-w-xl px-4 py-20 text-center">
        <p className="mb-4 text-muted-foreground">You need to be logged in to access this page.</p>
        <button
          onClick={login}
          className="rounded-md bg-primary px-5 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
        >
          Log in
        </button>
      </div>
    )
  }

  return children
}
