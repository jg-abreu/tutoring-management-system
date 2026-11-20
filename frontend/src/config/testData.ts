// The sample users from the API's seed, listed on the login screen to fill in the form.

export const SAMPLE_PASSWORD = 'senha123'

export type TestUser = {
  id: string
  email: string
  role: 'Tutor' | 'Admin' | 'Student'
}

export const TEST_USERS: TestUser[] = [
  { id: '899b90d5-673e-4137-abd2-1606d73f9b0e', email: 'tutor@tutoring.dev', role: 'Tutor' },
  { id: '2c53ba13-53cc-4c79-a4f2-31cfbcceb898', email: 'admin@tutoring.dev', role: 'Admin' },
  { id: '451c540f-a05f-4ead-a6d3-89528391d8ad', email: 'student1@tutoring.dev', role: 'Student' },
  { id: 'fb0e9fd2-7181-495c-922b-dd4c7e5baf2c', email: 'student2@tutoring.dev', role: 'Student' },
  { id: 'b5578fc3-17cd-4277-aec8-407227991e1f', email: 'student3@tutoring.dev', role: 'Student' },
]
