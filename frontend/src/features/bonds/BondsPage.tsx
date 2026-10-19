import { useState, type FormEvent } from 'react'
import { bondsApi } from '../../api/catalog'
import type { TutoringBond } from '../../api/types'
import { Feedback } from '../../components/Feedback'
import { StatusBadge } from '../../components/StatusBadge'
import { UuidInput } from '../../components/UuidInput'
import { KNOWN_SUBJECTS, userLabel } from '../../config/testData'
import { useCurrentUser } from '../../context/currentUserContext'
import { newestFirst, useKnownData } from '../../context/knownDataContext'
import { useApiAction } from '../../hooks/useApiAction'
import { shortId } from '../../lib/format'

type Decision = 'approve' | 'reject' | 'revoke'

const DECISION_TEXT: Record<Decision, string> = {
  approve: 'aprovado',
  reject: 'rejeitado',
  revoke: 'revogado (sessões futuras canceladas)',
}

export function BondsPage() {
  const { user } = useCurrentUser()
  const { subjects, bonds, saveBond } = useKnownData()
  const { busy, feedback, run, clearFeedback } = useApiAction()
  const [subjectUuid, setSubjectUuid] = useState('')

  const approvedSubjects = [
    ...KNOWN_SUBJECTS,
    ...newestFirst(subjects)
      .filter((s) => s.status === 'APPROVED' && !KNOWN_SUBJECTS.some((k) => k.id === s.id))
      .map((s) => ({ id: s.id, label: s.name })),
  ]

  function handleRequest(e: FormEvent) {
    e.preventDefault()
    run(async () => {
      const bond = await bondsApi.request(subjectUuid.trim(), user.id)
      saveBond(bond)
      return `Vínculo de ${user.label} em "${bond.subject.name}" pedido. Agora um admin precisa aprovar.`
    })
  }

  const decide = (bond: TutoringBond, decision: Decision) =>
    run(async () => {
      const updated = await bondsApi[decision](bond.id, user.id)
      saveBond(updated)
      return `Vínculo ${DECISION_TEXT[decision]}.`
    })

  const list = newestFirst(bonds)

  return (
    <div className="page">
      <Feedback message={feedback} onClose={clearFeedback} />

      <form className="card form" onSubmit={handleRequest}>
        <h2>Pedir vínculo de monitoria</h2>
        <UuidInput
          label="Matéria (precisa estar aprovada)"
          value={subjectUuid}
          onChange={setSubjectUuid}
          options={approvedSubjects}
        />
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={busy}>
            Pedir como {user.label}
          </button>
        </div>
      </form>

      <h2>Vínculos ({list.length})</h2>
      {list.length === 0 ? (
        <p className="muted">Nenhum vínculo pedido ou avaliado por esta tela ainda.</p>
      ) : (
        list.map((bond) => (
          <article key={bond.id} className="card row-card">
            <div className="row-main">
              <h3>
                {userLabel(bond.requester.id)} · {bond.subject.name}
              </h3>
              <p className="muted">
                {bond.evaluator ? `avaliado por ${userLabel(bond.evaluator.id)} · ` : ''}
                <span className="mono" title={bond.id}>
                  {shortId(bond.id)}
                </span>
              </p>
            </div>
            <StatusBadge status={bond.status} masculine />
            <div className="row-actions">
              {bond.status === 'PENDING' && (
                <>
                  <button type="button" className="btn btn-small btn-primary" disabled={busy} onClick={() => decide(bond, 'approve')}>
                    Aprovar
                  </button>
                  <button type="button" className="btn btn-small btn-danger" disabled={busy} onClick={() => decide(bond, 'reject')}>
                    Rejeitar
                  </button>
                </>
              )}
              {bond.status === 'APPROVED' && (
                <button type="button" className="btn btn-small btn-danger" disabled={busy} onClick={() => decide(bond, 'revoke')}>
                  Revogar
                </button>
              )}
            </div>
          </article>
        ))
      )}
    </div>
  )
}
