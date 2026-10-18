const LABELS: Record<string, string> = {
  ACTIVE: 'Ativa',
  CANCELLED: 'Cancelada',
  CONFIRMED: 'Confirmada',
  WAITLISTED: 'Lista de espera',
  REJECTED: 'Rejeitada',
  PENDING: 'Pendente',
  APPROVED: 'Aprovada',
  REVOKED: 'Revogada',
}

export function StatusBadge({ status, suffix }: { status: string; suffix?: string }) {
  return (
    <span className={`badge badge-${status.toLowerCase()}`}>
      {LABELS[status] ?? status}
      {suffix ? ` ${suffix}` : ''}
    </span>
  )
}
