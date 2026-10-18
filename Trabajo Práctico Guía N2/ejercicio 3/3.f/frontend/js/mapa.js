let mapa;
let marcadores;

function crearDetalle(proveedor) {
  const detalle = document.createElement("div");
  const nombre = document.createElement("strong");
  nombre.textContent = proveedor.nombre;
  const direccion = document.createElement("p");
  direccion.textContent = proveedor.direccion;
  const coordenadas = document.createElement("p");
  coordenadas.textContent = `Latitud: ${proveedor.latitud} · Longitud: ${proveedor.longitud}`;
  detalle.append(nombre, direccion, coordenadas);
  return detalle;
}

function coordenadasValidas(proveedor) {
  return Number.isFinite(proveedor.latitud) && Math.abs(proveedor.latitud) <= 90
    && Number.isFinite(proveedor.longitud) && Math.abs(proveedor.longitud) <= 180;
}

function verTodos() {
  if (marcadores.getLayers().length) {
    mapa.fitBounds(marcadores.getBounds(), { padding: [32, 32], maxZoom: 15 });
  }
}

export function renderProveedores(proveedores) {
  const lista = document.querySelector("#listaProveedores");
  const estado = document.querySelector("#estadoMapa");
  const botonTodos = document.querySelector("#verTodosBtn");
  const activos = proveedores.filter((proveedor) => !proveedor.eliminado);
  lista.replaceChildren();
  botonTodos.disabled = true;

  if (window.L && !mapa) {
    mapa = window.L.map("mapaProveedores", { scrollWheelZoom: false })
      .setView([-34.6037, -58.3816], 12);
    const capa = window.L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(mapa);
    capa.on("tileerror", () => {
      estado.textContent = "No se pudo cargar el fondo del mapa. Revisá tu conexión; las direcciones siguen disponibles.";
    });
    marcadores = window.L.featureGroup().addTo(mapa);
    botonTodos.addEventListener("click", verTodos);
  }

  if (marcadores) marcadores.clearLayers();
  let cantidadUbicados = 0;
  for (const proveedor of activos) {
    const item = document.createElement("li");
    item.append(crearDetalle(proveedor));
    if (mapa && coordenadasValidas(proveedor)) {
      const marcador = window.L.marker([proveedor.latitud, proveedor.longitud], {
        title: proveedor.nombre,
        alt: `Ubicación de ${proveedor.nombre}`
      }).bindPopup(crearDetalle(proveedor)).addTo(marcadores);
      const boton = document.createElement("button");
      boton.type = "button";
      boton.className = "ghost-button";
      boton.textContent = "Ver en el mapa";
      boton.setAttribute("aria-label", `Ver ${proveedor.nombre} en el mapa`);
      boton.addEventListener("click", () => {
        mapa.setView(marcador.getLatLng(), 16);
        marcador.openPopup();
        document.querySelector("#mapaProveedores").scrollIntoView({ behavior: "smooth", block: "nearest" });
      });
      item.append(boton);
      cantidadUbicados++;
    }
    lista.append(item);
  }

  if (!activos.length) {
    estado.textContent = "No hay proveedores para mostrar.";
  } else if (!mapa) {
    estado.textContent = "No se pudo cargar el mapa. Revisá tu conexión y recargá la página. Podés consultar las direcciones en la lista.";
  } else {
    estado.textContent = `${cantidadUbicados} de ${activos.length} proveedores ubicados en el mapa.`;
  }
  if (cantidadUbicados) {
    botonTodos.disabled = false;
    mapa.invalidateSize();
    verTodos();
  }
}
