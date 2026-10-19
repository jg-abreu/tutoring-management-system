import type { ReactNode } from 'react'
import type { Enrollment, Session, Subject, TutoringBond } from '../api/types'
import { useLocalStorage } from '../hooks/useLocalStorage'
import { KnownDataContext, type ById } from './knownDataContext'

export function KnownDataProvider({ children }: { children: ReactNode }) {
  const [subjects, setSubjects] = useLocalStorage<ById<Subject>>('knownSubjects', {})
  const [bonds, setBonds] = useLocalStorage<ById<TutoringBond>>('knownBonds', {})
  const [sessions, setSessions] = useLocalStorage<ById<Session>>('knownSessions', {})
  const [enrollments, setEnrollments] = useLocalStorage<ById<Enrollment>>('knownEnrollments', {})

  // As respostas trazem os objetos aninhados atualizados (ex.: a matrícula traz
  // a sessão, que traz o vínculo, que traz a matéria), então salvamos em cascata.
  const saveSubject = (subject: Subject) => setSubjects((prev) => ({ ...prev, [subject.id]: subject }))

  const saveBond = (bond: TutoringBond) => {
    setBonds((prev) => ({ ...prev, [bond.id]: bond }))
    saveSubject(bond.subject)
  }

  const saveSession = (session: Session) => {
    setSessions((prev) => ({ ...prev, [session.id]: session }))
    saveBond(session.bond)
  }

  const saveEnrollment = (enrollment: Enrollment) => {
    setEnrollments((prev) => ({ ...prev, [enrollment.id]: enrollment }))
    saveSession(enrollment.session)
  }

  const clearAll = () => {
    setSubjects({})
    setBonds({})
    setSessions({})
    setEnrollments({})
  }

  return (
    <KnownDataContext.Provider
      value={{
        subjects,
        bonds,
        sessions,
        enrollments,
        saveSubject,
        saveBond,
        saveSession,
        saveEnrollment,
        clearAll,
      }}
    >
      {children}
    </KnownDataContext.Provider>
  )
}
