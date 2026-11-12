const API_BASE = import.meta.env.VITE_API_URL || "/api";

function accountFromSession(session, fallbackEmail = "") {
  const user = session?.user || {};
  return {
    email: user.username || fallbackEmail,
    role: user.role === "ADMINISTRATIVO" ? "ADMIN" : user.role,
    displayName: user.fullName || user.username || fallbackEmail,
    token: session?.token,
    mustChangePassword: Boolean(user.mustChangePassword),
  };
}

async function request(path, init = {}) {
  const headers = new Headers(init.headers || {});
  if (!(init.body instanceof FormData) && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  try {
    const account = JSON.parse(sessionStorage.getItem("dulcemayor.account") || "null");
    if (account?.token) headers.set("Authorization", `Bearer ${account.token}`);
  } catch {
    // Ignore invalid local session data and let the API respond as unauthenticated.
  }
  const response = await fetch(API_BASE + path, { ...init, headers });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    const isAuthenticationRequest = path.endsWith("/auth/login") || path.endsWith("/auth/register");
    if (response.status === 401 && !isAuthenticationRequest) {
      sessionStorage.removeItem("dulcemayor.account");
      if (window.location.pathname !== "/ingresar" && window.location.pathname !== "/login") window.location.replace("/ingresar");
    }
    throw new Error(body?.detail || body?.message || `Error ${response.status}`);
  }
  if (response.status === 204) return;
  return response.json();
}

async function requestPdf(path) {
  const headers = new Headers();
  try {
    const account = JSON.parse(sessionStorage.getItem("dulcemayor.account") || "null");
    if (account?.token) headers.set("Authorization", "Bearer " + account.token);
  } catch {
    // Let the API reject an invalid or missing session.
  }
  const response = await fetch(API_BASE + path, { headers });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    if (response.status === 401) {
      sessionStorage.removeItem("dulcemayor.account");
      if (window.location.pathname !== "/ingresar" && window.location.pathname !== "/login") {
        window.location.replace("/ingresar");
      }
    }
    throw new Error(body?.detail || body?.message || "No se pudo descargar la factura.");
  }

  const disposition = response.headers.get("Content-Disposition") || "";
  const match = disposition.match(/filename\*?=(?:UTF-8''|")?([^;"\r\n"]+)/i);
  let filename = match?.[1]?.trim();
  if (filename) {
    try {
      filename = decodeURIComponent(filename);
    } catch {
      // Keep the server-provided filename if it is not URL encoded.
    }
    filename = filename.replace(/[\\/:*?"<>|]/g, "-");
  }
  return { blob: await response.blob(), filename };
}

export const api = {
  products: () => request("/products"),
  exchangeRate: () => request("/exchange-rate/usd"),
  branches: () => request("/admin/companies/branches"),
  publicBranches: () => request("/companies/branches"),
  createBranch: (branch) => request("/admin/companies/branches", { method: "POST", body: JSON.stringify(branch) }),
  updateBranch: (id, branch) => request(`/admin/companies/branches/${id}`, { method: "PUT", body: JSON.stringify(branch) }),
  uploadBranchPhoto: async (id, file) => {
    const form = new FormData();
    form.append("archivo", file);
    form.append("tipo", "FOTO_SUCURSAL");
    const image = await request("/imagenes", { method: "POST", body: form });
    return request(`/admin/companies/branches/${id}/images`, { method: "POST", body: JSON.stringify({ imagenId: image.id }) });
  },
  deleteBranch: (id) => request(`/admin/companies/branches/${id}`, { method: "DELETE" }),
  product: (id) => request(`/products/${id}`),
  login: async (email, password) => accountFromSession(
    await request("/auth/login", { method: "POST", body: JSON.stringify({ username: email, password }) }), email,
  ),
  profile: () => request("/profile"),
  updateProfile: (profile) => request("/profile", { method: "PUT", body: JSON.stringify(profile) }),
  provinces: () => request("/locations/provinces"),
  departments: (provinceId) => request(`/locations/provinces/${provinceId}/departments`),
  localities: (departmentId) => request(`/locations/departments/${departmentId}/localities`),
  register: (email, password) => request("/auth/register", { method: "POST", body: JSON.stringify({ email, password }) }),
  changeInitialPassword: async (newPassword) => accountFromSession(
    await request("/auth/first-login/change-password", { method: "POST", body: JSON.stringify({ newPassword }) }),
  ),
  changePassword: async (currentPassword, newPassword) => accountFromSession(
    await request("/auth/change-password", { method: "POST", body: JSON.stringify({ currentPassword, newPassword }) }),
  ),
  orders: () => request("/orders"),
  cancelOrder: (id) => request(`/orders/${encodeURIComponent(id)}/cancel`, { method: "POST", body: JSON.stringify({}) }),
  invoicePdf: (id) => requestPdf("/facturas/" + encodeURIComponent(id) + "/pdf"),
  validatePromotion: (code) => request("/promociones/validar", { method: "POST", body: JSON.stringify({ code }) }),
  chatbotBudget: async (mensaje) => {
    const response = await fetch(API_BASE + "/chatbot/presupuestos", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ mensaje }),
    });
    if (!response.ok) {
      const body = await response.json().catch(() => null);
      throw new Error(body?.detail || body?.message || `Error ${response.status}`);
    }
    return response.json();
  },
  createOrder: (items, promoCode) => request("/orders", { method: "POST", body: JSON.stringify({ clientName: "Cuenta mayorista", paymentMethod: "A_COORDINAR", items, ...(promoCode ? { promoCode } : {}) }) }),
  createManualOrder: (clientName, items) => request("/orders", { method: "POST", body: JSON.stringify({ clientName, paymentMethod: "A_COORDINAR", items }) }),
  updateOrderStatus: (id, status) => request(`/orders/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }),
  inventory: () => request("/admin/products"),
  updateStock: (id, stock) => request(`/admin/products/${id}/stock`, { method: "PATCH", body: JSON.stringify({ stock }) }),
  productRecords: () => request("/productos"),
  createProduct: (product) => request("/productos", { method: "POST", body: JSON.stringify(product) }),
  updateProduct: (id, product) => request(`/productos/${id}`, { method: "PUT", body: JSON.stringify(product) }),
  deleteProduct: (id) => request(`/productos/${id}`, { method: "DELETE" }),
  updateStock: (id, stock) => request(`/admin/products/${id}/stock`, { method: "PATCH", body: JSON.stringify({ stock }) }),
  updatePrice: (id, precio) => request(`/productos/${id}/precio`, { method: "PUT", body: JSON.stringify({ precio }) }),
  categories: () => request("/categorias"),
  createCategory: (category) => request("/categorias", { method: "POST", body: JSON.stringify(category) }),
  updateCategory: (id, category) => request(`/categorias/${id}`, { method: "PUT", body: JSON.stringify(category) }),
  deleteCategory: (id) => request(`/categorias/${id}`, { method: "DELETE" }),
  subcategories: () => request("/subcategorias"),
  createSubcategory: (subcategory) => request("/subcategorias", { method: "POST", body: JSON.stringify(subcategory) }),
  updateSubcategory: (id, subcategory) => request(`/subcategorias/${id}`, { method: "PUT", body: JSON.stringify(subcategory) }),
  deleteSubcategory: (id) => request(`/subcategorias/${id}`, { method: "DELETE" }),
  uploadImage: (file) => {
    const form = new FormData();
    form.append("archivo", file);
    return request("/imagenes", { method: "POST", body: form });
  },
  imageUrl: (id) => `${API_BASE}/imagenes/${id}`
};
