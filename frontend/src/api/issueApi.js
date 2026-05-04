import axios from 'axios'
import keycloak from '@/keycloak'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  if (keycloak.authenticated && keycloak.token) {
    config.headers.Authorization = `Bearer ${keycloak.token}`
  }
  return config
})

export const getAllIssues = (page = 0, size = 10, location = null) => {
  const params = new URLSearchParams({ page, size, sort: 'createdAt,desc' })
  if (location?.latitude != null && location?.longitude != null) {
    params.set('lat', location.latitude)
    params.set('lng', location.longitude)
    if (location.radius != null) params.set('radius', location.radius)
  }
  return api.get(`/issues?${params}`)
}

export const getTrendingIssues = (page = 0, size = 10, location = null) => {
  const params = new URLSearchParams({ page, size })
  if (location?.latitude != null && location?.longitude != null) {
    params.set('lat', location.latitude)
    params.set('lng', location.longitude)
    if (location.radius != null) params.set('radius', location.radius)
  }
  return api.get(`/issues/trending?${params}`)
}

export const getIssueById = (id) =>
  api.get(`/issues/${id}`)

export const createIssue = (data) =>
  api.post('/issues', data)

export const getComments = (issueId, page = 0, size = 20) => {
  const params = new URLSearchParams({ page, size })
  return api.get(`/issues/${issueId}/comments?${params}`)
}

export const addComment = (issueId, content) =>
  api.post(`/issues/${issueId}/comments`, { content })

export const getMyIssues = (page = 0, size = 10) => {
  const params = new URLSearchParams({ page, size, sort: 'createdAt,desc' })
  return api.get(`/issues/my-posts?${params}`)
}

export const toggleVote = (issueId) =>
  api.post(`/issues/${issueId}/vote`)
