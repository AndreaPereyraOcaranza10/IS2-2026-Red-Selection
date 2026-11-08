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
  const headers = new Headers({ "Content-Type": "application/json", ...init.headers });
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

export const api = {
  products: () => request("/products"),
  exchangeRate: () => request("/exchange-rate/usd"),
  branches: () => request("/admin/companies/branches"),
  publicBranches: () => request("/companies/branches"),
  createBranch: (branch) => request("/admin/companies/branches", { method: "POST", body: JSON.stringify(branch) }),
  updateBranch: (id, branch) => request(`/admin/companies/branches/${id}`, { method: "PUT", body: JSON.stringify(branch) }),
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
  createOrder: (items) => request("/orders", { method: "POST", body: JSON.stringify({ clientName: "Cuenta mayorista", paymentMethod: "A_COORDINAR", items }) }),
  createManualOrder: (clientName, items) => request("/orders", { method: "POST", body: JSON.stringify({ clientName, paymentMethod: "A_COORDINAR", items }) }),
  updateOrderStatus: (id, status) => request(`/orders/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }),
  inventory: () => request("/admin/products"),
  updateStock: (id, stock) => request(`/admin/products/${id}/stock`, { method: "PATCH", body: JSON.stringify({ stock }) }),
};
