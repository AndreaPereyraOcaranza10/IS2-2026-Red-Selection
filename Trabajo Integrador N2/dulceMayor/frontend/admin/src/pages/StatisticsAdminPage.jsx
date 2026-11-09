import { useCallback, useEffect, useMemo, useState } from 'react';
import { RefreshCw, TrendingUp, Warehouse } from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

const currency = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 });
const chartColors = ['#c2185b', '#008577', '#ef8b17', '#6750a4', '#1777b7', '#bd3c32', '#558b2f', '#a23f8d', '#007c91', '#d16a00'];

function pivotOrders(rows, periodKey) {
  const periods = new Map();
  const preventistas = new Map();
  rows.forEach((row) => {
    const period = row[periodKey];
    const id = String(row.preventistaId);
    if (!period || !id) return;
    preventistas.set(id, row.nombrePreventista || 'Preventista');
    if (!periods.has(period)) periods.set(period, { periodo: period });
    periods.get(period)[id] = Number(row.cantidadPedidos || 0);
  });
  return { rows: [...periods.values()].sort((a, b) => a.periodo.localeCompare(b.periodo)), preventistas: [...preventistas.entries()] };
}

function LoadingCard() {
  return <div className="stats-empty" role="status">Cargando estadísticas…</div>;
}

export function StatisticsAdminPage({ apiAdmin }) {
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [dailyOrders, monthlyOrders, stock, performance, bestSellers] = await Promise.all([
        apiAdmin.dailyStatistics(),
        apiAdmin.monthlyStatistics(),
        apiAdmin.stockStatistics(),
        apiAdmin.salespersonPerformance(),
        apiAdmin.bestSellingProducts(),
      ]);
      setStatistics({ dailyOrders, monthlyOrders, stock, performance, bestSellers });
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudieron cargar las estadísticas.');
      setStatistics(null);
    } finally {
      setLoading(false);
    }
  }, [apiAdmin]);

  useEffect(() => { void load(); }, [load]);

  const daily = useMemo(() => pivotOrders(statistics?.dailyOrders || [], 'fecha'), [statistics]);
  const monthly = useMemo(() => pivotOrders(statistics?.monthlyOrders || [], 'periodo'), [statistics]);
  const performanceTotals = useMemo(() => (statistics?.performance || []).reduce((totals, item) => ({
    pedidos: totals.pedidos + Number(item.cantidadPedidos || 0),
    facturado: totals.facturado + Number(item.montoFacturado || 0),
    preventistas: totals.preventistas + 1,
  }), { pedidos: 0, facturado: 0, preventistas: 0 }), [statistics]);
  const lowStockCount = useMemo(() => (statistics?.stock || []).filter((item) => item.bajoMinimo).length, [statistics]);
  const stockChart = useMemo(() => (statistics?.stock || []).map((item) => ({
    nombre: item.nombre,
    cantidad: Math.max(0, Number(item.stockActual || 0)),
  })).filter((item) => item.cantidad > 0), [statistics]);

  return (
    <section className="statistics-page" aria-labelledby="statistics-title">
      <header className="statistics-page-heading">
        <div>
          <span className="section-kicker">PANEL DEL JEFE</span>
          <h2 id="statistics-title">Estadísticas de operación</h2>
          <p>Pedidos, rendimiento comercial e inventario.</p>
        </div>
        <button className="ops-secondary" type="button" onClick={() => void load()} disabled={loading}>
          <RefreshCw className={loading ? 'spin' : ''} size={15} />
          Actualizar
        </button>
      </header>

      {error && <p className="statistics-error" role="alert">{error}</p>}
      {loading && !statistics ? <LoadingCard /> : statistics && (
        <>
          <div className="statistics-kpis">
            <article><span>Pedidos del período</span><strong>{performanceTotals.pedidos}</strong></article>
            <article><span>Facturación del período</span><strong>{currency.format(performanceTotals.facturado)}</strong></article>
            <article><span>Preventistas activos</span><strong>{performanceTotals.preventistas}</strong></article>
            <article className={lowStockCount ? 'is-warning' : ''}><span>Productos bajo mínimo</span><strong>{lowStockCount}</strong></article>
          </div>

          <div className="statistics-chart-grid">
            <OrdersChart title="Pedidos por preventista · últimos 30 días" chart={daily} empty="Todavía no hay pedidos en este período." />
            <OrdersChart title="Pedidos por preventista · últimos 12 meses" chart={monthly} empty="Todavía no hay pedidos en este período." />
          </div>

          <div className="statistics-chart-grid">
            <section className="statistics-card">
              <header><div><h3>Stock actual por producto</h3><p>Distribución de unidades disponibles</p></div><Warehouse size={18} /></header>
              {loading && !statistics ? <LoadingCard /> : stockChart.length ? (
                <div className="statistics-stock-layout">
                  <div className="statistics-pie">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie data={stockChart} dataKey="cantidad" nameKey="nombre" innerRadius={48} outerRadius={88} paddingAngle={1}>
                          {stockChart.map((item, index) => <Cell key={item.nombre} fill={chartColors[index % chartColors.length]} />)}
                        </Pie>
                        <Tooltip formatter={(value) => [`${Number(value).toLocaleString('es-AR')} unidades`, 'Stock']} />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                  <div className="statistics-stock-legend" aria-label="Stock por producto">
                    {(statistics.stock || []).map((item, index) => (
                      <div key={item.productoId}>
                        <i style={{ backgroundColor: chartColors[index % chartColors.length] }} />
                        <span title={item.nombre}>{item.nombre}</span>
                        <b>{Number(item.stockActual || 0).toLocaleString('es-AR')}</b>
                        {item.bajoMinimo && <small>Bajo mínimo</small>}
                      </div>
                    ))}
                  </div>
                </div>
              ) : <div className="stats-empty">No hay productos con stock disponible.</div>}
            </section>

            <section className="statistics-card">
              <header><div><h3>Productos más vendidos</h3><p>Unidades facturadas durante el mes actual</p></div><TrendingUp size={18} /></header>
              {(statistics.bestSellers || []).length ? (
                <ResponsiveContainer width="100%" height={290}>
                  <BarChart data={statistics.bestSellers} margin={{ top: 12, right: 12, left: -14, bottom: 56 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="nombre" angle={-28} textAnchor="end" interval={0} height={68} tick={{ fontSize: 9 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 9 }} />
                    <Tooltip formatter={(value) => [`${Number(value).toLocaleString('es-AR')} unidades`, 'Vendidas']} />
                    <Bar dataKey="unidadesVendidas" name="Unidades vendidas" fill={chartColors[0]} radius={[5, 5, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : <div className="stats-empty">Todavía no hay ventas facturadas este mes.</div>}
            </section>
          </div>

          <section className="statistics-card statistics-performance">
            <header><div><h3>Rendimiento por preventista</h3><p>Pedidos, cumplimiento y monto facturado en el mes actual</p></div></header>
            {(statistics.performance || []).length ? (
              <div className="statistics-performance-list">
                {statistics.performance.map((item) => (
                  <article key={item.preventistaId}>
                    <strong>{item.nombrePreventista}</strong>
                    <div><span>Pedidos</span><b>{item.cantidadPedidos}</b></div>
                    <div><span>Facturados</span><b>{item.pedidosFacturados}</b></div>
                    <div><span>Pendientes</span><b>{item.pedidosPendientes}</b></div>
                    <div><span>Anulados</span><b>{item.pedidosAnulados}</b></div>
                    <div><span>Monto facturado</span><b>{currency.format(item.montoFacturado || 0)}</b></div>
                  </article>
                ))}
              </div>
            ) : <div className="stats-empty">No hay preventistas para mostrar.</div>}
          </section>
        </>
      )}
    </section>
  );
}

function OrdersChart({ title, chart, empty }) {
  return (
    <section className="statistics-card">
      <header><div><h3>{title}</h3><p>Pedidos no anulados, agrupados por fecha de carga</p></div></header>
      {chart.rows.length ? (
        <ResponsiveContainer width="100%" height={290}>
          <BarChart data={chart.rows} margin={{ top: 12, right: 12, left: -16, bottom: 26 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="periodo" tick={{ fontSize: 9 }} interval="preserveStartEnd" angle={-18} textAnchor="end" height={42} />
            <YAxis allowDecimals={false} tick={{ fontSize: 9 }} />
            <Tooltip labelFormatter={(label) => `Período: ${label}`} />
            <Legend wrapperStyle={{ fontSize: 9 }} />
            {chart.preventistas.map(([id, name], index) => (
              <Bar key={id} dataKey={id} name={name} fill={chartColors[index % chartColors.length]} radius={[3, 3, 0, 0]} />
            ))}
          </BarChart>
        </ResponsiveContainer>
      ) : <div className="stats-empty">{empty}</div>}
    </section>
  );
}
