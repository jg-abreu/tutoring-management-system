import { useState, type FormEvent } from 'react'
import type { CreateSessionInput } from '../../api/sessions'
import { UuidInput, type UuidOption } from '../../components/UuidInput'
import { toOffsetDateTime } from '../../lib/format'

function tomorrowAt(hour: number): string {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(hour)}:00`
}

type Props = {
  userUuid: string
  userLabel: string
  bondOptions: UuidOption[]
  busy: boolean
  onSubmit: (input: CreateSessionInput) => void
}

export function CreateSessionForm({ userUuid, userLabel, bondOptions, busy, onSubmit }: Props) {
  const [bondUuid, setBondUuid] = useState(bondOptions[0]?.id ?? '')
  const [start, setStart] = useState(tomorrowAt(14))
  const [end, setEnd] = useState(tomorrowAt(16))
  const [spots, setSpots] = useState(1)
  const [allowsWaitlist, setAllowsWaitlist] = useState(true)

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    onSubmit({
      tutoringBondUuid: bondUuid.trim(),
      userUuid,
      startTime: toOffsetDateTime(start),
      endTime: toOffsetDateTime(end),
      spots,
      allowsWaitlist,
    })
  }

  return (
    <form className="card form" onSubmit={handleSubmit}>
      <h2>Nova sessão</h2>

      <UuidInput label="Vínculo (precisa estar aprovado)" value={bondUuid} onChange={setBondUuid} options={bondOptions} />

      <label className="field">
        <span>Início</span>
        <input type="datetime-local" value={start} onChange={(e) => setStart(e.target.value)} required />
      </label>

      <label className="field">
        <span>Fim</span>
        <input type="datetime-local" value={end} onChange={(e) => setEnd(e.target.value)} required />
      </label>

      <label className="field">
        <span>Vagas</span>
        <input type="number" min={1} value={spots} onChange={(e) => setSpots(Number(e.target.value))} required />
      </label>

      <label className="field field-check">
        <input type="checkbox" checked={allowsWaitlist} onChange={(e) => setAllowsWaitlist(e.target.checked)} />
        <span>Permite lista de espera</span>
      </label>

      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={busy}>
          Criar sessão como {userLabel}
        </button>
      </div>
    </form>
  )
}
