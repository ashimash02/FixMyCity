import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createIssue } from '@/api/issueApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card'
import { AlertCircle, CheckCircle2, Loader2 } from 'lucide-react'
import LocationAutocomplete from '@/components/LocationAutocomplete'
import ImageUpload from '@/components/ImageUpload'

const CATEGORIES = ['INFRASTRUCTURE', 'SANITATION', 'SAFETY', 'ENVIRONMENT', 'NOISE', 'OTHER']

const initialForm = {
  title: '',
  description: '',
  locationName: '',
  latitude: '',
  longitude: '',
  category: '',
  imageUrl: '',
}

export default function ReportIssuePage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [success, setSuccess] = useState(false)

  const validate = () => {
    const e = {}
    if (!form.title.trim()) e.title = 'Title is required'
    if (!form.category) e.category = 'Category is required'
    if (!form.latitude || !form.longitude) e.location = 'Please select a location from the suggestions'
    return e
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: undefined }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const validationErrors = validate()
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors)
      return
    }

    setSubmitting(true)
    try {
      const payload = {
        title: form.title,
        description: form.description,
        category: form.category,
        imageUrl: form.imageUrl,
        latitude: form.latitude,
        longitude: form.longitude,
      }
      const { data } = await createIssue(payload)
      setSuccess(true)
      setTimeout(() => navigate(`/issues/${data.id}`), 1200)
    } catch (err) {
      const serverErrors = err.response?.data?.errors
      if (serverErrors) {
        setErrors(serverErrors)
      } else {
        setErrors({ general: 'Something went wrong. Please try again.' })
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-xl px-4 py-8">
      <Card>
        <CardHeader>
          <CardTitle>Report an Issue</CardTitle>
          <CardDescription>Describe the problem and its location</CardDescription>
        </CardHeader>
        <CardContent>
          {success && (
            <div className="mb-4 flex items-center gap-2 rounded-lg bg-green-50 p-3 text-sm text-green-700 border border-green-200">
              <CheckCircle2 className="h-4 w-4" />
              Issue reported! Redirecting…
            </div>
          )}
          {errors.general && (
            <div className="mb-4 flex items-center gap-2 rounded-lg bg-destructive/10 p-3 text-sm text-destructive border border-destructive/20">
              <AlertCircle className="h-4 w-4" />
              {errors.general}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="title">Title *</Label>
              <Input
                id="title"
                name="title"
                placeholder="e.g. Broken streetlight on Park Road"
                value={form.title}
                onChange={handleChange}
              />
              {errors.title && <p className="text-xs text-destructive">{errors.title}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                name="description"
                placeholder="Provide more details about the issue…"
                value={form.description}
                onChange={handleChange}
              />
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="category">Category *</Label>
              <select
                id="category"
                name="category"
                value={form.category}
                onChange={handleChange}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <option value="">Select a category</option>
                {CATEGORIES.map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
              {errors.category && <p className="text-xs text-destructive">{errors.category}</p>}
            </div>

            <div className="space-y-1.5">
              <Label>Location *</Label>
              <LocationAutocomplete
                error={!!errors.location}
                onSelect={({ name, latitude, longitude }) => {
                  setForm((prev) => ({ ...prev, locationName: name, latitude, longitude }))
                  if (errors.location) setErrors((prev) => ({ ...prev, location: undefined }))
                }}
              />
              {errors.location && <p className="text-xs text-destructive">{errors.location}</p>}
              {form.latitude && form.longitude && (
                <p className="text-xs text-muted-foreground">
                  {form.latitude.toFixed(5)}, {form.longitude.toFixed(5)}
                </p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label>Image (optional)</Label>
              <ImageUpload
                onUpload={(url) => setForm((prev) => ({ ...prev, imageUrl: url }))}
                onClear={() => setForm((prev) => ({ ...prev, imageUrl: '' }))}
              />
            </div>

            <Button type="submit" className="w-full" disabled={submitting}>
              {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {submitting ? 'Submitting…' : 'Submit Issue'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
