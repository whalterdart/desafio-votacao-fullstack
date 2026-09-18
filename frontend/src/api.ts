import type { Agenda, Session, VotingResult } from './types'

const BASE_URL = `${import.meta.env.VITE_API_URL ?? '/api'}/v1`

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options?.headers }
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? 'Não foi possível concluir a operação')
  }
  return response.json() as Promise<T>
}

export const api = {
  listAgendas: () => request<Agenda[]>('/agendas'),
  createAgenda: (title: string, description: string) =>
    request<Agenda>('/agendas', { method: 'POST', body: JSON.stringify({ title, description }) }),
  openSession: (agendaId: string, durationSeconds: number) =>
    request<Session>(`/agendas/${agendaId}/sessions`, {
      method: 'POST', body: JSON.stringify({ durationSeconds })
    }),
  castVote: (agendaId: string, associateId: string, cpf: string, choice: 'SIM' | 'NAO') =>
    request(`/agendas/${agendaId}/votes`, {
      method: 'POST', body: JSON.stringify({ associateId, cpf, choice })
    }),
  result: (agendaId: string) => request<VotingResult>(`/agendas/${agendaId}/result`)
}

