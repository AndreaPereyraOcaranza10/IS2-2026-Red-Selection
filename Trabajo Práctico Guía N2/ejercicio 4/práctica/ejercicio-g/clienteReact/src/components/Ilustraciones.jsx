const A = '#175cdd'   // acento
const L = '#e8efff'   // celeste claro
const L2 = '#cfdcff'  // celeste medio
const T = '#2bb3a3'   // verde agua
const C = '#ff8a7a'   // coral
const Y = '#ffc857'   // amarillo
const SKIN = '#f2c6a0'
const HAIR = '#3b2f4a'
const INK = '#1d2a44'

function Marco({ children }) {
  return (
    <svg viewBox="0 0 200 150" role="img" aria-hidden="true" xmlns="http://www.w3.org/2000/svg">
      <ellipse cx="100" cy="80" rx="88" ry="62" fill={L} />
      <circle cx="30" cy="30" r="5" fill={L2} />
      <circle cx="176" cy="24" r="3.5" fill={Y} />
      <circle cx="170" cy="128" r="4" fill={L2} />
      <path d="M22 108h10M27 103v10" stroke={T} strokeWidth="3" strokeLinecap="round" />
      <path d="M168 52h8M172 48v8" stroke={C} strokeWidth="3" strokeLinecap="round" />
      {children}
    </svg>
  )
}

export function IlustracionClinica() {
  return (
    <Marco>
      <ellipse cx="100" cy="134" rx="74" ry="6" fill={L2} />
      <rect x="52" y="60" width="96" height="72" rx="8" fill="#fff" stroke={L2} strokeWidth="2" />
      <rect x="84" y="30" width="32" height="32" rx="9" fill={A} />
      <path d="M100 38v16M92 46h16" stroke="#fff" strokeWidth="5" strokeLinecap="round" />
      <rect x="90" y="96" width="20" height="36" rx="4" fill={A} />
      <circle cx="106" cy="115" r="1.8" fill="#fff" />
      <rect x="62" y="74" width="18" height="16" rx="3" fill={L2} />
      <rect x="120" y="74" width="18" height="16" rx="3" fill={L2} />
      <rect x="62" y="98" width="18" height="16" rx="3" fill={L2} />
      <rect x="120" y="98" width="18" height="16" rx="3" fill={L2} />
      <circle cx="40" cy="116" r="12" fill={T} />
      <rect x="38.5" y="120" width="3" height="14" fill={INK} opacity=".55" />
      <circle cx="162" cy="120" r="9" fill={T} opacity=".8" />
      <path d="M170 84c0-5 6-6 8-2 2-4 8-3 8 2 0 5-8 10-8 10s-8-5-8-10z" fill={C} />
    </Marco>
  )
}

export function IlustracionPacientes() {
  return (
    <Marco>
      <path d="M36 134c0-28 18-40 42-40s42 12 42 40z" fill={A} />
      <circle cx="78" cy="64" r="21" fill={SKIN} />
      <path d="M57 62c0-17 12-26 24-26 13 0 20 9 19 22-6-8-14-11-24-9-9 1-15 6-19 13z" fill={HAIR} />
      <circle cx="71" cy="66" r="2" fill={INK} />
      <circle cx="86" cy="66" r="2" fill={INK} />
      <path d="M73 75q5 4 10 0" stroke={INK} strokeWidth="2" fill="none" strokeLinecap="round" />
      <rect x="108" y="70" width="68" height="46" rx="9" fill="#fff" stroke={L2} strokeWidth="2" />
      <circle cx="128" cy="88" r="9" fill={L2} />
      <circle cx="128" cy="85" r="3.5" fill={A} />
      <path d="M120 97c1-6 15-6 16 0" fill={A} />
      <rect x="143" y="81" width="24" height="4.5" rx="2" fill={L2} />
      <rect x="143" y="91" width="18" height="4.5" rx="2" fill={L2} />
      <rect x="117" y="104" width="50" height="4.5" rx="2" fill={L2} />
      <path d="M170 64c0-5 6-6 8-2 2-4 8-3 8 2 0 5-8 10-8 10s-8-5-8-10z" fill={C} />
    </Marco>
  )
}

