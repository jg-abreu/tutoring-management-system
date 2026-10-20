import { createContext, useContext } from 'react'
import type { Enrollment, Session, Subject, TutoringBond } from '../api/types'

export type ById<T> = Record<string, T>

// TEMPORARY: with no GET in the API, the frontend only knows what it created or
// changed itself. Every POST/PATCH response is stored here (and in the browser).
export type KnownData = {
  subjects: ById<Subject>
  bonds: ById<TutoringBond>
  sessions: ById<Session>
  enrollments: ById<Enrollment>
  saveSubject: (subject: Subject) => void
  saveBond: (bond: TutoringBond) => void
  saveSession: (session: Session) => void
  saveEnrollment: (enrollment: Enrollment) => void
  clearAll: () => void
}

export const KnownDataContext = createContext<KnownData | null>(null)

export function useKnownData() {
  const context = useContext(KnownDataContext)
  if (!context) throw new Error('useKnownData must be used inside KnownDataProvider')
  return context
}

// The API returns createdAt with different offsets depending on the path (freshly
// created comes with -03:00; read from the database comes in UTC with Z), so comparing
// as text scrambles the order. Comparing as instants fixes it.
const time = (iso: string) => new Date(iso).getTime()

export function newestFirst<T extends { createdAt: string }>(items: ById<T>): T[] {
  return Object.values(items).sort((a, b) => time(b.createdAt) - time(a.createdAt))
}

export function oldestFirst<T extends { createdAt: string }>(items: T[]): T[] {
  return [...items].sort((a, b) => time(a.createdAt) - time(b.createdAt))
}
