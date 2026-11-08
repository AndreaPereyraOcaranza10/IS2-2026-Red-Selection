import { Icon } from './Icon.jsx';

/**
 * Desplegable con el mismo aspecto que Field. `options` = [{ id, name }].
 * `loading` muestra "Cargando…" mientras llegan las opciones; `allowEmpty={false}` quita la opción vacía.
 */
export function SelectField({ id, label, help, error, placeholder = 'Elegí una opción', options, loading, allowEmpty = true, ...select }) {
  return (
    <div className={`field ${error ? 'err' : ''}`}>
      <label htmlFor={id}>{label}</label>
      <div className="input">
        <select id={id} aria-invalid={!!error} aria-describedby={`${id}-h`} {...select}>
          {allowEmpty && <option value="">{loading ? 'Cargando…' : placeholder}</option>}
          {options.map((o) => (
            <option key={o.id} value={o.id}>
              {o.name}
            </option>
          ))}
        </select>
        <span aria-hidden="true" style={{ display: 'flex', transform: 'rotate(90deg)', color: 'var(--muted)' }}>
          <Icon name="chev" size={20} />
        </span>
      </div>
      <span className="help" id={`${id}-h`}>
        {error || help}
      </span>
    </div>
  );
}