import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getTools, getBookings } from '../api/api'
import { Wrench, Calendar, DollarSign, Plus, LogOut, BarChart2 } from 'lucide-react'

function StatCard({ icon: Icon, label, value, color = 'brand' }) {
    return (
        <div className="stat-card">
            <div className={`w-10 h-10 rounded-xl bg-${color}-500/20 flex items-center justify-center mb-3`}>
                <Icon size={20} className={`text-${color}-400`} />
            </div>
            <p className="text-gray-400 text-sm">{label}</p>
            <p className="text-2xl font-bold text-white mt-1">{value}</p>
        </div>
    )
}

export default function TenantDashboard() {
    const { user, logout } = useAuth()
    const navigate = useNavigate()
    const [stats, setStats] = useState({ tools: 0, bookings: 0, revenue: 0 })
    const [bookings, setBookings] = useState([])

    useEffect(() => {
        async function load() {
            try {
                const [toolsRes, bookingsRes] = await Promise.all([getTools(true), getBookings()])
                const allBookings = bookingsRes.data
                const revenue = allBookings
                    .filter((b) => b.status !== 'CANCELLED')
                    .reduce((sum, b) => sum + b.totalAmount, 0)
                setStats({ tools: toolsRes.data.length, bookings: allBookings.length, revenue })
                setBookings(allBookings.slice(0, 5))
            } catch (e) {
                console.error(e)
            }
        }
        load()
    }, [])

    function statusBadge(status) {
        const classes = {
            PENDING: 'badge-pending', CONFIRMED: 'badge-confirmed',
            COMPLETED: 'badge-completed', CANCELLED: 'badge-cancelled'
        }
        return <span className={classes[status] || 'badge-pending'}>{status}</span>
    }

    return (
        <div className="min-h-screen bg-gray-950">
            {/* Sidebar */}
            <div className="flex">
                <aside className="w-60 min-h-screen bg-gray-900 border-r border-gray-800 flex flex-col p-5 fixed">
                    <div className="flex items-center gap-2 mb-8">
                        <div className="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center">
                            <Wrench size={16} className="text-white" />
                        </div>
                        <span className="font-bold text-white">ToolRent</span>
                    </div>
                    <nav className="flex flex-col gap-1 flex-1">
                        <Link to="/dashboard" className="flex items-center gap-3 px-3 py-2 rounded-lg bg-brand-600/20 text-brand-400 text-sm font-medium">
                            <BarChart2 size={16} /> Dashboard
                        </Link>
                        <Link to="/tools" className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-400 hover:bg-gray-800 text-sm font-medium transition-colors">
                            <Wrench size={16} /> Tools
                        </Link>
                        <Link to="/bookings" className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-400 hover:bg-gray-800 text-sm font-medium transition-colors">
                            <Calendar size={16} /> Bookings
                        </Link>
                    </nav>
                    <button onClick={() => { logout(); navigate('/login') }}
                        className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-500 hover:text-red-400 text-sm transition-colors">
                        <LogOut size={16} /> Sign Out
                    </button>
                </aside>

                {/* Main */}
                <main className="ml-60 flex-1 p-8">
                    <div className="flex items-center justify-between mb-8">
                        <div>
                            <h1 className="text-2xl font-bold text-white">{user?.businessName}</h1>
                            <p className="text-gray-400 text-sm mt-1">{user?.subdomain}.toolrent.in</p>
                        </div>
                        <Link to="/tools/new" className="btn-primary flex items-center gap-2">
                            <Plus size={16} /> Add Tool
                        </Link>
                    </div>

                    {/* Stats */}
                    <div className="grid grid-cols-3 gap-4 mb-8">
                        <StatCard icon={Wrench} label="Total Tools" value={stats.tools} />
                        <StatCard icon={Calendar} label="Total Bookings" value={stats.bookings} color="purple" />
                        <StatCard icon={DollarSign} label="Total Revenue" value={`₹${stats.revenue.toLocaleString('en-IN')}`} color="green" />
                    </div>

                    {/* Recent bookings */}
                    <div className="card p-6">
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="font-semibold text-white">Recent Bookings</h2>
                            <Link to="/bookings" className="text-brand-400 text-sm hover:underline">View all →</Link>
                        </div>
                        {bookings.length === 0 ? (
                            <p className="text-gray-500 text-sm">No bookings yet. Share your storefront!</p>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="w-full text-sm">
                                    <thead>
                                        <tr className="border-b border-gray-800">
                                            <th className="text-left text-gray-400 pb-3 font-medium">Customer</th>
                                            <th className="text-left text-gray-400 pb-3 font-medium">Tool</th>
                                            <th className="text-left text-gray-400 pb-3 font-medium">Dates</th>
                                            <th className="text-left text-gray-400 pb-3 font-medium">Amount</th>
                                            <th className="text-left text-gray-400 pb-3 font-medium">Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {bookings.map((b) => (
                                            <tr key={b.id} className="border-b border-gray-800/50">
                                                <td className="py-3 text-gray-300">{b.customer?.email}</td>
                                                <td className="py-3 text-gray-300">{b.tool?.name}</td>
                                                <td className="py-3 text-gray-400">{b.startDate} – {b.endDate}</td>
                                                <td className="py-3 text-white font-medium">₹{b.totalAmount?.toLocaleString('en-IN')}</td>
                                                <td className="py-3">{statusBadge(b.status)}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </main>
            </div>
        </div>
    )
}
