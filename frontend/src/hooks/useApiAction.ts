import { useState } from 'react'
import { ApiError } from '../api/client'
import type { FeedbackMessage } from '../components/Feedback'

// Executa uma chamada à API controlando "ocupado" e a mensagem de sucesso/erro.
// A ação devolve o texto de sucesso; erros da API viram "Erro <status>: <mensagem>".
export function useApiAction() {
  const [busy, setBusy] = useState(false)
  const [feedback, setFeedback] = useState<FeedbackMessage | null>(null)

  async function run(action: () => Promise<string>) {
    setBusy(true)
    setFeedback(null)
    try {
      setFeedback({ kind: 'success', text: await action() })
    } catch (error) {
      let text = 'Erro inesperado.'
      if (error instanceof ApiError) {
        text = error.status ? `Erro ${error.status}: ${error.message}` : error.message
      }
      setFeedback({ kind: 'error', text })
    } finally {
      setBusy(false)
    }
  }

  return { busy, feedback, run, clearFeedback: () => setFeedback(null) }
}
