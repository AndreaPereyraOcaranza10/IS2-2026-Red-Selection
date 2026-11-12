import { api } from '@dulcemayor/shared';
const API_BASE = import.meta.env.VITE_API_URL ?? '';

/**
 * Ruta de un reporte del backend. `ventas` es global (solo el Jefe) y vive bajo /globales.
 * Sin `formato` devuelve JSON (la pantalla); con 'pdf' o 'excel', el archivo. Las fechas son opcionales (yyyy-MM-dd).
 */
export function reportPath(tipo, formato, desde, hasta) {
  const base = tipo === 'ventas' ? '/api/reportes/globales/ventas' : `/api/reportes/${tipo}`;
  const params = new URLSearchParams();
  if (desde) params.set('desde', desde);
  if (hasta) params.set('hasta', hasta);
  const query = params.toString();
  return `${base}${formato ? `/${formato}` : ''}${query ? `?${query}` : ''}`;
}


export const apiAdmin = {
  orders: () => api.get('/api/orders'),
  inventory: () => api.get('/api/admin/products'),
  products: () => api.get('/api/productos'),
  productRecords: () => api.get('/api/productos'),
  createProduct: (product) => api.post('/api/productos', product),
  updateProduct: (id, product) => api.put(`/api/productos/${id}`, product),
  deleteProduct: (id) => api.delete(`/api/productos/${id}`),
  updateOrderStatus: (id, status) => api.patch(`/api/orders/${id}/status`, { status }),
  payments: (invoiceId) => api.get(`/api/facturas/${invoiceId}/pagos`),
  confirmPayment: (invoiceId, tipoPago) => api.post(`/api/facturas/${invoiceId}/pagos/confirmar`, { tipoPago }),
  updateStock: (id, stock) => api.patch(`/api/admin/products/${id}/stock`, { stock }),
  updatePrice: (id, precio) => api.put(`/api/productos/${id}/precio`, { precio }),
  categories: () => api.get('/api/categorias'),
  createCategory: (category) => api.post('/api/categorias', category),
  updateCategory: (id, category) => api.put(`/api/categorias/${id}`, category),
  deleteCategory: (id) => api.delete(`/api/categorias/${id}`),
  subcategories: () => api.get('/api/subcategorias'),
  createSubcategory: (subcategory) => api.post('/api/subcategorias', subcategory),
  updateSubcategory: (id, subcategory) => api.put(`/api/subcategorias/${id}`, subcategory),
  deleteSubcategory: (id) => api.delete(`/api/subcategorias/${id}`),
  uploadImage: (file) => {
    const form = new FormData();
    form.append('archivo', file);
    return api.upload('/api/imagenes', form);
  },
  imageUrl: (id) => `${API_BASE}/api/imagenes/${id}`,
  users: () => api.get('/api/usuarios'),
  createUser: (user) => api.post('/api/usuarios', user),
  resetUserPassword: (id) => api.post(`/api/usuarios/${id}/reiniciar-clave`),
  deleteUser: (id) => api.delete(`/api/usuarios/${id}`),
  promotions: () => api.get('/api/promociones'),
  createPromotion: (promotion) => api.post('/api/promociones', promotion),
  updatePromotion: (id, promotion) => api.put(`/api/promociones/${id}`, promotion),
  deletePromotion: (id) => api.delete(`/api/promociones/${id}`),
  promotionProducts: () => api.get('/api/productos'),
  dailyStatistics: () => api.get('/api/estadisticas/pedidos/preventistas/dia'),
  monthlyStatistics: () => api.get('/api/estadisticas/pedidos/preventistas/mes'),
  stockStatistics: () => api.get('/api/estadisticas/stock/productos'),
  report: (tipo, desde, hasta) => api.get(reportPath(tipo, null, desde, hasta)),
  downloadReport: (tipo, formato, desde, hasta) => api.download(
    reportPath(tipo, formato, desde, hasta),
    `reporte-${tipo}-${desde || 'inicio'}_${hasta || 'hoy'}.${formato === 'excel' ? 'xlsx' : 'pdf'}`,
  ),
  salespersonPerformance: () => api.get('/api/estadisticas/rendimiento/preventistas'),
  bestSellingProducts: () => api.get('/api/estadisticas/productos-mas-vendidos'),
  createManualOrder: (clientName, items) => api.post('/api/orders', {
    clientName,
    paymentMethod: 'A_COORDINAR',
    items,
  }),
  branches: () => api.get('/api/admin/companies/branches'),
  createBranch: (branch) => api.post('/api/admin/companies/branches', branch),
  updateBranch: (id, branch) => api.put(`/api/admin/companies/branches/${id}`, branch),
  deleteBranch: (id) => api.delete(`/api/admin/companies/branches/${id}`),
  provinces: () => api.get('/api/locations/provinces'),
  departments: (provinceId) => api.get(`/api/locations/provinces/${provinceId}/departments`),
  localities: (departmentId) => api.get(`/api/locations/departments/${departmentId}/localities`),
};
