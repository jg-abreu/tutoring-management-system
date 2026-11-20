const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
const LOGIN_PATH = '/auth/login'

let authToken: string | null = null
let unauthorizedHandler: (() => void) | null = null

export function setAuthToken(token: string | null) {
  authToken = token
}

export function setUnauthorizedHandler(handler: (() => void) | null) {
  unauthorizedHandler = handler
}

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

// Business-rule and authentication errors (GlobalExceptionHandler and the
// 401 entry point) return the message as plain text. Errors Spring handles
// on its own (DTO validation, malformed JSON) come
// as application/problem+json, with "detail" and "title" fields.
async function readErrorMessage(response: Response): Promise<string> {
  const text = await response.text()
  if (!text) return response.statusText || 'Error with no message'
  try {
    const body = JSON.parse(text)
    return body.detail || body.title || body.message || body.error || text
  } catch {
    return text
  }
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  let response: Response
  try {
    const headers: Record<string, string> = {}
    if (body !== undefined) headers['Content-Type'] = 'application/json'
    if (authToken) headers.Authorization = `Bearer ${authToken}`
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(0, `Could not connect to the API at ${BASE_URL}. Is it running?`)
  }

  if (response.status === 401 && path !== LOGIN_PATH) {
    unauthorizedHandler?.()
  }

  if (!response.ok) {
    throw new ApiError(response.status, await readErrorMessage(response))
  }
  return (await response.json()) as T
}

export const api = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, body),
  patch: <T>(path: string, body?: unknown) => request<T>('PATCH', path, body),
}
