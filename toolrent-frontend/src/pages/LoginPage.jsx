import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { login } from '../api/api'
import toast from 'react-hot-toast'
import { Wrench } from 'lucide-react'

export default function LoginPage() {
    const { setAuth, isAdmin } = useAuth()
    const navigate = useNavigate()
    const [form, setForm] = useState({ email: '', password: '' })
    const [loading, setLoading] = useState(false)

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)
        try {
            const { data } = await login(form)
            setAuth(data)
            toast.success('Welcome back!')
            navigate(data.role === 'TENANT_ADMIN' ? '/dashboard' : '/my-bookings')
        } catch (err) {
            toast.error(err.response?.data?.message || 'Invalid credentials')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-950 px-4">
            <div className="card p-8 w-full max-w-md animate-slide-up">
                <div className="flex items-center gap-3 mb-8">
                    <div className="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center">
                        <Wrench size={20} className="text-white" />
                    </div>
                    <span className="text-xl font-bold text-white">ToolRent</span>
                </div>

                <h1 className="text-2xl font-bold text-white mb-1">Sign in</h1>
                <p className="text-gray-400 mb-6 text-sm">Access your rental dashboard</p>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="label">Email</label>
                        <input type="email" className="input" placeholder="you@example.com"
                            value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
                    </div>
                    <div>
                        <label className="label">Password</label>
                        <input type="password" className="input" placeholder="Your password"
                            value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
                    </div>
                    <button type="submit" className="btn-primary w-full mt-2" disabled={loading}>
                        {loading ? 'Signing in...' : 'Sign In →'}
                    </button>
                </form>

                <p className="text-center text-gray-500 text-sm mt-5">
                    Don't have a store?{' '}
                    <Link to="/register" className="text-brand-400 hover:underline">Register free</Link>
                </p>
            </div>
        </div>
    )
}
