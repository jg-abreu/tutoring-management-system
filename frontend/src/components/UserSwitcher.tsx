import { TEST_USERS } from '../config/testData'
import { useCurrentUser } from '../context/currentUserContext'

export function UserSwitcher() {
  const { user, setUserId } = useCurrentUser()

  return (
    <label className="user-switcher">
      <span>Agindo como</span>
      <select value={user.id} onChange={(e) => setUserId(e.target.value)}>
        {TEST_USERS.map((u) => (
          <option key={u.id} value={u.id}>
            {u.label} ({u.role})
          </option>
        ))}
      </select>
    </label>
  )
}
