import { FormEvent, useCallback, useEffect, useState } from 'react'
import { BarChart3, Check, Clock3, Plus, RefreshCw, Users, Vote as VoteIcon, X } from 'lucide-react'
import { api } from './api'
import type { Agenda, VotingResult } from './types'

const statusLabel = { OPEN: 'Em votação', CLOSED: 'Encerrada', NOT_STARTED: 'Aguardando sessão' }
const resultLabel = { PENDING: 'Resultado pendente', TIE: 'Empate', APPROVED: 'Aprovada', REJECTED: 'Rejeitada' }

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
}

function secondsLeft(closesAt?: string, now = Date.now()) {
  return closesAt ? Math.max(0, Math.ceil((new Date(closesAt).getTime() - now) / 1000)) : 0
}

export default function App() {
  const [agendas, setAgendas] = useState<Agenda[]>([])
  const [results, setResults] = useState<Record<string, VotingResult>>({})
  const [selected, setSelected] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null)
  const [now, setNow] = useState(Date.now())

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const data = await api.listAgendas()
      setAgendas(data)
      const entries = await Promise.all(data.map(async agenda => [agenda.id, await api.result(agenda.id)] as const))
      setResults(Object.fromEntries(entries))
    } catch (error) {
      setMessage({ type: 'error', text: (error as Error).message })
    } finally { setLoading(false) }
  }, [])

  useEffect(() => { void load() }, [load])
  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 1000)
    return () => window.clearInterval(timer)
  }, [])

  async function run(action: () => Promise<unknown>, success: string) {
    setBusy(true); setMessage(null)
    try { await action(); setMessage({ type: 'success', text: success }); await load() }
    catch (error) { setMessage({ type: 'error', text: (error as Error).message }) }
    finally { setBusy(false) }
  }

  async function createAgenda(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    await run(() => api.createAgenda(String(form.get('title')), String(form.get('description'))), 'Pauta cadastrada com sucesso.')
    event.currentTarget.reset()
  }

  const openCount = agendas.filter(a => secondsLeft(a.session?.closesAt, now) > 0).length
  const totalVotes = Object.values(results).reduce((sum, result) => sum + result.total, 0)

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="Coopera Voto — início">
          <span className="brand-mark"><VoteIcon size={21} /></span>
          <span>Coopera <strong>Voto</strong></span>
        </a>
        <a className="docs-link" href="/swagger-ui.html" target="_blank" rel="noreferrer">Documentação da API</a>
      </header>

      <main id="top">
        <section className="hero">
          <div>
            <span className="eyebrow">Assembleias cooperativas</span>
            <h1>Decisões transparentes,<br /><em>um voto por associado.</em></h1>
            <p>Crie pautas, abra sessões e acompanhe os resultados em uma experiência simples e segura.</p>
          </div>
          <div className="hero-card" aria-label="Resumo das votações">
            <div><span><BarChart3 size={19} /> Pautas</span><strong>{agendas.length}</strong></div>
            <div><span><Clock3 size={19} /> Sessões abertas</span><strong>{openCount}</strong></div>
            <div><span><Users size={19} /> Votos registrados</span><strong>{totalVotes}</strong></div>
          </div>
        </section>

        {message && <div className={`toast ${message.type}`} role="status">
          {message.type === 'success' ? <Check size={18} /> : <X size={18} />}{message.text}
          <button onClick={() => setMessage(null)} aria-label="Fechar aviso">×</button>
        </div>}

        <section className="workspace">
          <aside className="create-panel">
            <div className="section-heading"><span className="step">01</span><div><h2>Nova pauta</h2><p>Defina o tema que será decidido.</p></div></div>
            <form onSubmit={createAgenda}>
              <label>Título<input name="title" required maxLength={160} placeholder="Ex.: Aprovação do orçamento anual" /></label>
              <label>Descrição<textarea name="description" maxLength={1000} rows={5} placeholder="Contextualize a decisão para os associados" /></label>
              <button className="primary" disabled={busy}><Plus size={18} /> Cadastrar pauta</button>
            </form>
          </aside>

          <section className="agenda-panel">
            <div className="list-header">
              <div className="section-heading"><span className="step">02</span><div><h2>Pautas</h2><p>Gerencie sessões, votos e resultados.</p></div></div>
              <button className="icon-button" onClick={() => void load()} disabled={loading} aria-label="Atualizar pautas"><RefreshCw size={18} /></button>
            </div>
            {loading ? <div className="empty">Carregando pautas…</div> : agendas.length === 0 ?
              <div className="empty"><VoteIcon size={32} /><strong>Nenhuma pauta cadastrada</strong><span>Use o formulário para criar a primeira.</span></div> :
              <div className="agenda-list">{agendas.map(agenda =>
                <AgendaCard key={agenda.id} agenda={agenda} result={results[agenda.id]} now={now}
                  expanded={selected === agenda.id} onToggle={() => setSelected(selected === agenda.id ? null : agenda.id)}
                  busy={busy} run={run} />
              )}</div>}
          </section>
        </section>
      </main>
      <footer>Coopera Voto <span>•</span> API REST v1 <span>•</span> Java + Spring Boot + React</footer>
    </div>
  )
}

