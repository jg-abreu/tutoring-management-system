import { createContext, useContext } from 'react'
import type { Enrollment, Session, Subject, TutoringBond } from '../api/types'

export type ListState<T> = {
  items: T[]
  loading: boolean
  error: string | null
}

export type AppData = {
  subjects: ListState<Subject>
  bonds: ListState<TutoringBond>
  sessions: ListState<Session>
  enrollmentsBySession: Record<string, Enrollment[]>
  reloadSubjects: () => Promise<void>
  reloadBonds: () => Promise<void>
  reloadSessions: () => Promise<void>
}

export const AppDataContext = createContext<AppData | null>(null)

export function useAppData() {
  const context = useContext(AppDataContext)
  if (!context) throw new Error('useAppData must be used inside AppDataProvider')
  return context
}

export const emptyList = <T,>(): ListState<T> => ({ items: [], loading: true, error: null })
