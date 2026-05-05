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

export const getMe = () => api.get('/user/me')

export const updateBio = (bio) => api.patch('/user/me/bio', { bio })

export const getUserProfile = (userId) => api.get(`/user/${userId}`)

export const getUserIssues = (userId, page = 0, size = 10) => {
  const params = new URLSearchParams({ page, size })
  return api.get(`/user/${userId}/issues?${params}`)
}

export const getFollowStatus = (userId) => api.get(`/user/${userId}/follow-status`)

export const followUser = (userId) => api.post(`/user/${userId}/follow`)

export const unfollowUser = (userId) => api.delete(`/user/${userId}/follow`)

export const getFollowers = (userId) => api.get(`/user/${userId}/followers`)

export const getFollowing = (userId) => api.get(`/user/${userId}/following`)
