import { useCallback, useEffect, useRef, useState } from 'react';
import { FileSpreadsheet, FileText, LoaderCircle, RefreshCw } from 'lucide-react';

/**
 * Reportes del panel (BK-10). Todos salen del mismo reporte del backend: la pantalla muestra el JSON y los botones
 * descargan el PDF o el Excel de ese mismo reporte. "Ventas" es global: solo lo ve y lo pide el Jefe.
 */
const REPORT_TYPES = [
  { id: 'pedidos', label: 'Pedidos', description: 'Pedidos cargados en el período, con su estado y total.' },
  { id: 'montos', label: 'Montos', description: 'Importes por estado del pedido y por preventista.' },
  { id: 'envios', label: 'Envíos', description: 'Despacho y entrega de cada pedido, con su transportista.' },
  { id: 'clientes', label: 'Clientes', description: 'Cartera actual de comercios. No depende del período.', sinPeriodo: true },
  { id: 'ventas', label: 'Ventas por departamento y preventista', description: 'Ventas facturadas por departamento y preventista (Excel histórico).', soloJefe: true },
];

/** Columnas numéricas que son cantidades; cualquier otro número de un reporte es plata. */
const COUNT_COLUMNS = new Set(['Pedidos']);

const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 2, maximumFractionDigits: 2 });
const count = new Intl.NumberFormat('es-AR');

