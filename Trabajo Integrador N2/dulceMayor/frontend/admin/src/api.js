import { api } from '@dulcemayor/shared';
const API_BASE = import.meta.env.VITE_API_URL ?? '';

export const apiAdmin = {
  orders: () => api.get('/api/orders'),
  inventory: () => api.get('/api/admin/products'),
  products: () => api.get('/api/productos'),
  productRecords: () => api.get('/api/productos'),
  createProduct: (product) => api.post('/api/productos', product),
  updateProduct: (id, product) => api.put(`/api/productos/${id}`, product),
  deleteProduct: (id) => api.delete(`/api/productos/${id}`),
  updateOrderStatus: (id, status) => api.patch(`/api/orders/${id}/status`, { status }),
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
