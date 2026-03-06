import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getToolById, createBooking } from '../api/api'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'
import { ArrowLeft, Wrench, Calendar, Shield } from 'lucide-react'

export default function ToolDetail() {
    const { subdomain, toolId } = useParams()
    const { user } = useAuth()
    const navigate = useNavigate()
    const [tool, setTool] = useState(null)
    const [loading, setLoading] = useState(true)
    const [booking, setBooking] = useState(false)
    const [dates, setDates] = useState({ startDate: '', endDate: '' })

    useEffect(() => {
        localStorage.setItem('subdomain', subdomain)
        getToolById(toolId).then(r => setTool(r.data)).finally(() => setLoading(false))
    }, [toolId, subdomain])

    const totalDays = dates.startDate && dates.endDate
        ? Math.max(1, Math.ceil((new Date(dates.endDate) - new Date(dates.startDate)) / 86400000) + 1)
        : 0

    const handleBook = async () => {
        if (!user) { navigate('/login'); return }
        if (!dates.startDate || !dates.endDate) { toast.error('Select dates'); return }
        setBooking(true)
        try {
            const { data } = await createBooking({
                toolId: tool.id, startDate: dates.startDate, endDate: dates.endDate
            })
            toast.success('Booking created! Proceeding to payment...')
            navigate(`/checkout/${data.id}`)
        } catch (err) {
            toast.error(err.response?.data?.message || 'Booking failed')
        } finally {
            setBooking(false)
        }
    }

    if (loading) return <div className="min-h-screen bg-gray-950 flex items-center justify-center text-gray-500">Loading...</div>
    if (!tool) return <div className="min-h-screen bg-gray-950 flex items-center justify-center text-gray-500">Tool not found</div>

    return (
        <div className="min-h-screen bg-gray-950 px-4 py-8">
            <div className="max-w-4xl mx-auto">
                <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-gray-400 hover:text-white text-sm mb-6 transition-colors">
                    <ArrowLeft size={16} /> Back to store
                </button>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    {/* Image */}
                    <div className="card h-72 flex items-center justify-center bg-gradient-to-br from-gray-800 to-gray-900">
                        <Wrench size={60} className="text-gray-600" />
                    </div>

                    {/* Details */}
                    <div>
                        {tool.category && <span className="text-xs font-semibold text-brand-400 uppercase tracking-wide">{tool.category}</span>}
                        <h1 className="text-2xl font-bold text-white mt-1 mb-2">{tool.name}</h1>
                        <p className="text-gray-400 text-sm mb-4">{tool.description}</p>

                        <div className="flex items-center gap-4 mb-6">
                            <div>
                                <span className="text-3xl font-bold text-white">₹{tool.pricePerDay}</span>
                                <span className="text-gray-500 text-sm">/day</span>
                            </div>
                            {tool.depositAmount > 0 && (
                                <div className="flex items-center gap-1 text-gray-400 text-sm">
                                    <Shield size={14} className="text-brand-400" />
                                    <span>₹{tool.depositAmount} deposit</span>
                                </div>
                            )}
                        </div>

                        {/* Date picker */}
                        <div className="card p-4 mb-4">
                            <div className="flex items-center gap-2 text-gray-300 text-sm mb-3">
                                <Calendar size={15} className="text-brand-400" />
                                Select rental dates
                            </div>
                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <label className="label text-xs">Start Date</label>
                                    <input type="date" className="input text-sm"
                                        value={dates.startDate} min={new Date().toISOString().split('T')[0]}
                                        onChange={e => setDates({ ...dates, startDate: e.target.value })} />
                                </div>
                                <div>
                                    <label className="label text-xs">End Date</label>
                                    <input type="date" className="input text-sm"
                                        value={dates.endDate} min={dates.startDate || new Date().toISOString().split('T')[0]}
                                        onChange={e => setDates({ ...dates, endDate: e.target.value })} />
                                </div>
                            </div>
                            {totalDays > 0 && (
                                <div className="mt-3 p-3 bg-gray-800 rounded-lg">
                                    <div className="flex justify-between text-sm text-gray-400">
                                        <span>Rental ({totalDays} day{totalDays > 1 ? 's' : ''})</span>
                                        <span className="text-white">₹{(tool.pricePerDay * totalDays).toLocaleString('en-IN')}</span>
                                    </div>
                                    {tool.depositAmount > 0 && (
                                        <div className="flex justify-between text-sm text-gray-400 mt-1">
                                            <span>Security deposit</span>
                                            <span className="text-white">₹{tool.depositAmount?.toLocaleString('en-IN')}</span>
                                        </div>
                                    )}
                                    <div className="flex justify-between font-semibold mt-2 pt-2 border-t border-gray-700">
                                        <span className="text-white">Total</span>
                                        <span className="text-brand-400 text-lg">₹{((tool.pricePerDay * totalDays) + (tool.depositAmount || 0)).toLocaleString('en-IN')}</span>
                                    </div>
                                </div>
                            )}
                        </div>

                        <button onClick={handleBook} disabled={booking || !tool.isAvailable} className="btn-primary w-full">
                            {!tool.isAvailable ? 'Not Available' : booking ? 'Creating Booking...' : 'Book Now →'}
                        </button>
                        {!user && <p className="text-xs text-gray-500 text-center mt-2">You'll be asked to sign in</p>}
                    </div>
                </div>
            </div>
        </div>
    )
}
