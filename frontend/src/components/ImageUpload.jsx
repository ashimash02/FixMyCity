import { useRef, useState } from 'react'
import axios from 'axios'
import { Loader2, ImageIcon, X } from 'lucide-react'

const CLOUDINARY_URL = 'https://api.cloudinary.com/v1_1/dmoix39fv/image/upload'
const UPLOAD_PRESET = 'issue_tracker_upload'
const MAX_SIZE_BYTES = 5 * 1024 * 1024 // 5 MB
const ACCEPTED = ['image/jpeg', 'image/png']

export default function ImageUpload({ onUpload, onClear }) {
  const [preview, setPreview] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState(null)
  const inputRef = useRef(null)

  const handleFile = async (file) => {
    setError(null)

    if (!ACCEPTED.includes(file.type)) {
      setError('Only JPG and PNG files are allowed.')
      return
    }
    if (file.size > MAX_SIZE_BYTES) {
      setError('File must be 5 MB or smaller.')
      return
    }

    setPreview(URL.createObjectURL(file))
    setUploading(true)

    const formData = new FormData()
    formData.append('file', file)
    formData.append('upload_preset', UPLOAD_PRESET)

    try {
      const { data } = await axios.post(CLOUDINARY_URL, formData)
      onUpload(data.secure_url)
    } catch (err) {
      const msg = err.response?.data?.error?.message
      console.error('Cloudinary upload error:', err.response?.data ?? err.message)
      setError(msg ? `Upload failed: ${msg}` : 'Upload failed. Please try again.')
      setPreview(null)
      onUpload('')
    } finally {
      setUploading(false)
    }
  }

  const handleChange = (e) => {
    const file = e.target.files?.[0]
    if (file) handleFile(file)
  }

  const handleClear = () => {
    setPreview(null)
    setError(null)
    if (inputRef.current) inputRef.current.value = ''
    onClear()
  }

  return (
    <div className="space-y-2">
      {!preview ? (
        <label className="flex cursor-pointer flex-col items-center justify-center gap-2 rounded-md border border-dashed border-input bg-background px-4 py-6 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground">
          <ImageIcon className="h-6 w-6" />
          <span>Click to upload an image (JPG, PNG · max 5 MB)</span>
          <input
            ref={inputRef}
            type="file"
            accept=".jpg,.jpeg,.png"
            className="sr-only"
            onChange={handleChange}
          />
        </label>
      ) : (
        <div className="relative w-full overflow-hidden rounded-md border border-border">
          <img
            src={preview}
            alt="Preview"
            className="max-h-48 w-full object-cover"
          />
          {uploading && (
            <div className="absolute inset-0 flex items-center justify-center bg-black/40">
              <Loader2 className="h-6 w-6 animate-spin text-white" />
            </div>
          )}
          {!uploading && (
            <button
              type="button"
              onClick={handleClear}
              className="absolute right-2 top-2 rounded-full bg-black/60 p-1 text-white hover:bg-black/80"
              aria-label="Remove image"
            >
              <X className="h-4 w-4" />
            </button>
          )}
        </div>
      )}

      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  )
}
