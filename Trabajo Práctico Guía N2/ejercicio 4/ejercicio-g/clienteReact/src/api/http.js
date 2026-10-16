export async function http(ruta, opciones = {}) {
  let respuesta
  try {
    respuesta = await fetch('/api' + ruta, {
      ...opciones,
      headers: { 'Content-Type': 'application/json', ...opciones.headers },
      body: opciones.body !== undefined ? JSON.stringify(opciones.body) : undefined,
    })
  } catch {
    throw new Error('No se pudo conectar con el servidor. ¿Está encendido?')
  }

  if (!respuesta.ok) {
    let mensaje = 'Error del servidor (' + respuesta.status + ')'
    try {
      const cuerpo = await respuesta.json()
      if (cuerpo && cuerpo.error) mensaje = cuerpo.error
    } catch {
      // el cuerpo no era JSON: dejamos el mensaje genérico
    }
    throw new Error(mensaje)
  }

  if (respuesta.status === 204) return null // DELETE devuelve 204 sin cuerpo
  return respuesta.json()
}