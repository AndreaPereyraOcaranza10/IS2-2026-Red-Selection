export function fotoSrc(foto) {
  if (!foto || !foto.contenido) return null
  return `data:${foto.mime};base64,${foto.contenido}`
}

export function archivoAFoto(archivo) {
  return new Promise((resolve, reject) => {
    const lector = new FileReader()
    lector.onload = () => {
      const base64 = String(lector.result).split(',')[1]
      resolve({ nombre: archivo.name, mime: archivo.type, contenido: base64 })
    }
    lector.onerror = () => reject(new Error('No se pudo leer la foto'))
    lector.readAsDataURL(archivo)
  })
}