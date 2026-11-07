/** Clave por defecto de todo usuario nuevo del sistema administrativo (regla de negocio de la consigna). */
export const INITIAL_PASSWORD = 'dulcemayor';

export function passwordStrength(v) {
  let s = 0;
  if (v.length >= 8) s++;
  if (/[A-Z]/.test(v) && /\d/.test(v)) s++;
  if (/[^A-Za-z0-9]/.test(v)) s++;
  if (v.length >= 12) s++;
  return s;
}

export const STRENGTH_LABELS = ['Escribí tu clave', 'Débil', 'Media', 'Buena', 'Muy buena'];

export function passwordRequirements(v, confirm) {
  return [
    { text: 'Al menos 8 caracteres', ok: v.length >= 8 },
    { text: 'Una mayúscula y un número', ok: /[A-Z]/.test(v) && /\d/.test(v) },
    { text: 'Un símbolo (#, $, !, ?)', ok: /[^A-Za-z0-9]/.test(v) },
    { text: 'Distinta de la clave inicial', ok: !!v && v.toLowerCase() !== INITIAL_PASSWORD },
    { text: 'Las dos claves coinciden', ok: !!v && v === confirm },
  ];
}
