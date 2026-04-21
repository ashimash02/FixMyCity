import { Link, useLocation } from 'react-router-dom'
import { MapPin, LogIn, LogOut } from 'lucide-react'
import { cn } from '@/lib/utils'
import { useAuth } from '@/context/AuthContext'

export default function Navbar() {
  const { pathname } = useLocation()
  const { authenticated, user, login, logout } = useAuth()

  const links = [
    { to: '/home', label: 'All Issues' },
    { to: '/report', label: 'Report Issue' },
  ]

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-white shadow-sm">
      <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
        <Link to="/home" className="flex items-center gap-2 font-bold text-primary">
          <MapPin className="h-5 w-5" />
          <span>LocalIssues</span>
        </Link>
        <div className="flex items-center gap-3">
          <nav className="flex items-center gap-1">
            {links.map(({ to, label }) => (
              <Link
                key={to}
                to={to}
                className={cn(
                  'rounded-md px-4 py-1.5 text-sm font-medium transition-colors',
                  pathname === to
                    ? 'bg-primary text-white'
                    : 'text-muted-foreground hover:bg-accent hover:text-foreground'
                )}
              >
                {label}
              </Link>
            ))}
          </nav>

          {authenticated ? (
            <div className="flex items-center gap-2 border-l pl-3">
              <span className="text-sm text-muted-foreground">
                {user?.preferred_username}
              </span>
              <button
                onClick={logout}
                className="flex items-center gap-1 rounded-md px-3 py-1.5 text-sm font-medium text-muted-foreground hover:bg-accent hover:text-foreground transition-colors"
              >
                <LogOut className="h-4 w-4" />
                Log out
              </button>
            </div>
          ) : (
            <button
              onClick={login}
              className="flex items-center gap-1 rounded-md bg-primary px-3 py-1.5 text-sm font-medium text-primary-foreground hover:bg-primary/90 transition-colors"
            >
              <LogIn className="h-4 w-4" />
              Log in
            </button>
          )}
        </div>
      </div>
    </header>
  )
}
