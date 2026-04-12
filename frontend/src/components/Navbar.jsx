import { Link, useLocation } from 'react-router-dom'
import { MapPin } from 'lucide-react'
import { cn } from '@/lib/utils'

export default function Navbar() {
  const { pathname } = useLocation()

  const links = [
    { to: '/', label: 'All Issues' },
    { to: '/report', label: 'Report Issue' },
  ]

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-white shadow-sm">
      <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
        <Link to="/" className="flex items-center gap-2 font-bold text-primary">
          <MapPin className="h-5 w-5" />
          <span>LocalIssues</span>
        </Link>
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
      </div>
    </header>
  )
}
