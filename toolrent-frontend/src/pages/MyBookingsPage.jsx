import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getBookings } from '../api/api'
import { Calendar, Wrench, ArrowLeft } from 'lucide-react'

export default function MyBookingsPage() {
    const navigate = useNavigate()
    const [bookings, setBookings] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        getBookings(true).then(r => setBookings(r.data)).finally(() => setLoading(false))
    }, [])

    function statusBadge(status) {
        const map = { PENDING: 'badge-pending', CONFIRMED: 'badge-confirmed', COMPLETED: 'badge-completed', CANCELLED: 'badge-cancelled' }
        return <span className={map[status]}>{status}</span>
    }

    return (
        <div className="min-h-screen bg-gray-950 px-4 py-8">
            <div className="max-w-3xl mx-auto">
                <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-gray-400 hover:text-white text-sm mb-6 transition-colors">
                    <ArrowLeft size={16} /> Back
                </button>
                <h1 className="text-2xl font-bold text-white mb-6">My Bookings</h1>

                {loading ? (
                    <div className="text-gray-500">Loading...</div>
                ) : bookings.length === 0 ? (
                    <div className="card p-12 text-center">
                        <Calendar size={40} className="mx-auto text-gray-700 mb-4" />
                        <p className="text-gray-400">No bookings yet. Browse tools to get started!</p>
                    </div>
                ) : (
                    <div className="flex flex-col gap-4">
                        {bookings.map(b => (
                            <div key={b.id} className="card p-5 animate-fade-in">
                                <div className="flex items-start justify-between">
                                    <div className="flex items-center gap-3">
                                        <div className="w-10 h-10 rounded-xl bg-gray-800 flex items-center justify-center">
                                            <Wrench size={18} className="text-brand-400" />
                                        </div>
                                        <div>
                                            <h3 className="font-semibold text-white">{b.toolName || b.tool?.name}</h3>
                                            <p className="text-gray-400 text-xs mt-0.5">{b.startDate} → {b.endDate} ({b.totalDays} days)</p>
                                        </div>
                                    </div>
                                    {statusBadge(b.status)}
                                </div>
                                <div className="mt-3 pt-3 border-t border-gray-800 flex items-center justify-between">
                                    <div className="text-sm text-gray-400">
                                        Rent: ₹{b.rentalAmount?.toLocaleString('en-IN')} + Deposit: ₹{b.depositAmount?.toLocaleString('en-IN')}
                                    </div>
                                    <div className="font-bold text-white">₹{b.totalAmount?.toLocaleString('en-IN')}</div>
                                </div>
                                {b.status === 'PENDING' && (
                                    <div className="mt-3">
                                        <button onClick={() => navigate(`/checkout/${b.id}`)} className="btn-primary text-sm py-2">
                                            Pay Now →
                                        </button>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    )
}
