import { api } from './client'
import type { Subject, TutoringBond } from './types'

export const subjectsApi = {
  suggest: (subjectName: string) => api.post<Subject>('/subjects', { subjectName }),

  approve: (subjectUuid: string) => api.patch<Subject>(`/subjects/${subjectUuid}/approve`),

  reject: (subjectUuid: string) => api.patch<Subject>(`/subjects/${subjectUuid}/reject`),
}

export const bondsApi = {
  request: (subjectUuid: string) => api.post<TutoringBond>('/tutoring-bonds', { subjectUuid }),

  approve: (bondUuid: string) => api.patch<TutoringBond>(`/tutoring-bonds/${bondUuid}/approve`),

  reject: (bondUuid: string) => api.patch<TutoringBond>(`/tutoring-bonds/${bondUuid}/reject`),

  revoke: (bondUuid: string) => api.patch<TutoringBond>(`/tutoring-bonds/${bondUuid}/revoke`),
}
