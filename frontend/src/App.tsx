import { UserSwitcher } from './components/UserSwitcher'
import { CurrentUserProvider } from './context/CurrentUser'
import { KnownDataProvider } from './context/KnownData'
import { useKnownData } from './context/knownDataContext'
import { BondsPage } from './features/bonds/BondsPage'
import { SessionsPage } from './features/sessions/SessionsPage'
import { SubjectsPage } from './features/subjects/SubjectsPage'
import { useLocalStorage } from './hooks/useLocalStorage'

const TABS = [
  { id: 'subjects', label: '1. Matérias', page: SubjectsPage },
  { id: 'bonds', label: '2. Vínculos', page: BondsPage },
  { id: 'sessions', label: '3. Sessões', page: SessionsPage },
] as const

type TabId = (typeof TABS)[number]['id']

function Shell() {
  const [tabId, setTabId] = useLocalStorage<TabId>('currentTab', 'sessions')
  const { clearAll } = useKnownData()
  const Page = (TABS.find((t) => t.id === tabId) ?? TABS[2]).page

  return (
    <>
      <header className="app-header">
        <div className="app-title">
          <h1>Monitoria</h1>
        </div>
        <UserSwitcher />
      </header>

      <nav className="tabs" aria-label="Seções">
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
            <strong>Limitação atual:</strong> a API ainda não tem endpoints <code>GET</code>. As listas mostram só o
            que foi criado ou alterado por esta tela (guardado no navegador), e mudanças feitas pelo servidor em outros
            registros — como a promoção da lista de espera — não aparecem sozinhas.
          </div>
          <button type="button" className="btn btn-small" onClick={clearAll}>
            Limpar dados locais
          </button>
        </section>
        <Page />
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
