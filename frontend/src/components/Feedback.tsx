export type FeedbackMessage = {
  kind: 'success' | 'error'
  text: string
}

function FeedbackIcon({ kind }: { kind: FeedbackMessage['kind'] }) {
  return (
    <svg className="feedback-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
      <circle cx="12" cy="12" r="9" />
      {kind === 'success' ? <path d="m8 12.5 2.5 2.5L16 9.5" /> : <path d="M12 7.5v5.5M12 16.5v.01" />}
    </svg>
  )
}

export function Feedback({ message, onClose }: { message: FeedbackMessage | null; onClose: () => void }) {
  if (!message) return null

  return (
    <div className={`feedback feedback-${message.kind}`} role={message.kind === 'error' ? 'alert' : 'status'}>
      <FeedbackIcon kind={message.kind} />
      <span className="feedback-text">{message.text}</span>
      <button type="button" className="feedback-close" onClick={onClose} aria-label="Close message">
        ×
      </button>
    </div>
  )
}
