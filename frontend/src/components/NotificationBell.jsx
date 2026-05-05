import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Bell, Loader2, CheckCheck } from 'lucide-react'
import { cn } from '@/lib/utils'
import {
  getNotifications,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
} from '@/api/notificationApi'

const TYPE_ICON = {
  FOLLOW: '👤',
  COMMENT: '💬',
  VOTE: '👍',
}

export default function NotificationBell() {
  const navigate = useNavigate()
  const ref = useRef(null)

  const [open, setOpen] = useState(false)
  const [unread, setUnread] = useState(0)
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(false)
  const [marking, setMarking] = useState(false)

  // Poll unread count every 30s
  useEffect(() => {
    const fetchCount = () =>
      getUnreadCount()
        .then(({ data }) => setUnread(data.count))
        .catch(() => {})

    fetchCount()
    const interval = setInterval(fetchCount, 30_000)
    return () => clearInterval(interval)
  }, [])

  // Close on outside click
  useEffect(() => {
    const handler = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false)
    }
    if (open) document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [open])

  const handleOpen = () => {
    setOpen((o) => !o)
    if (!open) {
      setLoading(true)
      getNotifications()
        .then(({ data }) => setNotifications(data))
        .catch(() => {})
        .finally(() => setLoading(false))
    }
  }

  const handleClick = async (n) => {
    if (!n.isRead) {
      await markAsRead(n.id).catch(() => {})
      setNotifications((prev) =>
        prev.map((x) => (x.id === n.id ? { ...x, isRead: true } : x))
      )
      setUnread((c) => Math.max(0, c - 1))
    }
    setOpen(false)
    if (n.issueId) navigate(`/issues/${n.issueId}`)
    else if (n.senderId) navigate(`/user/${n.senderId}`)
  }

  const handleMarkAll = async () => {
    if (marking) return
    setMarking(true)
    await markAllAsRead().catch(() => {})
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })))
    setUnread(0)
    setMarking(false)
  }

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={handleOpen}
        className={cn(
          'relative flex items-center justify-center rounded-full p-2 transition-colors',
          open
            ? 'bg-primary/10 text-primary'
            : 'text-muted-foreground hover:bg-accent hover:text-foreground'
        )}
        aria-label="Notifications"
      >
        <Bell className="h-5 w-5" />
        {unread > 0 && (
          <span className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-white">
            {unread > 9 ? '9+' : unread}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 top-[calc(100%+8px)] z-50 w-80 overflow-hidden rounded-xl border bg-white shadow-lg">
          {/* Header */}
          <div className="flex items-center justify-between border-b px-4 py-2.5">
            <span className="text-sm font-semibold">Notifications</span>
            {unread > 0 && (
              <button
                onClick={handleMarkAll}
                disabled={marking}
                className="flex items-center gap-1 text-xs text-primary hover:underline disabled:opacity-50"
              >
                {marking
                  ? <Loader2 className="h-3 w-3 animate-spin" />
                  : <CheckCheck className="h-3 w-3" />
                }
                Mark all read
              </button>
            )}
          </div>

          {/* Body */}
          <div className="max-h-96 overflow-y-auto">
            {loading && (
              <div className="flex justify-center py-8">
                <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
              </div>
            )}

            {!loading && notifications.length === 0 && (
              <p className="py-8 text-center text-sm text-muted-foreground">
                No notifications yet
              </p>
            )}

            {!loading && notifications.map((n) => (
              <button
                key={n.id}
                onClick={() => handleClick(n)}
                className={cn(
                  'flex w-full items-start gap-3 px-4 py-3 text-left transition-colors hover:bg-accent',
                  !n.isRead && 'bg-primary/5'
                )}
              >
                <span className="mt-0.5 text-base">{TYPE_ICON[n.type] ?? '🔔'}</span>
                <div className="min-w-0 flex-1">
                  <p className={cn('text-sm leading-snug', !n.isRead && 'font-medium')}>
                    {n.message}
                  </p>
                  <p className="mt-0.5 text-xs text-muted-foreground">
                    {new Date(n.createdAt).toLocaleString()}
                  </p>
                </div>
                {!n.isRead && (
                  <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-primary" />
                )}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
