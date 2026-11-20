import { sessionsApi, type CreateSessionInput } from '../../api/sessions'
import type { Enrollment, Session } from '../../api/types'
import { Feedback } from '../../components/Feedback'
import { KNOWN_BONDS, userLabel } from '../../config/testData'
import { useCurrentUser } from '../../context/currentUserContext'
import { newestFirst, oldestFirst, useKnownData } from '../../context/knownDataContext'
import { useApiAction } from '../../hooks/useApiAction'
import { CreateSessionForm } from './CreateSessionForm'
import { SessionCard } from './SessionCard'

export function SessionsPage() {
  const { user } = useCurrentUser()
  const { bonds, sessions, enrollments, saveSession, saveEnrollment } = useKnownData()
  const { busy, feedback, run, clearFeedback } = useApiAction()

  // Only approved bonds where the current user can create sessions
  // (the API accepts the bond's requester or evaluator).
  const canUse = (requesterId: string, evaluatorId?: string | null) =>
    requesterId === user.id || evaluatorId === user.id

  const bondOptions = [
    ...KNOWN_BONDS.filter((k) => !bonds[k.id] && canUse(k.requesterId, k.evaluatorId)),
    ...newestFirst(bonds)
      .filter((b) => b.status === 'APPROVED' && canUse(b.requester.id, b.evaluator?.id))
      .map((b) => ({ id: b.id, label: `${userLabel(b.requester.id)} · ${b.subject.name}` })),
  ]

  const handleCreate = (input: CreateSessionInput) =>
    run(async () => {
      saveSession(await sessionsApi.create(input))
      return 'Session created.'
    })

  const handleCancelSession = (session: Session) =>
    run(async () => {
      saveSession(await sessionsApi.cancel(session.id, user.id))
      return 'Session cancelled.'
    })

  const handleEnroll = (session: Session) =>
    run(async () => {
      const enrollment = await sessionsApi.enroll(session.id, user.id)
      saveEnrollment(enrollment)
      return enrollment.status === 'WAITLISTED'
        ? `${user.label} joined the waitlist (position ${enrollment.queuePosition}).`
        : `${user.label} enrolled.`
    })

  const handleCancelEnrollment = (enrollment: Enrollment) =>
    run(async () => {
      saveEnrollment(await sessionsApi.cancelEnrollment(enrollment.id, user.id))
      return `Enrollment of ${userLabel(enrollment.student.id)} cancelled.`
    })

  const sessionList = newestFirst(sessions)
  const enrollmentsOf = (sessionId: string) =>
    oldestFirst(Object.values(enrollments).filter((e) => e.session.id === sessionId))

  return (
    <div className="page">
      <Feedback message={feedback} onClose={clearFeedback} />

      <CreateSessionForm
        userUuid={user.id}
        userLabel={user.label}
        bondOptions={bondOptions}
        busy={busy}
        onSubmit={handleCreate}
      />

      <h2>Sessions ({sessionList.length})</h2>
      {sessionList.length === 0 ? (
        <p className="muted">No sessions created from this screen yet.</p>
      ) : (
        sessionList.map((session) => (
          <SessionCard
            key={session.id}
            session={session}
            enrollments={enrollmentsOf(session.id)}
            currentUserId={user.id}
            busy={busy}
            onEnroll={() => handleEnroll(session)}
            onCancelSession={() => handleCancelSession(session)}
            onCancelEnrollment={handleCancelEnrollment}
          />
        ))
      )}
    </div>
  )
}
