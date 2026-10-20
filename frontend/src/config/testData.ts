// TEMPORARY: the API has no GET /users or GET /tutoring-bonds yet, so the test
// users and the test tutoring bond from the database are hardcoded here. Once
// authentication exists (v2.0), the "current user" will come from the login.

export type TestUser = {
  id: string
  label: string
  role: 'Tutor' | 'Admin' | 'Student'
}

export const TEST_USERS: TestUser[] = [
  { id: '899b90d5-673e-4137-abd2-1606d73f9b0e', label: 'jgabreuteste', role: 'Tutor' },
  { id: '2c53ba13-53cc-4c79-a4f2-31cfbcceb898', label: 'admin', role: 'Admin' },
  { id: '451c540f-a05f-4ead-a6d3-89528391d8ad', label: 'student1', role: 'Student' },
  { id: 'fb0e9fd2-7181-495c-922b-dd4c7e5baf2c', label: 'student2', role: 'Student' },
  { id: 'b5578fc3-17cd-4277-aec8-407227991e1f', label: 'student3', role: 'Student' },
]

// Records that already existed in the database before the frontend (shown as suggestions in the forms).
export const KNOWN_SUBJECTS = [{ id: 'a4ce163a-0660-4262-9b5c-15f457fba955', label: 'Calculus I' }]

// Approved tutoring bonds that already existed. requesterId/evaluatorId tell who can
// create sessions on them (the API accepts the bond's requester or evaluator).
export const KNOWN_BONDS = [
  {
    id: '0128d64d-7818-4eec-9b8e-a55e8421ab1f',
    label: 'jgabreuteste · Calculus I',
    requesterId: '899b90d5-673e-4137-abd2-1606d73f9b0e',
    evaluatorId: '2c53ba13-53cc-4c79-a4f2-31cfbcceb898',
  },
]

export function userLabel(id: string): string {
  return TEST_USERS.find((u) => u.id === id)?.label ?? id.slice(0, 8)
}
