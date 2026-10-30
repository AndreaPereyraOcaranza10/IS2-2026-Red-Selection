-- Catalogo inicial de ejemplo: Argentina y cuatro provincias.

-- Idempotente: conserva ids y datos existentes; no crea personas, direcciones ni cuentas.

INSERT INTO pais (nombre, eliminado, version) SELECT 'Argentina', false, 0 WHERE NOT EXISTS (SELECT 1 FROM pais WHERE lower(nombre) = lower('Argentina') AND eliminado = false);

INSERT INTO provincia (nombre, pais_id, eliminado, version) SELECT 'Mendoza', p.id, false, 0 FROM pais p WHERE lower(p.nombre) = lower('Argentina') AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM provincia v WHERE lower(v.nombre) = lower('Mendoza') AND v.pais_id = p.id AND v.eliminado = false);

INSERT INTO departamento (nombre, provincia_id, eliminado, version) SELECT 'Capital', v.id, false, 0 FROM provincia v JOIN pais p ON p.id = v.pais_id WHERE lower(v.nombre) = lower('Mendoza') AND lower(p.nombre) = lower('Argentina') AND v.eliminado = false AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM departamento d WHERE lower(d.nombre) = lower('Capital') AND d.provincia_id = v.id AND d.eliminado = false);

INSERT INTO localidad (nombre, codigo_postal, departamento_id, eliminado, version) SELECT 'Ciudad de Mendoza', '5500', d.id, false, 0 FROM departamento d JOIN provincia v ON v.id = d.provincia_id JOIN pais p ON p.id = v.pais_id WHERE lower(d.nombre) = lower('Capital') AND lower(v.nombre) = lower('Mendoza') AND lower(p.nombre) = lower('Argentina') AND d.eliminado = false AND v.eliminado = false AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM localidad l WHERE lower(l.nombre) = lower('Ciudad de Mendoza') AND l.departamento_id = d.id AND l.eliminado = false);

INSERT INTO provincia (nombre, pais_id, eliminado, version) SELECT 'Buenos Aires', p.id, false, 0 FROM pais p WHERE lower(p.nombre) = lower('Argentina') AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM provincia v WHERE lower(v.nombre) = lower('Buenos Aires') AND v.pais_id = p.id AND v.eliminado = false);

INSERT INTO departamento (nombre, provincia_id, eliminado, version) SELECT 'La Plata', v.id, false, 0 FROM provincia v JOIN pais p ON p.id = v.pais_id WHERE lower(v.nombre) = lower('Buenos Aires') AND lower(p.nombre) = lower('Argentina') AND v.eliminado = false AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM departamento d WHERE lower(d.nombre) = lower('La Plata') AND d.provincia_id = v.id AND d.eliminado = false);

INSERT INTO localidad (nombre, codigo_postal, departamento_id, eliminado, version) SELECT 'La Plata', '1900', d.id, false, 0 FROM departamento d JOIN provincia v ON v.id = d.provincia_id JOIN pais p ON p.id = v.pais_id WHERE lower(d.nombre) = lower('La Plata') AND lower(v.nombre) = lower('Buenos Aires') AND lower(p.nombre) = lower('Argentina') AND d.eliminado = false AND v.eliminado = false AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM localidad l WHERE lower(l.nombre) = lower('La Plata') AND l.departamento_id = d.id AND l.eliminado = false);

INSERT INTO provincia (nombre, pais_id, eliminado, version) SELECT 'Córdoba', p.id, false, 0 FROM pais p WHERE lower(p.nombre) = lower('Argentina') AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM provincia v WHERE lower(v.nombre) = lower('Córdoba') AND v.pais_id = p.id AND v.eliminado = false);

INSERT INTO departamento (nombre, provincia_id, eliminado, version) SELECT 'Capital', v.id, false, 0 FROM provincia v JOIN pais p ON p.id = v.pais_id WHERE lower(v.nombre) = lower('Córdoba') AND lower(p.nombre) = lower('Argentina') AND v.eliminado = false AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM departamento d WHERE lower(d.nombre) = lower('Capital') AND d.provincia_id = v.id AND d.eliminado = false);

INSERT INTO localidad (nombre, codigo_postal, departamento_id, eliminado, version) SELECT 'Córdoba', '5000', d.id, false, 0 FROM departamento d JOIN provincia v ON v.id = d.provincia_id JOIN pais p ON p.id = v.pais_id WHERE lower(d.nombre) = lower('Capital') AND lower(v.nombre) = lower('Córdoba') AND lower(p.nombre) = lower('Argentina') AND d.eliminado = false AND v.eliminado = false AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM localidad l WHERE lower(l.nombre) = lower('Córdoba') AND l.departamento_id = d.id AND l.eliminado = false);

INSERT INTO provincia (nombre, pais_id, eliminado, version) SELECT 'Santa Fe', p.id, false, 0 FROM pais p WHERE lower(p.nombre) = lower('Argentina') AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM provincia v WHERE lower(v.nombre) = lower('Santa Fe') AND v.pais_id = p.id AND v.eliminado = false);

INSERT INTO departamento (nombre, provincia_id, eliminado, version) SELECT 'Rosario', v.id, false, 0 FROM provincia v JOIN pais p ON p.id = v.pais_id WHERE lower(v.nombre) = lower('Santa Fe') AND lower(p.nombre) = lower('Argentina') AND v.eliminado = false AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM departamento d WHERE lower(d.nombre) = lower('Rosario') AND d.provincia_id = v.id AND d.eliminado = false);

INSERT INTO localidad (nombre, codigo_postal, departamento_id, eliminado, version) SELECT 'Rosario', '2000', d.id, false, 0 FROM departamento d JOIN provincia v ON v.id = d.provincia_id JOIN pais p ON p.id = v.pais_id WHERE lower(d.nombre) = lower('Rosario') AND lower(v.nombre) = lower('Santa Fe') AND lower(p.nombre) = lower('Argentina') AND d.eliminado = false AND v.eliminado = false AND p.eliminado = false AND NOT EXISTS (SELECT 1 FROM localidad l WHERE lower(l.nombre) = lower('Rosario') AND l.departamento_id = d.id AND l.eliminado = false);
