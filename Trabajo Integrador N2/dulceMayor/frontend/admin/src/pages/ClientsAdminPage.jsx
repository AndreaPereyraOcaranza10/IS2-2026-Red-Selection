import { useCallback, useEffect, useMemo, useState } from 'react';
import { Eye, LoaderCircle, Mail, MapPin, Phone, RefreshCw, Search, Store, UserRound, X } from 'lucide-react';
import { apiAdmin as api } from '../api';
import './clients.css';

/** Cartera de clientes (puntos de venta): solo lectura para administración. */
export function ClientsAdminPage() {
  const [clients, setClients] = useState([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try { setClients(await api.clients()); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudieron cargar los clientes.'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const visible = useMemo(() => {
    const text = query.trim().toLocaleLowerCase();
    return clients
      .filter((client) => `${client.nombreComercio} ${client.razonSocial} ${client.cuit} ${client.responsable?.nombre} ${client.responsable?.apellido} ${client.responsable?.email} ${client.direccion?.localidadNombre} ${client.direccion?.departamentoNombre} ${client.preventistaNombre || ''}`
        .toLocaleLowerCase().includes(text))
      .sort((a, b) => String(a.nombreComercio).localeCompare(String(b.nombreComercio), 'es'));
  }, [clients, query]);

  const owner = (client) => client.responsable ? `${client.responsable.nombre} ${client.responsable.apellido}` : '—';

  return <section className="admin-resource-card">
    <header className="admin-resource-heading">
      <div><span className="section-kicker">COMERCIOS</span><h2>Clientes</h2><p>Comercios minoristas registrados, su responsable y el preventista que los atiende.</p></div>
      <div className="admin-resource-actions">
        <button className="ops-secondary" type="button" onClick={() => void load()} disabled={loading}><RefreshCw className={loading ? 'spin' : ''} /> Actualizar</button>
      </div>
    </header>
    {error && <div className="ops-notice ops-notice-error" role="alert">{error}<button type="button" onClick={() => void load()}>Reintentar</button></div>}
    <div className="admin-resource-toolbar"><label><Search /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Buscar por comercio, CUIT, responsable, localidad o preventista..." /></label><span>{visible.length} de {clients.length} clientes</span></div>
    {loading ? <div className="ops-loading"><LoaderCircle className="spin" /> Cargando clientes…</div> : <div className="ops-table-wrap">
      <table className="ops-table admin-resource-table"><thead><tr><th>Comercio</th><th>CUIT</th><th>Responsable</th><th>Localidad</th><th>Preventista</th><th>Detalle</th></tr></thead>
        <tbody>{visible.map((client) => <tr key={client.id}>
          <td><b>{client.nombreComercio}</b><br /><small>{client.razonSocial}</small></td>
          <td>{client.cuit}</td>
          <td>{owner(client)}</td>
          <td>{client.direccion?.localidadNombre || '—'}</td>
          <td>{client.preventistaNombre || <span className="admin-active-pill">Sin asignar</span>}</td>
          <td><div className="admin-row-actions"><button type="button" title="Ver detalle" aria-label={`Ver detalle de ${client.nombreComercio}`} onClick={() => setSelected(client)}><Eye /></button></div></td>
        </tr>)}</tbody>
      </table>
      {!visible.length && <div className="ops-empty">{clients.length ? 'No hay clientes que coincidan.' : 'Todavía no hay clientes registrados.'}</div>}
    </div>}

    {selected && <div className="resource-modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && setSelected(null)}>
      <section className="resource-modal" role="dialog" aria-modal="true" aria-labelledby="client-detail-title">
        <header><div><span className="section-kicker">CLIENTE</span><h2 id="client-detail-title">{selected.nombreComercio}</h2></div><button type="button" className="resource-close" onClick={() => setSelected(null)} aria-label="Cerrar"><X /></button></header>
        <div className="client-detail">
          <div className="client-detail-lead">
            <Store aria-hidden="true" />
            <div><b>{selected.razonSocial}</b><span>CUIT {selected.cuit}</span></div>
          </div>
          <dl className="client-detail-grid">
            <div className="client-detail-item"><UserRound aria-hidden="true" /><div><dt>Responsable</dt><dd>{owner(selected)}{selected.responsable?.documentoParcial ? ` · ${selected.responsable.tipoDocumento} ${selected.responsable.documentoParcial}` : ''}</dd></div></div>
            <div className="client-detail-item"><UserRound aria-hidden="true" /><div><dt>Preventista</dt><dd>{selected.preventistaNombre || 'Sin asignar'}</dd></div></div>
            <div className="client-detail-item"><Mail aria-hidden="true" /><div><dt>Correo</dt><dd>{selected.responsable?.email || 'Sin correo'}</dd></div></div>
            <div className="client-detail-item"><Phone aria-hidden="true" /><div><dt>Teléfono</dt><dd>{selected.responsable?.telefono || 'Sin teléfono'}</dd></div></div>
            <div className="client-detail-item client-detail-full"><MapPin aria-hidden="true" /><div><dt>Dirección</dt><dd>{selected.direccion?.textoCompleto || 'Sin dirección'}</dd></div></div>
          </dl>
          <footer className="client-detail-footer"><button className="ops-secondary" type="button" onClick={() => setSelected(null)}>Cerrar</button></footer>
        </div>
      </section>
    </div>}
  </section>;
}