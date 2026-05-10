import axios from 'axios'

const BASE = 'http://localhost:8085'
const OWNER = 'Nikhil-keshri2213'
const REPO  = 'web-servers'
const PR    = 1

export const fetchStats = () =>
  axios.get(`${BASE}/api/dashboard/${OWNER}/${REPO}/stats`).then(r => r.data.data)

export const fetchTrends = () =>
  axios.get(`${BASE}/api/dashboard/${OWNER}/${REPO}/trends`).then(r => r.data.data)

export const fetchDevelopers = () =>
  axios.get(`${BASE}/api/dashboard/${OWNER}/${REPO}/developers`).then(r => r.data.data)

export const fetchReviews = () =>
  axios.get(`${BASE}/api/reviews/${OWNER}/${REPO}/pulls/${PR}`).then(r => r.data.data)