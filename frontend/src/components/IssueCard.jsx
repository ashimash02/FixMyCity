import { Link } from 'react-router-dom'
import { MapPin, ThumbsUp, Tag } from 'lucide-react'
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/card'
import StatusBadge from './StatusBadge'

export default function IssueCard({ issue }) {
  return (
    <Link to={`/issues/${issue.id}`}>
      <Card className="h-full transition-shadow hover:shadow-md cursor-pointer">
        <CardHeader className="pb-2">
          <div className="flex items-start justify-between gap-2">
            <CardTitle className="text-base leading-snug line-clamp-2">{issue.title}</CardTitle>
            <StatusBadge status={issue.status} />
          </div>
        </CardHeader>

        <CardContent className="pb-3">
          {issue.description && (
            <p className="text-sm text-muted-foreground line-clamp-2 mb-3">{issue.description}</p>
          )}
          <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
            {issue.category && (
              <span className="flex items-center gap-1">
                <Tag className="h-3 w-3" />
                {issue.category}
              </span>
            )}
            {issue.latitude && issue.longitude && (
              <span className="flex items-center gap-1">
                <MapPin className="h-3 w-3" />
                {issue.latitude.toFixed(4)}, {issue.longitude.toFixed(4)}
              </span>
            )}
          </div>
        </CardContent>

        <CardFooter className="pt-0 justify-between text-xs text-muted-foreground">
          <span className="flex items-center gap-1">
            <ThumbsUp className="h-3 w-3" />
            {issue.voteCount} {issue.voteCount === 1 ? 'vote' : 'votes'}
          </span>
          <span>{new Date(issue.createdAt).toLocaleDateString()}</span>
        </CardFooter>
      </Card>
    </Link>
  )
}
