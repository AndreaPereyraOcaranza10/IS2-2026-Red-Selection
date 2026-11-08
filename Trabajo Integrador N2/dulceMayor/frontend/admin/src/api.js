import { api } from '@dulcemayor/shared';

export const apiAdmin = {
  orders: () => api.get('/api/orders'),
  inventory: () => api.get('/api/admin/products'),
  updateOrderStatus: (id, status) => api.patch(`/api/orders/${id}/status`, { status }),
  updateStock: (id, stock) => api.patch(`/api/admin/products/${id}/stock`, { stock }),
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
