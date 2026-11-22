import { useState, type FormEvent } from 'react'
import { ApiError } from '../../api/client'
import { AppHeader } from '../../components/AppHeader'
import { Feedback, type FeedbackMessage } from '../../components/Feedback'
import { SAMPLE_PASSWORD, TEST_USERS } from '../../config/testData'
import { useSession } from '../../context/currentUserContext'
import { displayName, initials } from '../../lib/format'

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
    <>
      <AppHeader />
      <main className="login-page">
        <div className="login-intro">
          <h1>Log in</h1>
          <p className="muted">
            Admins approve who can tutor each subject, tutors open sessions with limited spots, and students enroll, with
            an automatic waitlist when a session is full.
          </p>
        </div>

        <form className="card form login-form" onSubmit={handleSubmit}>
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

        <section className="sample-users" aria-labelledby="sample-users-title">
          <h2 id="sample-users-title">Sample users</h2>
          <p className="muted">From the API's sample data, all with the same password. Pick one to fill in the form.</p>
          <ul className="sample-user-list">
            {TEST_USERS.map((u) => (
              <li key={u.id}>
                <button
                  type="button"
                  className={`sample-user${email === u.email ? ' sample-user-selected' : ''}`}
                  aria-pressed={email === u.email}
                  onClick={() => fillIn(u.email)}
                >
                  <span className="avatar" aria-hidden="true">
                    {initials(displayName(u.email))}
                  </span>
                  <span className="sample-user-text">
                    <strong>{displayName(u.email)}</strong>
                    <span className="muted">{u.email}</span>
                  </span>
                  <span className="role-tag">{u.role === 'Admin' ? 'Admin' : `User, ${u.role.toLowerCase()}`}</span>
                </button>
              </li>
            ))}
          </ul>
        </section>
      </main>
    </>
  )
}
