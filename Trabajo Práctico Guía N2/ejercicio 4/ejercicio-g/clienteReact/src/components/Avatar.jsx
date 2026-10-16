import { fotoSrc } from '../utils/fotos.js'

export default function Avatar({ foto, tam = 40 }) {
  const src = fotoSrc(foto)
  if (src) {
    return <img className="avatar" src={src} alt="foto" width={tam} height={tam} />
  }
  return (
    <span className="avatar-vacio" style={{ width: tam, height: tam }}>
      <svg viewBox="0 0 40 40" width="100%" height="100%" aria-hidden="true">
        <defs>
          <clipPath id="avatar-recorte">
            <circle cx="20" cy="20" r="20" />
          </clipPath>
        </defs>
        <circle cx="20" cy="20" r="20" fill="#e8efff" />
        <g clipPath="url(#avatar-recorte)">
          <circle cx="20" cy="15.5" r="6.5" fill="#9db8f2" />
          <path d="M5 40c0-9 6-14 15-14s15 5 15 14z" fill="#9db8f2" />
        </g>
      </svg>
    </span>
  )
}