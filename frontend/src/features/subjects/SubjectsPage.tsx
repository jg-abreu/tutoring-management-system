import { useState, type FormEvent } from 'react'
import { subjectsApi } from '../../api/catalog'
import type { Subject } from '../../api/types'
import { Feedback } from '../../components/Feedback'
import { LoadState } from '../../components/LoadState'
import { StatusBadge } from '../../components/StatusBadge'
import { useAppData } from '../../context/appDataContext'
import { useCurrentUser } from '../../context/currentUserContext'
import { useApiAction } from '../../hooks/useApiAction'
import { displayName, shortId } from '../../lib/format'

export function SubjectsPage() {
  const { user } = useCurrentUser()
  const { subjects, reloadSubjects } = useAppData()
  const { busy, feedback, run, clearFeedback } = useApiAction()
  const [name, setName] = useState('')
  const isAdmin = user.role === 'ADMIN'

  function handleSuggest(e: FormEvent) {
    e.preventDefault()
    run(async () => {
      const subject = await subjectsApi.suggest(name.trim())
      setName('')
      await reloadSubjects()
      return `Subject "${subject.name}" suggested. Now an admin needs to approve it.`
    })
  }

  const decide = (subject: Subject, approve: boolean) =>
    run(async () => {
      const updated = approve ? await subjectsApi.approve(subject.id) : await subjectsApi.reject(subject.id)
      await reloadSubjects()
      return `Subject "${updated.name}" ${approve ? 'approved' : 'rejected'}.`
    })

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

      <section aria-labelledby="subject-list">
        <h2 id="subject-list" className="visually-hidden">
          Subject list
        </h2>
        <LoadState list={subjects} onRetry={reloadSubjects} emptyText="No subjects yet. Suggest one above; an admin approves or rejects it." />
        {subjects.items.length > 0 && (
          <ul className="item-list">
            {subjects.items.map((subject) => (
              <li key={subject.id} className="item">
                <div className="item-main">
                  <h3>{subject.name}</h3>
                  <p className="item-meta muted">
                    <span>Suggested by {displayName(subject.creator.email)}</span>
                    {subject.evaluator && <span>Evaluated by {displayName(subject.evaluator.email)}</span>}
                    <span className="mono" title={subject.id}>
                      {shortId(subject.id)}
                    </span>
                  </p>
                </div>
                <StatusBadge status={subject.status} />
                {isAdmin && subject.status === 'PENDING' && (
                  <div className="item-actions">
                    <button type="button" className="btn btn-small btn-primary" disabled={busy} onClick={() => decide(subject, true)}>
                      Approve
                    </button>
                    <button type="button" className="btn btn-small btn-danger" disabled={busy} onClick={() => decide(subject, false)}>
                      Reject
                    </button>
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
