import { api, request } from "./api.js";
import { state } from "./estado.js";
import { $, render, renderCarrito } from "./vistas.js";

async function cargarInicio() {
  $("#mensaje").textContent = "";
  const data = await request(api.inicio);
  state.articulos = data.articulos;
  state.carrito = data.carrito;
  state.usuario = data.usuario;
  render();
}

async function agregarArticulo(idArticulo) {
  const data = await request(api.agregar, {
    method: "POST",
    body: JSON.stringify({ idArticulo })
  });
  state.carrito = data.carrito;
  $("#mensaje").textContent = data.mensaje;
  renderCarrito();
}

async function quitarArticulo(idArticulo) {
  const data = await request(api.quitar, {
    method: "POST",
    body: JSON.stringify({ idArticulo })
  });
  state.carrito = data.carrito;
  $("#mensaje").textContent = data.mensaje;
  renderCarrito();
}

async function finalizarCompra() {
  const data = await request(api.finalizar, { method: "POST" });
  state.carrito = data.carrito;
  $("#mensaje").textContent = data.mensaje;
  renderCarrito();
}

document.addEventListener("click", async (event) => {
  const addButton = event.target.closest("[data-add]");
  const removeButton = event.target.closest("[data-remove]");

  try {
    if (addButton) {
      await agregarArticulo(addButton.dataset.add);
    }
    if (removeButton) {
      await quitarArticulo(removeButton.dataset.remove);
    }
  } catch (error) {
    $("#mensaje").textContent = error.message;
  }
});

$("#recargarBtn").addEventListener("click", cargarInicio);
$("#finalizarBtn").addEventListener("click", async () => {
  try {
    await finalizarCompra();
  } catch (error) {
    $("#mensaje").textContent = error.message;
  }
});

cargarInicio().catch((error) => {
  $("#mensaje").textContent = error.message;
});
