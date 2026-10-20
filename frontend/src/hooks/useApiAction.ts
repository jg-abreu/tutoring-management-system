import { useState } from 'react'
import { ApiError } from '../api/client'
import type { FeedbackMessage } from '../components/Feedback'

// Runs an API call, tracking the "busy" state and the success/error message.
// The action returns the success text; API errors become "Error <status>: <message>".
export function useApiAction() {
  const [busy, setBusy] = useState(false)
  const [feedback, setFeedback] = useState<FeedbackMessage | null>(null)

  async function run(action: () => Promise<string>) {
    setBusy(true)
    setFeedback(null)
    try {
      setFeedback({ kind: 'success', text: await action() })
    } catch (error) {
      let text = 'Unexpected error.'
      if (error instanceof ApiError) {
        text = error.status ? `Error ${error.status}: ${error.message}` : error.message
      }
      setFeedback({ kind: 'error', text })
    } finally {
      setBusy(false)
    }
  }

  return { busy, feedback, run, clearFeedback: () => setFeedback(null) }
}
