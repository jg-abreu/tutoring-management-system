import { Sidebar } from './components/Sidebar'
import { AppHeader } from './components/AppHeader'
import { AppDataProvider } from './context/AppData'
import { useAppData } from './context/appDataContext'
import { CurrentUserProvider } from './context/CurrentUser'
import { useCurrentUser, useSession } from './context/currentUserContext'
import { BondsPage } from './features/bonds/BondsPage'
import { LoginPage } from './features/login/LoginPage'
import { SessionsPage } from './features/sessions/SessionsPage'
import { SubjectsPage } from './features/subjects/SubjectsPage'
import { useLocalStorage } from './hooks/useLocalStorage'

const SECTIONS = [
  { id: 'subjects', label: 'Subjects', page: SubjectsPage },
  { id: 'bonds', label: 'Tutoring bonds', page: BondsPage },
  { id: 'sessions', label: 'Sessions', page: SessionsPage },
] as const

type SectionId = (typeof SECTIONS)[number]['id']

function Shell() {
  const { user } = useCurrentUser()
  return (
    <AppDataProvider key={user.id}>
      <ShellContent />
    </AppDataProvider>
  )
}

function ShellContent() {
  const [sectionId, setSectionId] = useLocalStorage<SectionId>('currentTab', 'sessions')
  const { subjects, bonds, sessions } = useAppData()
  const { user } = useCurrentUser()
  const section = SECTIONS.find((s) => s.id === sectionId) ?? SECTIONS[2]
  const Page = section.page

  const counts: Record<SectionId, number> = {
    subjects: subjects.items.length,
    bonds: bonds.items.length,
    sessions: sessions.items.length,
  }

  return (
    <>
      <AppHeader />
      <div className="app-body">
        <Sidebar
          sections={SECTIONS.map((s) => ({ id: s.id, label: s.label, count: counts[s.id] }))}
          currentId={section.id}
          onSelect={(id) => setSectionId(id as SectionId)}
        />

        <main className="content">
          <h1>{section.label}</h1>
          <Page key={user.id} />
        </main>
      </div>
    </>
  )
}

function AppContent() {
  const { user } = useSession()
  return user ? <Shell /> : <LoginPage />
}

export default function App() {
  return (
    <CurrentUserProvider>
      <AppContent />
    </CurrentUserProvider>
  )
}
