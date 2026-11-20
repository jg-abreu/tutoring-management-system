import type { Enrollment, Session } from '../../api/types'
import { SeatMap } from '../../components/SeatMap'
import { StatusBadge } from '../../components/StatusBadge'
import { displayName, formatTimeRange, shortId } from '../../lib/format'

type Props = {
  session: Session
  enrollments: Enrollment[]
  currentUserLabel: string
  busy: boolean
  onEnroll: () => void
  onCancelSession: () => void
  onCancelEnrollment: (enrollment: Enrollment) => void
}

export function SessionCard({
  session,
  enrollments,
  currentUserLabel,
  busy,
  onEnroll,
  onCancelSession,
  onCancelEnrollment,
}: Props) {
  const active = session.status === 'ACTIVE'

  return (
    <article className={`session-row${active ? '' : ' session-cancelled'}`}>
      <header className="session-head">
        <p className="session-time tabular">{formatTimeRange(session.startTime, session.endTime)}</p>
        <h3>{session.bond.subject.name}</h3>
        <p className="muted">Tutor {displayName(session.bond.requester.email)}</p>
        <StatusBadge status={session.status} />
      </header>

      <SeatMap spots={session.spots} allowsWaitlist={session.allowsWaitlist} enrollments={enrollments} />

      {active && (
        <div className="session-actions">
          <button type="button" className="btn btn-primary btn-small" disabled={busy} onClick={onEnroll}>
            Enroll {currentUserLabel}
          </button>
          <button type="button" className="btn btn-danger btn-small" disabled={busy} onClick={onCancelSession}>
            Cancel session
          </button>
        </div>
      )}

      {enrollments.length > 0 && (
        <ul className="enrollment-list">
          {enrollments.map((enrollment) => (
            <li key={enrollment.id}>
              <span className="enrollment-name">{displayName(enrollment.student.email)}</span>
              <StatusBadge
                status={enrollment.status}
                suffix={enrollment.queuePosition ? `#${enrollment.queuePosition}` : undefined}
              />
              {active && (enrollment.status === 'CONFIRMED' || enrollment.status === 'WAITLISTED') && (
                <button
                  type="button"
                  className="btn btn-small btn-ghost"
                  disabled={busy}
                  onClick={() => onCancelEnrollment(enrollment)}
                >
                  Cancel as {currentUserLabel}
                </button>
              )}
            </li>
          ))}
        </ul>
      )}

      <p className="session-id muted">
        Session ID{' '}
        <span className="mono" title={session.id}>
          {shortId(session.id)}
        </span>
      </p>
    </article>
  )
}
