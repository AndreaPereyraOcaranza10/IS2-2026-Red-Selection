import { useCallback, useEffect, useState } from 'react'

export function useCargar(funcion, dependencias = []) {
  const [datos, setDatos] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  const recargar = useCallback(async () => {
    setCargando(true)
    setError('')
    try {
      setDatos(await funcion())
    } catch (e) {
      setError(e.message)
    } finally {
      setCargando(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, dependencias)

  useEffect(() => {
    recargar()
  }, [recargar])

  return { datos, cargando, error, recargar }
}