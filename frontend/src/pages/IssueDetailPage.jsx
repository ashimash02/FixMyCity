import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getIssueById, addVote } from '@/api/issueApi'
import { useAuth } from '@/context/AuthContext'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import StatusBadge from '@/components/StatusBadge'
import { MapPin, Tag, ThumbsUp, ArrowLeft, Loader2, AlertCircle } from 'lucide-react'

export default function IssueDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { authenticated, login } = useAuth()

  const [issue, setIssue] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [voting, setVoting] = useState(false)
  const [voteError, setVoteError] = useState(null)
  const [voteSuccess, setVoteSuccess] = useState(false)

  useEffect(() => {
    getIssueById(id)
      .then(({ data }) => setIssue(data))
      .catch(() => setError('Issue not found or server unavailable.'))
      .finally(() => setLoading(false))
  }, [id])

  const handleVote = async () => {
    setVoting(true)
    setVoteError(null)
    try {
      const { data } = await addVote(id)
      setIssue((prev) => ({ ...prev, voteCount: data.voteCount }))
      setVoteSuccess(true)
    } catch (err) {
      const msg = err.response?.data?.error
      setVoteError(msg ?? 'Failed to submit vote.')
    } finally {
      setVoting(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-12 text-center">
        <p className="text-destructive">{error}</p>
        <Button variant="outline" className="mt-4" onClick={() => navigate('/')}>
          Back to Issues
        </Button>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-8">
      <button
        onClick={() => navigate('/')}
        className="mb-6 flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to Issues
      </button>

      <Card>
        <CardHeader>
          <div className="flex items-start justify-between gap-3">
            <CardTitle className="text-xl leading-snug">{issue.title}</CardTitle>
            <StatusBadge status={issue.status} />
          </div>
          <div className="flex flex-wrap gap-3 pt-1 text-sm text-muted-foreground">
            {issue.category && (
              <span className="flex items-center gap-1">
                <Tag className="h-3.5 w-3.5" />
                {issue.category}
              </span>
            )}
            {issue.latitude && issue.longitude && (
              <span className="flex items-center gap-1">
                <MapPin className="h-3.5 w-3.5" />
                {issue.latitude.toFixed(5)}, {issue.longitude.toFixed(5)}
              </span>
            )}
            <span className="ml-auto text-xs">
              {new Date(issue.createdAt).toLocaleString()}
            </span>
          </div>
        </CardHeader>

        <CardContent className="space-y-6">
          {issue.description && (
            <p className="text-sm leading-relaxed text-foreground/80">{issue.description}</p>
          )}

          {issue.imageUrl && (
            <img
              src={issue.imageUrl}
              alt="Issue"
              className="w-full rounded-lg border object-cover max-h-72"
              onError={(e) => (e.currentTarget.style.display = 'none')}
            />
          )}

          {/* Vote Section */}
          <div className="rounded-lg border bg-muted/40 p-4 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium">Community Votes</span>
              <span className="flex items-center gap-1.5 text-sm font-semibold text-primary">
                <ThumbsUp className="h-4 w-4" />
                {issue.voteCount} {issue.voteCount === 1 ? 'vote' : 'votes'}
              </span>
            </div>

            {voteSuccess ? (
              <p className="text-sm text-green-600 font-medium">
                Your vote was recorded. Thank you!
              </p>
            ) : authenticated ? (
              <Button onClick={handleVote} disabled={voting} size="default">
                {voting
                  ? <Loader2 className="h-4 w-4 animate-spin" />
                  : <><ThumbsUp className="mr-1.5 h-4 w-4" />Vote</>
                }
              </Button>
            ) : (
              <button
                onClick={login}
                className="text-sm text-primary underline underline-offset-4 hover:text-primary/80"
              >
                Log in to vote
              </button>
            )}

            {voteError && (
              <div className="flex items-center gap-1.5 text-xs text-destructive">
                <AlertCircle className="h-3.5 w-3.5" />
                {voteError}
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
