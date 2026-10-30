import axios from "axios";
export type Row = {
  id: number;
  nombre?: string;
  apellido?: string;
  dni?: string;
  fechaNacimiento?: string;
  codigoPostal?: string;
  calle?: string;
  numeracion?: string;
  ubicacion: string;
  [key: string]: string | number | undefined;
};
export type User = {
  id: number;
  email: string;
  proveedorAutenticacion: string;
  tienePassword: boolean;
  persona: Row | null;
};
export const api = axios.create({ baseURL: "/api", timeout: 20000 });
api.interceptors.request.use((c) => {
  const t = sessionStorage.getItem("atlas-token");
  if (t) c.headers.Authorization = `Bearer ${t}`;
  return c;
});
api.interceptors.response.use(
  (r) => r,
  (e) => {
    if (e.response?.status === 401 && !e.config?.url?.startsWith("/auth/")) {
      sessionStorage.removeItem("atlas-token");
      window.dispatchEvent(new Event("atlas-logout"));
    }
    return Promise.reject(e);
  },
);
export const errorMessage = (e: unknown): string =>
  axios.isAxiosError(e)
    ? e.response?.data?.message ||
      "No se pudo conectar con el servidor. Intentá nuevamente."
    : "Ocurrió un error inesperado";
export const labels: Record<string, string> = {
  nombre: "Nombre",
  apellido: "Apellido",
  dni: "DNI",
  fechaNacimiento: "Fecha de nacimiento",
  codigoPostal: "Código postal",
  calle: "Calle",
  numeracion: "Numeración",
  barrio: "Barrio",
  manzanaPiso: "Manzana / Piso",
  casaDepartamento: "Casa / Departamento",
  referencia: "Referencia",
  email: "Email",
  actual: "Contraseña actual",
  nueva: "Nueva contraseña",
  confirmacion: "Confirmar nueva contraseña",
};
export const levels = [
  { key: "paisId", module: "paises", label: "País" },
  { key: "provinciaId", module: "provincias", label: "Provincia" },
  { key: "departamentoId", module: "departamentos", label: "Departamento" },
  { key: "localidadId", module: "localidades", label: "Localidad" },
];
