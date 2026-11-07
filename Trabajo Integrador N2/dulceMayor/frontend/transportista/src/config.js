import { ROLES } from '@dulcemayor/shared';

/**
 * Roles que pueden usar esta app.
 * La consigna no define un rol "transportista": asigna el reparto al equipo de despacho/logística,
 * que acá se mapea a ALMACENERO. Si el equipo decide crear un rol propio (ej. TRANSPORTISTA),
 * se cambia únicamente esta línea (y el enum del backend).
 */
export const ALLOWED_ROLES = [ROLES.ALMACENERO];
export const DEMO_USER = 'despacho.mza';
export const HOME = '/reparto';
