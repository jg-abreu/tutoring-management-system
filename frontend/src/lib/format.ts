// "2026-11-20T14:00" (input datetime-local, sem fuso) -> "2026-11-20T14:00:00-03:00".
// A API recebe OffsetDateTime, então o fuso do navegador precisa ir junto.
export function toOffsetDateTime(localValue: string): string {
  const offsetMinutes = -new Date(localValue).getTimezoneOffset()
  const sign = offsetMinutes >= 0 ? '+' : '-'
  const abs = Math.abs(offsetMinutes)
  const hh = String(Math.floor(abs / 60)).padStart(2, '0')
  const mm = String(abs % 60).padStart(2, '0')
  return `${localValue}:00${sign}${hh}:${mm}`
}

const dateTimeFormat = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'short',
  timeStyle: 'short',
})

const timeFormat = new Intl.DateTimeFormat('pt-BR', { timeStyle: 'short' })

export function formatRange(start: string, end: string): string {
  return `${dateTimeFormat.format(new Date(start))} – ${timeFormat.format(new Date(end))}`
}

export function shortId(id: string): string {
  return id.slice(0, 8)
}