export function IlustracionMedicos() {
  return (
    <Marco>
      <path d="M52 136c0-30 20-42 48-42s48 12 48 42z" fill="#fff" stroke={L2} strokeWidth="2" />
      <path d="M84 96l16 22 16-22z" fill={A} />
      <circle cx="100" cy="58" r="21" fill={SKIN} />
      <path d="M79 56c0-18 12-26 22-26 12 0 21 8 20 24-5-7-10-10-21-10-9 0-16 4-21 12z" fill={HAIR} />
      <circle cx="93" cy="60" r="2" fill={INK} />
      <circle cx="108" cy="60" r="2" fill={INK} />
      <path d="M95 69q5 4 10 0" stroke={INK} strokeWidth="2" fill="none" strokeLinecap="round" />
      <path d="M80 98c-10 14-4 28 10 30 9 1 14-6 12-14" stroke={INK} strokeWidth="3" fill="none" strokeLinecap="round" />
      <circle cx="102" cy="116" r="6" fill={Y} stroke={INK} strokeWidth="2" />
      <rect x="122" y="108" width="16" height="16" rx="4" fill={C} />
      <path d="M130 111v10M125 116h10" stroke="#fff" strokeWidth="3" strokeLinecap="round" />
    </Marco>
  )
}

export function IlustracionUsuarios() {
  return (
    <Marco>
      <path d="M32 134c0-28 18-40 42-40s42 12 42 40z" fill={T} />
      <circle cx="74" cy="64" r="21" fill={SKIN} />
      <path d="M53 66c-2-20 10-32 24-32 14 0 22 10 19 28-4-9-12-13-22-12-8 1-16 6-21 16z" fill={HAIR} />
      <circle cx="67" cy="67" r="2" fill={INK} />
      <circle cx="82" cy="67" r="2" fill={INK} />
      <path d="M69 76q5 4 10 0" stroke={INK} strokeWidth="2" fill="none" strokeLinecap="round" />
      <path d="M142 58l26 9v22c0 19-14 29-26 34-12-5-26-15-26-34V67z" fill={A} />
      <path d="M142 70l16 6v13c0 11-8 18-16 22" fill="#fff" opacity=".18" />
      <rect x="133" y="86" width="18" height="14" rx="3" fill="#fff" />
      <path d="M137 86v-4a5 5 0 0 1 10 0v4" stroke="#fff" strokeWidth="3" fill="none" />
      <circle cx="142" cy="93" r="2.2" fill={A} />
    </Marco>
  )
}

export function IlustracionHistorias() {
  return (
    <Marco>
      <rect x="70" y="30" width="76" height="104" rx="10" fill={L2} transform="rotate(7 108 82)" />
      <rect x="58" y="28" width="78" height="104" rx="10" fill="#fff" stroke={A} strokeWidth="2.5" />
      <rect x="80" y="20" width="34" height="16" rx="6" fill={A} />
      <circle cx="97" cy="28" r="3" fill="#fff" />
      <path d="M68 70h18l7-18 11 40 9-26 6 4h9" stroke={C} strokeWidth="3.5" fill="none" strokeLinecap="round" strokeLinejoin="round" />
      <rect x="68" y="102" width="56" height="5" rx="2.5" fill={L2} />
      <rect x="68" y="113" width="38" height="5" rx="2.5" fill={L2} />
      <g transform="rotate(35 160 100)">
        <rect x="154" y="62" width="12" height="56" rx="3" fill={Y} />
        <path d="M154 118h12l-6 14z" fill={SKIN} />
        <path d="M158 126h4l-2 6z" fill={INK} />
        <rect x="154" y="62" width="12" height="8" rx="3" fill={C} />
      </g>
    </Marco>
  )
}