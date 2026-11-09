export const ROUTES = {
  home: "/",
  catalog: "/#catalogo",
  productDetail: "/productos/:id",
  product: (id) => `/productos/${id}`,
  orders: "/pedidos",
  profile: "/perfil",
  login: "/ingresar",
  admin: "/admin/inicio"
};
export const ADMIN_ROUTES = {
  home: "/admin/inicio",
  products: "/admin/productos",
  stock: "/admin/stock",
  orders: "/admin/pedidos",
  billing: "/admin/facturacion",
  dispatch: "/admin/despacho",
  account: "/admin/cuenta",
  branches: "/admin/sucursales"
};
export const ACCOUNT_STORAGE_KEY = "dulcemayor.account";
export const ADMIN_ROLES = ["ADMIN", "JEFE_ADMINISTRATIVO"];

export function adminAppUrl(path = "/login") {
  const base = import.meta.env.VITE_ADMIN_URL
    || `${window.location.protocol}//${window.location.hostname}:5176`;
  return new URL(path, base).toString();
}
