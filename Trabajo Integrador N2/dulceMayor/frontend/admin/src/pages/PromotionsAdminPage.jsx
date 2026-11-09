import { useCallback, useEffect, useMemo, useState } from 'react';
import { CalendarDays, LoaderCircle, Pencil, Plus, RefreshCw, Search, Tag, Trash2, X } from 'lucide-react';
import { apiAdmin as api } from '../api';

const today = () => {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};
const emptyForm = () => ({ codigo: '', tipoPromocion: 'VOLUMEN', porcentajeDescuento: '', cantidadMinima: 1, productoId: '', fechaDesde: today(), fechaHasta: '', activa: true });
const promoType = (type) => type === 'PRODUCTO_DESTACADO' ? 'Producto destacado' : 'Por volumen';
const date = (value) => value ? new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium' }).format(new Date(`${value}T12:00:00`)) : 'Sin vencimiento';

export function PromotionsAdminPage() {
  const [promotions, setPromotions] = useState([]);
  const [products, setProducts] = useState([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [dialog, setDialog] = useState(null);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const [promotionRows, productRows] = await Promise.all([api.promotions(), api.promotionProducts()]);
      setPromotions(promotionRows);
      setProducts(productRows);
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudieron cargar las promociones.'); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(); }, [load]);

  const visible = useMemo(() => promotions.filter((entry) => `${entry.codigo} ${entry.productoNombre || ''} ${promoType(entry.tipoPromocion)}`.toLocaleLowerCase().includes(query.toLocaleLowerCase())), [promotions, query]);

  const edit = (entry) => {
    setError('');
    setForm({ codigo: entry.codigo, tipoPromocion: entry.tipoPromocion, porcentajeDescuento: entry.porcentajeDescuento, cantidadMinima: entry.cantidadMinima ?? 1, productoId: entry.productoId ?? '', fechaDesde: entry.fechaDesde, fechaHasta: entry.fechaHasta ?? '', activa: entry.activa });
    setDialog(entry);
  };

  const submit = async (event) => {
    event.preventDefault(); setSaving(true); setError('');
    const payload = {
      codigo: form.codigo.trim().toUpperCase(),
      tipoPromocion: form.tipoPromocion,
      porcentajeDescuento: Number(form.porcentajeDescuento),
      cantidadMinima: form.tipoPromocion === 'VOLUMEN' ? Number(form.cantidadMinima) : null,
      productoId: form.tipoPromocion === 'PRODUCTO_DESTACADO' ? form.productoId : null,
      fechaDesde: form.fechaDesde,
      fechaHasta: form.fechaHasta || null,
      activa: form.activa,
    };
    try {
      if (dialog?.id) await api.updatePromotion(dialog.id, payload);
      else await api.createPromotion(payload);
      setDialog(null);
      setNotice(`Promoción ${payload.codigo} ${dialog?.id ? 'actualizada' : 'creada'}.`);
      await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo guardar la promoción.'); }
    finally { setSaving(false); }
  };

  const remove = async (entry) => {
    if (!window.confirm(`¿Dar de baja la promoción ${entry.codigo}?`)) return;
    setError('');
    try { await api.deletePromotion(entry.id); setNotice(`Promoción ${entry.codigo} dada de baja.`); await load(); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo dar de baja la promoción.'); }
  };

  return <section className="admin-resource-card">
    <header className="admin-resource-heading">
      <div><span className="section-kicker">VENTAS</span><h2>Promociones</h2><p>Administrá códigos, descuentos y vigencias del catálogo.</p></div>
      <div className="admin-resource-actions"><button className="ops-secondary" type="button" onClick={() => void load()} disabled={loading}><RefreshCw className={loading ? 'spin' : ''} /> Actualizar</button><button className="ops-primary" type="button" onClick={() => { setForm(emptyForm()); setError(''); setDialog({}); }}><Plus /> Nueva promoción</button></div>
    </header>
    {notice && <div className="ops-notice" role="status">{notice}<button type="button" onClick={() => setNotice('')}><X /></button></div>}
    {error && <div className="ops-notice ops-notice-error" role="alert">{error}<button type="button" onClick={() => void load()}>Reintentar</button></div>}
    <div className="admin-resource-toolbar"><label><Search /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Buscar por código o producto..." /></label><span>{promotions.length} promociones</span></div>
    {loading ? <div className="ops-loading"><LoaderCircle className="spin" /> Cargando promociones…</div> : <div className="ops-table-wrap">
      <table className="ops-table admin-resource-table"><thead><tr><th>Código</th><th>Tipo</th><th>Beneficio</th><th>Vigencia</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>{visible.map((entry) => <tr key={entry.id}>
          <td><b>{entry.codigo}</b></td><td>{promoType(entry.tipoPromocion)}</td>
          <td>{entry.porcentajeDescuento}%{entry.tipoPromocion === 'VOLUMEN' ? ` · desde ${entry.cantidadMinima} unidades` : ` · ${entry.productoNombre || 'Producto'}`}</td>
          <td>{date(entry.fechaDesde)} – {date(entry.fechaHasta)}</td>
          <td><span className={`admin-active-pill ${entry.vigente ? 'is-active' : ''}`}>{entry.vigente ? 'Vigente' : entry.activa ? 'Fuera de vigencia' : 'Inactiva'}</span></td>
          <td><div className="admin-row-actions"><button type="button" title="Editar" aria-label={`Editar ${entry.codigo}`} onClick={() => edit(entry)}><Pencil /></button><button type="button" className="danger" title="Dar de baja" aria-label={`Dar de baja ${entry.codigo}`} onClick={() => void remove(entry)}><Trash2 /></button></div></td>
        </tr>)}</tbody>
      </table>
      {!visible.length && <div className="ops-empty">{promotions.length ? 'No hay promociones que coincidan.' : 'Todavía no hay promociones.'}</div>}
    </div>}

    {dialog && <div className="resource-modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && setDialog(null)}>
      <section className="resource-modal" role="dialog" aria-modal="true" aria-labelledby="promotion-dialog-title">
        <header><div><span className="section-kicker">VENTAS</span><h2 id="promotion-dialog-title">{dialog.id ? 'Editar promoción' : 'Nueva promoción'}</h2></div><button type="button" className="resource-close" onClick={() => setDialog(null)} aria-label="Cerrar"><X /></button></header>
        <form onSubmit={submit}>
          <div className="resource-form-grid">
            <label>Código<input required minLength="3" maxLength="20" pattern="[A-Za-z0-9]+" value={form.codigo} onChange={(event) => setForm({ ...form, codigo: event.target.value.toUpperCase() })} placeholder="DULCE10" /></label>
            <label>Tipo<select value={form.tipoPromocion} onChange={(event) => setForm({ ...form, tipoPromocion: event.target.value })}><option value="VOLUMEN">Por volumen</option><option value="PRODUCTO_DESTACADO">Producto destacado</option></select></label>
            <label>Descuento (%)<input type="number" required min="0.01" max="100" step="0.01" value={form.porcentajeDescuento} onChange={(event) => setForm({ ...form, porcentajeDescuento: event.target.value })} /></label>
            {form.tipoPromocion === 'VOLUMEN' ? <label>Cantidad mínima<input type="number" required min="1" step="1" value={form.cantidadMinima} onChange={(event) => setForm({ ...form, cantidadMinima: event.target.value })} /></label> : <label>Producto<select required value={form.productoId} onChange={(event) => setForm({ ...form, productoId: event.target.value })}><option value="">Seleccioná un producto</option>{products.map((product) => <option key={product.id} value={product.id}>{product.nombre}</option>)}</select></label>}
            <label>Válida desde<input type="date" required value={form.fechaDesde} onChange={(event) => setForm({ ...form, fechaDesde: event.target.value })} /></label>
            <label>Válida hasta <small>(opcional)</small><input type="date" min={form.fechaDesde} value={form.fechaHasta} onChange={(event) => setForm({ ...form, fechaHasta: event.target.value })} /></label>
            <label className="resource-checkbox"><input type="checkbox" checked={form.activa} onChange={(event) => setForm({ ...form, activa: event.target.checked })} /> Promoción activa</label>
          </div>
          <p className="admin-resource-note"><CalendarDays /> El backend verifica la vigencia y calcula el descuento al validar el código.</p>
          {error && <p className="manual-error" role="alert">{error}</p>}
          <footer><button className="ops-secondary" type="button" onClick={() => setDialog(null)}>Cancelar</button><button className="ops-primary" disabled={saving}>{saving ? <LoaderCircle className="spin" /> : <Tag />} Guardar promoción</button></footer>
        </form>
      </section>
    </div>}
  </section>;
}
