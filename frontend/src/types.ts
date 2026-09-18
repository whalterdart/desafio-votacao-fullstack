export type Session = { id: string; openedAt: string; closesAt: string; open: boolean }
export type Agenda = { id: string; title: string; description?: string; createdAt: string; session?: Session }
export type VotingResult = {
  agendaId: string
  yes: number
  no: number
  total: number
  sessionStatus: 'OPEN' | 'CLOSED' | 'NOT_STARTED'
  result: 'PENDING' | 'TIE' | 'APPROVED' | 'REJECTED'
}

