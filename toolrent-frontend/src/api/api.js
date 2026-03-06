import axios from 'axios'

const api = axios.create({
    baseURL: '/api',
    headers: { 'Content-Type': 'application/json' },
})

// Attach JWT token to every request
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken')
    if (token) config.headers.Authorization = `Bearer ${token}`

    // Multi-tenant: attach subdomain header
    const subdomain = localStorage.getItem('subdomain')
    if (subdomain) config.headers['X-Tenant-ID'] = subdomain

    return config
})

// Auto-logout on 401
api.interceptors.response.use(
    (res) => res,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.clear()
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

// ─── Auth ────────────────────────────────────────────
export const registerTenant = (data) => api.post('/auth/register-tenant', data)
export const login = (data) => api.post('/auth/login', data)

// ─── Tools ───────────────────────────────────────────
export const getTools = (adminView = false) => api.get(`/tools?adminView=${adminView}`)
export const getToolById = (id) => api.get(`/tools/${id}`)
export const createTool = (data) => api.post('/tools', data)
export const updateTool = (id, data) => api.put(`/tools/${id}`, data)
export const deleteTool = (id) => api.delete(`/tools/${id}`)

// ─── Bookings ─────────────────────────────────────────
export const getBookings = (myBookings = false) => api.get(`/bookings?myBookings=${myBookings}`)
export const createBooking = (data) => api.post('/bookings', data)
export const confirmBooking = (id) => api.put(`/bookings/${id}/confirm`)
export const cancelBooking = (id) => api.put(`/bookings/${id}/cancel`)
export const completeBooking = (id) => api.put(`/bookings/${id}/complete`)

// ─── Payments ─────────────────────────────────────────
export const createPaymentOrder = (bookingId) => api.post('/payments/create-order', { bookingId })
export const verifyPayment = (data) => api.post('/payments/verify', data)

export default api
