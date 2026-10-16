import { createContext, useCallback, useContext, useState } from 'react'

const AvisosContext = createContext(null)

export function AvisosProvider({ children }) {
  const [aviso, setAviso] = useState(null) // { tipo: 'exito' | 'error', texto }

  const exito = useCallback((texto) => setAviso({ tipo: 'exito', texto }), [])
  const error = useCallback((texto) => setAviso({ tipo: 'error', texto }), [])
  const limpiar = useCallback(() => setAviso(null), [])

  return (
    <AvisosContext.Provider value={{ aviso, exito, error, limpiar }}>
      {children}
    </AvisosContext.Provider>
  )
}

export function useAvisos() {
  return useContext(AvisosContext)
}

export function BannerAviso() {
  const { aviso, limpiar } = useAvisos()
  if (!aviso) return null
  return (
    <div className={`aviso aviso-${aviso.tipo}`} role="alert">
      <span>{aviso.texto}</span>
      <button className="aviso-cerrar" onClick={limpiar} aria-label="Cerrar">×</button>
    </div>
  )
}