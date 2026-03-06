import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'

// Pages
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import TenantDashboard from './pages/TenantDashboard'
import ToolsPage from './pages/ToolsPage'
import AddToolPage from './pages/AddToolPage'
import StorefrontPage from './pages/StorefrontPage'
import ToolDetail from './pages/ToolDetail'
import BookingsPage from './pages/BookingsPage'
import MyBookingsPage from './pages/MyBookingsPage'
import CheckoutPage from './pages/CheckoutPage'

function PrivateRoute({ children, adminOnly = false }) {
    const { user, isAdmin } = useAuth()
    if (!user) return <Navigate to="/login" replace />
    if (adminOnly && !isAdmin()) return <Navigate to="/my-bookings" replace />
    return children
}

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* Public */}
                <Route path="/" element={<Navigate to="/login" replace />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/store/:subdomain" element={<StorefrontPage />} />
                <Route path="/store/:subdomain/tools/:toolId" element={<ToolDetail />} />

                {/* Admin routes */}
                <Route path="/dashboard" element={
                    <PrivateRoute adminOnly><TenantDashboard /></PrivateRoute>
                } />
                <Route path="/tools" element={
                    <PrivateRoute adminOnly><ToolsPage /></PrivateRoute>
                } />
                <Route path="/tools/new" element={
                    <PrivateRoute adminOnly><AddToolPage /></PrivateRoute>
                } />
                <Route path="/tools/:id/edit" element={
                    <PrivateRoute adminOnly><AddToolPage /></PrivateRoute>
                } />
                <Route path="/bookings" element={
                    <PrivateRoute adminOnly><BookingsPage /></PrivateRoute>
                } />

                {/* Customer routes */}
                <Route path="/my-bookings" element={
                    <PrivateRoute><MyBookingsPage /></PrivateRoute>
                } />
                <Route path="/checkout/:bookingId" element={
                    <PrivateRoute><CheckoutPage /></PrivateRoute>
                } />

                {/* Fallback */}
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    )
}
