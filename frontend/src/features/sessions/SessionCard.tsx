import type { Enrollment, Session } from '../../api/types'
import { StatusBadge } from '../../components/StatusBadge'
import { userLabel } from '../../config/testData'
import { formatRange, shortId } from '../../lib/format'

type Props = {
  session: Session
  enrollments: Enrollment[]
  currentUserId: string
  busy: boolean
  onEnroll: () => void
  onCancelSession: () => void
  onCancelEnrollment: (enrollment: Enrollment) => void
}

export function SessionCard({
  session,
  enrollments,
  currentUserId,
  busy,
  onEnroll,
  onCancelSession,
  onCancelEnrollment,
}: Props) {
  const active = session.status === 'ACTIVE'

  return (
    <article className="card session-card">
      <header className="session-header">
        <div>
          <h3>{session.bond.subject.name}</h3>
          <p className="muted">
            {formatRange(session.startTime, session.endTime)} · tutor {userLabel(session.bond.requester.id)}
          </p>
        </div>
        <StatusBadge status={session.status} />
      </header>

      <dl className="session-facts">
        <div>
          <dt>Capacity</dt>
          <dd>{session.spots}</dd>
        </div>
        <div>
          <dt>Waitlist</dt>
          <dd>{session.allowsWaitlist ? 'Yes' : 'No'}</dd>
        </div>
        <div>
          <dt>ID</dt>
          <dd className="mono" title={session.id}>
            {shortId(session.id)}
          </dd>
        </div>
      </dl>

      {active && (
        <div className="session-actions">
          <button type="button" className="btn btn-primary" disabled={busy} onClick={onEnroll}>
            Enroll {userLabel(currentUserId)}
          </button>
          <button type="button" className="btn btn-danger" disabled={busy} onClick={onCancelSession}>
            Cancel session
          </button>
        </div>
      )}

      <h4>Enrollments made from this screen</h4>
      {enrollments.length === 0 ? (
        <p className="muted">None yet.</p>
      ) : (
        <ul className="enrollment-list">
          {enrollments.map((enrollment) => (
            <li key={enrollment.id}>
              <span>{userLabel(enrollment.student.id)}</span>
              <StatusBadge
                status={enrollment.status}
                suffix={enrollment.queuePosition ? `#${enrollment.queuePosition}` : undefined}
              />
              {active && (enrollment.status === 'CONFIRMED' || enrollment.status === 'WAITLISTED') && (
                <button
                  type="button"
                  className="btn btn-small"
                  disabled={busy}
                  onClick={() => onCancelEnrollment(enrollment)}
                >
                  Cancel as {userLabel(currentUserId)}
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </article>
  )
}
