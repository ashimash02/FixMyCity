import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

export const getAllIssues = (page = 0, size = 10) =>
  api.get(`/issues?page=${page}&size=${size}&sort=createdAt,desc`)

export const getIssueById = (id) =>
  api.get(`/issues/${id}`)

export const createIssue = (data) =>
  api.post('/issues', data)

export const addVote = (issueId, userId) =>
  api.post(`/issues/${issueId}/vote`, { userId })
