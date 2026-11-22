import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@fontsource-variable/inter/wght.css'
import '@fontsource/jetbrains-mono/latin-400.css'
import './index.css'
import App from './App.tsx'

for (const key of ['knownSubjects', 'knownBonds', 'knownSessions', 'knownEnrollments', 'currentUserId']) {
  try {
    localStorage.removeItem(key)
  } catch {
    break
  }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
