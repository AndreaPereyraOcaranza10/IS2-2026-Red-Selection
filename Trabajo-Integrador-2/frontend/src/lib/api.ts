import type { Product } from '../data/products';

const API_BASE = import.meta.env.VITE_API_URL || '/api';

export type CustomerProfile = {
  nombre: string;
  apellido: string;
  sexo: 'MASCULINO' | 'FEMENINO' | 'OTRO' | null;
  fechaNacimiento: string;
  tipoDocumento: 'DNI' | 'PASAPORTE';
  numeroDocumento: string;
  email: string;
  telefono: string;
  tipoTelefono: 'FIJO' | 'CELULAR';
  provincia: string;
  departamento: string;
  localidad: string;
  codigoPostal: string;
  calle: string;
  numeroCalle: string;
  manzanaPiso: string;
  casaDepartamento: string;
  referencia: string;
};

export type OrderLine = { productId: string; productName: string; quantity: number; unitPrice: number; total: number };
export type CustomerOrder = {
  id: string;
  clientName: string;
  status: string;
  paymentMethod: string;
  createdAt: string;
  invoicedAt?: string | null;
  total: number;
  items: OrderLine[];
};
export type LocationOption = { id: string; name: string };
export type DollarQuote = { currency: string; source: string; buy: number; sell: number; updatedAt: string; lastScrapedAt: string; stale: boolean; sourceUrl: string };
export type CompanyBranch = {
  id: string; razonSocial: string; cuit: string; calle: string; numeracion: string; barrio: string | null;
  manzanaPiso: string | null; casaDepartamento: string | null; referencia: string | null;
  localidadId: string; localidad: string; departamento: string; provincia: string; email: string;
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.detail || body?.message || `Error ${response.status}`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export const api = {
  products: () => request<Product[]>('/products'),
  exchangeRate: () => request<DollarQuote>('/exchange-rate/usd'),
  branches: () => request<CompanyBranch[]>('/admin/companies/branches'),
  publicBranches: () => request<CompanyBranch[]>('/companies/branches'),
  createBranch: (branch: Omit<CompanyBranch, 'id' | 'localidad' | 'departamento' | 'provincia'>) => request<CompanyBranch>('/admin/companies/branches', { method: 'POST', body: JSON.stringify(branch) }),
  updateBranch: (id: string, branch: Omit<CompanyBranch, 'id' | 'localidad' | 'departamento' | 'provincia'>) => request<CompanyBranch>(`/admin/companies/branches/${id}`, { method: 'PUT', body: JSON.stringify(branch) }),
  deleteBranch: (id: string) => request<void>(`/admin/companies/branches/${id}`, { method: 'DELETE' }),
  product: (id: string | number) => request<Product>(`/products/${id}`),
  login: (email: string, password: string) => request<{ email: string; role: string; displayName: string }>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  profile: () => request<CustomerProfile>('/profile'),
  updateProfile: (profile: CustomerProfile) => request<CustomerProfile>('/profile', { method: 'PUT', body: JSON.stringify(profile) }),
  provinces: () => request<LocationOption[]>('/locations/provinces'),
  departments: (provinceId: string) => request<LocationOption[]>(`/locations/provinces/${provinceId}/departments`),
  localities: (departmentId: string) => request<LocationOption[]>(`/locations/departments/${departmentId}/localities`),
  register: (email: string, password: string) => request<{ email: string; message: string }>('/auth/register', { method: 'POST', body: JSON.stringify({ email, password }) }),
  orders: () => request<CustomerOrder[]>('/orders'),
  createOrder: (items: Array<{ productId: string | number; quantity: number }>) => request<CustomerOrder>('/orders', { method: 'POST', body: JSON.stringify({ clientName: 'Cuenta mayorista', paymentMethod: 'A_COORDINAR', items }) }),
  createManualOrder: (clientName: string, items: Array<{ productId: string | number; quantity: number }>) => request<CustomerOrder>('/orders', { method: 'POST', body: JSON.stringify({ clientName, paymentMethod: 'A_COORDINAR', items }) }),
  updateOrderStatus: (id: string, status: string) => request(`/orders/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
  inventory: () => request<Product[]>('/admin/products'),
  updateStock: (id: string | number, stock: number) => request<Product>(`/admin/products/${id}/stock`, { method: 'PATCH', body: JSON.stringify({ stock }) }),
};
