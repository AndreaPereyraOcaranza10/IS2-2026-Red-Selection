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
export const ADMIN_ROLES = ["ADMIN", "ALMACENERO", "JEFE_ADMINISTRATIVO"];
