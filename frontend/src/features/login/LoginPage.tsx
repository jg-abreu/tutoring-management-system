import { useState, type FormEvent } from 'react'
import { ApiError } from '../../api/client'
import { Feedback, type FeedbackMessage } from '../../components/Feedback'
import { SAMPLE_PASSWORD, TEST_USERS } from '../../config/testData'
import { useSession } from '../../context/currentUserContext'

export function LoginPage() {
  const { login, sessionExpired } = useSession()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [feedback, setFeedback] = useState<FeedbackMessage | null>(
    sessionExpired ? { kind: 'error', text: 'Your session expired. Log in again.' } : null,
  )

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setFeedback(null)
    try {
      await login(email.trim(), password)
    } catch (error) {
      const text = error instanceof ApiError ? error.message : 'Unexpected error.'
      setFeedback({ kind: 'error', text })
      setBusy(false)
    }
  }

  function fillIn(sampleEmail: string) {
    setEmail(sampleEmail)
    setPassword(SAMPLE_PASSWORD)
  }

  return (
    <main className="login-page">
      <form className="card form" onSubmit={handleSubmit}>
        <h1>Tutoring</h1>
        <p className="muted field-wide">Log in to manage subjects, tutoring bonds and sessions.</p>
        <Feedback message={feedback} onClose={() => setFeedback(null)} />
        <label className="field field-wide">
          <span>Email</span>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="username" required />
        </label>
        <label className="field field-wide">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </label>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? 'Logging in…' : 'Log in'}
          </button>
        </div>
      </form>

      <section className="card sample-users">
        <h2>Sample users</h2>
        <p className="muted">From the API's sample data. Click one to fill in the form.</p>
        <div className="sample-user-list">
          {TEST_USERS.map((u) => (
            <button key={u.id} type="button" className="btn btn-small" onClick={() => fillIn(u.email)}>
              {u.label} · {u.role}
            </button>
          ))}
        </div>
      </section>
    </main>
  )
}
