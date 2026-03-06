import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getTools } from '../api/api'
import { Wrench, MapPin } from 'lucide-react'

function ToolCard({ tool, subdomain }) {
    return (
        <Link to={`/store/${subdomain}/tools/${tool.id}`} className="tool-card block">
            <div className="h-40 bg-gradient-to-br from-gray-800 to-gray-900 flex items-center justify-center">
                <Wrench size={40} className="text-gray-600" />
            </div>
            <div className="p-4">
                <h3 className="font-semibold text-white mb-1 line-clamp-1">{tool.name}</h3>
                {tool.category && <p className="text-xs text-brand-400 mb-2">{tool.category}</p>}
                <p className="text-gray-400 text-xs line-clamp-2 mb-3">{tool.description || 'No description provided.'}</p>
                <div className="flex items-end justify-between">
                    <div>
                        <span className="text-xl font-bold text-white">₹{tool.pricePerDay}</span>
                        <span className="text-gray-500 text-xs">/day</span>
                    </div>
                    {tool.depositAmount > 0 && (
                        <span className="text-xs text-gray-500">+₹{tool.depositAmount} deposit</span>
                    )}
                </div>
            </div>
        </Link>
    )
}

export default function StorefrontPage() {
    const { subdomain } = useParams()
    const [tools, setTools] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        // Set subdomain for the request
        const prev = localStorage.getItem('subdomain')
        localStorage.setItem('subdomain', subdomain)
        getTools().then(r => setTools(r.data)).finally(() => {
            setLoading(false)
            if (!prev) localStorage.removeItem('subdomain')
        })
    }, [subdomain])

    return (
        <div className="min-h-screen bg-gray-950">
            {/* Hero */}
            <div className="bg-gradient-to-br from-gray-900 via-brand-950 to-gray-900 border-b border-gray-800 py-12 px-6">
                <div className="max-w-5xl mx-auto">
                    <div className="flex items-center gap-2 text-brand-400 text-sm mb-3">
                        <MapPin size={14} /> {subdomain}.toolrent.in
                    </div>
                    <h1 className="text-3xl font-bold text-white capitalize">{subdomain} Tool Rentals</h1>
                    <p className="text-gray-400 mt-2">Browse and book professional tools for your next project</p>
                </div>
            </div>

            {/* Grid */}
            <div className="max-w-5xl mx-auto px-6 py-8">
                {loading ? (
                    <div className="text-gray-500">Loading tools...</div>
                ) : tools.length === 0 ? (
                    <div className="text-center py-20 text-gray-600">
                        <Wrench size={48} className="mx-auto mb-4" />
                        <p>No tools available right now.</p>
                    </div>
                ) : (
                    <>
                        <p className="text-gray-400 text-sm mb-5">{tools.length} tool{tools.length !== 1 ? 's' : ''} available</p>
                        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-5">
                            {tools.map(t => <ToolCard key={t.id} tool={t} subdomain={subdomain} />)}
                        </div>
                    </>
                )}
            </div>
        </div>
    )
}
