import { useCallback, useEffect, useState, type Dispatch, type ReactNode, type SetStateAction } from 'react'
import { bondsApi, subjectsApi } from '../api/catalog'
import { ApiError } from '../api/client'
import { sessionsApi } from '../api/sessions'
import type { Enrollment, Session, Subject, TutoringBond } from '../api/types'
import { AppDataContext, emptyList, type ListState } from './appDataContext'

const errorText = (error: unknown) => (error instanceof ApiError ? error.message : 'Unexpected error.')

async function fill<T>(setList: Dispatch<SetStateAction<ListState<T>>>, load: () => Promise<T[]>) {
  try {
    const items = await load()
    setList({ items, loading: false, error: null })
  } catch (error) {
    setList((prev) => ({ ...prev, loading: false, error: errorText(error) }))
  }
}

const markLoading = <T,>(prev: ListState<T>): ListState<T> => ({ ...prev, loading: true, error: null })

export function AppDataProvider({ children }: { children: ReactNode }) {
  const [subjects, setSubjects] = useState<ListState<Subject>>(emptyList)
  const [bonds, setBonds] = useState<ListState<TutoringBond>>(emptyList)
  const [sessions, setSessions] = useState<ListState<Session>>(emptyList)
  const [enrollmentsBySession, setEnrollmentsBySession] = useState<Record<string, Enrollment[]>>({})

  const fetchSubjects = useCallback(() => fill(setSubjects, () => subjectsApi.list()), [])
  const fetchBonds = useCallback(() => fill(setBonds, () => bondsApi.list()), [])
  const fetchSessions = useCallback(
    () =>
      fill(setSessions, async () => {
        const items = await sessionsApi.list()
        const lists = await Promise.all(items.map((session) => sessionsApi.enrollments(session.id)))
        setEnrollmentsBySession(Object.fromEntries(items.map((session, i) => [session.id, lists[i]])))
        return items
      }),
    [],
  )

  useEffect(() => {
    void fetchSubjects()
    void fetchBonds()
    void fetchSessions()
  }, [fetchSubjects, fetchBonds, fetchSessions])

  const reloadSubjects = () => {
    setSubjects(markLoading)
    return fetchSubjects()
  }

  const reloadBonds = () => {
    setBonds(markLoading)
    return fetchBonds()
  }

  const reloadSessions = () => {
    setSessions(markLoading)
    return fetchSessions()
  }

  return (
    <AppDataContext.Provider
      value={{ subjects, bonds, sessions, enrollmentsBySession, reloadSubjects, reloadBonds, reloadSessions }}
    >
      {children}
    </AppDataContext.Provider>
  )
}
