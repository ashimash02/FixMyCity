import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Loader2, AlertCircle, ArrowLeft, FileText } from 'lucide-react'
import { getUserProfile, getUserIssues } from '@/api/userApi'
import IssueCard from '@/components/IssueCard'
import { Button } from '@/components/ui/button'

export default function UserProfilePage() {
  const { userId } = useParams()
  const navigate = useNavigate()

  const [profile, setProfile] = useState(null)
  const [profileLoading, setProfileLoading] = useState(true)
  const [profileError, setProfileError] = useState(null)

  const [issues, setIssues] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [issuesLoading, setIssuesLoading] = useState(true)

  useEffect(() => {
    setProfileLoading(true)
    setProfileError(null)
    getUserProfile(userId)
      .then(({ data }) => setProfile(data))
      .catch(() => setProfileError('User not found.'))
      .finally(() => setProfileLoading(false))
  }, [userId])

  useEffect(() => {
    setIssuesLoading(true)
    getUserIssues(userId, page)
      .then(({ data }) => {
        setIssues(data.content)
        setTotalPages(data.totalPages)
      })
      .finally(() => setIssuesLoading(false))
  }, [userId, page])

  if (profileLoading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (profileError) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-12 text-center">
        <div className="flex items-center justify-center gap-2 text-destructive mb-4">
          <AlertCircle className="h-5 w-5" />
          <span>{profileError}</span>
        </div>
        <Button variant="outline" size="sm" onClick={() => navigate(-1)}>
          Go back
        </Button>
      </div>
    )
  }

  const initials = profile.username.slice(0, 2).toUpperCase()

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <button
        onClick={() => navigate(-1)}
        className="mb-6 flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back
      </button>

      {/* Profile card */}
      <div className="rounded-xl border bg-white shadow-sm p-6 mb-8 flex items-start gap-5">
        <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-primary text-xl font-bold text-primary-foreground">
          {initials}
        </div>
        <div className="min-w-0">
          <h1 className="text-xl font-bold">{profile.username}</h1>
          {profile.bio ? (
            <p className="mt-1.5 text-sm text-muted-foreground leading-relaxed whitespace-pre-wrap">
              {profile.bio}
            </p>
          ) : (
            <p className="mt-1.5 text-sm text-muted-foreground italic">No bio yet.</p>
          )}
        </div>
      </div>

      {/* Issues section */}
      <div>
        <h2 className="flex items-center gap-2 text-base font-semibold mb-4">
          <FileText className="h-4 w-4" />
          Reported Issues
        </h2>

        {issuesLoading && (
          <div className="flex justify-center py-10">
            <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
          </div>
        )}

        {!issuesLoading && issues.length === 0 && (
          <p className="text-sm text-muted-foreground py-6">
            This user hasn't reported any issues yet.
          </p>
        )}

        {!issuesLoading && issues.length > 0 && (
          <>
            <div className="grid gap-4 sm:grid-cols-2">
              {issues.map((issue) => (
                <IssueCard key={issue.id} issue={issue} />
              ))}
            </div>

            {totalPages > 1 && (
              <div className="mt-6 flex items-center justify-center gap-2">
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
    </div>
  )
}
