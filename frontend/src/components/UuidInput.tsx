import { useId } from 'react'

export type UuidOption = { id: string; label: string }

type Props = {
  label: string
  value: string
  onChange: (value: string) => void
  options: UuidOption[]
}

// UUID field with suggestions. Suggestions come from what the frontend already knows;
// any UUID from the database can still be pasted in (there is no GET to fetch the list).
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
        placeholder="Pick from the list or paste a UUID"
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
