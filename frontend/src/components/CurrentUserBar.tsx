import { useCurrentUser } from '../context/currentUserContext'

export function CurrentUserBar() {
  const { user, logout } = useCurrentUser()

  return (
    <div className="user-switcher">
      <span>
        Logged in as <strong>{user.label}</strong> <span className="muted">({user.role})</span>
      </span>
      <button type="button" className="btn btn-small" onClick={logout}>
        Log out
      </button>
    </div>
  )
}
