# Carrito de compras con mapa de proveedores

El proyecto conserva el carrito con Template Method e incorpora la dirección, latitud y longitud de cada proveedor. La sección de proveedores muestra un marcador por proveedor activo, aunque no tenga artículos, y ajusta el mapa para incluirlos a todos. Cada marcador muestra el nombre, la dirección y las coordenadas. La lista permite centrar un proveedor y el botón «Ver todos» restablece la vista general.


## Datos y API

- `Proveedor` guarda `direccion` (texto), `latitud` y `longitud` (números). Valida dirección obligatoria y coordenadas finitas dentro de los rangos [-90, 90] y [-180, 180].
- `CatalogoDemo.java` define los proveedores y sus ubicaciones ilustrativas en Buenos Aires. Se pueden modificar allí y reiniciar el servidor. 
- `GET /api/proveedores` devuelve la lista de proveedores activos con sus direcciones y coordenadas.
- `GET /api/inicio` incluye también `proveedores`, además del usuario, artículos y carrito. Los artículos conservan su proveedor con los nuevos campos.
- Como en el proyecto original, los datos se almacenan en memoria. No se agregó una base de datos ni un formulario de alta/edición. No se realiza geocodificación: la dirección y las coordenadas se cargan juntas en el catálogo.

## Investigación: OpenStreetMap y Leaflet

OpenStreetMap aporta los datos y las imágenes del mapa; Leaflet es la biblioteca JavaScript que permite mostrarlos e interactuar con marcadores y ventanas emergentes. El módulo `frontend/js/mapa.js` crea una sola instancia del mapa y reemplaza los marcadores al recargar para evitar duplicados.

Se necesita conexión a Internet para cargar Leaflet y el fondo del mapa. Si falla la carga, la página avisa y conserva la lista de direcciones; el carrito sigue disponible. No se necesita una clave de API ni acceso a la ubicación del usuario.
