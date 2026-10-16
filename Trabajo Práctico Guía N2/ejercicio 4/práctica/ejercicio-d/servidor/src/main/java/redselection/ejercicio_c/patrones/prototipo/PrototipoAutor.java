package redselection.ejercicio_c.patrones.prototipo;

/*

PATRÓN PROTOTIPO:

Autor implementa PrototipoAutor y define clonar() mediante

un constructor de copia. Se copian nombre, apellido y biografía,

sin reutilizar el ID original. AutorServiceImpl busca el original,

genera la copia y la guarda como un nuevo registro.
*/

public interface PrototipoAutor<T> {
    T clonar();
}
