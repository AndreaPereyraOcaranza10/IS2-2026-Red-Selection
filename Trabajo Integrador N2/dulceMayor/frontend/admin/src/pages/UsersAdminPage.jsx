import { useCallback, useEffect, useMemo, useState } from 'react';
import { KeyRound, LoaderCircle, Plus, RefreshCw, Search, Trash2, UserRoundPlus, X } from 'lucide-react';
import { INITIAL_PASSWORD, useAuth } from '@dulcemayor/shared';
import { apiAdmin as api } from '../api';

const emptyForm = () => ({
  correoElectronico: '', nombre: '', apellido: '', tipoDocumento: 'DNI', numeroDocumento: '',
  rol: 'PREVENTISTA', zonaAsignada: '', patenteVehiculo: '',
});

const roles = {
  PREVENTISTA: 'Preventista',
  TRANSPORTISTA: 'Transportista',
  ADMINISTRATIVO: 'Administrativo',
  JEFE_ADMINISTRATIVO: 'Jefe administrativo',
};

export function UsersAdminPage() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try { setUsers(await api.users()); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudieron cargar los usuarios.'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const visibleUsers = useMemo(() => users.filter((entry) =>
    `${entry.nombreUsuario} ${entry.nombre} ${entry.apellido} ${entry.rol}`.toLocaleLowerCase().includes(query.toLocaleLowerCase())), [users, query]);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...form,
        correoElectronico: form.correoElectronico.trim().toLowerCase(),
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        numeroDocumento: form.numeroDocumento.trim(),
        zonaAsignada: form.rol === 'PREVENTISTA' ? form.zonaAsignada.trim() : null,
        patenteVehiculo: form.rol === 'TRANSPORTISTA' ? form.patenteVehiculo.trim() : null,
      };
      await api.createUser(payload);
      setOpen(false);
      setForm(emptyForm());
      setNotice(`Usuario creado. Clave inicial: ${INITIAL_PASSWORD}. Deberá cambiarla al ingresar.`);
      await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo crear el usuario.'); }
    finally { setSaving(false); }
  };

  const resetPassword = async (entry) => {
    if (!window.confirm(`¿Restablecer la clave de ${entry.nombreUsuario}? La sesión actual se cerrará.`)) return;
    setError('');
    try {
      await api.resetUserPassword(entry.id);
      setNotice(`Clave restablecida a ${INITIAL_PASSWORD}. ${entry.nombreUsuario} deberá cambiarla al ingresar.`);
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo restablecer la clave.'); }
  };

  const remove = async (entry) => {
    if (!window.confirm(`¿Dar de baja a ${entry.nombre} ${entry.apellido}? Se cerrarán sus sesiones.`)) return;
    setError('');
    try {
      await api.deleteUser(entry.id);
      setNotice(`Se dio de baja a ${entry.nombreUsuario}.`);
      await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo dar de baja al usuario.'); }
  };

  if (user?.role !== 'JEFE_ADMINISTRATIVO') return <section className="admin-resource-card"><h2>Acceso denegado</h2><p>La gestión de usuarios es exclusiva del Jefe Administrativo.</p></section>;

  return <section className="admin-resource-card">
    <header className="admin-resource-heading">
      <div><span className="section-kicker">PERSONAL</span><h2>Usuarios internos</h2><p>Altas, acceso y bajas lógicas del equipo.</p></div>
      <div className="admin-resource-actions">
        <button className="ops-secondary" type="button" onClick={() => void load()} disabled={loading}><RefreshCw className={loading ? 'spin' : ''} /> Actualizar</button>
        <button className="ops-primary" type="button" onClick={() => { setForm(emptyForm()); setError(''); setOpen(true); }}><Plus /> Nuevo usuario</button>
      </div>
    </header>
    {notice && <div className="ops-notice" role="status">{notice}<button type="button" onClick={() => setNotice('')}><X /></button></div>}
    {error && <div className="ops-notice ops-notice-error" role="alert">{error}<button type="button" onClick={() => void load()}>Reintentar</button></div>}
    <div className="admin-resource-toolbar"><label><Search /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Buscar por correo, nombre o rol..." /></label><span>{users.length} usuarios</span></div>
    {loading ? <div className="ops-loading"><LoaderCircle className="spin" /> Cargando usuarios…</div> : <div className="ops-table-wrap">
      <table className="ops-table admin-resource-table"><thead><tr><th>Usuario (correo)</th><th>Nombre</th><th>Documento</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>{visibleUsers.map((entry) => <tr key={entry.id}>
          <td><b>{entry.nombreUsuario}</b></td><td>{entry.nombre} {entry.apellido}</td><td>{entry.tipoDocumento} · {entry.documentoParcial}</td>
          <td><span className="admin-role-pill">{roles[entry.rol] || entry.rol}</span></td>
          <td><span className={`admin-active-pill ${entry.activo ? 'is-active' : ''}`}>{entry.activo ? (entry.primerIngreso ? 'Debe cambiar clave' : 'Activo') : 'Inactivo'}</span></td>
          <td><div className="admin-row-actions">
            {entry.activo && <button type="button" title="Restablecer clave" aria-label={`Restablecer clave de ${entry.nombreUsuario}`} onClick={() => void resetPassword(entry)}><KeyRound /></button>}
            {entry.rol !== 'JEFE_ADMINISTRATIVO' && <button type="button" className="danger" title="Dar de baja" aria-label={`Dar de baja a ${entry.nombreUsuario}`} onClick={() => void remove(entry)}><Trash2 /></button>}
          </div></td>
        </tr>)}</tbody>
      </table>
      {!visibleUsers.length && <div className="ops-empty">{users.length ? 'No hay usuarios que coincidan.' : 'Todavía no hay usuarios internos.'}</div>}
    </div>}
    <p className="admin-resource-note"><KeyRound /> La clave inicial es <b>{INITIAL_PASSWORD}</b>; cada usuario debe reemplazarla al iniciar sesión.</p>

    {open && <div className="resource-modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && setOpen(false)}>
      <section className="resource-modal" role="dialog" aria-modal="true" aria-labelledby="new-user-title">
        <header><div><span className="section-kicker">PERSONAL</span><h2 id="new-user-title">Alta de usuario</h2></div><button type="button" className="resource-close" onClick={() => setOpen(false)} aria-label="Cerrar"><X /></button></header>
        <form onSubmit={submit}>
          <div className="resource-form-grid">
            <label>Correo electrónico (será su usuario)<input required type="email" maxLength="80" autoComplete="off" placeholder="nombre@dulcemayor.com" value={form.correoElectronico} onChange={(event) => setForm({ ...form, correoElectronico: event.target.value })} /></label>
            <label>Rol<select value={form.rol} onChange={(event) => setForm({ ...form, rol: event.target.value })}><option value="PREVENTISTA">Preventista</option><option value="TRANSPORTISTA">Transportista</option><option value="ADMINISTRATIVO">Administrativo</option></select></label>
            <label>Nombre<input required maxLength="60" value={form.nombre} onChange={(event) => setForm({ ...form, nombre: event.target.value })} /></label>
            <label>Apellido<input required maxLength="60" value={form.apellido} onChange={(event) => setForm({ ...form, apellido: event.target.value })} /></label>
            <label>Tipo de documento<select value={form.tipoDocumento} onChange={(event) => setForm({ ...form, tipoDocumento: event.target.value })}><option value="DNI">DNI</option><option value="PASAPORTE">Pasaporte</option></select></label>
            <label>Número de documento<input required maxLength="9" value={form.numeroDocumento} onChange={(event) => setForm({ ...form, numeroDocumento: event.target.value })} /></label>
            {form.rol === 'PREVENTISTA' && <label className="resource-form-full">Zona asignada<input required maxLength="100" value={form.zonaAsignada} onChange={(event) => setForm({ ...form, zonaAsignada: event.target.value })} /></label>}
            {form.rol === 'TRANSPORTISTA' && <label className="resource-form-full">Patente del vehículo<input required maxLength="20" value={form.patenteVehiculo} onChange={(event) => setForm({ ...form, patenteVehiculo: event.target.value.toUpperCase() })} /></label>}
          </div>
          <p className="admin-resource-note"><UserRoundPlus /> Se asignará la clave temporal <b>{INITIAL_PASSWORD}</b>.</p>
          {error && <p className="manual-error" role="alert">{error}</p>}
          <footer><button className="ops-secondary" type="button" onClick={() => setOpen(false)}>Cancelar</button><button className="ops-primary" disabled={saving}>{saving ? <LoaderCircle className="spin" /> : <Plus />} Crear usuario</button></footer>
        </form>
      </section>
    </div>}
  </section>;
}