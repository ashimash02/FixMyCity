import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { getIssueById, toggleVote, editIssue, deleteIssue } from '@/api/issueApi'
import { useAuth } from '@/context/AuthContext'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import StatusBadge from '@/components/StatusBadge'
import CommentSection from '@/components/CommentSection'
import {
  MapPin, Tag, ThumbsUp, ArrowLeft, Loader2, AlertCircle,
  User, Pencil, Trash2, X, Check,
} from 'lucide-react'

export default function IssueDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { authenticated, login, user } = useAuth()

  const [issue, setIssue] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // vote
  const [hasVoted, setHasVoted] = useState(false)
  const [voting, setVoting] = useState(false)
  const [voteError, setVoteError] = useState(null)

  // edit
  const [editing, setEditing] = useState(false)
  const [editForm, setEditForm] = useState({})
  const [saving, setSaving] = useState(false)
  const [editError, setEditError] = useState(null)

  // delete
  const [confirmDelete, setConfirmDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    getIssueById(id)
      .then(({ data }) => {
        setIssue(data)
        setHasVoted(data.hasVoted)
      })
      .catch(() => setError('Issue not found or server unavailable.'))
      .finally(() => setLoading(false))
  }, [id])

  const isOwner = user && issue && user.sub === issue.createdBy

  // ── Vote ────────────────────────────────────────────────────────────────────
  const handleVote = async () => {
    if (voting) return
    const wasVoted = hasVoted
    setHasVoted(!wasVoted)
    setIssue((prev) => ({ ...prev, voteCount: wasVoted ? prev.voteCount - 1 : prev.voteCount + 1 }))
    setVoting(true)
    setVoteError(null)
    try {
      const { data } = await toggleVote(id)
      setIssue((prev) => ({ ...prev, voteCount: data.totalVotes }))
      setHasVoted(data.hasVoted)
    } catch (err) {
      setHasVoted(wasVoted)
      setIssue((prev) => ({ ...prev, voteCount: wasVoted ? prev.voteCount + 1 : prev.voteCount - 1 }))
      setVoteError(err.response?.data?.error ?? 'Failed to submit vote.')
    } finally {
      setVoting(false)
    }
  }

  // ── Edit ────────────────────────────────────────────────────────────────────
  const startEdit = () => {
    setEditForm({
      title: issue.title,
      description: issue.description ?? '',
      category: issue.category ?? '',
      locationName: issue.locationName ?? '',
      latitude: issue.latitude,
      longitude: issue.longitude,
      imageUrl: issue.imageUrl ?? '',
    })
    setEditError(null)
    setEditing(true)
  }

  const handleSave = async () => {
    if (!editForm.title?.trim()) { setEditError('Title is required.'); return }
    setSaving(true)
    setEditError(null)
    try {
      const { data } = await editIssue(id, editForm)
      setIssue(data)
      setEditing(false)
    } catch (err) {
      setEditError(err.response?.data?.error ?? 'Failed to save changes.')
    } finally {
      setSaving(false)
    }
  }

  // ── Delete ──────────────────────────────────────────────────────────────────
  const handleDelete = async () => {
    setDeleting(true)
    try {
      await deleteIssue(id)
      navigate('/my-posts', { replace: true })
    } catch {
      setConfirmDelete(false)
      setDeleting(false)
    }
  }

  // ── Render guards ────────────────────────────────────────────────────────────
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
        <Button variant="outline" className="mt-4" onClick={() => navigate('/home')}>Back to Issues</Button>
      </div>
    )
  }

  const wasEdited = issue.updatedAt && issue.updatedAt !== issue.createdAt

  return (
    <div className="mx-auto max-w-2xl px-4 py-8">
      <button
        onClick={() => navigate(-1)}
        className="mb-6 flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back
      </button>

      <Card>
        <CardHeader>
          <div className="flex items-start justify-between gap-3">
            {editing ? (
              <input
                value={editForm.title}
                onChange={(e) => setEditForm((f) => ({ ...f, title: e.target.value }))}
                className="flex-1 rounded-md border px-3 py-1.5 text-lg font-semibold focus:outline-none focus:ring-2 focus:ring-primary/40"
              />
            ) : (
              <CardTitle className="text-xl leading-snug">{issue.title}</CardTitle>
            )}
            <div className="flex items-center gap-2 shrink-0">
              <StatusBadge status={issue.status} />
              {isOwner && !editing && (
                <>
                  <button
                    onClick={startEdit}
                    className="rounded-md p-1.5 text-muted-foreground hover:bg-accent hover:text-foreground transition-colors"
                    title="Edit"
                  >
                    <Pencil className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => setConfirmDelete(true)}
                    className="rounded-md p-1.5 text-muted-foreground hover:bg-destructive/10 hover:text-destructive transition-colors"
                    title="Delete"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </>
              )}
            </div>
          </div>

          <div className="flex flex-wrap gap-3 pt-1 text-sm text-muted-foreground">
            {(editing ? editForm.category : issue.category) && !editing && (
              <span className="flex items-center gap-1">
                <Tag className="h-3.5 w-3.5" />
                {issue.category}
              </span>
            )}
            {(issue.locationName || (issue.latitude && issue.longitude)) && !editing && (
              <span className="flex items-center gap-1">
                <MapPin className="h-3.5 w-3.5" />
                {issue.locationName ?? `${issue.latitude.toFixed(5)}, ${issue.longitude.toFixed(5)}`}
              </span>
            )}
            {issue.createdByUsername && (
              <span className="flex items-center gap-1">
                <User className="h-3.5 w-3.5" />
                <Link
                  to={`/user/${issue.createdBy}`}
                  className="hover:text-primary hover:underline underline-offset-2"
                >
                  {issue.createdByUsername}
                </Link>
              </span>
            )}
            <span className="ml-auto text-xs">
              {wasEdited
                ? <>updated {new Date(issue.updatedAt).toLocaleString()}</>
                : new Date(issue.createdAt).toLocaleString()
              }
            </span>
          </div>
        </CardHeader>

        <CardContent className="space-y-6">
          {/* Description */}
          {editing ? (
            <textarea
              value={editForm.description}
              onChange={(e) => setEditForm((f) => ({ ...f, description: e.target.value }))}
              rows={4}
              placeholder="Description"
              className="w-full resize-none rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40"
            />
          ) : (
            issue.description && (
              <p className="text-sm leading-relaxed text-foreground/80">{issue.description}</p>
            )
          )}

          {/* Category + location edit fields */}
          {editing && (
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs text-muted-foreground mb-1 block">Category</label>
                <input
                  value={editForm.category}
                  onChange={(e) => setEditForm((f) => ({ ...f, category: e.target.value }))}
                  placeholder="Category"
                  className="w-full rounded-md border px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40"
                />
              </div>
              <div>
                <label className="text-xs text-muted-foreground mb-1 block">Location name</label>
                <input
                  value={editForm.locationName}
                  onChange={(e) => setEditForm((f) => ({ ...f, locationName: e.target.value }))}
                  placeholder="Location"
                  className="w-full rounded-md border px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40"
                />
              </div>
              <div className="col-span-2">
                <label className="text-xs text-muted-foreground mb-1 block">Image URL</label>
                <input
                  value={editForm.imageUrl}
                  onChange={(e) => setEditForm((f) => ({ ...f, imageUrl: e.target.value }))}
                  placeholder="Image URL"
                  className="w-full rounded-md border px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40"
                />
              </div>
            </div>
          )}

          {/* Image */}
          {!editing && issue.imageUrl && (
            <img
              src={issue.imageUrl}
              alt="Issue"
              className="w-full rounded-lg border object-cover max-h-72"
              onError={(e) => (e.currentTarget.style.display = 'none')}
            />
          )}

          {/* Edit action bar */}
          {editing && (
            <div className="flex items-center gap-2">
              <Button size="sm" onClick={handleSave} disabled={saving}>
                {saving ? <Loader2 className="h-3.5 w-3.5 animate-spin mr-1" /> : <Check className="h-3.5 w-3.5 mr-1" />}
                Save
              </Button>
              <Button size="sm" variant="outline" onClick={() => setEditing(false)} disabled={saving}>
                <X className="h-3.5 w-3.5 mr-1" />
                Cancel
              </Button>
              {editError && (
                <span className="flex items-center gap-1 text-xs text-destructive">
                  <AlertCircle className="h-3.5 w-3.5" />
                  {editError}
                </span>
              )}
            </div>
          )}

          {/* Delete confirm */}
          {confirmDelete && (
            <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 space-y-3">
              <p className="text-sm font-medium text-destructive">Delete this issue? This cannot be undone.</p>
              <div className="flex gap-2">
                <Button size="sm" variant="destructive" onClick={handleDelete} disabled={deleting}>
                  {deleting && <Loader2 className="h-3.5 w-3.5 animate-spin mr-1" />}
                  Yes, delete
                </Button>
                <Button size="sm" variant="outline" onClick={() => setConfirmDelete(false)} disabled={deleting}>
                  Cancel
                </Button>
              </div>
            </div>
          )}

          {/* Comments */}
          <div className="rounded-lg border bg-muted/20 p-4">
            <CommentSection issueId={id} />
          </div>

          {/* Vote */}
          <div className="rounded-lg border bg-muted/40 p-4 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium">Community Votes</span>
              <span className="flex items-center gap-1.5 text-sm font-semibold text-primary">
                <ThumbsUp className="h-4 w-4" />
                {issue.voteCount} {issue.voteCount === 1 ? 'vote' : 'votes'}
              </span>
            </div>

            {authenticated ? (
              <button
                onClick={handleVote}
                disabled={voting}
                className={[
                  'flex items-center gap-2 rounded-full px-4 py-1.5 text-sm font-medium transition-colors',
                  hasVoted
                    ? 'bg-primary text-primary-foreground hover:bg-primary/90'
                    : 'border border-input text-muted-foreground hover:bg-accent hover:text-foreground',
                  voting ? 'opacity-60 cursor-not-allowed' : '',
                ].join(' ')}
              >
                {voting ? <Loader2 className="h-4 w-4 animate-spin" /> : <ThumbsUp className="h-4 w-4" />}
                {hasVoted ? 'Voted' : 'Vote'}
              </button>
            ) : (
              <button onClick={login} className="text-sm text-primary underline underline-offset-4 hover:text-primary/80">
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
