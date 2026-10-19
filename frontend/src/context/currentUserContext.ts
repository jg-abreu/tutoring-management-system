import { createContext, useContext } from 'react'
import type { TestUser } from '../config/testData'

type CurrentUserContextValue = {
  user: TestUser
  setUserId: (id: string) => void
}

export const CurrentUserContext = createContext<CurrentUserContextValue | null>(null)

export function useCurrentUser() {
  const context = useContext(CurrentUserContext)
  if (!context) throw new Error('useCurrentUser precisa estar dentro de CurrentUserProvider')
  return context
}