/** yyyy-MM-dd en hora local (toISOString usaría UTC y a la noche daría el día siguiente). */
export function toInputDate(date) {
  const pad = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function defaultPeriod(today = new Date()) {
  const from = new Date(today);
  from.setDate(from.getDate() - 29);
  return { desde: toInputDate(from), hasta: toInputDate(today) };
}

export function formatCell(value, column) {
  if (value === null || value === undefined || value === '') return '';
  if (typeof value === 'number') return COUNT_COLUMNS.has(column) ? count.format(value) : money.format(value);
  return String(value);
}

function ReportTable({ table }) {
  const numeric = (value) => typeof value === 'number';
  return (
    <section className="report-card">
      <h3>{table.nombre}</h3>
      {table.filas.length === 0 ? (
        <p className="report-empty">No hay datos para el período elegido.</p>
      ) : (
        <div className="report-table-wrap">
          <table className="report-table">
            <thead>
              <tr>{table.columnas.map((column) => <th key={column}>{column}</th>)}</tr>
            </thead>
            <tbody>
              {table.filas.map((row, i) => (
                <tr key={i}>
                  {row.map((value, c) => (
                    <td key={c} className={numeric(value) ? 'is-number' : undefined}>{formatCell(value, table.columnas[c])}</td>
                  ))}
                </tr>
              ))}
            </tbody>
            {table.totales?.length > 0 && (
              <tfoot>
                <tr>
                  {table.totales.map((value, c) => (
                    <td key={c} className={numeric(value) ? 'is-number' : undefined}>{formatCell(value, table.columnas[c])}</td>
                  ))}
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      )}
    </section>
  );
}

export function ReportsAdminPage({ apiAdmin, role }) {
  const available = REPORT_TYPES.filter((type) => !type.soloJefe || role === 'JEFE_ADMINISTRATIVO');
  const [tipo, setTipo] = useState(available[0].id);
  const [{ desde, hasta }, setPeriod] = useState(defaultPeriod);
  const [departamento, setDepartamento] = useState('');
  const [departments, setDepartments] = useState([]);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [downloading, setDownloading] = useState(null);
  const [error, setError] = useState('');
  const lastRequest = useRef(0);

  // Departamentos de Mendoza desde Georef (a través del backend). Si falla, el filtro queda en "Todos".
  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const provinces = await apiAdmin.provinces();
        const mendoza = provinces.find((p) => p.name?.toLowerCase().includes('mendoza'));
        if (!mendoza) return;
        const list = await apiAdmin.departments(mendoza.id);
        if (active) setDepartments([...list].sort((a, b) => a.name.localeCompare(b.name, 'es')));
      } catch {
        if (active) setDepartments([]);
      }
    })();
    return () => { active = false; };
  }, [apiAdmin]);

  const current = available.find((type) => type.id === tipo) || available[0];
  const periodError = !current.sinPeriodo && desde && hasta && desde > hasta
    ? "La fecha 'desde' no puede ser posterior a 'hasta'." : '';

  const load = useCallback(async () => {
    if (periodError) return;
    const request = ++lastRequest.current;          // si el usuario cambia de reporte, se descarta la respuesta vieja
    setLoading(true);
    setError('');
    try {
      const data = await apiAdmin.report(
        tipo, current.sinPeriodo ? undefined : desde, current.sinPeriodo ? undefined : hasta, departamento || undefined);
      if (request === lastRequest.current) setReport(data);
    } catch (cause) {
      if (request === lastRequest.current) {
        setReport(null);
        setError(cause instanceof Error ? cause.message : 'No se pudo cargar el reporte.');
      }
    } finally {
      if (request === lastRequest.current) setLoading(false);
    }
  }, [apiAdmin, tipo, desde, hasta, departamento, current.sinPeriodo, periodError]);

  // Al elegir otro reporte se carga solo; las fechas y el departamento se aplican con el botón "Ver reporte".
  useEffect(() => { load(); }, [tipo]); // eslint-disable-line react-hooks/exhaustive-deps

  const download = async (formato) => {
    if (periodError) return;
    setDownloading(formato);
    setError('');
    try {
      const { blob, filename } = await apiAdmin.downloadReport(
        tipo, formato, current.sinPeriodo ? undefined : desde, current.sinPeriodo ? undefined : hasta, departamento || undefined);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo descargar el reporte.');
    } finally {
      setDownloading(null);
    }
  };

  return (
    <div className="reports-page">
      <section className="report-card report-controls">
        <div className="report-types" role="radiogroup" aria-label="Tipo de reporte">
          {available.map((type) => (
            <button
              key={type.id}
              type="button"
              role="radio"
              aria-checked={type.id === tipo}
              className={type.id === tipo ? 'report-type active' : 'report-type'}
              onClick={() => setTipo(type.id)}
            >
              <b>{type.label}</b>
              <small>{type.description}</small>
            </button>
          ))}
        </div>

        <div className="report-filters">
          <label>
            Desde
            <input type="date" value={desde} max={hasta || undefined} disabled={current.sinPeriodo}
              onChange={(event) => setPeriod((p) => ({ ...p, desde: event.target.value }))} />
          </label>
          <label>
            Hasta
            <input type="date" value={hasta} min={desde || undefined} disabled={current.sinPeriodo}
              onChange={(event) => setPeriod((p) => ({ ...p, hasta: event.target.value }))} />
          </label>
          <label>
            Departamento
            <select value={departamento} onChange={(event) => setDepartamento(event.target.value)}>
              <option value="">Todos</option>
              {departments.map((d) => <option key={d.id} value={d.name}>{d.name}</option>)}
            </select>
          </label>
          <div className="report-actions">
            <button type="button" className="ops-secondary" onClick={load} disabled={loading || Boolean(periodError)}>
              {loading ? <LoaderCircle className="spin" /> : <RefreshCw />} Ver reporte
            </button>
            <button type="button" className="ops-primary" onClick={() => download('pdf')} disabled={Boolean(downloading) || Boolean(periodError)}>
              {downloading === 'pdf' ? <LoaderCircle className="spin" /> : <FileText />} Descargar PDF
            </button>
            <button type="button" className="ops-primary" onClick={() => download('excel')} disabled={Boolean(downloading) || Boolean(periodError)}>
              {downloading === 'excel' ? <LoaderCircle className="spin" /> : <FileSpreadsheet />} Descargar Excel
            </button>
          </div>
        </div>
        {(periodError || error) && <p className="manual-error" role="alert">{periodError || error}</p>}
      </section>

      {loading && !report && <div className="stats-empty" role="status">Cargando reporte…</div>}
      {report && (
        <>
          <header className="report-heading">
            <h2>{report.titulo}</h2>
            <p>{report.periodo}</p>
          </header>
          {report.tablas.map((table) => <ReportTable key={table.nombre} table={table} />)}
        </>
      )}
    </div>
  );
}