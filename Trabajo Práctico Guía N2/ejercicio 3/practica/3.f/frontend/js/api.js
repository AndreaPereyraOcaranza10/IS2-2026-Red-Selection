export const api = {
  inicio: "/api/inicio",
  proveedores: "/api/proveedores",
  agregar: "/api/carrito/agregar",
  quitar: "/api/carrito/quitar",
  finalizar: "/api/carrito/finalizar"
};

export async function request(url, options = {}) {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.error || "No se pudo completar la operacion");
  }
  return data;
}
