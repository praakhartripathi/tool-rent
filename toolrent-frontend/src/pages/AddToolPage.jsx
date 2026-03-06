import { useState, useEffect } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { createTool, updateTool, getToolById } from '../api/api'
import toast from 'react-hot-toast'
import { Wrench, ArrowLeft } from 'lucide-react'

export default function AddToolPage() {
    const { id } = useParams()
    const isEdit = !!id
    const navigate = useNavigate()
    const [form, setForm] = useState({
        name: '', description: '', pricePerDay: '', depositAmount: '', quantity: 1, category: ''
    })
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        if (isEdit) {
            getToolById(id).then(r => {
                const t = r.data
                setForm({ name: t.name, description: t.description || '', pricePerDay: t.pricePerDay, depositAmount: t.depositAmount, quantity: t.quantity, category: t.category || '' })
            })
        }
    }, [id])

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)
        try {
            if (isEdit) {
                await updateTool(id, form)
                toast.success('Tool updated!')
            } else {
                await createTool(form)
                toast.success('Tool added!')
            }
            navigate('/tools')
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to save tool')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="min-h-screen bg-gray-950 p-8">
            <div className="max-w-2xl mx-auto">
                <Link to="/tools" className="flex items-center gap-2 text-gray-400 hover:text-white text-sm mb-6 transition-colors">
                    <ArrowLeft size={16} /> Back to Tools
                </Link>

                <div className="card p-8">
                    <div className="flex items-center gap-3 mb-6">
                        <div className="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center">
                            <Wrench size={20} className="text-white" />
                        </div>
                        <h1 className="text-xl font-bold text-white">{isEdit ? 'Edit Tool' : 'Add New Tool'}</h1>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div>
                            <label className="label">Tool Name</label>
                            <input name="name" className="input" placeholder="e.g. Concrete Mixer 1HP"
                                value={form.name} onChange={handleChange} required />
                        </div>
                        <div>
                            <label className="label">Category</label>
                            <input name="category" className="input" placeholder="e.g. Concrete & Masonry"
                                value={form.category} onChange={handleChange} />
                        </div>
                        <div>
                            <label className="label">Description</label>
                            <textarea name="description" className="input min-h-[90px] resize-none" placeholder="Describe the tool, condition, features..."
                                value={form.description} onChange={handleChange} />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="label">Price per Day (₹)</label>
                                <input name="pricePerDay" type="number" min="0" step="0.01" className="input"
                                    placeholder="500" value={form.pricePerDay} onChange={handleChange} required />
                            </div>
                            <div>
                                <label className="label">Security Deposit (₹)</label>
                                <input name="depositAmount" type="number" min="0" step="0.01" className="input"
                                    placeholder="2000" value={form.depositAmount} onChange={handleChange} />
                            </div>
                        </div>
                        <div>
                            <label className="label">Quantity Available</label>
                            <input name="quantity" type="number" min="1" className="input w-32"
                                value={form.quantity} onChange={handleChange} required />
                        </div>
                        <div className="flex gap-3 pt-2">
                            <button type="submit" className="btn-primary flex-1" disabled={loading}>
                                {loading ? 'Saving...' : isEdit ? 'Update Tool' : 'Add Tool'}
                            </button>
                            <Link to="/tools" className="btn-secondary">Cancel</Link>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    )
}
