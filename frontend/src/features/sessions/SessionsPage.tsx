import { sessionsApi, type CreateSessionInput } from '../../api/sessions'
import type { Enrollment, Session } from '../../api/types'
import { Feedback } from '../../components/Feedback'
import { KNOWN_BONDS, userLabel } from '../../config/testData'
import { useCurrentUser } from '../../context/currentUserContext'
import { newestFirst, useKnownData } from '../../context/knownDataContext'
import { useApiAction } from '../../hooks/useApiAction'
import { CreateSessionForm } from './CreateSessionForm'
import { SessionCard } from './SessionCard'

export function SessionsPage() {
  const { user } = useCurrentUser()
  const { bonds, sessions, enrollments, saveSession, saveEnrollment } = useKnownData()
  const { busy, feedback, run, clearFeedback } = useApiAction()

  const bondOptions = [
    ...KNOWN_BONDS,
    ...newestFirst(bonds)
      .filter((b) => b.status === 'APPROVED' && !KNOWN_BONDS.some((k) => k.id === b.id))
      .map((b) => ({ id: b.id, label: `${userLabel(b.requester.id)} · ${b.subject.name}` })),
  ]

  const handleCreate = (input: CreateSessionInput) =>
    run(async () => {
      saveSession(await sessionsApi.create(input))
      return 'Sessão criada.'
    })

  const handleCancelSession = (session: Session) =>
    run(async () => {
      saveSession(await sessionsApi.cancel(session.id, user.id))
      return 'Sessão cancelada.'
    })

  const handleEnroll = (session: Session) =>
    run(async () => {
      const enrollment = await sessionsApi.enroll(session.id, user.id)
      saveEnrollment(enrollment)
      return enrollment.status === 'WAITLISTED'
        ? `${user.label} entrou na lista de espera (posição ${enrollment.queuePosition}).`
        : `${user.label} matriculado(a).`
    })

  const handleCancelEnrollment = (enrollment: Enrollment) =>
    run(async () => {
      saveEnrollment(await sessionsApi.cancelEnrollment(enrollment.id, user.id))
      return `Matrícula de ${userLabel(enrollment.student.id)} cancelada.`
    })

  const sessionList = newestFirst(sessions)
  const enrollmentsOf = (sessionId: string) =>
    Object.values(enrollments)
      .filter((e) => e.session.id === sessionId)
      .sort((a, b) => a.createdAt.localeCompare(b.createdAt))

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

      <h2>Sessões ({sessionList.length})</h2>
      {sessionList.length === 0 ? (
        <p className="muted">Nenhuma sessão criada por esta tela ainda.</p>
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
