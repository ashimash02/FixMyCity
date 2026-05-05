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

export const getNotifications = () => api.get('/notifications')

export const getUnreadCount = () => api.get('/notifications/unread-count')

export const markAsRead = (id) => api.patch(`/notifications/${id}/read`)

export const markAllAsRead = () => api.patch('/notifications/read-all')