function AgendaCard({ agenda, result, now, expanded, onToggle, busy, run }: {
  agenda: Agenda; result?: VotingResult; now: number; expanded: boolean; onToggle: () => void; busy: boolean
  run: (action: () => Promise<unknown>, success: string) => Promise<void>
}) {
  const remaining = secondsLeft(agenda.session?.closesAt, now)
  const status = !agenda.session ? 'NOT_STARTED' : remaining > 0 ? 'OPEN' : 'CLOSED'
  const yesPercent = result?.total ? Math.round(result.yes / result.total * 100) : 0
  const noPercent = result?.total ? 100 - yesPercent : 0

  async function openSession(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const seconds = Number(new FormData(event.currentTarget).get('duration') || 60)
    await run(() => api.openSession(agenda.id, seconds), 'Sessão aberta para votação.')
  }

  async function vote(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    await run(() => api.castVote(agenda.id, String(form.get('associateId')), String(form.get('cpf')),
      String(form.get('choice')) as 'SIM' | 'NAO'), 'Voto registrado com sucesso.')
    event.currentTarget.reset()
  }

  return <article className={`agenda-card ${expanded ? 'expanded' : ''}`}>
    <button className="agenda-summary" onClick={onToggle} aria-expanded={expanded}>
      <div><span className={`status ${status.toLowerCase()}`}>{statusLabel[status]}</span><h3>{agenda.title}</h3>
        <p>{agenda.description || 'Sem descrição.'}</p></div>
      <div className="agenda-meta"><span>{formatDate(agenda.createdAt)}</span><strong>{result?.total ?? 0} votos</strong></div>
    </button>
    {expanded && <div className="agenda-details">
      {!agenda.session && <form className="inline-form" onSubmit={openSession}>
        <div><h4>Abrir sessão</h4><p>Sem duração informada, a sessão dura 1 minuto.</p></div>
        <label>Duração (segundos)<input name="duration" type="number" min="1" max="86400" defaultValue="60" /></label>
        <button className="secondary" disabled={busy}><Clock3 size={17} /> Abrir votação</button>
      </form>}
      {agenda.session && <div className="session-line">
        <span><Clock3 size={17} /> Encerra em {formatDate(agenda.session.closesAt)}</span>
        {status === 'OPEN' && <strong>{remaining}s restantes</strong>}
      </div>}
      {status === 'OPEN' && <form className="vote-form" onSubmit={vote}>
        <h4>Registrar voto</h4>
        <label>ID do associado<input name="associateId" required maxLength={80} placeholder="assoc-123" /></label>
        <label>CPF<input name="cpf" required inputMode="numeric" placeholder="529.982.247-25" /></label>
        <fieldset><legend>Escolha</legend><label className="choice yes"><input type="radio" name="choice" value="SIM" required /><Check /> Sim</label>
          <label className="choice no"><input type="radio" name="choice" value="NAO" required /><X /> Não</label></fieldset>
        <button className="primary" disabled={busy}><VoteIcon size={17} /> Confirmar voto</button>
      </form>}
      {result && <div className="result-box">
        <div className="result-title"><div><span>Resultado</span><strong>{resultLabel[result.result]}</strong></div><b>{result.total} votos</b></div>
        <div className="result-bar"><span style={{ width: `${yesPercent}%` }} /></div>
        <div className="result-numbers"><span><i className="dot yes-dot" /> Sim <strong>{result.yes}</strong> ({yesPercent}%)</span>
          <span><i className="dot no-dot" /> Não <strong>{result.no}</strong> ({noPercent}%)</span></div>
      </div>}
    </div>}
  </article>
}

export { secondsLeft }
