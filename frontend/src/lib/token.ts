export function userIdFromToken(token: string): string {
  const payload = token.split('.')[1] ?? ''
  const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
  const sub = (JSON.parse(json) as { sub?: unknown }).sub
  if (typeof sub !== 'string') throw new Error('Token without a user id')
  return sub
}
