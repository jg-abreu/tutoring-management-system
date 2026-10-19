// Matéria, sessão e matrícula são substantivos femininos; vínculo é masculino.
const FEMININE: Record<string, string> = {
  ACTIVE: 'Ativa',
  CANCELLED: 'Cancelada',
  CONFIRMED: 'Confirmada',
  WAITLISTED: 'Lista de espera',
  REJECTED: 'Rejeitada',
  PENDING: 'Pendente',
  APPROVED: 'Aprovada',
  REVOKED: 'Revogada',
}

const MASCULINE: Record<string, string> = {
  ...FEMININE,
  REJECTED: 'Rejeitado',
  APPROVED: 'Aprovado',
  REVOKED: 'Revogado',
}

type Props = {
  status: string
  suffix?: string
  masculine?: boolean
}

export function StatusBadge({ status, suffix, masculine = false }: Props) {
  const labels = masculine ? MASCULINE : FEMININE

  return (
    <span className={`badge badge-${status.toLowerCase()}`}>
      {labels[status] ?? status}
      {suffix ? ` ${suffix}` : ''}
    </span>
  )
}
