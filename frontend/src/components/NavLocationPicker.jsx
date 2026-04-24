import { useRef, useState, useEffect, useCallback } from 'react'
import { MapPin, ChevronDown, Loader2, X, AlertCircle } from 'lucide-react'
import { useLocationContext } from '@/context/LocationContext'

function shortName(displayName) {
  // Show only the first segment (city/area) for the navbar button
  return displayName?.split(',')[0]?.trim() ?? displayName
}

export default function NavLocationPicker() {
  const { location, setLocation, clearLocation } = useLocationContext()
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const [suggestions, setSuggestions] = useState([])
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState(null)

  const containerRef = useRef(null)
  const inputRef = useRef(null)
  const debounceTimer = useRef(null)

  // Close on outside click
  useEffect(() => {
    const handler = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  // Focus input when dropdown opens
  useEffect(() => {
    if (open) {
      setQuery('')
      setSuggestions([])
      setApiError(null)
      setTimeout(() => inputRef.current?.focus(), 50)
    }
  }, [open])

  const fetchSuggestions = useCallback(async (q) => {
    setLoading(true)
    setApiError(null)
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(q)}&limit=6`,
        { headers: { 'Accept-Language': 'en' } }
      )
      if (!res.ok) throw new Error()
      setSuggestions(await res.json())
    } catch {
      setApiError('Could not load suggestions.')
      setSuggestions([])
    } finally {
      setLoading(false)
    }
  }, [])

  const handleQueryChange = (e) => {
    const val = e.target.value
    setQuery(val)
    if (debounceTimer.current) clearTimeout(debounceTimer.current)
    if (val.trim().length < 3) { setSuggestions([]); return }
    debounceTimer.current = setTimeout(() => fetchSuggestions(val.trim()), 300)
  }

  const handleSelect = (place) => {
    setLocation({ name: place.display_name, latitude: parseFloat(place.lat), longitude: parseFloat(place.lon) })
    setOpen(false)
  }

  const handleClear = () => {
    clearLocation()
    setOpen(false)
  }

  return (
    <div ref={containerRef} className="relative">
      {/* Trigger button */}
      <button
        onClick={() => setOpen((o) => !o)}
        className="flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-medium transition-colors hover:bg-accent max-w-[180px]"
      >
        <MapPin className="h-4 w-4 shrink-0 text-primary" />
        <span className="truncate text-foreground">
          {location ? shortName(location.name) : 'All locations'}
        </span>
        <ChevronDown className={`h-3.5 w-3.5 shrink-0 text-muted-foreground transition-transform ${open ? 'rotate-180' : ''}`} />
      </button>

      {/* Dropdown panel */}
      {open && (
        <div className="absolute left-0 top-full mt-2 w-80 rounded-xl border bg-white shadow-lg z-[60]">
          <div className="p-3 border-b">
            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-2">
              Filter by location
            </p>

            {/* Search input */}
            <div className="relative">
              <MapPin className="pointer-events-none absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
              <input
                ref={inputRef}
                type="text"
                value={query}
                onChange={handleQueryChange}
                placeholder="Search city or area…"
                autoComplete="off"
                className="w-full rounded-md border border-input bg-background py-1.5 pl-8 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
              />
              {loading && (
                <Loader2 className="absolute right-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 animate-spin text-muted-foreground" />
              )}
              {!loading && query && (
                <button
                  onMouseDown={(e) => { e.preventDefault(); setQuery(''); setSuggestions([]) }}
                  className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  <X className="h-3.5 w-3.5" />
                </button>
              )}
            </div>
          </div>

          {/* Suggestions */}
          <ul className="max-h-52 overflow-y-auto">
            {/* Clear filter option — shown when a location is active */}
            {location && (
              <li>
                <button
                  onMouseDown={handleClear}
                  className="flex w-full items-center gap-2 px-3 py-2.5 text-sm text-destructive hover:bg-destructive/5 transition-colors"
                >
                  <X className="h-3.5 w-3.5" />
                  Clear filter — show all locations
                </button>
              </li>
            )}

            {apiError && (
              <li className="flex items-center gap-2 px-3 py-2.5 text-sm text-destructive">
                <AlertCircle className="h-3.5 w-3.5" />
                {apiError}
              </li>
            )}

            {!apiError && query.length >= 3 && suggestions.length === 0 && !loading && (
              <li className="px-3 py-2.5 text-sm text-muted-foreground">No results found.</li>
            )}

            {!location && !query && suggestions.length === 0 && !apiError && (
              <li className="px-3 py-2.5 text-sm text-muted-foreground">
                Type to search for a city or area
              </li>
            )}

            {suggestions.map((place) => (
              <li key={place.place_id}>
                <button
                  onMouseDown={() => handleSelect(place)}
                  className="flex w-full items-start gap-2 px-3 py-2.5 text-sm hover:bg-accent transition-colors text-left"
                >
                  <MapPin className="h-3.5 w-3.5 mt-0.5 shrink-0 text-muted-foreground" />
                  <span className="line-clamp-2">{place.display_name}</span>
                </button>
              </li>
            ))}
          </ul>

          {/* Active filter indicator */}
          {location && (
            <div className="border-t px-3 py-2 bg-primary/5">
              <p className="text-xs text-primary font-medium truncate">
                Showing: {shortName(location.name)}
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
