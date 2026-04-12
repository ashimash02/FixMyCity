import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'

const statusStyles = {
  OPEN: 'bg-blue-100 text-blue-700 border-blue-200',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-700 border-yellow-200',
  RESOLVED: 'bg-green-100 text-green-700 border-green-200',
  CLOSED: 'bg-gray-100 text-gray-600 border-gray-200',
}

export default function StatusBadge({ status }) {
  return (
    <Badge
      variant="outline"
      className={cn('font-medium', statusStyles[status] ?? 'bg-gray-100 text-gray-600')}
    >
      {status?.replace('_', ' ')}
    </Badge>
  )
}
