import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { createPaymentOrder, verifyPayment } from '../api/api'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'
import { Shield, CreditCard, Wrench } from 'lucide-react'

export default function CheckoutPage() {
    const { bookingId } = useParams()
    const { user } = useAuth()
    const navigate = useNavigate()
    const [orderData, setOrderData] = useState(null)
    const [loading, setLoading] = useState(true)
    const [paying, setPaying] = useState(false)

    useEffect(() => {
        createPaymentOrder(bookingId)
            .then(r => setOrderData(r.data))
            .catch(() => toast.error('Failed to create payment order'))
            .finally(() => setLoading(false))
    }, [bookingId])

    const handlePay = () => {
        if (!orderData) return
        setPaying(true)

        const options = {
            key: orderData.keyId,
            amount: orderData.amount,
            currency: orderData.currency,
            name: 'ToolRent',
            description: `Booking #${bookingId.slice(0, 8)}`,
            order_id: orderData.orderId,
            prefill: { email: user?.email },
            theme: { color: '#0ea5e9' },
            handler: async (response) => {
                try {
                    await verifyPayment({
                        razorpayOrderId: response.razorpay_order_id,
                        razorpayPaymentId: response.razorpay_payment_id,
                        razorpaySignature: response.razorpay_signature,
                    })
                    toast.success('Payment successful! Booking confirmed 🎉')
                    navigate('/my-bookings')
                } catch {
                    toast.error('Payment verification failed. Contact support.')
                } finally {
                    setPaying(false)
                }
            },
            modal: {
                ondismiss: () => setPaying(false)
            }
        }

        const rzp = new window.Razorpay(options)
        rzp.open()
    }

    if (loading) return (
        <div className="min-h-screen bg-gray-950 flex items-center justify-center text-gray-500">
            Preparing checkout...
        </div>
    )

    if (!orderData) return (
        <div className="min-h-screen bg-gray-950 flex items-center justify-center text-red-400">
            Failed to load checkout. Please try again.
        </div>
    )

    const amountInRupees = orderData.amount / 100

    return (
        <div className="min-h-screen bg-gray-950 flex items-center justify-center px-4">
            {/* Load Razorpay SDK dynamically */}
            <script src="https://checkout.razorpay.com/v1/checkout.js"></script>

            <div className="card p-8 w-full max-w-md animate-slide-up">
                <div className="flex items-center gap-3 mb-6">
                    <div className="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center">
                        <CreditCard size={20} className="text-white" />
                    </div>
                    <div>
                        <h1 className="text-xl font-bold text-white">Checkout</h1>
                        <p className="text-gray-400 text-xs">Secure payment via Razorpay</p>
                    </div>
                </div>

                <div className="bg-gray-800 rounded-xl p-4 mb-6">
                    <div className="flex items-start gap-3">
                        <div className="w-8 h-8 rounded-lg bg-gray-700 flex items-center justify-center flex-shrink-0 mt-0.5">
                            <Wrench size={14} className="text-brand-400" />
                        </div>
                        <div>
                            <p className="text-white font-medium text-sm">Booking #{bookingId.slice(0, 8)}</p>
                            <p className="text-gray-400 text-xs mt-0.5">Order ID: {orderData.orderId}</p>
                        </div>
                    </div>
                </div>

                <div className="space-y-3 mb-6">
                    <div className="flex justify-between text-sm">
                        <span className="text-gray-400">Total Amount</span>
                        <span className="text-white font-semibold">₹{amountInRupees.toLocaleString('en-IN')}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                        <span className="text-gray-400">Currency</span>
                        <span className="text-gray-300">INR</span>
                    </div>
                </div>

                <div className="flex items-center gap-2 text-xs text-gray-500 mb-5">
                    <Shield size={12} className="text-green-400" />
                    256-bit SSL encrypted · Powered by Razorpay
                </div>

                <button onClick={handlePay} disabled={paying} className="btn-primary w-full flex items-center justify-center gap-2">
                    <CreditCard size={16} />
                    {paying ? 'Processing...' : `Pay ₹${amountInRupees.toLocaleString('en-IN')}`}
                </button>
                <button onClick={() => navigate('/my-bookings')} className="btn-secondary w-full mt-3 text-sm">
                    Cancel
                </button>
            </div>
        </div>
    )
}
