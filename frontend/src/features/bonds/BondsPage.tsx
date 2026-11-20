import { useState, type FormEvent } from 'react'
import { bondsApi } from '../../api/catalog'
import type { TutoringBond } from '../../api/types'
import { Feedback } from '../../components/Feedback'
import { LoadState } from '../../components/LoadState'
import { RecordSelect } from '../../components/RecordSelect'
import { StatusBadge } from '../../components/StatusBadge'
import { useAppData } from '../../context/appDataContext'
import { useCurrentUser } from '../../context/currentUserContext'
import { useApiAction } from '../../hooks/useApiAction'
import { displayName, shortId } from '../../lib/format'

type Decision = 'approve' | 'reject' | 'revoke'

const DECISION_TEXT: Record<Decision, string> = {
  approve: 'approved',
  reject: 'rejected',
  revoke: 'revoked (future sessions cancelled)',
}

export function BondsPage() {
  const { user } = useCurrentUser()
  const { subjects, bonds, reloadBonds, reloadSessions } = useAppData()
  const { busy, feedback, run, clearFeedback } = useApiAction()
  const [subjectUuid, setSubjectUuid] = useState('')
  const isAdmin = user.role === 'ADMIN'

  const approvedSubjects = subjects.items
    .filter((s) => s.status === 'APPROVED')
    .map((s) => ({ id: s.id, label: s.name }))

  function handleRequest(e: FormEvent) {
    e.preventDefault()
    run(async () => {
      const bond = await bondsApi.request(subjectUuid)
      setSubjectUuid('')
      await reloadBonds()
      return `Tutoring bond for ${user.label} in "${bond.subject.name}" requested. Now an admin needs to approve it.`
    })
  }

  const decide = (bond: TutoringBond, decision: Decision) =>
    run(async () => {
      await bondsApi[decision](bond.id)
      await Promise.all(decision === 'revoke' ? [reloadBonds(), reloadSessions()] : [reloadBonds()])
      return `Tutoring bond ${DECISION_TEXT[decision]}.`
    })

  return (
    <div className="page">
      <Feedback message={feedback} onClose={clearFeedback} />

      <form className="card form" onSubmit={handleRequest}>
        <h2>Request a tutoring bond</h2>
        <RecordSelect
          label="Subject"
          value={subjectUuid}
          onChange={setSubjectUuid}
          options={approvedSubjects}
          placeholder="Pick an approved subject"
          emptyText="No approved subjects yet"
        />
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={busy || approvedSubjects.length === 0}>
            Request as {user.label}
          </button>
        </div>
      </form>

      <section aria-labelledby="bond-list">
        <h2 id="bond-list" className="visually-hidden">
          Tutoring bond list
        </h2>
        <LoadState
          list={bonds}
          onRetry={reloadBonds}
          emptyText={isAdmin ? 'No tutoring bonds yet.' : 'You have no tutoring bonds yet. Request one above for an approved subject.'}
        />
        {bonds.items.length > 0 && (
          <ul className="item-list">
            {bonds.items.map((bond) => (
              <li key={bond.id} className="item">
                <div className="item-main">
                  <h3>{bond.subject.name}</h3>
                  <p className="item-meta muted">
                    <span>Tutor {displayName(bond.requester.email)}</span>
                    {bond.evaluator && <span>Evaluated by {displayName(bond.evaluator.email)}</span>}
                    <span className="mono" title={bond.id}>
                      {shortId(bond.id)}
                    </span>
                  </p>
                </div>
                <StatusBadge status={bond.status} />
                {isAdmin && (bond.status === 'PENDING' || bond.status === 'APPROVED') && (
                  <div className="item-actions">
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
                )}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
