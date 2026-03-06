import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getBookings, confirmBooking, cancelBooking, completeBooking } from '../api/api'
import toast from 'react-hot-toast'
import { Wrench, Calendar, BarChart2, LogOut, Check, X, CheckCheck } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function BookingsPage() {
    const { logout } = useAuth()
    const navigate = useNavigate()
    const [bookings, setBookings] = useState([])
    const [filter, setFilter] = useState('ALL')
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        getBookings().then(r => setBookings(r.data)).finally(() => setLoading(false))
    }, [])

    const action = async (fn, id, label) => {
        try {
            const { data } = await fn(id)
            setBookings(bks => bks.map(b => b.id === id ? data : b))
            toast.success(`Booking ${label}`)
        } catch { toast.error('Action failed') }
    }

    function statusBadge(status) {
        const map = { PENDING: 'badge-pending', CONFIRMED: 'badge-confirmed', COMPLETED: 'badge-completed', CANCELLED: 'badge-cancelled' }
        return <span className={map[status]}>{status}</span>
    }

    const filtered = filter === 'ALL' ? bookings : bookings.filter(b => b.status === filter)

    return (
        <div className="min-h-screen bg-gray-950 flex">
            <aside className="w-60 min-h-screen bg-gray-900 border-r border-gray-800 flex flex-col p-5 fixed">
                <div className="flex items-center gap-2 mb-8">
                    <div className="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center"><Wrench size={16} className="text-white" /></div>
                    <span className="font-bold text-white">ToolRent</span>
                </div>
                <nav className="flex flex-col gap-1 flex-1">
                    <Link to="/dashboard" className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-400 hover:bg-gray-800 text-sm font-medium transition-colors"><BarChart2 size={16} /> Dashboard</Link>
                    <Link to="/tools" className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-400 hover:bg-gray-800 text-sm font-medium transition-colors"><Wrench size={16} /> Tools</Link>
                    <Link to="/bookings" className="flex items-center gap-3 px-3 py-2 rounded-lg bg-brand-600/20 text-brand-400 text-sm font-medium"><Calendar size={16} /> Bookings</Link>
                </nav>
                <button onClick={() => { logout(); navigate('/login') }} className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-500 hover:text-red-400 text-sm transition-colors">
                    <LogOut size={16} /> Sign Out
                </button>
            </aside>

            <main className="ml-60 flex-1 p-8">
                <h1 className="text-2xl font-bold text-white mb-6">Bookings</h1>

                {/* Filter tabs */}
                <div className="flex gap-2 mb-5">
                    {['ALL', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'].map(s => (
                        <button key={s} onClick={() => setFilter(s)}
                            className={`px-4 py-1.5 rounded-lg text-sm font-medium transition-colors ${filter === s ? 'bg-brand-600 text-white' : 'bg-gray-800 text-gray-400 hover:text-white'}`}>
                            {s}
                        </button>
                    ))}
                </div>

                {loading ? (
                    <div className="text-gray-500">Loading...</div>
                ) : filtered.length === 0 ? (
                    <div className="card p-12 text-center text-gray-500">No bookings found.</div>
                ) : (
                    <div className="card overflow-hidden">
                        <table className="w-full text-sm">
                            <thead className="border-b border-gray-800">
                                <tr>
                                    {['Customer', 'Tool', 'Dates', 'Days', 'Amount', 'Status', 'Actions'].map(h => (
                                        <th key={h} className="text-left text-gray-400 p-4 font-medium">{h}</th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map(b => (
                                    <tr key={b.id} className="border-b border-gray-800/50 hover:bg-gray-800/20 transition-colors">
                                        <td className="p-4 text-gray-300">{b.customer?.email}</td>
                                        <td className="p-4 text-white font-medium">{b.tool?.name}</td>
                                        <td className="p-4 text-gray-400 text-xs">{b.startDate}<br />{b.endDate}</td>
                                        <td className="p-4 text-gray-400">{b.totalDays}d</td>
                                        <td className="p-4 text-white font-semibold">₹{b.totalAmount?.toLocaleString('en-IN')}</td>
                                        <td className="p-4">{statusBadge(b.status)}</td>
                                        <td className="p-4">
                                            <div className="flex gap-1">
                                                {b.status === 'PENDING' && (
                                                    <button onClick={() => action(confirmBooking, b.id, 'confirmed')}
                                                        title="Confirm" className="p-1.5 rounded-lg bg-green-900/30 hover:bg-green-900/60 text-green-400 transition-colors">
                                                        <Check size={14} />
                                                    </button>
                                                )}
                                                {b.status === 'CONFIRMED' && (
                                                    <button onClick={() => action(completeBooking, b.id, 'completed')}
                                                        title="Complete" className="p-1.5 rounded-lg bg-brand-900/30 hover:bg-brand-900/60 text-brand-400 transition-colors">
                                                        <CheckCheck size={14} />
                                                    </button>
                                                )}
                                                {['PENDING', 'CONFIRMED'].includes(b.status) && (
                                                    <button onClick={() => action(cancelBooking, b.id, 'cancelled')}
                                                        title="Cancel" className="p-1.5 rounded-lg bg-red-900/30 hover:bg-red-900/60 text-red-400 transition-colors">
                                                        <X size={14} />
                                                    </button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </main>
        </div>
    )
}
