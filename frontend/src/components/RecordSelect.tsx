export type RecordOption = { id: string; label: string }

type Props = {
  label: string
  value: string
  onChange: (value: string) => void
  options: RecordOption[]
  placeholder: string
  emptyText: string
}

export function RecordSelect({ label, value, onChange, options, placeholder, emptyText }: Props) {
  const selected = options.some((option) => option.id === value) ? value : ''

  return (
    <label className="field field-wide">
      <span>{label}</span>
      <select value={selected} onChange={(e) => onChange(e.target.value)} disabled={options.length === 0} required>
        <option value="" disabled>
          {options.length === 0 ? emptyText : placeholder}
        </option>
        {options.map((option) => (
          <option key={option.id} value={option.id}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  )
}
