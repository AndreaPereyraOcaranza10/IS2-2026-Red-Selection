import { ROLES } from '@dulcemayor/shared';

/** Roles que pueden usar esta app (rol PREVENTISTA del backend). */
export const ALLOWED_ROLES = [ROLES.PREVENTISTA];
export const DEMO_USER = 'preventista.mza';
export const HOME = '/ruta';
/** Provincia que se preselecciona en el alta de puntos de venta: Mendoza (id oficial de Georef). */
export const DEFAULT_PROVINCE_ID = '50';
export const CATEGORIES = ['Todas', 'Alfajores', 'Chocolates', 'Caramelos', 'Chicles', 'Gomitas', 'Turrones', 'Snacks'];