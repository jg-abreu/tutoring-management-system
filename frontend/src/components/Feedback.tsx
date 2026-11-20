export type FeedbackMessage = {
  kind: 'success' | 'error'
  text: string
}

export function Feedback({ message, onClose }: { message: FeedbackMessage | null; onClose: () => void }) {
  if (!message) return null

  return (
    <div className={`feedback feedback-${message.kind}`} role={message.kind === 'error' ? 'alert' : 'status'}>
      <span>{message.text}</span>
      <button type="button" className="feedback-close" onClick={onClose} aria-label="Close">
        ×
      </button>
    </div>
  )
}
