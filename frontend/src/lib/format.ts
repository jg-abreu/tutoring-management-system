// "2026-11-20T14:00" (datetime-local input, no offset) -> "2026-11-20T14:00:00-03:00".
// The API expects an OffsetDateTime, so the browser's offset has to be sent along.
export function toOffsetDateTime(localValue: string): string {
  const offsetMinutes = -new Date(localValue).getTimezoneOffset()
  const sign = offsetMinutes >= 0 ? '+' : '-'
  const abs = Math.abs(offsetMinutes)
  const hh = String(Math.floor(abs / 60)).padStart(2, '0')
  const mm = String(abs % 60).padStart(2, '0')
  return `${localValue}:00${sign}${hh}:${mm}`
}

export function shortId(id: string): string {
  return id.slice(0, 8)
}

export function initials(label: string): string {
  const clean = label.replace(/[^a-zA-Z0-9]/g, '')
  const number = clean.match(/\d+$/)?.[0]
  return (number ? clean[0] + number : clean.slice(0, 2)).toUpperCase()
}

const timeOnly = new Intl.DateTimeFormat('en-US', { hour: 'numeric', minute: '2-digit' })

export function formatTimeRange(start: string, end: string): string {
  return `${timeOnly.format(new Date(start))} – ${timeOnly.format(new Date(end))}`
}

export function dayKey(iso: string): string {
  const d = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function dayParts(iso: string) {
  const d = new Date(iso)
  return {
    weekday: d.toLocaleDateString('en-US', { weekday: 'short' }),
    day: d.getDate(),
    month: d.toLocaleDateString('en-US', { month: 'short' }),
    full: d.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' }),
  }
}

export function displayName(email: string): string {
  return email.split('@')[0]
}
