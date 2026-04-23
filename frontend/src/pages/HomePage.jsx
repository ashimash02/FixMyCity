import { useEffect, useState } from 'react'
import { getAllIssues } from '@/api/issueApi'
import IssueCard from '@/components/IssueCard'
import { Button } from '@/components/ui/button'
import { Loader2, AlertCircle } from 'lucide-react'

export default function HomePage() {
  const [issues, setIssues] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    getAllIssues(page)
      .then(({ data }) => {
        setIssues(data.content)
        setTotalPages(data.totalPages)
      })
      .catch(() => setError('Failed to load issues. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [page])

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Reported Issues</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Browse and vote on issues reported in your area
        </p>
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
        <div className="py-20 text-center text-muted-foreground">
          No issues reported yet. Be the first to report one!
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
