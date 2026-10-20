import { UserSwitcher } from './components/UserSwitcher'
import { CurrentUserProvider } from './context/CurrentUser'
import { useCurrentUser } from './context/currentUserContext'
import { KnownDataProvider } from './context/KnownData'
import { useKnownData } from './context/knownDataContext'
import { BondsPage } from './features/bonds/BondsPage'
import { SessionsPage } from './features/sessions/SessionsPage'
import { SubjectsPage } from './features/subjects/SubjectsPage'
import { useLocalStorage } from './hooks/useLocalStorage'

const TABS = [
  { id: 'subjects', label: '1. Subjects', page: SubjectsPage },
  { id: 'bonds', label: '2. Tutoring bonds', page: BondsPage },
  { id: 'sessions', label: '3. Sessions', page: SessionsPage },
] as const

type TabId = (typeof TABS)[number]['id']

function Shell() {
  const [tabId, setTabId] = useLocalStorage<TabId>('currentTab', 'sessions')
  const { clearAll } = useKnownData()
  const { user } = useCurrentUser()
  const Page = (TABS.find((t) => t.id === tabId) ?? TABS[2]).page

  return (
    <>
      <header className="app-header">
        <div className="app-title">
          <h1>Tutoring</h1>
        </div>
        <UserSwitcher />
      </header>

      <nav className="tabs" aria-label="Sections">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            type="button"
            className={`tab${tab.id === tabId ? ' tab-active' : ''}`}
            aria-current={tab.id === tabId ? 'page' : undefined}
            onClick={() => setTabId(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <main>
        <section className="notice">
          <div>
            <strong>Current limitation:</strong> the API has no <code>GET</code> endpoints yet. The lists only show
            what was created or changed from this screen (stored in the browser), and changes the server makes to other
            records — such as waitlist promotions — don't show up on their own.
          </div>
          <button type="button" className="btn btn-small" onClick={clearAll}>
            Clear local data
          </button>
        </section>
        {/* key: switching users remounts the page, clearing the message and the forms */}
        <Page key={user.id} />
      </main>
    </>
  )
}

export default function App() {
  return (
    <CurrentUserProvider>
      <KnownDataProvider>
        <Shell />
      </KnownDataProvider>
    </CurrentUserProvider>
  )
}
