import { useState } from 'react'
import type { Enrollment } from '../api/types'
import { displayName, initials } from '../lib/format'

type Props = {
  spots: number
  allowsWaitlist: boolean
  enrollments: Enrollment[]
}

export function SeatMap({ spots, allowsWaitlist, enrollments }: Props) {
  const confirmed = enrollments.filter((e) => e.status === 'CONFIRMED')
  const waitlist = enrollments
    .filter((e) => e.status === 'WAITLISTED')
    .sort((a, b) => (a.queuePosition ?? 0) - (b.queuePosition ?? 0))
  const [firstSeen] = useState(() => new Set(confirmed.map((e) => e.id)))

  const seats = Array.from({ length: Math.max(spots, confirmed.length) }, (_, i) => confirmed[i] ?? null)
  const free = Math.max(spots - confirmed.length, 0)
  const summary = [
    `${confirmed.length} of ${spots} spots taken`,
    allowsWaitlist ? `${waitlist.length} on the waitlist` : 'no waitlist',
  ].join(', ')

  return (
    <div className="seat-map">
      <div className="seats" role="img" aria-label={summary}>
        {seats.map((enrollment, i) =>
          enrollment ? (
            <span
              key={enrollment.id}
              className={`seat seat-taken${firstSeen.has(enrollment.id) ? '' : ' seat-new'}`}
              title={`${displayName(enrollment.student.email)}, confirmed`}
            >
              {initials(displayName(enrollment.student.email))}
            </span>
          ) : (
            <span key={`free-${i}`} className="seat" title="Free spot" />
          ),
        )}
        {waitlist.length > 0 && <span className="seat-divider" />}
        {waitlist.map((enrollment) => (
          <span
            key={enrollment.id}
            className="seat seat-waiting"
            title={`${displayName(enrollment.student.email)}, waitlist position ${enrollment.queuePosition}`}
          >
            {initials(displayName(enrollment.student.email))}
            <span className="seat-position tabular">{enrollment.queuePosition}</span>
          </span>
        ))}
      </div>
      <p className="seat-legend muted tabular" aria-hidden="true">
        <span>{free === 0 ? 'Full' : `${free} free`}</span>
        <span>{allowsWaitlist ? `${waitlist.length} waiting` : 'No waitlist'}</span>
      </p>
    </div>
  )
}
