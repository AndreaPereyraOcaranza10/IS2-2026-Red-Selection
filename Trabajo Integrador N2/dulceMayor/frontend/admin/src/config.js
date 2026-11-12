export const ADMIN_ROLES = ['ADMINISTRATIVO', 'JEFE_ADMINISTRATIVO'];
export const ADMIN_ROUTES = {
  home: '/admin/inicio',
  products: '/admin/productos',
  stock: '/admin/stock',
  orders: '/admin/pedidos',
  billing: '/admin/facturacion',
  dispatch: '/admin/despacho',
  account: '/admin/cuenta',
  branches: '/admin/sucursales',
  users: '/admin/usuarios',
  promotions: '/admin/promociones',
  statistics: '/admin/estadisticas',
  reports: '/admin/reportes'
};

export function institutionalSiteUrl() {
  const configuredUrl = import.meta.env.VITE_CLIENT_URL;
  if (configuredUrl) return configuredUrl;
  if (window.location.port === '5176') return `${window.location.protocol}//${window.location.hostname}:5175`;
  return '/';
}
