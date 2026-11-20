import { sessionsApi, type CreateSessionInput } from '../../api/sessions'
import type { Enrollment, Session } from '../../api/types'
import { Feedback } from '../../components/Feedback'
import { LoadState } from '../../components/LoadState'
import { useAppData } from '../../context/appDataContext'
import { useCurrentUser } from '../../context/currentUserContext'
import { useApiAction } from '../../hooks/useApiAction'
import { dayKey, dayParts, displayName } from '../../lib/format'
import { CreateSessionForm } from './CreateSessionForm'
import { SessionCard } from './SessionCard'

export function SessionsPage() {
  const { user } = useCurrentUser()
  const { bonds, sessions, enrollmentsBySession, reloadSessions } = useAppData()
  const { busy, feedback, run, clearFeedback } = useApiAction()

  const bondOptions = bonds.items
    .filter((b) => b.status === 'APPROVED' && (b.requester.id === user.id || b.evaluator?.id === user.id))
    .map((b) => ({ id: b.id, label: `${b.subject.name}, tutor ${displayName(b.requester.email)}` }))

  const handleCreate = (input: CreateSessionInput) =>
    run(async () => {
      await sessionsApi.create(input)
      await reloadSessions()
      return 'Session created.'
    })

  const handleCancelSession = (session: Session) =>
    run(async () => {
      await sessionsApi.cancel(session.id)
      await reloadSessions()
      return 'Session cancelled.'
    })

  const handleEnroll = (session: Session) =>
    run(async () => {
      const enrollment = await sessionsApi.enroll(session.id)
      await reloadSessions()
      return enrollment.status === 'WAITLISTED'
        ? `${user.label} joined the waitlist (position ${enrollment.queuePosition}).`
        : `${user.label} enrolled.`
    })

  const handleCancelEnrollment = (enrollment: Enrollment) =>
    run(async () => {
      await sessionsApi.cancelEnrollment(enrollment.id)
      await reloadSessions()
      return `Enrollment of ${displayName(enrollment.student.email)} cancelled.`
    })

  const days = sessions.items.reduce<{ key: string; sessions: Session[] }[]>((groups, session) => {
    const key = dayKey(session.startTime)
    const last = groups.at(-1)
    if (last?.key === key) last.sessions.push(session)
    else groups.push({ key, sessions: [session] })
    return groups
  }, [])

  return (
    <div className="page">
      <Feedback message={feedback} onClose={clearFeedback} />

      <CreateSessionForm userLabel={user.label} bondOptions={bondOptions} busy={busy} onSubmit={handleCreate} />

      <section className="timetable" aria-label="Timetable">
        <LoadState
          list={sessions}
          onRetry={reloadSessions}
          emptyText="No sessions yet. Create one above with an approved tutoring bond."
        />
        {days.map((day) => {
          const parts = dayParts(day.sessions[0].startTime)
          return (
            <div key={day.key} className="day">
              <h2 className="day-label">
                <span className="visually-hidden">{parts.full}</span>
                <span aria-hidden="true" className="day-weekday">
                  {parts.weekday}
                </span>
                <span aria-hidden="true" className="day-number tabular">
                  {parts.day}
                </span>
                <span aria-hidden="true" className="day-month">
                  {parts.month}
                </span>
              </h2>
              <div className="day-sessions">
                {day.sessions.map((session) => (
                  <SessionCard
                    key={session.id}
                    session={session}
                    enrollments={enrollmentsBySession[session.id] ?? []}
                    currentUserLabel={user.label}
                    busy={busy}
                    onEnroll={() => handleEnroll(session)}
                    onCancelSession={() => handleCancelSession(session)}
                    onCancelEnrollment={handleCancelEnrollment}
                  />
                ))}
              </div>
            </div>
          )
        })}
      </section>
    </div>
  )
}
