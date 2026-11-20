import { useCallback, useEffect, useState, type ReactNode } from 'react'
import { authApi } from '../api/auth'
import { setAuthToken, setUnauthorizedHandler } from '../api/client'
import { TEST_USERS } from '../config/testData'
import { userIdFromToken } from '../lib/token'
import { CurrentUserContext, type CurrentUser } from './currentUserContext'

export function CurrentUserProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [sessionExpired, setSessionExpired] = useState(false)

  const logout = useCallback(() => {
    setAuthToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setAuthToken(null)
      setUser(null)
      setSessionExpired(true)
    })
    return () => setUnauthorizedHandler(null)
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login(email, password)
    const id = userIdFromToken(response.token)
    setAuthToken(response.token)
    setUser({
      id,
      email,
      role: response.role,
      label: TEST_USERS.find((u) => u.id === id)?.label ?? email.split('@')[0],
    })
    setSessionExpired(false)
  }, [])

  return (
    <CurrentUserContext.Provider value={{ user, sessionExpired, login, logout }}>{children}</CurrentUserContext.Provider>
  )
}
