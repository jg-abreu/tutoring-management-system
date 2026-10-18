import { createContext, useContext } from 'react'
import type { Enrollment, Session, Subject, TutoringBond } from '../api/types'

export type ById<T> = Record<string, T>

// PROVISÓRIO: sem GET na API, o frontend só conhece o que ele mesmo criou ou
// alterou. Cada resposta de POST/PATCH é guardada aqui (e no navegador).
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
  if (!context) throw new Error('useKnownData precisa estar dentro de KnownDataProvider')
  return context
}

export function newestFirst<T extends { createdAt: string }>(items: ById<T>): T[] {
  return Object.values(items).sort((a, b) => b.createdAt.localeCompare(a.createdAt))
}
