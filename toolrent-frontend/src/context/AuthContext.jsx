import { createContext, useContext, useState, useEffect } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        try {
            const stored = localStorage.getItem('user')
            return stored ? JSON.parse(stored) : null
        } catch {
            return null
        }
    })

    const setAuth = (data) => {
        localStorage.setItem('accessToken', data.accessToken)
        localStorage.setItem('refreshToken', data.refreshToken)
        localStorage.setItem('subdomain', data.subdomain || '')
        const userData = {
            userId: data.userId,
            email: data.email,
            role: data.role,
            tenantId: data.tenantId,
            subdomain: data.subdomain,
            businessName: data.businessName,
        }
        localStorage.setItem('user', JSON.stringify(userData))
        setUser(userData)
    }

    const logout = () => {
        localStorage.clear()
        setUser(null)
    }

    const isAdmin = () => user?.role === 'TENANT_ADMIN'
    const isCustomer = () => user?.role === 'CUSTOMER'

    return (
        <AuthContext.Provider value={{ user, setAuth, logout, isAdmin, isCustomer }}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext)
