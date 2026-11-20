import { api } from './client'
import type { Enrollment, Session } from './types'

export type CreateSessionInput = {
  tutoringBondUuid: string
  startTime: string
  endTime: string
  spots: number
  allowsWaitlist: boolean
}

export const sessionsApi = {
  create: (input: CreateSessionInput) => api.post<Session>('/sessions', input),

  cancel: (sessionUuid: string) => api.patch<Session>(`/sessions/${sessionUuid}/cancel`),

  enroll: (sessionUuid: string) => api.post<Enrollment>(`/sessions/${sessionUuid}/enrollments`),

  cancelEnrollment: (enrollmentUuid: string) => api.patch<Enrollment>(`/enrollments/${enrollmentUuid}/cancel`),
}
