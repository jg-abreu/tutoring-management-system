import { ThemeToggle } from './ThemeToggle'

export function AppHeader() {
  return (
    <header className="app-header">
      <div className="app-header-inner">
        <span className="brand">
          <span className="brand-mark" aria-hidden="true">
            T
          </span>
          Tutoring
        </span>
        <a className="home-link" href="https://joaoabreu.dev">
          joaoabreu.dev
        </a>
        <ThemeToggle />
      </div>
    </header>
  )
}
