import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Loader2, AlertCircle, PlusCircle } from 'lucide-react'
import { getMyIssues } from '@/api/issueApi'
import { useAuth } from '@/context/AuthContext'
import IssueCard from '@/components/IssueCard'
import { Button } from '@/components/ui/button'

export default function MyPostsPage() {
  const { user } = useAuth()
  const [issues, setIssues] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    getMyIssues(page)
      .then(({ data }) => {
        setIssues(data.content)
        setTotalPages(data.totalPages)
      })
      .catch(() => setError('Failed to load your posts. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [page])

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">My Posts</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Issues reported by {user?.preferred_username ?? 'you'}
          </p>
        </div>
        <Button asChild size="sm">
          <Link to="/report">
            <PlusCircle className="mr-1.5 h-4 w-4" />
            Report New Issue
          </Link>
        </Button>
      </div>

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
        <div className="flex flex-col items-center gap-4 py-20 text-center text-muted-foreground">
          <p>You haven't reported any issues yet.</p>
          <Button asChild variant="outline" size="sm">
            <Link to="/report">Report your first issue</Link>
          </Button>
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
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => p - 1)}
                disabled={page === 0}
              >
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {page + 1} of {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => p + 1)}
                disabled={page >= totalPages - 1}
              >
                Next
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
