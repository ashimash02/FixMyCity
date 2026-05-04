import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { User, FileText, Settings, LogOut, ChevronDown } from 'lucide-react'
import { useAuth } from '@/context/AuthContext'
import { cn } from '@/lib/utils'

export default function ProfileDropdown() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const ref = useRef(null)

  useEffect(() => {
    function handleOutsideClick(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false)
    }
    if (open) document.addEventListener('mousedown', handleOutsideClick)
    return () => document.removeEventListener('mousedown', handleOutsideClick)
  }, [open])

  const go = (path) => {
    setOpen(false)
    navigate(path)
  }

  const username = user?.preferred_username ?? 'Account'
  const initials = username.slice(0, 2).toUpperCase()

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setOpen((o) => !o)}
        className={cn(
          'flex items-center gap-2 rounded-full border px-3 py-1.5 text-sm font-medium transition-colors',
          open
            ? 'border-primary bg-primary/5 text-primary'
            : 'border-input text-muted-foreground hover:border-primary/40 hover:bg-accent hover:text-foreground'
        )}
      >
        <span className="flex h-6 w-6 items-center justify-center rounded-full bg-primary text-[11px] font-bold text-primary-foreground">
          {initials}
        </span>
        <span className="hidden sm:inline">{username}</span>
        <ChevronDown
          className={cn('h-3.5 w-3.5 transition-transform', open && 'rotate-180')}
        />
      </button>

      {open && (
        <div className="absolute right-0 top-[calc(100%+8px)] z-50 min-w-[180px] overflow-hidden rounded-xl border bg-white shadow-lg">
          <div className="border-b px-4 py-3">
            <p className="text-sm font-medium">{username}</p>
            {user?.email && (
              <p className="mt-0.5 truncate text-xs text-muted-foreground">{user.email}</p>
            )}
          </div>

          <div className="py-1">
            <DropdownItem icon={FileText} label="My Posts" onClick={() => go('/my-posts')} />
            <DropdownItem icon={Settings} label="Settings" onClick={() => go('/settings')} />
          </div>

          <div className="border-t py-1">
            <DropdownItem
              icon={LogOut}
              label="Log out"
              onClick={() => { setOpen(false); logout() }}
              danger
            />
          </div>
        </div>
      )}
    </div>
  )
}

function DropdownItem({ icon: Icon, label, onClick, danger }) {
  return (
    <button
      onClick={onClick}
      className={cn(
        'flex w-full items-center gap-2.5 px-4 py-2 text-sm transition-colors',
        danger
          ? 'text-destructive hover:bg-destructive/10'
          : 'text-foreground hover:bg-accent'
      )}
    >
      <Icon className="h-4 w-4 shrink-0" />
      {label}
    </button>
  )
}
