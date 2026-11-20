import { createContext, useContext } from 'react'
import type { UserRole } from '../api/types'

export type CurrentUser = {
  id: string
  email: string
  role: UserRole
  label: string
}

type SessionContextValue = {
  user: CurrentUser | null
  sessionExpired: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

export const CurrentUserContext = createContext<SessionContextValue | null>(null)

export function useSession() {
  const context = useContext(CurrentUserContext)
  if (!context) throw new Error('useSession must be used inside CurrentUserProvider')
  return context
}

export function useCurrentUser() {
  const { user, logout } = useSession()
  if (!user) throw new Error('useCurrentUser must be used while logged in')
  return { user, logout }
}
