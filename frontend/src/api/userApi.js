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
