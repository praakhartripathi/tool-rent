import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getTools, deleteTool } from '../api/api'
import toast from 'react-hot-toast'
import { Wrench, Plus, Pencil, Trash2, Calendar, BarChart2, LogOut } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function ToolsPage() {
    const { logout } = useAuth()
    const navigate = useNavigate()
    const [tools, setTools] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        getTools(true).then(r => setTools(r.data)).finally(() => setLoading(false))
    }, [])

    const handleDelete = async (id, name) => {
        if (!confirm(`Delete "${name}"?`)) return
        try {
            await deleteTool(id)
            setTools(tools.filter(t => t.id !== id))
            toast.success('Tool deleted')
        } catch { toast.error('Delete failed') }
    }

    return (
        <div className="min-h-screen bg-gray-950 flex">
            {/* Sidebar */}
            <aside className="w-60 min-h-screen bg-gray-900 border-r border-gray-800 flex flex-col p-5 fixed">
                <div className="flex items-center gap-2 mb-8">
                    <div className="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center">
                        <Wrench size={16} className="text-white" />
                    </div>
                    <span className="font-bold text-white">ToolRent</span>
                </div>
                <nav className="flex flex-col gap-1 flex-1">
                    <Link to="/dashboard" className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-400 hover:bg-gray-800 text-sm font-medium transition-colors"><BarChart2 size={16} /> Dashboard</Link>
                    <Link to="/tools" className="flex items-center gap-3 px-3 py-2 rounded-lg bg-brand-600/20 text-brand-400 text-sm font-medium"><Wrench size={16} /> Tools</Link>
                    <Link to="/bookings" className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-400 hover:bg-gray-800 text-sm font-medium transition-colors"><Calendar size={16} /> Bookings</Link>
                </nav>
                <button onClick={() => { logout(); navigate('/login') }} className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-500 hover:text-red-400 text-sm transition-colors">
                    <LogOut size={16} /> Sign Out
                </button>
            </aside>

            <main className="ml-60 flex-1 p-8">
                <div className="flex items-center justify-between mb-8">
                    <h1 className="text-2xl font-bold text-white">Tool Inventory</h1>
                    <Link to="/tools/new" className="btn-primary flex items-center gap-2"><Plus size={16} /> Add Tool</Link>
                </div>

                {loading ? (
                    <div className="text-gray-500">Loading tools...</div>
                ) : tools.length === 0 ? (
                    <div className="card p-12 text-center">
                        <Wrench size={40} className="mx-auto text-gray-700 mb-4" />
                        <p className="text-gray-400">No tools yet. Add your first tool!</p>
                        <Link to="/tools/new" className="btn-primary inline-flex items-center gap-2 mt-4"><Plus size={16} /> Add Tool</Link>
                    </div>
                ) : (
                    <div className="card overflow-hidden">
                        <table className="w-full text-sm">
                            <thead className="border-b border-gray-800">
                                <tr>
                                    <th className="text-left text-gray-400 p-4 font-medium">Name</th>
                                    <th className="text-left text-gray-400 p-4 font-medium">Category</th>
                                    <th className="text-left text-gray-400 p-4 font-medium">Price/Day</th>
                                    <th className="text-left text-gray-400 p-4 font-medium">Deposit</th>
                                    <th className="text-left text-gray-400 p-4 font-medium">Qty</th>
                                    <th className="text-left text-gray-400 p-4 font-medium">Status</th>
                                    <th className="p-4"></th>
                                </tr>
                            </thead>
                            <tbody>
                                {tools.map(t => (
                                    <tr key={t.id} className="border-b border-gray-800/50 hover:bg-gray-800/30 transition-colors">
                                        <td className="p-4 text-white font-medium">{t.name}</td>
                                        <td className="p-4 text-gray-400">{t.category || '—'}</td>
                                        <td className="p-4 text-white">₹{t.pricePerDay}</td>
                                        <td className="p-4 text-gray-300">₹{t.depositAmount}</td>
                                        <td className="p-4 text-gray-300">{t.availableQuantity}/{t.quantity}</td>
                                        <td className="p-4">
                                            <span className={t.isAvailable ? 'badge-confirmed' : 'badge-cancelled'}>
                                                {t.isAvailable ? 'Available' : 'Unavailable'}
                                            </span>
                                        </td>
                                        <td className="p-4">
                                            <div className="flex items-center gap-2">
                                                <Link to={`/tools/${t.id}/edit`} className="p-2 rounded-lg hover:bg-gray-700 text-gray-400 hover:text-white transition-colors"><Pencil size={14} /></Link>
                                                <button onClick={() => handleDelete(t.id, t.name)} className="p-2 rounded-lg hover:bg-red-900/30 text-gray-400 hover:text-red-400 transition-colors"><Trash2 size={14} /></button>
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
