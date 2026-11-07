/** Campo de texto con etiqueta, ayuda y estado de error/éxito. `right` = elemento a la derecha (ej. ojo de clave). */
export function Field({ id, label, help, error, ok, right, left, ...input }) {
  return (
    <div className={`field ${error ? 'err' : ok ? 'ok' : ''}`}>
      <label htmlFor={id}>{label}</label>
      <div className="input">
        {left}
        <input id={id} aria-invalid={!!error} aria-describedby={`${id}-h`} {...input} />
        {right}
      </div>
      <span className="help" id={`${id}-h`}>
        {error || help}
      </span>
    </div>
  );
}
