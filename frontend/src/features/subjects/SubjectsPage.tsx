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
      return `Matéria "${subject.name}" sugerida. Agora um admin precisa aprovar.`
    })
  }

  const decide = (subject: Subject, approve: boolean) =>
    run(async () => {
      const updated = approve
        ? await subjectsApi.approve(subject.id, user.id)
        : await subjectsApi.reject(subject.id, user.id)
      saveSubject(updated)
      return `Matéria "${updated.name}" ${approve ? 'aprovada' : 'rejeitada'}.`
    })

  const list = newestFirst(subjects)

  return (
    <div className="page">
      <Feedback message={feedback} onClose={clearFeedback} />

      <form className="card form" onSubmit={handleSuggest}>
        <h2>Sugerir matéria</h2>
        <label className="field field-wide">
          <span>Nome da matéria</span>
          {/* maxLength igual ao @Size(max = 255) do SuggestSubjectRequest */}
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Ex.: Álgebra Linear"
            maxLength={255}
            required
          />
        </label>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={busy}>
            Sugerir como {user.label}
          </button>
        </div>
      </form>

      <h2>Matérias ({list.length})</h2>
      {list.length === 0 ? (
        <p className="muted">Nenhuma matéria sugerida por esta tela ainda.</p>
      ) : (
        list.map((subject) => (
          <article key={subject.id} className="card row-card">
            <div className="row-main">
              <h3>{subject.name}</h3>
              <p className="muted">
                sugerida por {userLabel(subject.creator.id)}
                {subject.evaluator && ` · avaliada por ${userLabel(subject.evaluator.id)}`} ·{' '}
                <span className="mono" title={subject.id}>
                  {shortId(subject.id)}
                </span>
              </p>
            </div>
            <StatusBadge status={subject.status} />
            {subject.status === 'PENDING' && (
              <div className="row-actions">
                <button type="button" className="btn btn-small btn-primary" disabled={busy} onClick={() => decide(subject, true)}>
                  Aprovar
                </button>
                <button type="button" className="btn btn-small btn-danger" disabled={busy} onClick={() => decide(subject, false)}>
                  Rejeitar
                </button>
              </div>
            )}
          </article>
        ))
      )}
    </div>
  )
}
