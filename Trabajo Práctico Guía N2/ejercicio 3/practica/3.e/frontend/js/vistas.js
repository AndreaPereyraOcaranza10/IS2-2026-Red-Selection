import { state } from "./estado.js";

const formatter = new Intl.NumberFormat("es-AR", {
  style: "currency",
  currency: "ARS",
  maximumFractionDigits: 0
});

export const $ = (selector) => document.querySelector(selector);

function renderUsuario() {
  $("#usuarioActivo").textContent = state.usuario
    ? `${state.usuario.nombre} ${state.usuario.apellido}`
    : "Invitado";
}

function renderProductos() {
  $("#productos").innerHTML = state.articulos
    .map((articulo) => {
      const imagen = articulo.imagenes[0];
      return `
        <article class="product-card">
          <img src="${imagen.contenido}" alt="${imagen.nombre}">
          <div class="product-body">
            <h3 class="product-title">${articulo.nombre}</h3>
            <p class="product-meta">Proveedor: ${articulo.proveedor.nombre}</p>
            <span class="product-price">${formatter.format(articulo.precio)}</span>
            <button class="add-button" data-add="${articulo.id}" type="button">Agregar</button>
          </div>
        </article>
      `;
    })
    .join("");
}

export function renderCarrito() {
  const carrito = state.carrito;
  const detalles = carrito?.detalles || [];
  const totalItems = detalles.reduce((total, detalle) => total + detalle.cantidad, 0);

  $("#cantidadItems").textContent = totalItems;
  $("#subtotal").textContent = formatter.format(carrito?.subtotal || 0);
  $("#envio").textContent = formatter.format(carrito?.envio || 0);
  $("#total").textContent = formatter.format(carrito?.total || 0);

  if (detalles.length === 0) {
    $("#detalles").innerHTML = '<div class="empty-state">El carrito esta vacio.</div>';
    return;
  }

  $("#detalles").innerHTML = detalles
    .map((detalle) => {
      const articulo = detalle.articulo;
      const imagen = articulo.imagenes[0];
      return `
        <article class="cart-item">
          <img src="${imagen.contenido}" alt="${imagen.nombre}">
          <div class="cart-info">
            <h3>${articulo.nombre}</h3>
            <p>${formatter.format(articulo.precio)} x ${detalle.cantidad}</p>
            <div class="cart-actions">
              <button class="quantity-button" data-remove="${articulo.id}" type="button">-</button>
              <strong>${detalle.cantidad}</strong>
              <button class="quantity-button" data-add="${articulo.id}" type="button">+</button>
            </div>
          </div>
        </article>
      `;
    })
    .join("");
}

export function render() {
  renderUsuario();
  renderProductos();
  renderCarrito();
}
