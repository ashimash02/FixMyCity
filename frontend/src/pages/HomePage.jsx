import { useEffect, useState } from 'react'
import { getAllIssues, getTrendingIssues, getFollowingFeed } from '@/api/issueApi'
import { useLocationContext } from '@/context/LocationContext'
import IssueCard from '@/components/IssueCard'
import { Button } from '@/components/ui/button'
import { Loader2, AlertCircle, Clock, TrendingUp, Users } from 'lucide-react'

const TABS = [
  { key: 'trending',  label: 'Trending',  icon: TrendingUp },
  { key: 'following', label: 'Following', icon: Users },
  { key: 'latest',    label: 'Latest',    icon: Clock },
]

export default function HomePage() {
  const { location } = useLocationContext()
  const [tab, setTab] = useState('trending')
  const [issues, setIssues] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => { setPage(0) }, [location, tab])

  useEffect(() => {
    setLoading(true)
    setError(null)

    let request
    if (tab === 'following') {
      request = getFollowingFeed(page)
    } else if (tab === 'trending') {
      request = getTrendingIssues(page, 10, location ?? null)
    } else {
      request = getAllIssues(page, 10, location ?? null)
    }

    request
      .then(({ data }) => {
        setIssues(data.content)
        setTotalPages(data.totalPages)
      })
      .catch(() => setError('Failed to load issues. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [tab, page, location])

  const handleTabChange = (key) => {
    if (key === tab) return
    setTab(key)
    setPage(0)
  }

  const emptyMessage = tab === 'following'
    ? "You're not following anyone yet, or they haven't posted issues."
    : 'No issues reported yet. Be the first to report one!'

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Reported Issues</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Browse and vote on issues reported in your area
        </p>
      </div>

      {/* Tab toggle */}
      <div className="mb-6 inline-flex rounded-lg border bg-muted p-1 gap-1">
        {TABS.map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            onClick={() => handleTabChange(key)}
            className={[
              'flex items-center gap-1.5 rounded-md px-4 py-1.5 text-sm font-medium transition-colors',
              tab === key
                ? 'bg-white text-foreground shadow-sm'
                : 'text-muted-foreground hover:text-foreground',
            ].join(' ')}
          >
            <Icon className="h-3.5 w-3.5" />
            {label}
          </button>
        ))}
      </div>

      {/* Location filter pill — not shown on following tab */}
      {location && tab !== 'following' && (
        <div className="mb-4 flex items-center gap-2 text-sm text-muted-foreground">
          <span>Showing issues in</span>
          <span className="rounded-full bg-primary/10 px-3 py-0.5 text-xs font-medium text-primary">
            {location.name.split(',')[0].trim()}
          </span>
        </div>
      )}

      {loading && (
        <div className="flex justify-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      )}

      {error && (
        <div className="flex items-center gap-2 rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          <AlertCircle className="h-4 w-4 shrink-0" />
          {error}
        </div>
      )}

      {!loading && !error && issues.length === 0 && (
        <div className="py-20 text-center text-muted-foreground">
          {emptyMessage}
        </div>
      )}

      {!loading && !error && issues.length > 0 && (
        <>
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {issues.map((issue) => (
              <IssueCard key={issue.id} issue={issue} />
            ))}
          </div>

          {totalPages > 1 && (
            <div className="mt-8 flex items-center justify-center gap-2">
              <Button variant="outline" size="sm" onClick={() => setPage((p) => p - 1)} disabled={page === 0}>
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {page + 1} of {totalPages}
              </span>
              <Button variant="outline" size="sm" onClick={() => setPage((p) => p + 1)} disabled={page >= totalPages - 1}>
                Next
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
