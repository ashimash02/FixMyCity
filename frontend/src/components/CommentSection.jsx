import { useEffect, useRef, useState } from 'react'
import { Loader2, Send, MessageSquare, AlertCircle } from 'lucide-react'
import { getComments, addComment } from '@/api/issueApi'
import { useAuth } from '@/context/AuthContext'
import { Button } from '@/components/ui/button'

export default function CommentSection({ issueId }) {
  const { authenticated, login } = useAuth()
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [text, setText] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState(null)
  const textareaRef = useRef(null)

  useEffect(() => {
    getComments(issueId)
      .then(({ data }) => setComments(data.content))
      .catch(() => setError('Failed to load comments.'))
      .finally(() => setLoading(false))
  }, [issueId])

  const handleSubmit = async (e) => {
    e.preventDefault()
    const trimmed = text.trim()
    if (!trimmed || submitting) return

    setSubmitting(true)
    setSubmitError(null)
    try {
      const { data } = await addComment(issueId, trimmed)
      setComments((prev) => [data, ...prev])
      setText('')
      textareaRef.current?.focus()
    } catch {
      setSubmitError('Failed to post comment. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleKeyDown = (e) => {
    // Ctrl+Enter or Cmd+Enter submits
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') handleSubmit(e)
  }

  return (
    <div className="space-y-4">
      <h3 className="flex items-center gap-2 text-sm font-semibold">
        <MessageSquare className="h-4 w-4" />
        Comments
        {comments.length > 0 && (
          <span className="rounded-full bg-muted px-2 py-0.5 text-xs font-normal text-muted-foreground">
            {comments.length}
          </span>
        )}
      </h3>

      {/* Input */}
      {authenticated ? (
        <form onSubmit={handleSubmit} className="space-y-2">
          <textarea
            ref={textareaRef}
            value={text}
            onChange={(e) => setText(e.target.value)}
            onKeyDown={handleKeyDown}
            rows={2}
            maxLength={1000}
            placeholder="Write a comment… (Ctrl+Enter to submit)"
            className="w-full resize-none rounded-lg border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40"
          />
          <div className="flex items-center justify-between">
            <span className="text-xs text-muted-foreground">{text.length}/1000</span>
            <div className="flex items-center gap-2">
              {submitError && (
                <span className="flex items-center gap-1 text-xs text-destructive">
                  <AlertCircle className="h-3.5 w-3.5" />
                  {submitError}
                </span>
              )}
              <Button size="sm" type="submit" disabled={!text.trim() || submitting}>
                {submitting
                  ? <Loader2 className="h-3.5 w-3.5 animate-spin" />
                  : <Send className="h-3.5 w-3.5" />
                }
                Post
              </Button>
            </div>
          </div>
        </form>
      ) : (
        <p className="text-sm text-muted-foreground">
          <button onClick={login} className="text-primary underline underline-offset-4 hover:text-primary/80">
            Log in
          </button>{' '}
          to leave a comment.
        </p>
      )}

      {/* List */}
      {loading && (
        <div className="flex justify-center py-4">
          <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
        </div>
      )}

      {error && (
        <p className="flex items-center gap-1.5 text-xs text-destructive">
          <AlertCircle className="h-3.5 w-3.5" />
          {error}
        </p>
      )}

      {!loading && !error && comments.length === 0 && (
        <p className="text-sm text-muted-foreground">No comments yet. Be the first!</p>
      )}

      {!loading && comments.length > 0 && (
        <ul className="space-y-3">
          {comments.map((c) => (
            <li key={c.id} className="rounded-lg border bg-muted/30 px-4 py-3 text-sm">
              <div className="flex items-center justify-between gap-2 mb-1">
                <span className="font-medium">{c.createdByUsername}</span>
                <span className="text-xs text-muted-foreground">
                  {new Date(c.createdAt).toLocaleString()}
                </span>
              </div>
              <p className="text-foreground/80 leading-relaxed whitespace-pre-wrap">{c.content}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
