import { useId } from 'react'

export type UuidOption = { id: string; label: string }

type Props = {
  label: string
  value: string
  onChange: (value: string) => void
  options: UuidOption[]
}

// Campo de UUID com sugestões. As sugestões vêm do que o frontend já conhece;
// ainda dá para colar um UUID qualquer do banco (não há GET para buscar a lista).
export function UuidInput({ label, value, onChange, options }: Props) {
  const listId = useId()

  return (
    <label className="field field-wide">
      <span>{label}</span>
      <input
        className="mono"
        list={listId}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Escolha da lista ou cole um UUID"
        required
      />
      <datalist id={listId}>
        {options.map((option) => (
          <option key={option.id} value={option.id}>
            {option.label}
          </option>
        ))}
      </datalist>
    </label>
  )
}
