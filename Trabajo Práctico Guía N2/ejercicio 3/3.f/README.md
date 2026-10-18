# Carrito de compras con mapa de proveedores

El proyecto conserva el carrito con Template Method e incorpora la dirección, latitud y longitud de cada proveedor. La sección de proveedores muestra un marcador por proveedor activo, aunque no tenga artículos, y ajusta el mapa para incluirlos a todos. Cada marcador muestra el nombre, la dirección y las coordenadas. La lista permite centrar un proveedor y el botón «Ver todos» restablece la vista general.

## Ejecutar en Windows (PowerShell)

Requiere JDK 17 o superior. Desde la carpeta `3.f`:

```powershell
New-Item -ItemType Directory -Force backend/build
$fuentes = (Get-ChildItem backend/src -Recurse -Filter *.java).FullName
javac --release 17 -encoding UTF-8 -d backend/build $fuentes
java -cp backend/build carrito.ServidorCarrito
```

Abrir <http://localhost:3000>. Para elegir otro puerto: `java -cp backend/build carrito.ServidorCarrito 3001`.

## Ejecutar desde VS Code

Abrir **la carpeta `3.f`** con **Archivo → Abrir carpeta**, para que VS Code use la configuración `.vscode` incluida en este proyecto. En **Ejecutar y depurar**, seleccionar **Iniciar carrito con mapa** y presionar **F5**. Esta configuración compila todo `backend/src` y ejecuta `carrito.ServidorCarrito` desde `backend/build`, con el directorio de trabajo correcto para servir el frontend.

La tarea de F5 compila directamente con `javac --release 17`, sin necesitar `iniciar.ps1`. Así las clases generadas también funcionan cuando VS Code usa Java 25 y la terminal tiene JDK 26.

Si aparece `Unresolved compilation problem: CarritoController cannot be resolved`, el código puede estar ejecutándose desde la caché temporal de Java de VS Code. Abrir la paleta de comandos (`Ctrl+Shift+P`), ejecutar **Java: Clean Java Language Server Workspace** y aceptar el reinicio. Luego usar la configuración indicada. Si se abrió todo el repositorio en lugar de `3.f`, su configuración general puede apuntar a otra clase o a carpetas de código incorrectas; los comandos de terminal anteriores permiten ejecutar este ejercicio de forma independiente.

## Datos y API

- `Proveedor` guarda `direccion` (texto), `latitud` y `longitud` (números). Valida dirección obligatoria y coordenadas finitas dentro de los rangos [-90, 90] y [-180, 180].
- `CatalogoDemo.java` define los proveedores y sus ubicaciones ilustrativas en Buenos Aires. Se pueden modificar allí y reiniciar el servidor. Los comercios son ficticios; las ubicaciones no representan domicilios comerciales verificados.
- `GET /api/proveedores` devuelve la lista de proveedores activos con sus direcciones y coordenadas.
- `GET /api/inicio` incluye también `proveedores`, además del usuario, artículos y carrito. Los artículos conservan su proveedor con los nuevos campos.
- Como en el proyecto original, los datos se almacenan en memoria. No se agregó una base de datos ni un formulario de alta/edición. No se realiza geocodificación: la dirección y las coordenadas se cargan juntas en el catálogo.

## Investigación: OpenStreetMap y Leaflet

OpenStreetMap aporta los datos y las imágenes del mapa; Leaflet es la biblioteca JavaScript que permite mostrarlos e interactuar con marcadores y ventanas emergentes. Se integró Leaflet 1.9.4 desde CDN, con hashes de integridad, siguiendo su [guía oficial](https://leafletjs.com/examples/quick-start/). El módulo `frontend/js/mapa.js` crea una sola instancia del mapa y reemplaza los marcadores al recargar para evitar duplicados.

Se utiliza `https://tile.openstreetmap.org/{z}/{x}/{y}.png` con atribución visible a OpenStreetMap y solicitudes normales del navegador, sin descargas masivas ni precarga, de acuerdo con la [política oficial de teselas](https://operations.osmfoundation.org/policies/tiles/). El servicio público tiene disponibilidad sin garantías y debe evaluarse un proveedor adecuado antes de un uso intensivo en producción.

Se necesita conexión a Internet para cargar Leaflet y el fondo del mapa. Si falla la carga, la página avisa y conserva la lista de direcciones; el carrito sigue disponible. No se necesita una clave de API ni acceso a la ubicación del usuario.
