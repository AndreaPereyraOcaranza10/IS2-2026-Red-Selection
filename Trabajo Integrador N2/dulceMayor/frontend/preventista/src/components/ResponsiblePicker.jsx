import { useState } from 'react';
import { Field, Icon, useDebounced, useOptions } from '@dulcemayor/shared';
import { preventistaApi } from '../api.js';

/**
 * Elegir un responsable que ya está cargado (un mismo dueño puede tener varios comercios).
 * Busca por nombre, apellido o documento; el documento llega enmascarado desde el backend.
 */
export default function ResponsiblePicker({ selected, onSelect, onClear, error }) {
  const [text, setText] = useState('');
  const term = useDebounced(text.trim(), 300);
  const search = useOptions(preventistaApi.searchResponsibles, term, { enabled: !selected && term.length >= 2 });

  if (selected) {
    return (
      <article className="card row" aria-label="Responsable elegido">
        <div className="grow">
          <b>{selected.fullName}</b>
          <p className="small sec">
            {selected.documentType} {selected.documentMasked}
          </p>
          <p className="small sec">
            {selected.email} · {selected.phone}
          </p>
        </div>
        <button type="button" className="linkbtn small" onClick={onClear}>
          Cambiar
        </button>
      </article>
    );
  }

  const short = text.trim().length > 0 && text.trim().length < 2;
  const empty = term.length >= 2 && term === text.trim() && !search.loading && !search.error && search.options.length === 0;

  return (
    <div className="col" style={{ gap: 8 }}>
      <Field
        id="pv-search"
        label="Buscar responsable"
        placeholder="Nombre, apellido o documento"
        autoComplete="off"
        left={<Icon name="search" size={20} />}
        value={text}
        onChange={(e) => setText(e.target.value)}
        error={error || (search.error && search.error.message) || (short && 'Escribí al menos 2 letras')}
        help={empty ? 'No encontramos a nadie con ese dato. Podés cargarlo como nuevo.' : 'Si ya cargaste al dueño en otro comercio, buscalo acá.'}
      />
      <div className="col" style={{ gap: 8 }} role="radiogroup" aria-label="Responsables encontrados">
        {search.options.map((r) => (
          <button key={r.id} type="button" className="opt" role="radio" aria-checked={false} onClick={() => onSelect(r)}>
            <span className="radio" />
            <span className="grow">
              <b>{r.fullName}</b>
              <br />
              <span className="small sec">
                {r.documentType} {r.documentMasked} · {r.phone}
              </span>
            </span>
          </button>
        ))}
      </div>
    </div>
  );
}