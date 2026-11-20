import { useState, type FormEvent } from 'react'
import { subjectsApi } from '../../api/catalog'
import type { Subject } from '../../api/types'
import { Feedback } from '../../components/Feedback'
import { StatusBadge } from '../../components/StatusBadge'
import { userLabel } from '../../config/testData'
import { useCurrentUser } from '../../context/currentUserContext'
import { newestFirst, useKnownData } from '../../context/knownDataContext'
import { useApiAction } from '../../hooks/useApiAction'
import { shortId } from '../../lib/format'

export function SubjectsPage() {
  const { user } = useCurrentUser()
  const { subjects, saveSubject } = useKnownData()
  const { busy, feedback, run, clearFeedback } = useApiAction()
  const [name, setName] = useState('')

  function handleSuggest(e: FormEvent) {
    e.preventDefault()
    run(async () => {
      const subject = await subjectsApi.suggest(user.id, name.trim())
      saveSubject(subject)
      setName('')
      return `Subject "${subject.name}" suggested. Now an admin needs to approve it.`
    })
  }

  const decide = (subject: Subject, approve: boolean) =>
    run(async () => {
      const updated = approve
        ? await subjectsApi.approve(subject.id, user.id)
        : await subjectsApi.reject(subject.id, user.id)
      saveSubject(updated)
      return `Subject "${updated.name}" ${approve ? 'approved' : 'rejected'}.`
    })

  const list = newestFirst(subjects)

  return (
    <div className="page">
      <Feedback message={feedback} onClose={clearFeedback} />

      <form className="card form" onSubmit={handleSuggest}>
        <h2>Suggest a subject</h2>
        <label className="field field-wide">
          <span>Subject name</span>
          {/* maxLength matches @Size(max = 255) in SuggestSubjectRequest */}
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Linear Algebra"
            maxLength={255}
            required
          />
        </label>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={busy}>
            Suggest as {user.label}
          </button>
        </div>
      </form>

      <h2>Subjects ({list.length})</h2>
      {list.length === 0 ? (
        <p className="muted">No subjects suggested from this screen yet.</p>
      ) : (
        list.map((subject) => (
          <article key={subject.id} className="card row-card">
            <div className="row-main">
              <h3>{subject.name}</h3>
              <p className="muted">
                suggested by {userLabel(subject.creator.id)}
                {subject.evaluator && ` · evaluated by ${userLabel(subject.evaluator.id)}`} ·{' '}
                <span className="mono" title={subject.id}>
                  {shortId(subject.id)}
                </span>
              </p>
            </div>
            <StatusBadge status={subject.status} />
            {subject.status === 'PENDING' && (
              <div className="row-actions">
                <button type="button" className="btn btn-small btn-primary" disabled={busy} onClick={() => decide(subject, true)}>
                  Approve
                </button>
                <button type="button" className="btn btn-small btn-danger" disabled={busy} onClick={() => decide(subject, false)}>
                  Reject
                </button>
              </div>
            )}
          </article>
        ))
      )}
    </div>
  )
}
