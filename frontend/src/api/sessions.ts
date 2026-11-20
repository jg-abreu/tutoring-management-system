import { api } from './client'
import type { Enrollment, Session } from './types'

export type CreateSessionInput = {
  tutoringBondUuid: string
  userUuid: string
  startTime: string
  endTime: string
  spots: number
  allowsWaitlist: boolean
}

export const sessionsApi = {
  create: (input: CreateSessionInput) => api.post<Session>('/sessions', input),

  cancel: (sessionUuid: string, userUuid: string) =>
    api.patch<Session>(`/sessions/${sessionUuid}/cancel`, { userUuid }),

  enroll: (sessionUuid: string, userUuid: string) =>
    api.post<Enrollment>(`/sessions/${sessionUuid}/enrollments`, { userUuid }),

  cancelEnrollment: (enrollmentUuid: string, userUuid: string) =>
    api.patch<Enrollment>(`/enrollments/${enrollmentUuid}/cancel`, { userUuid }),
}
