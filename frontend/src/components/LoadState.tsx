import type { ListState } from '../context/appDataContext'

type Props = {
  list: ListState<unknown>
  onRetry: () => void
  emptyText: string
}

export function LoadState({ list, onRetry, emptyText }: Props) {
  if (list.error) {
    return (
      <div className="feedback feedback-error" role="alert">
        <span className="feedback-text">Could not load this list. {list.error}</span>
        <button type="button" className="btn btn-small" onClick={onRetry}>
          Try again
        </button>
      </div>
    )
  }
  if (list.items.length > 0) return null
  return <p className="empty muted">{list.loading ? 'Loading…' : emptyText}</p>
}
