/** Error normalizado: refleja el cuerpo que devuelve el @RestControllerAdvice del backend. */
export class ApiError extends Error {
  /**
   * @param {number} status  código HTTP (0 = sin conexión)
   * @param {string} message mensaje apto para mostrar al usuario
   * @param {{code?: string, errors?: {field: string, message: string}[]}} [extra]
   */
  constructor(status, message, { code, errors } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.errors = errors || [];
  }

  /** Errores de validación (@Valid) como mapa campo -> mensaje. */
  fieldErrors() {
    return Object.fromEntries(this.errors.map((e) => [e.field, e.message]));
  }
}
