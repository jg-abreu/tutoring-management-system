import { api } from './client'
import type { BondStatus, Subject, SubjectStatus, TutoringBond } from './types'

const withStatus = (path: string, status?: string) => (status ? `${path}?status=${status}` : path)

export const subjectsApi = {
  list: (status?: SubjectStatus) => api.get<Subject[]>(withStatus('/subjects', status)),

  suggest: (subjectName: string) => api.post<Subject>('/subjects', { subjectName }),

  approve: (subjectUuid: string) => api.patch<Subject>(`/subjects/${subjectUuid}/approve`),

  reject: (subjectUuid: string) => api.patch<Subject>(`/subjects/${subjectUuid}/reject`),
}

export const bondsApi = {
  list: (status?: BondStatus) => api.get<TutoringBond[]>(withStatus('/tutoring-bonds', status)),

  request: (subjectUuid: string) => api.post<TutoringBond>('/tutoring-bonds', { subjectUuid }),

  approve: (bondUuid: string) => api.patch<TutoringBond>(`/tutoring-bonds/${bondUuid}/approve`),

  reject: (bondUuid: string) => api.patch<TutoringBond>(`/tutoring-bonds/${bondUuid}/reject`),

  revoke: (bondUuid: string) => api.patch<TutoringBond>(`/tutoring-bonds/${bondUuid}/revoke`),
}
