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
  approve: 'approved',
  reject: 'rejected',
  revoke: 'revoked (future sessions cancelled)',
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
      return `Tutoring bond for ${user.label} in "${bond.subject.name}" requested. Now an admin needs to approve it.`
    })
  }

  const decide = (bond: TutoringBond, decision: Decision) =>
    run(async () => {
      const updated = await bondsApi[decision](bond.id, user.id)
      saveBond(updated)
      return `Tutoring bond ${DECISION_TEXT[decision]}.`
    })

  const list = newestFirst(bonds)

  return (
    <div className="page">
      <Feedback message={feedback} onClose={clearFeedback} />

      <form className="card form" onSubmit={handleRequest}>
        <h2>Request a tutoring bond</h2>
        <UuidInput
          label="Subject (must be approved)"
          value={subjectUuid}
          onChange={setSubjectUuid}
          options={approvedSubjects}
        />
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={busy}>
            Request as {user.label}
          </button>
        </div>
      </form>

      <h2>Tutoring bonds ({list.length})</h2>
      {list.length === 0 ? (
        <p className="muted">No tutoring bonds requested or evaluated from this screen yet.</p>
      ) : (
        list.map((bond) => (
          <article key={bond.id} className="card row-card">
            <div className="row-main">
              <h3>
                {userLabel(bond.requester.id)} · {bond.subject.name}
              </h3>
              <p className="muted">
                {bond.evaluator ? `evaluated by ${userLabel(bond.evaluator.id)} · ` : ''}
                <span className="mono" title={bond.id}>
                  {shortId(bond.id)}
                </span>
              </p>
            </div>
            <StatusBadge status={bond.status} />
            <div className="row-actions">
              {bond.status === 'PENDING' && (
                <>
                  <button type="button" className="btn btn-small btn-primary" disabled={busy} onClick={() => decide(bond, 'approve')}>
                    Approve
                  </button>
                  <button type="button" className="btn btn-small btn-danger" disabled={busy} onClick={() => decide(bond, 'reject')}>
                    Reject
                  </button>
                </>
              )}
              {bond.status === 'APPROVED' && (
                <button type="button" className="btn btn-small btn-danger" disabled={busy} onClick={() => decide(bond, 'revoke')}>
                  Revoke
                </button>
              )}
            </div>
          </article>
        ))
      )}
    </div>
  )
}
