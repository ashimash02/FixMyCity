import { useState } from 'react'
import { Link } from 'react-router-dom'
import { MapPin, Tag, ThumbsUp, User, Calendar } from 'lucide-react'
import { toggleVote } from '@/api/issueApi'
import { useAuth } from '@/context/AuthContext'
import StatusBadge from './StatusBadge'

export default function IssueCard({ issue }) {
  const { authenticated, login } = useAuth()
  const [voteCount, setVoteCount] = useState(issue.voteCount)
  const [hasVoted, setHasVoted] = useState(issue.hasVoted)
  const [voting, setVoting] = useState(false)

  const handleVote = async (e) => {
    e.preventDefault() // prevent Link navigation when clicking the button
    if (!authenticated) { login(); return }
    if (voting) return

    // Optimistic update
    const wasVoted = hasVoted
    setHasVoted(!wasVoted)
    setVoteCount((c) => (wasVoted ? c - 1 : c + 1))
    setVoting(true)

    try {
      const { data } = await toggleVote(issue.id)
      setVoteCount(data.totalVotes)
      setHasVoted(data.hasVoted)
    } catch {
      // Roll back on failure
      setHasVoted(wasVoted)
      setVoteCount((c) => (wasVoted ? c + 1 : c - 1))
    } finally {
      setVoting(false)
    }
  }

  return (
    <div className="rounded-xl border bg-white shadow-sm overflow-hidden flex flex-col">
      {/* Image */}
      {issue.imageUrl && (
        <Link to={`/issues/${issue.id}`}>
          <img
            src={issue.imageUrl}
            alt={issue.title}
            className="w-full h-48 object-cover"
            onError={(e) => (e.currentTarget.style.display = 'none')}
          />
        </Link>
      )}

      <div className="flex flex-col flex-1 p-4 gap-3">
        {/* Author + date */}
        <div className="flex items-center justify-between text-xs text-muted-foreground">
          <span className="flex items-center gap-1">
            <User className="h-3 w-3" />
            {issue.createdByUsername ?? 'Anonymous'}
          </span>
          <span className="flex items-center gap-1">
            <Calendar className="h-3 w-3" />
            {new Date(issue.createdAt).toLocaleDateString()}
          </span>
        </div>

        {/* Title */}
        <Link to={`/issues/${issue.id}`} className="hover:underline">
          <h3 className="font-semibold text-sm leading-snug line-clamp-2">{issue.title}</h3>
        </Link>

        {/* Description */}
        {issue.description && (
          <p className="text-xs text-muted-foreground line-clamp-2">{issue.description}</p>
        )}

        {/* Tags row */}
        <div className="flex flex-wrap gap-2 text-xs text-muted-foreground">
          {issue.locationName && (
            <span className="flex items-center gap-1">
              <MapPin className="h-3 w-3 shrink-0" />
              <span className="line-clamp-1">{issue.locationName}</span>
              {issue.distanceKm != null && (
                <span className="text-primary font-medium">
                  · {issue.distanceKm < 1
                    ? `${Math.round(issue.distanceKm * 1000)} m`
                    : `${issue.distanceKm.toFixed(1)} km`} away
                </span>
              )}
            </span>
          )}
          {issue.category && (
            <span className="flex items-center gap-1">
              <Tag className="h-3 w-3" />
              {issue.category}
            </span>
          )}
        </div>

        {/* Footer: status + vote */}
        <div className="flex items-center justify-between mt-auto pt-2 border-t">
          <StatusBadge status={issue.status} />

          <button
            onClick={handleVote}
            disabled={voting}
            className={[
              'flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium transition-colors',
              hasVoted
                ? 'bg-primary text-primary-foreground hover:bg-primary/90'
                : 'border border-input text-muted-foreground hover:bg-accent hover:text-foreground',
              voting ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer',
            ].join(' ')}
          >
            <ThumbsUp className="h-3.5 w-3.5" />
            {voteCount}
          </button>
        </div>
      </div>
    </div>
  )
}
