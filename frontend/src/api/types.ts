// Shapes returned by the API (the response records in its dto/ package).

export type UserRole = 'ADMIN' | 'USER'

export type UserSummary = {
  id: string
  email: string
}

export type LoginResponse = {
  token: string
  tokenType: 'Bearer'
  expiresAt: string
  role: UserRole
}

export type SubjectStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export type Subject = {
  id: string
  name: string
  status: SubjectStatus
  creator: UserSummary
  evaluator: UserSummary | null
  createdAt: string
}

export type BondStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REVOKED'

export type TutoringBond = {
  id: string
  status: BondStatus
  requester: UserSummary
  evaluator: UserSummary | null
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
  student: UserSummary
  createdAt: string
}
