import type { ReactNode } from 'react'
import { TEST_USERS } from '../config/testData'
import { useLocalStorage } from '../hooks/useLocalStorage'
import { CurrentUserContext } from './currentUserContext'

export function CurrentUserProvider({ children }: { children: ReactNode }) {
  const [userId, setUserId] = useLocalStorage('currentUserId', TEST_USERS[0].id)
  const user = TEST_USERS.find((u) => u.id === userId) ?? TEST_USERS[0]

  return (
    <CurrentUserContext.Provider value={{ user, setUserId }}>
      {children}
    </CurrentUserContext.Provider>
  )
}
