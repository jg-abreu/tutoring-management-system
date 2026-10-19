// Formatos devolvidos pela API (as entidades JPA serializadas pelo Jackson).

export type User = {
  id: string
  email: string
  active: boolean
  createdAt: string
}

export type SubjectStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export type Subject = {
  id: string
  name: string
  status: SubjectStatus
  creator: User
  evaluator: User | null
  createdAt: string
}

export type BondStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REVOKED'

export type TutoringBond = {
  id: string
  status: BondStatus
  requester: User
  evaluator: User | null
  subject: Subject
  createdAt: string
}

export type SessionStatus = 'ACTIVE' | 'CANCELLED'

export type Session = {
  id: string
  startTime: string
  endTime: string
  spots: number
  allowsWaitlist: boolean
  status: SessionStatus
  bond: TutoringBond
  createdAt: string
}

export type EnrollmentStatus = 'CONFIRMED' | 'WAITLISTED' | 'CANCELLED' | 'REJECTED'

export type Enrollment = {
  id: string
  status: EnrollmentStatus
  queuePosition: number | null
  session: Session
  student: User
  createdAt: string
}
