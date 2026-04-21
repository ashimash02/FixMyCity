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

export const getAllIssues = (page = 0, size = 10) =>
  api.get(`/issues?page=${page}&size=${size}&sort=createdAt,desc`)

export const getIssueById = (id) =>
  api.get(`/issues/${id}`)

export const createIssue = (data) =>
  api.post('/issues', data)

export const addVote = (issueId) =>
  api.post(`/issues/${issueId}/vote`)
