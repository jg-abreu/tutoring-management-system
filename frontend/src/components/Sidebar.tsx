import { useCurrentUser } from '../context/currentUserContext'
import { initials } from '../lib/format'

export type SectionLink = {
  id: string
  label: string
  count: number
}

type Props = {
  sections: SectionLink[]
  currentId: string
  onSelect: (id: string) => void
}

export function Sidebar({ sections, currentId, onSelect }: Props) {
  const { user, logout } = useCurrentUser()

  return (
    <aside className="sidebar">
      <nav aria-label="Sections">
        <ol className="section-list">
          {sections.map((section) => (
            <li key={section.id}>
              <button
                type="button"
                className="section-link"
                aria-current={section.id === currentId ? 'page' : undefined}
                onClick={() => onSelect(section.id)}
              >
                <span>{section.label}</span>
                <span className="section-count tabular" aria-label={`${section.count} known`}>
                  {section.count}
                </span>
              </button>
            </li>
          ))}
        </ol>
      </nav>

      <div className="user-card">
        <span className="avatar" aria-hidden="true">
          {initials(user.label)}
        </span>
        <div className="user-text">
          <strong>{user.label}</strong>
          <span className="muted">{user.role === 'ADMIN' ? 'Admin' : 'User'}</span>
        </div>
        <button type="button" className="btn btn-small btn-ghost" onClick={logout}>
          Log out
        </button>
      </div>
    </aside>
  )
}
