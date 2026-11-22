const LABELS: Record<string, string> = {
  ACTIVE: 'Active',
  CANCELLED: 'Cancelled',
  CONFIRMED: 'Confirmed',
  WAITLISTED: 'Waitlisted',
  REJECTED: 'Rejected',
  PENDING: 'Pending',
  APPROVED: 'Approved',
  REVOKED: 'Revoked',
}

type Props = {
  status: string
  suffix?: string
}

export function StatusBadge({ status, suffix }: Props) {
  return (
    <span className={`badge badge-${status.toLowerCase()}`}>
      {LABELS[status] ?? status}
      {suffix ? ` ${suffix}` : ''}
    </span>
  )
}
