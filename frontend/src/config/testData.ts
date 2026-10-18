// PROVISÓRIO: a API ainda não tem GET /users nem GET /tutoring-bonds, então os
// usuários e o vínculo de teste do banco ficam fixos aqui. Quando houver
// autenticação (v2.0), o "usuário atual" passa a vir do login.

export type TestUser = {
  id: string
  label: string
  role: 'Monitor' | 'Admin' | 'Aluno'
}

export const TEST_USERS: TestUser[] = [
  { id: '899b90d5-673e-4137-abd2-1606d73f9b0e', label: 'jgabreuteste', role: 'Monitor' },
  { id: '2c53ba13-53cc-4c79-a4f2-31cfbcceb898', label: 'admin', role: 'Admin' },
  { id: '451c540f-a05f-4ead-a6d3-89528391d8ad', label: 'aluno1', role: 'Aluno' },
  { id: 'fb0e9fd2-7181-495c-922b-dd4c7e5baf2c', label: 'aluno2', role: 'Aluno' },
  { id: 'b5578fc3-17cd-4277-aec8-407227991e1f', label: 'aluno3', role: 'Aluno' },
]

// Registros que já existiam no banco antes do frontend (aparecem como sugestão nos formulários).
export const KNOWN_SUBJECTS = [{ id: 'a4ce163a-0660-4262-9b5c-15f457fba955', label: 'Cálculo I (aprovada)' }]

export const KNOWN_BONDS = [
  { id: '0128d64d-7818-4eec-9b8e-a55e8421ab1f', label: 'jgabreuteste · Cálculo I (aprovado)' },
]

export function userLabel(id: string): string {
  return TEST_USERS.find((u) => u.id === id)?.label ?? id.slice(0, 8)
}
