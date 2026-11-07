import { PATHS } from './iconPaths.js';

export function Icon({ name, size = 24, ...rest }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      dangerouslySetInnerHTML={{ __html: PATHS[name] || '' }}
      {...rest}
    />
  );
}

export function Candy({ size = 24 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <circle cx="12" cy="12" r="5.5" />
      <path d="M7 12 1.5 7.5v9zM17 12l5.5-4.5v9z" />
    </svg>
  );
}

/** Logo placeholder: caramelo + nombre. Reemplazar por el logo real cuando exista. */
export function Logo({ small = false }) {
  return (
    <div className={`logo ${small ? 'sm' : ''}`}>
      <div className="mk">
        <Candy size={small ? 28 : 64} />
      </div>
      <span>Dulce Mayor</span>
    </div>
  );
}
