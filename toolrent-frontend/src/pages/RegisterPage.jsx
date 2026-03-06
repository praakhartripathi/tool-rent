import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { registerTenant } from '../api/api'
import toast from 'react-hot-toast'
import { Wrench, Eye, EyeOff } from 'lucide-react'

export default function RegisterPage() {
    const { setAuth } = useAuth()
    const navigate = useNavigate()
    const [form, setForm] = useState({
        businessName: '', subdomain: '', email: '', password: '', fullName: ''
    })
    const [loading, setLoading] = useState(false)

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)
        try {
            const { data } = await registerTenant(form)
            setAuth(data)
            toast.success(`Welcome, ${data.businessName}! 🎉`)
            navigate('/dashboard')
        } catch (err) {
            toast.error(err.response?.data?.message || 'Registration failed')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-950 px-4">
            <div className="card p-8 w-full max-w-md animate-slide-up">
                {/* Logo */}
                <div className="flex items-center gap-3 mb-8">
                    <div className="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center">
                        <Wrench size={20} className="text-white" />
                    </div>
                    <span className="text-xl font-bold text-white">ToolRent</span>
                </div>

                <h1 className="text-2xl font-bold text-white mb-1">Create your store</h1>
                <p className="text-gray-400 mb-6 text-sm">Register your rental business in minutes</p>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="label">Business Name</label>
                        <input name="businessName" className="input" placeholder="Sharma Tool Rentals"
                            value={form.businessName} onChange={handleChange} required />
                    </div>
                    <div>
                        <label className="label">Subdomain</label>
                        <div className="flex items-center gap-1">
                            <input name="subdomain" className="input" placeholder="sharma"
                                value={form.subdomain} onChange={handleChange} required />
                            <span className="text-gray-500 text-sm whitespace-nowrap">.toolrent.in</span>
                        </div>
                    </div>
                    <div>
                        <label className="label">Your Name</label>
                        <input name="fullName" className="input" placeholder="Rajesh Sharma"
                            value={form.fullName} onChange={handleChange} />
                    </div>
                    <div>
                        <label className="label">Email</label>
                        <input name="email" type="email" className="input" placeholder="you@example.com"
                            value={form.email} onChange={handleChange} required />
                    </div>
                    <div>
                        <label className="label">Password</label>
                        <input name="password" type="password" className="input" placeholder="Min 8 characters"
                            value={form.password} onChange={handleChange} required minLength={8} />
                    </div>

                    <button type="submit" className="btn-primary w-full mt-2" disabled={loading}>
                        {loading ? 'Creating store...' : 'Create Store →'}
                    </button>
                </form>

                <p className="text-center text-gray-500 text-sm mt-5">
                    Already have a store?{' '}
                    <Link to="/login" className="text-brand-400 hover:underline">Sign in</Link>
                </p>
            </div>
        </div>
    )
}
