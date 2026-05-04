import { useEffect, useRef, useState } from 'react'
import { Loader2, AlertCircle, CheckCircle2, User, Mail, FileText } from 'lucide-react'
import { getMe, updateBio } from '@/api/userApi'
import { Button } from '@/components/ui/button'

export default function SettingsPage() {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [bio, setBio] = useState('')
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const savedTimer = useRef(null)

  useEffect(() => {
    getMe()
      .then(({ data }) => {
        setProfile(data)
        setBio(data.bio ?? '')
      })
      .catch(() => setError('Failed to load profile.'))
      .finally(() => setLoading(false))

    return () => clearTimeout(savedTimer.current)
  }, [])

  const handleSave = async () => {
    setSaving(true)
    setSaved(false)
    try {
      const { data } = await updateBio(bio)
      setProfile(data)
      setSaved(true)
      savedTimer.current = setTimeout(() => setSaved(false), 3000)
    } catch {
      setError('Failed to save bio.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (error && !profile) {
    return (
      <div className="mx-auto max-w-xl px-4 py-8">
        <div className="flex items-center gap-2 rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          <AlertCircle className="h-4 w-4 shrink-0" />
          {error}
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-xl px-4 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Settings</h1>
        <p className="mt-1 text-sm text-muted-foreground">Your account information</p>
      </div>

      <div className="rounded-xl border bg-white shadow-sm divide-y">
        {/* Read-only fields */}
        <div className="px-6 py-4 flex items-center gap-3">
          <User className="h-4 w-4 shrink-0 text-muted-foreground" />
          <div>
            <p className="text-xs text-muted-foreground">Username</p>
            <p className="text-sm font-medium">{profile.username}</p>
          </div>
        </div>

        <div className="px-6 py-4 flex items-center gap-3">
          <Mail className="h-4 w-4 shrink-0 text-muted-foreground" />
          <div>
            <p className="text-xs text-muted-foreground">Email</p>
            <p className="text-sm font-medium">{profile.email ?? '—'}</p>
          </div>
        </div>

        {/* Editable bio */}
        <div className="px-6 py-4">
          <label className="flex items-center gap-2 text-xs text-muted-foreground mb-2">
            <FileText className="h-4 w-4" />
            Bio
          </label>
          <textarea
            value={bio}
            onChange={(e) => setBio(e.target.value)}
            rows={3}
            maxLength={300}
            placeholder="Tell the community a little about yourself..."
            className="w-full resize-none rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40"
          />
          <div className="mt-1 flex items-center justify-between">
            <span className="text-xs text-muted-foreground">{bio.length}/300</span>
            {error && saved === false && (
              <span className="text-xs text-destructive">{error}</span>
            )}
          </div>

          <div className="mt-3 flex items-center gap-3">
            <Button onClick={handleSave} disabled={saving} size="sm">
              {saving && <Loader2 className="mr-1.5 h-3.5 w-3.5 animate-spin" />}
              Save Bio
            </Button>
            {saved && (
              <span className="flex items-center gap-1 text-xs text-green-600">
                <CheckCircle2 className="h-3.5 w-3.5" />
                Saved
              </span>
            )}
          </div>
        </div>
      </div>

      <p className="mt-4 text-xs text-muted-foreground">
        Username and email are managed by your Keycloak account. To change them, contact your administrator.
      </p>
    </div>
  )
}
