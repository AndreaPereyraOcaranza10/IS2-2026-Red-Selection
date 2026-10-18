import { useEffect } from 'react'
import { NavLink, Outlet, Link, useLocation } from 'react-router-dom'
import { BannerAviso, useAvisos } from './Avisos.jsx'

const ENLACES = [
  { a: '/', texto: 'Inicio', end: true },
  { a: '/pacientes', texto: 'Pacientes' },
  { a: '/medicos', texto: 'Médicos' },
  { a: '/usuarios', texto: 'Usuarios' },
  { a: '/historias', texto: 'Historias clínicas' },
]

export default function Layout() {
  const { limpiar } = useAvisos()
  const { pathname } = useLocation()

  const seccion = pathname.split('/')[1]
  useEffect(() => {
    return () => limpiar()
  }, [seccion, limpiar])

  return (
    <>
      <header className="encabezado">
        <div className="contenedor encabezado-fila">
          <Link to="/" className="logo">
            <svg className="logo-icono" viewBox="0 0 32 32" width="30" height="30" aria-hidden="true">
              <rect width="32" height="32" rx="9" fill="#175cdd" />
              <path d="M16 8v16M8 16h16" stroke="#fff" strokeWidth="4" strokeLinecap="round" />
            </svg>
            Consultorio
          </Link>
          <nav className="menu">
            {ENLACES.map((e) => (
              <NavLink key={e.a} to={e.a} end={e.end} className={({ isActive }) => (isActive ? 'activo' : '')}>
                {e.texto}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>
      <main className="contenedor principal">
        <BannerAviso />
        <Outlet />
      </main>
    </>
  )
}