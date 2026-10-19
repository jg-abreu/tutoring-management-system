import { api } from './client'
import type { Subject, TutoringBond } from './types'

export const subjectsApi = {
  suggest: (userUuid: string, subjectName: string) => api.post<Subject>('/subjects', { userUuid, subjectName }),

  approve: (subjectUuid: string, adminUuid: string) =>
    api.patch<Subject>(`/subjects/${subjectUuid}/approve`, { adminUuid }),

  reject: (subjectUuid: string, adminUuid: string) =>
    api.patch<Subject>(`/subjects/${subjectUuid}/reject`, { adminUuid }),
}

export const bondsApi = {
  request: (subjectUuid: string, requesterUuid: string) =>
    api.post<TutoringBond>('/tutoring-bonds', { subjectUuid, requesterUuid }),

  approve: (bondUuid: string, adminUuid: string) =>
    api.patch<TutoringBond>(`/tutoring-bonds/${bondUuid}/approve`, { adminUuid }),

  reject: (bondUuid: string, adminUuid: string) =>
    api.patch<TutoringBond>(`/tutoring-bonds/${bondUuid}/reject`, { adminUuid }),

  revoke: (bondUuid: string, adminUuid: string) =>
    api.patch<TutoringBond>(`/tutoring-bonds/${bondUuid}/revoke`, { adminUuid }),
}
