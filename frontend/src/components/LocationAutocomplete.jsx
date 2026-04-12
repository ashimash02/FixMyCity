import { useState, useEffect, useRef, useCallback } from 'react'
import { cn } from '@/lib/utils'
import { Loader2, MapPin, AlertCircle } from 'lucide-react'

export default function LocationAutocomplete({ onSelect, error }) {
  const [query, setQuery] = useState('')
  const [suggestions, setSuggestions] = useState([])
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState(null)
  const [open, setOpen] = useState(false)
  const [selected, setSelected] = useState(false)

  const debounceTimer = useRef(null)
  const containerRef = useRef(null)

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const fetchSuggestions = useCallback(async (q) => {
    setLoading(true)
    setApiError(null)
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(q)}&limit=6`,
        { headers: { 'Accept-Language': 'en' } }
      )
      if (!res.ok) throw new Error('Failed to fetch suggestions')
      const data = await res.json()
      setSuggestions(data)
      setOpen(true)
    } catch {
      setApiError('Could not load suggestions. Check your connection.')
      setSuggestions([])
    } finally {
      setLoading(false)
    }
  }, [])

  const handleInputChange = (e) => {
    const val = e.target.value
    setQuery(val)
    setSelected(false)

    if (debounceTimer.current) clearTimeout(debounceTimer.current)

    if (val.trim().length < 3) {
      setSuggestions([])
      setOpen(false)
      return
    }

    debounceTimer.current = setTimeout(() => fetchSuggestions(val.trim()), 300)
  }

  const handleSelect = (place) => {
    setQuery(place.display_name)
    setSelected(true)
    setOpen(false)
    setSuggestions([])
    onSelect({
      name: place.display_name,
      latitude: parseFloat(place.lat),
      longitude: parseFloat(place.lon),
    })
  }

  const handleClear = () => {
    setQuery('')
    setSelected(false)
    setSuggestions([])
    setOpen(false)
    onSelect({ name: '', latitude: '', longitude: '' })
  }

  return (
    <div ref={containerRef} className="relative">
      <div className="relative">
        <MapPin className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <input
          type="text"
          value={query}
          onChange={handleInputChange}
          onFocus={() => suggestions.length > 0 && setOpen(true)}
          placeholder="Search for a location…"
          autoComplete="off"
          className={cn(
            'flex h-10 w-full rounded-md border border-input bg-background py-2 pl-9 pr-8 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
            error && 'border-destructive focus-visible:ring-destructive'
          )}
        />
        <div className="absolute right-3 top-1/2 -translate-y-1/2">
          {loading
            ? <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
            : query && (
              <button
                type="button"
                onClick={handleClear}
                className="text-muted-foreground hover:text-foreground text-lg leading-none"
                aria-label="Clear location"
              >
                ×
              </button>
            )
          }
        </div>
      </div>

      {open && (
        <ul className="absolute z-50 mt-1 max-h-60 w-full overflow-auto rounded-md border border-border bg-background shadow-md text-sm">
          {apiError && (
            <li className="flex items-center gap-2 px-3 py-2 text-destructive">
              <AlertCircle className="h-4 w-4 shrink-0" />
              {apiError}
            </li>
          )}
          {!apiError && suggestions.length === 0 && !loading && (
            <li className="px-3 py-2 text-muted-foreground">No results found.</li>
          )}
          {suggestions.map((place) => (
            <li
              key={place.place_id}
              onMouseDown={() => handleSelect(place)}
              className="cursor-pointer px-3 py-2 hover:bg-accent hover:text-accent-foreground"
            >
              {place.display_name}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
