import http from 'k6/http'
import { check, sleep } from 'k6'

export const options = {
  scenarios: {
    voting: {
      executor: 'ramping-arrival-rate',
      startRate: 20,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 300,
      stages: [
        { target: 100, duration: '20s' },
        { target: 100, duration: '40s' },
        { target: 0, duration: '10s' }
      ]
    }
  },
  thresholds: { http_req_duration: ['p(95)<500'], checks: ['rate>0.99'] }
}

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080/api/v1'
const acceptedStatuses = http.expectedStatuses(201, 404)

export function setup() {
  const agenda = http.post(`${baseUrl}/agendas`, JSON.stringify({
    title: `Teste de carga ${Date.now()}`,
    description: 'Pauta criada automaticamente pelo k6'
  }), { headers: { 'Content-Type': 'application/json' } }).json()
  http.post(`${baseUrl}/agendas/${agenda.id}/sessions`, JSON.stringify({ durationSeconds: 300 }), {
    headers: { 'Content-Type': 'application/json' }
  })
  return { agendaId: agenda.id }
}

export default function (data) {
  const id = `${__VU}-${__ITER}-${Date.now()}`
  const response = http.post(`${baseUrl}/agendas/${data.agendaId}/votes`, JSON.stringify({
    associateId: id,
    cpf: '52998224725',
    choice: __ITER % 2 === 0 ? 'SIM' : 'NAO'
  }), {
    headers: { 'Content-Type': 'application/json' },
    responseCallback: acceptedStatuses
  })
  check(response, { 'voto aceito ou recusado pelo client fake': r => r.status === 201 || r.status === 404 })
  sleep(0.05)
}

