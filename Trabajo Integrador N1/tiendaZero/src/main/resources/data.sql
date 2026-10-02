-- Datos de demostracion para TiendaZero (MySQL 8).
-- Ejecutar manualmente UNA vez, despues de que Hibernate haya creado las tablas.
-- El script crea tambien las categorias que necesitan sus productos y es repetible:
-- usa IDs estables para los registros principales e inserciones geograficas condicionadas.
-- No activar spring.sql.init para MySQL: este archivo no debe correr en cada arranque.

USE tienda_zero;

START TRANSACTION;

-- Geografia argentina: provincias, departamentos y localidades seleccionados.
INSERT INTO pais (id, nombre, eliminado)
SELECT UUID(), 'Argentina', FALSE
WHERE NOT EXISTS (SELECT 1 FROM pais WHERE LOWER(nombre) = LOWER('Argentina') AND eliminado = FALSE);
SET @pais_argentina = (SELECT id FROM pais WHERE LOWER(nombre) = LOWER('Argentina') AND eliminado = FALSE ORDER BY id LIMIT 1);
INSERT INTO nacionalidad (id, nombre, eliminado)
SELECT UUID(), 'Argentina', FALSE
WHERE NOT EXISTS (SELECT 1 FROM nacionalidad WHERE LOWER(nombre) = LOWER('Argentina') AND eliminado = FALSE);

INSERT INTO provincia (id, nombre, eliminado, pais_id)
SELECT UUID(), 'Mendoza', FALSE, @pais_argentina
WHERE NOT EXISTS (SELECT 1 FROM provincia WHERE LOWER(nombre) = LOWER('Mendoza') AND pais_id = @pais_argentina AND eliminado = FALSE);
INSERT INTO provincia (id, nombre, eliminado, pais_id)
SELECT UUID(), 'Buenos Aires', FALSE, @pais_argentina
WHERE NOT EXISTS (SELECT 1 FROM provincia WHERE LOWER(nombre) = LOWER('Buenos Aires') AND pais_id = @pais_argentina AND eliminado = FALSE);
INSERT INTO provincia (id, nombre, eliminado, pais_id)
SELECT UUID(), 'Cordoba', FALSE, @pais_argentina
WHERE NOT EXISTS (SELECT 1 FROM provincia WHERE LOWER(nombre) = LOWER('Cordoba') AND pais_id = @pais_argentina AND eliminado = FALSE);

SET @prov_mendoza = (SELECT id FROM provincia WHERE nombre = 'Mendoza' AND pais_id = @pais_argentina AND eliminado = FALSE ORDER BY id LIMIT 1);
SET @prov_bsas = (SELECT id FROM provincia WHERE nombre = 'Buenos Aires' AND pais_id = @pais_argentina AND eliminado = FALSE ORDER BY id LIMIT 1);
SET @prov_cordoba = (SELECT id FROM provincia WHERE nombre = 'Cordoba' AND pais_id = @pais_argentina AND eliminado = FALSE ORDER BY id LIMIT 1);

INSERT INTO departamento (id, nombre, eliminado, provincia_id)
SELECT UUID(), 'Capital', FALSE, @prov_mendoza
WHERE NOT EXISTS (SELECT 1 FROM departamento WHERE nombre = 'Capital' AND provincia_id = @prov_mendoza AND eliminado = FALSE);
INSERT INTO departamento (id, nombre, eliminado, provincia_id)
SELECT UUID(), 'Godoy Cruz', FALSE, @prov_mendoza
WHERE NOT EXISTS (SELECT 1 FROM departamento WHERE nombre = 'Godoy Cruz' AND provincia_id = @prov_mendoza AND eliminado = FALSE);
INSERT INTO departamento (id, nombre, eliminado, provincia_id)
SELECT UUID(), 'La Plata', FALSE, @prov_bsas
WHERE NOT EXISTS (SELECT 1 FROM departamento WHERE nombre = 'La Plata' AND provincia_id = @prov_bsas AND eliminado = FALSE);
INSERT INTO departamento (id, nombre, eliminado, provincia_id)
SELECT UUID(), 'Capital', FALSE, @prov_cordoba
WHERE NOT EXISTS (SELECT 1 FROM departamento WHERE nombre = 'Capital' AND provincia_id = @prov_cordoba AND eliminado = FALSE);

SET @dep_mendoza_capital = (SELECT id FROM departamento WHERE nombre = 'Capital' AND provincia_id = @prov_mendoza AND eliminado = FALSE ORDER BY id LIMIT 1);
SET @dep_godoy_cruz = (SELECT id FROM departamento WHERE nombre = 'Godoy Cruz' AND provincia_id = @prov_mendoza AND eliminado = FALSE ORDER BY id LIMIT 1);
SET @dep_la_plata = (SELECT id FROM departamento WHERE nombre = 'La Plata' AND provincia_id = @prov_bsas AND eliminado = FALSE ORDER BY id LIMIT 1);
SET @dep_cordoba_capital = (SELECT id FROM departamento WHERE nombre = 'Capital' AND provincia_id = @prov_cordoba AND eliminado = FALSE ORDER BY id LIMIT 1);

INSERT INTO localidad (id, nombre, codigo_postal, eliminado, departamento_id)
SELECT UUID(), 'Ciudad de Mendoza', '5500', FALSE, @dep_mendoza_capital
WHERE NOT EXISTS (SELECT 1 FROM localidad WHERE nombre = 'Ciudad de Mendoza' AND departamento_id = @dep_mendoza_capital AND eliminado = FALSE);
INSERT INTO localidad (id, nombre, codigo_postal, eliminado, departamento_id)
SELECT UUID(), 'Godoy Cruz', '5501', FALSE, @dep_godoy_cruz
WHERE NOT EXISTS (SELECT 1 FROM localidad WHERE nombre = 'Godoy Cruz' AND departamento_id = @dep_godoy_cruz AND eliminado = FALSE);
INSERT INTO localidad (id, nombre, codigo_postal, eliminado, departamento_id)
SELECT UUID(), 'La Plata', '1900', FALSE, @dep_la_plata
WHERE NOT EXISTS (SELECT 1 FROM localidad WHERE nombre = 'La Plata' AND departamento_id = @dep_la_plata AND eliminado = FALSE);
INSERT INTO localidad (id, nombre, codigo_postal, eliminado, departamento_id)
SELECT UUID(), 'Ciudad de Cordoba', '5000', FALSE, @dep_cordoba_capital
WHERE NOT EXISTS (SELECT 1 FROM localidad WHERE nombre = 'Ciudad de Cordoba' AND departamento_id = @dep_cordoba_capital AND eliminado = FALSE);

SET @loc_mendoza = (SELECT id FROM localidad WHERE nombre = 'Ciudad de Mendoza' AND departamento_id = @dep_mendoza_capital AND eliminado = FALSE ORDER BY id LIMIT 1);
SET @loc_godoy_cruz = (SELECT id FROM localidad WHERE nombre = 'Godoy Cruz' AND departamento_id = @dep_godoy_cruz AND eliminado = FALSE ORDER BY id LIMIT 1);
SET @loc_la_plata = (SELECT id FROM localidad WHERE nombre = 'La Plata' AND departamento_id = @dep_la_plata AND eliminado = FALSE ORDER BY id LIMIT 1);

-- Formas de pago utilizadas por las facturas y las compras de clientes.
INSERT IGNORE INTO forma_de_pago (id, numero, tipo_pago, observacion, eliminado) VALUES
('00000000-0000-4000-a000-000000000001', NULL, 'TRANSFERENCIA', 'Transferencia bancaria', FALSE),
('00000000-0000-4000-a000-000000000002', NULL, 'BILLETERA_VIRTUAL', 'Mercado Pago', FALSE),
('00000000-0000-4000-a000-000000000003', NULL, 'EFECTIVO', 'Pago en efectivo', FALSE);

-- Cuentas iniciales de desarrollo. Claves: 1234, admin1234 y jefe1234, respectivamente.
-- Los hashes se generaron con el mismo BCryptPasswordEncoder que configura SecurityConfig.
INSERT IGNORE INTO usuario (id, nombre_usuario, clave, rol, eliminado, codigo_activacion, fecha_expiracion_codigo, cuenta_activada) VALUES
('00000000-0000-4000-a000-000000000150', 'admin@tiendazero.com', '$2a$10$ohGWWLVrhTrXFV1zZAlfEelHFHL/LWwy8dytlsqXSOv2C4yoT6XZu', 'JEFE', FALSE, NULL, NULL, TRUE),
('00000000-0000-4000-a000-000000000151', 'administrativo@tiendazero.com', '$2a$10$rrRjzaqCMtzjm.yKGpyBr.BsjvOiavCUQrkPoFRR.RamMLzN3s6Rq', 'ADMINISTRATIVO', FALSE, NULL, NULL, TRUE),
('00000000-0000-4000-a000-000000000152', 'jefe@tiendazero.com', '$2a$10$iPsnHxgd8tKT7tylFfO0x.lMWHsv3TBCCkuJCwFa55AmFpB1974/C', 'JEFE', FALSE, NULL, NULL, TRUE);

-- Proveedores y sus teléfonos.
INSERT IGNORE INTO proveedor (id, razon_social, eliminado) VALUES
('00000000-0000-4000-a000-000000000010', 'Textiles Cuyanos S.R.L.', FALSE),
('00000000-0000-4000-a000-000000000011', 'Calzados del Plata S.A.', FALSE);
INSERT IGNORE INTO contacto (id, tipo_contacto, observacion, eliminado, forma_contacto, proveedor_id) VALUES
('00000000-0000-4000-a000-000000000020', 'EMPRESA', 'Contacto comercial de proveedor', FALSE, 'TELEFONO', '00000000-0000-4000-a000-000000000010'),
('00000000-0000-4000-a000-000000000021', 'EMPRESA', 'Contacto comercial de proveedor', FALSE, 'TELEFONO', '00000000-0000-4000-a000-000000000011');
INSERT IGNORE INTO contacto_telefonico (id, telefono, tipo_telefono) VALUES
('00000000-0000-4000-a000-000000000020', '+5492615341338', 'CELULAR'),
('00000000-0000-4000-a000-000000000021', '+5492613179999', 'CELULAR');

-- Direcciones de sede central y sucursales.
INSERT IGNORE INTO direccion (id, calle, barrio, numeracion, manzana_piso, casa_departamento, referencia, eliminado, localidad_id) VALUES
('00000000-0000-4000-a000-000000000030', 'Avenida San Martin', 'Centro', '1250', NULL, NULL, 'Sede central', FALSE, @loc_mendoza),
('00000000-0000-4000-a000-000000000031', 'Las Heras', 'Centro', '840', NULL, NULL, 'Sucursal Mendoza', FALSE, @loc_mendoza),
('00000000-0000-4000-a000-000000000032', 'Calle 12', 'Centro', '650', NULL, NULL, 'Sucursal La Plata', FALSE, @loc_la_plata);

INSERT IGNORE INTO contacto (id, tipo_contacto, observacion, eliminado, forma_contacto) VALUES
('00000000-0000-4000-a000-000000000040', 'EMPRESA', 'Telefono de sede central', FALSE, 'TELEFONO'),
('00000000-0000-4000-a000-000000000041', 'EMPRESA', 'Telefono de sucursal Mendoza', FALSE, 'TELEFONO'),
('00000000-0000-4000-a000-000000000042', 'EMPRESA', 'Telefono de sucursal La Plata', FALSE, 'TELEFONO');
INSERT IGNORE INTO contacto_telefonico (id, telefono, tipo_telefono) VALUES
('00000000-0000-4000-a000-000000000040', '+5492615341338', 'CELULAR'),
('00000000-0000-4000-a000-000000000041', '+5492613179999', 'CELULAR'),
('00000000-0000-4000-a000-000000000042', '+5492615341338', 'CELULAR');

INSERT IGNORE INTO empresa (id, razon_social, cuit, tipo_empresa, eliminado, direccion_id, contacto_id) VALUES
('00000000-0000-4000-a000-000000000050', 'Red Selection Sede Central', '30745000101', 'SEDE_CENTRAL', FALSE, '00000000-0000-4000-a000-000000000030', '00000000-0000-4000-a000-000000000040'),
('00000000-0000-4000-a000-000000000051', 'Red Selection Sucursal Mendoza', '30745000102', 'SUCURSAL', FALSE, '00000000-0000-4000-a000-000000000031', '00000000-0000-4000-a000-000000000041'),
('00000000-0000-4000-a000-000000000052', 'Red Selection Sucursal La Plata', '30745000103', 'SUCURSAL', FALSE, '00000000-0000-4000-a000-000000000032', '00000000-0000-4000-a000-000000000042');

-- Cuatro empleados en las sucursales y sede central (herencia JOINED: persona + empleado).
INSERT IGNORE INTO persona (id, nombre, apellido, sexo, fecha_nacimiento, tipo_documento, numero_documento, eliminado, tipo_persona, imagen_id, usuario_id) VALUES
('00000000-0000-4000-a000-000000000060', 'Abril', 'Navarro', 'FEMENINO', '1993-02-17', 'DNI', '44100111', FALSE, 'EMPLEADO', NULL, NULL),
('00000000-0000-4000-a000-000000000061', 'Julian', 'Sosa', 'MASCULINO', '1995-07-09', 'DNI', '44200222', FALSE, 'EMPLEADO', NULL, NULL),
('00000000-0000-4000-a000-000000000062', 'Micaela', 'Quiroga', 'FEMENINO', '1997-04-26', 'DNI', '44300333', FALSE, 'EMPLEADO', NULL, NULL),
('00000000-0000-4000-a000-000000000063', 'Nicolas', 'Acosta', 'MASCULINO', '1990-12-03', 'DNI', '44400444', FALSE, 'EMPLEADO', NULL, NULL);
INSERT IGNORE INTO empleado (id, tipo_empleado, empresa_id) VALUES
('00000000-0000-4000-a000-000000000060', 'JEFE', '00000000-0000-4000-a000-000000000050'),
('00000000-0000-4000-a000-000000000061', 'ADMINISTRATIVO', '00000000-0000-4000-a000-000000000050'),
('00000000-0000-4000-a000-000000000062', 'ADMINISTRATIVO', '00000000-0000-4000-a000-000000000051'),
('00000000-0000-4000-a000-000000000063', 'JEFE', '00000000-0000-4000-a000-000000000052');

-- Catalogo base: cuatro categorias, cada una con ropa, calzado y accesorios.
INSERT INTO categoria (id, nombre, eliminado)
SELECT UUID(), 'Hombres', FALSE
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Hombres' AND eliminado = FALSE);
INSERT INTO categoria (id, nombre, eliminado)
SELECT UUID(), 'Mujeres', FALSE
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Mujeres' AND eliminado = FALSE);
INSERT INTO categoria (id, nombre, eliminado)
SELECT UUID(), 'Niños', FALSE
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Niños' AND eliminado = FALSE);
INSERT INTO categoria (id, nombre, eliminado)
SELECT UUID(), 'Niñas', FALSE
WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Niñas' AND eliminado = FALSE);
SET @cat_hombres = (SELECT id FROM categoria WHERE nombre = 'Hombres' AND eliminado = FALSE ORDER BY id LIMIT 1);
SET @cat_mujeres = (SELECT id FROM categoria WHERE nombre = 'Mujeres' AND eliminado = FALSE ORDER BY id LIMIT 1);
SET @cat_ninos = (SELECT id FROM categoria WHERE nombre = 'Niños' AND eliminado = FALSE ORDER BY id LIMIT 1);
SET @cat_ninas = (SELECT id FROM categoria WHERE nombre = 'Niñas' AND eliminado = FALSE ORDER BY id LIMIT 1);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Ropa', FALSE, @cat_hombres
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Ropa' AND categoria_id = @cat_hombres AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Calzado', FALSE, @cat_mujeres
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Calzado' AND categoria_id = @cat_mujeres AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Accesorios', FALSE, @cat_hombres
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Accesorios' AND categoria_id = @cat_hombres AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Ropa', FALSE, @cat_mujeres
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Ropa' AND categoria_id = @cat_mujeres AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Accesorios', FALSE, @cat_mujeres
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Accesorios' AND categoria_id = @cat_mujeres AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Calzado', FALSE, @cat_hombres
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Calzado' AND categoria_id = @cat_hombres AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Ropa', FALSE, @cat_ninos
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Ropa' AND categoria_id = @cat_ninos AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Calzado', FALSE, @cat_ninos
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Calzado' AND categoria_id = @cat_ninos AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Accesorios', FALSE, @cat_ninos
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Accesorios' AND categoria_id = @cat_ninos AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Ropa', FALSE, @cat_ninas
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Ropa' AND categoria_id = @cat_ninas AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Calzado', FALSE, @cat_ninas
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Calzado' AND categoria_id = @cat_ninas AND eliminado = FALSE);
INSERT INTO subcategoria (id, nombre, eliminado, categoria_id)
SELECT UUID(), 'Accesorios', FALSE, @cat_ninas
WHERE NOT EXISTS (SELECT 1 FROM subcategoria WHERE nombre = 'Accesorios' AND categoria_id = @cat_ninas AND eliminado = FALSE);

SET @sub_ropa_hombres = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Ropa' AND c.nombre = 'Hombres' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);
SET @sub_calzado_hombres = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Calzado' AND c.nombre = 'Hombres' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);
SET @sub_calzado_mujeres = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Calzado' AND c.nombre = 'Mujeres' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);
SET @sub_accesorios_hombres = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Accesorios' AND c.nombre = 'Hombres' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);
SET @sub_ropa_mujeres = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Ropa' AND c.nombre = 'Mujeres' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);
SET @sub_accesorios_mujeres = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Accesorios' AND c.nombre = 'Mujeres' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);
SET @sub_ropa_ninos = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Ropa' AND c.nombre = 'Niños' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);
SET @sub_calzado_ninos = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Calzado' AND c.nombre = 'Niños' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);
SET @sub_ropa_ninas = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Ropa' AND c.nombre = 'Niñas' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);
SET @sub_calzado_ninas = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Calzado' AND c.nombre = 'Niñas' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);
SET @sub_accesorios_ninas = (SELECT s.id FROM subcategoria s JOIN categoria c ON c.id = s.categoria_id WHERE s.nombre = 'Accesorios' AND c.nombre = 'Niñas' AND s.eliminado = FALSE AND c.eliminado = FALSE ORDER BY s.id LIMIT 1);

-- Ilustraciones SVG distintas, guardadas como BLOB para que funcionen sin LOAD_FILE.
INSERT IGNORE INTO imagen (id, nombre, mime, contenido, tipo_imagen, eliminado) VALUES
('00000000-0000-4000-a000-000000000160', 'remera-zero-active.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#f1f5f9"/><path d="M90 55 112 40h32l22 15 30 24-18 25-20-14v79H98V90l-20 14-18-25z" fill="#df5a3c"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">REMERA</text></svg>' AS BINARY), 'PRODUCTO', FALSE),
('00000000-0000-4000-a000-000000000161', 'zapatillas-run-plata.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#e7f0f8"/><path d="M42 148q30-5 49-55l20-31q10-12 20 1l28 35q12 13 34 18l28 10q16 6 16 21v19H42z" fill="#286f9c"/><path d="M109 111l23 8m-38 2 24 9m-35 1 23 9" stroke="white" stroke-width="7"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">RUN</text></svg>' AS BINARY), 'PRODUCTO', FALSE),
('00000000-0000-4000-a000-000000000162', 'mochila-training.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#fff5dd"/><rect x="75" y="55" width="106" height="135" rx="25" fill="#d79c2b"/><path d="M101 56q0-39 27-39t27 39M75 88H52v70h23m106-70h23v70h-23" fill="none" stroke="#39465a" stroke-width="10"/><rect x="94" y="115" width="68" height="48" rx="12" fill="white"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">MOCHILA</text></svg>' AS BINARY), 'PRODUCTO', FALSE),
('00000000-0000-4000-a000-000000000163', 'botines-training.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#e7f5ed"/><path d="M44 149q30-4 49-56l19-31q10-13 21 1l28 34q12 13 33 18l28 9q17 6 17 21v20H44z" fill="#3c8b62"/><path d="M111 110l22 9m-37 2 24 9m-35 2 24 9" stroke="white" stroke-width="7"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">TRAINING</text></svg>' AS BINARY), 'PRODUCTO', FALSE),
('00000000-0000-4000-a000-000000000164', 'calza-fit.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#f4eafa"/><path d="M83 42h90l-7 67-10 75h-39l-7-66-8 66H62l14-78z" fill="#9a5bb5"/><path d="M84 60h88m-47 0v49" stroke="white" stroke-width="6"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">CALZA</text></svg>' AS BINARY), 'PRODUCTO', FALSE),
('00000000-0000-4000-a000-000000000165', 'gorra-active.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#fce9ee"/><path d="M60 132q8-74 68-82 62 3 72 77l-53 4q-20-32-48 0z" fill="#db657f"/><path d="M128 52v55m-68 26q63 16 141-5l20 18q-75 28-160 5z" fill="#344256"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">GORRA</text></svg>' AS BINARY), 'PRODUCTO', FALSE),
('00000000-0000-4000-a000-000000000166', 'buzo-kids.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#e8edff"/><path d="M101 55q27-39 54 0l27 12 27 37-24 20-16-15v62H87v-62l-16 15-24-20 27-37z" fill="#5c78c5"/><path d="M101 56q27 33 54 0m-27 26v40" fill="none" stroke="white" stroke-width="6"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">BUZO KIDS</text></svg>' AS BINARY), 'PRODUCTO', FALSE),
('00000000-0000-4000-a000-000000000167', 'zapatillas-kids.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#e3f8f7"/><path d="M45 148q25-8 39-43l22-39q10-15 23-5l29 32q13 12 34 16l28 8q17 5 17 21v20H45z" fill="#25a6a1"/><path d="M97 113l25 9m-38 2 25 9m-35 2 25 9" stroke="white" stroke-width="7"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">KIDS RUN</text></svg>' AS BINARY), 'PRODUCTO', FALSE),
('00000000-0000-4000-a000-000000000168', 'vestido-sport.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#fff0e9"/><path d="M105 46h46l13 48 42 73H50l42-73z" fill="#e06b4f"/><path d="M105 49q23 22 46 0m-23 21v94M72 139h112" fill="none" stroke="white" stroke-width="6"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">VESTIDO</text></svg>' AS BINARY), 'PRODUCTO', FALSE),
('00000000-0000-4000-a000-000000000169', 'zapatillas-girls.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#fff3dc"/><path d="M45 148q25-8 39-43l22-39q10-15 23-5l29 32q13 12 34 16l28 8q17 5 17 21v20H45z" fill="#e5a233"/><path d="M97 113l25 9m-38 2 25 9m-35 2 25 9" stroke="white" stroke-width="7"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">GIRLS RUN</text></svg>' AS BINARY), 'PRODUCTO', FALSE),
('00000000-0000-4000-a000-000000000170', 'vincha-training.svg', 'image/svg+xml', CAST('<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><rect width="256" height="256" rx="20" fill="#f0eafa"/><path d="M55 151q7-103 73-104 67 1 73 104" fill="none" stroke="#7550a1" stroke-width="27"/><path d="M62 151q66 27 132 0" fill="none" stroke="#344256" stroke-width="10"/><text x="128" y="220" text-anchor="middle" font-family="Arial" font-size="16">VINCHA</text></svg>' AS BINARY), 'PRODUCTO', FALSE);

INSERT IGNORE INTO producto (id, codigo, nombre, descripcion, talle, en_oferta, porcentaje_descuento, stock_ideal, eliminado, subcategoria_id, proveedor_id, imagen_id) VALUES
('00000000-0000-4000-a000-000000000070', 'SQL-REM-001', 'Remera Zero Active', 'Remera deportiva de secado rapido', 'L', FALSE, 0, 30, FALSE, @sub_ropa_hombres, '00000000-0000-4000-a000-000000000010', '00000000-0000-4000-a000-000000000160'),
('00000000-0000-4000-a000-000000000071', 'SQL-ZAP-002', 'Zapatillas Run Plata', 'Zapatillas para running', '42', FALSE, 0, 20, FALSE, @sub_calzado_mujeres, '00000000-0000-4000-a000-000000000011', '00000000-0000-4000-a000-000000000161'),
('00000000-0000-4000-a000-000000000072', 'SQL-MOC-003', 'Mochila Training', 'Mochila deportiva impermeable', 'UNICO', FALSE, 0, 15, FALSE, @sub_accesorios_hombres, '00000000-0000-4000-a000-000000000010', '00000000-0000-4000-a000-000000000162'),
('00000000-0000-4000-a000-000000000120', 'SQL-ZAP-004', 'Botines Training Zero', 'Calzado deportivo para entrenamiento', '41', FALSE, 0, 24, FALSE, @sub_calzado_hombres, '00000000-0000-4000-a000-000000000010', '00000000-0000-4000-a000-000000000163'),
('00000000-0000-4000-a000-000000000121', 'SQL-CAL-005', 'Calza Fit Mujer', 'Calza deportiva de cintura alta', 'M', FALSE, 0, 20, FALSE, @sub_ropa_mujeres, '00000000-0000-4000-a000-000000000010', '00000000-0000-4000-a000-000000000164'),
('00000000-0000-4000-a000-000000000122', 'SQL-GOR-006', 'Gorra Active Mujer', 'Gorra liviana para actividades al aire libre', 'UNICO', FALSE, 0, 18, FALSE, @sub_accesorios_mujeres, '00000000-0000-4000-a000-000000000011', '00000000-0000-4000-a000-000000000165'),
('00000000-0000-4000-a000-000000000123', 'SQL-BUZ-007', 'Buzo Deportivo Kids', 'Buzo infantil para entrenamiento', '10', FALSE, 0, 15, FALSE, @sub_ropa_ninos, '00000000-0000-4000-a000-000000000010', '00000000-0000-4000-a000-000000000166'),
('00000000-0000-4000-a000-000000000124', 'SQL-ZNK-008', 'Zapatillas Kids Run', 'Zapatillas infantiles para correr', '32', FALSE, 0, 16, FALSE, @sub_calzado_ninos, '00000000-0000-4000-a000-000000000011', '00000000-0000-4000-a000-000000000167'),
('00000000-0000-4000-a000-000000000125', 'SQL-VES-009', 'Vestido Sport Nina', 'Vestido deportivo infantil', '10', FALSE, 0, 22, FALSE, @sub_ropa_ninas, '00000000-0000-4000-a000-000000000010', '00000000-0000-4000-a000-000000000168'),
('00000000-0000-4000-a000-000000000126', 'SQL-ZNG-010', 'Zapatillas Girls Run', 'Zapatillas livianas para running infantil', '32', FALSE, 0, 18, FALSE, @sub_calzado_ninas, '00000000-0000-4000-a000-000000000011', '00000000-0000-4000-a000-000000000169'),
('00000000-0000-4000-a000-000000000127', 'SQL-VIN-011', 'Vincha Training Nina', 'Vincha elastica para actividad deportiva', 'UNICO', FALSE, 0, 14, FALSE, @sub_accesorios_ninas, '00000000-0000-4000-a000-000000000010', '00000000-0000-4000-a000-000000000170');

-- Actualiza también los tres productos del borrador anterior si el SQL ya se había ejecutado.
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000160' WHERE id = '00000000-0000-4000-a000-000000000070';
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000161' WHERE id = '00000000-0000-4000-a000-000000000071';
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000162' WHERE id = '00000000-0000-4000-a000-000000000072';
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000163' WHERE id = '00000000-0000-4000-a000-000000000120';
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000164' WHERE id = '00000000-0000-4000-a000-000000000121';
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000165' WHERE id = '00000000-0000-4000-a000-000000000122';
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000166' WHERE id = '00000000-0000-4000-a000-000000000123';
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000167' WHERE id = '00000000-0000-4000-a000-000000000124';
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000168' WHERE id = '00000000-0000-4000-a000-000000000125';
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000169' WHERE id = '00000000-0000-4000-a000-000000000126';
UPDATE producto SET imagen_id = '00000000-0000-4000-a000-000000000170' WHERE id = '00000000-0000-4000-a000-000000000127';

INSERT IGNORE INTO historial_precios (id, fecha_desde, fecha_hasta, precio, eliminado, id_producto) VALUES
('00000000-0000-4000-a000-000000000073', '2026-05-01', '2026-07-01', 15725.00, FALSE, '00000000-0000-4000-a000-000000000070'),
('00000000-0000-4000-a000-000000000074', '2026-07-01', NULL, 18500.00, FALSE, '00000000-0000-4000-a000-000000000070'),
('00000000-0000-4000-a000-000000000075', '2026-05-01', '2026-07-01', 35700.00, FALSE, '00000000-0000-4000-a000-000000000071'),
('00000000-0000-4000-a000-000000000076', '2026-07-01', NULL, 42000.00, FALSE, '00000000-0000-4000-a000-000000000071'),
('00000000-0000-4000-a000-000000000077', '2026-05-01', '2026-07-01', 27200.00, FALSE, '00000000-0000-4000-a000-000000000072'),
('00000000-0000-4000-a000-000000000078', '2026-07-01', NULL, 32000.00, FALSE, '00000000-0000-4000-a000-000000000072');
INSERT IGNORE INTO historial_precios (id, fecha_desde, fecha_hasta, precio, eliminado, id_producto) VALUES
('00000000-0000-4000-a000-000000000128', '2026-07-01', NULL, 22000.00, FALSE, '00000000-0000-4000-a000-000000000120'),
('00000000-0000-4000-a000-000000000129', '2026-07-01', NULL, 24900.00, FALSE, '00000000-0000-4000-a000-000000000121'),
('00000000-0000-4000-a000-000000000130', '2026-07-01', NULL, 12500.00, FALSE, '00000000-0000-4000-a000-000000000122'),
('00000000-0000-4000-a000-000000000131', '2026-07-01', NULL, 28500.00, FALSE, '00000000-0000-4000-a000-000000000123'),
('00000000-0000-4000-a000-000000000132', '2026-07-01', NULL, 34500.00, FALSE, '00000000-0000-4000-a000-000000000124'),
('00000000-0000-4000-a000-000000000133', '2026-07-01', NULL, 19500.00, FALSE, '00000000-0000-4000-a000-000000000125'),
('00000000-0000-4000-a000-000000000134', '2026-07-01', NULL, 32000.00, FALSE, '00000000-0000-4000-a000-000000000126'),
('00000000-0000-4000-a000-000000000135', '2026-07-01', NULL, 9000.00, FALSE, '00000000-0000-4000-a000-000000000127');

INSERT IGNORE INTO stock (id, movimiento, cantidad_actual, observacion, fecha, eliminado, producto_id, detalle_factura_id, detalle_compra_id) VALUES
('00000000-0000-4000-a000-000000000080', 18, 18, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000070', NULL, NULL),
('00000000-0000-4000-a000-000000000081', 12, 12, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000071', NULL, NULL),
('00000000-0000-4000-a000-000000000082', 8, 8, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000072', NULL, NULL);
INSERT IGNORE INTO stock (id, movimiento, cantidad_actual, observacion, fecha, eliminado, producto_id, detalle_factura_id, detalle_compra_id) VALUES
('00000000-0000-4000-a000-000000000136', 14, 14, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000120', NULL, NULL),
('00000000-0000-4000-a000-000000000137', 10, 10, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000121', NULL, NULL),
('00000000-0000-4000-a000-000000000138', 9, 9, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000122', NULL, NULL),
('00000000-0000-4000-a000-000000000139', 6, 6, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000123', NULL, NULL),
('00000000-0000-4000-a000-000000000140', 7, 7, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000124', NULL, NULL),
('00000000-0000-4000-a000-000000000141', 11, 11, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000125', NULL, NULL),
('00000000-0000-4000-a000-000000000142', 8, 8, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000126', NULL, NULL),
('00000000-0000-4000-a000-000000000143', 5, 5, 'Stock inicial de demostracion', '2026-09-01 09:00:00', FALSE, '00000000-0000-4000-a000-000000000127', NULL, NULL);

-- Dos ordenes de compra a proveedores, cada una vinculada a su factura.
INSERT IGNORE INTO orden_compra (id, proveedor_id, fecha_creacion, entregada) VALUES
('00000000-0000-4000-a000-000000000090', '00000000-0000-4000-a000-000000000010', '2026-09-02', TRUE),
('00000000-0000-4000-a000-000000000091', '00000000-0000-4000-a000-000000000011', '2026-09-05', TRUE);
INSERT IGNORE INTO detalle_orden_compra (id, producto_id, cantidad, precio_unitario, orden_compra_id) VALUES
('00000000-0000-4000-a000-000000000092', '00000000-0000-4000-a000-000000000070', 10, 10000.00, '00000000-0000-4000-a000-000000000090'),
('00000000-0000-4000-a000-000000000093', '00000000-0000-4000-a000-000000000072', 5, 14000.00, '00000000-0000-4000-a000-000000000090'),
('00000000-0000-4000-a000-000000000094', '00000000-0000-4000-a000-000000000071', 8, 42000.00, '00000000-0000-4000-a000-000000000091');

INSERT IGNORE INTO factura (id, numero_factura, fecha_factura, total_pagado, estado, eliminado, forma_de_pago_id, tipo_factura) VALUES
('00000000-0000-4000-a000-000000000100', 90001, '2026-09-03', 170000.00, 'PAGADA', FALSE, '00000000-0000-4000-a000-000000000001', 'PROVEEDOR'),
('00000000-0000-4000-a000-000000000101', 90002, '2026-09-06', 336000.00, 'PAGADA', FALSE, '00000000-0000-4000-a000-000000000001', 'PROVEEDOR');
INSERT IGNORE INTO factura_proveedor (id, proveedor_id, orden_compra_proveedor_id) VALUES
('00000000-0000-4000-a000-000000000100', '00000000-0000-4000-a000-000000000010', '00000000-0000-4000-a000-000000000090'),
('00000000-0000-4000-a000-000000000101', '00000000-0000-4000-a000-000000000011', '00000000-0000-4000-a000-000000000091');
INSERT IGNORE INTO detalle_factura (id, cantidad, subtotal, eliminado, producto_id, factura_id) VALUES
('00000000-0000-4000-a000-000000000102', 10, 100000.00, FALSE, '00000000-0000-4000-a000-000000000070', '00000000-0000-4000-a000-000000000100'),
('00000000-0000-4000-a000-000000000103', 5, 70000.00, FALSE, '00000000-0000-4000-a000-000000000072', '00000000-0000-4000-a000-000000000100'),
('00000000-0000-4000-a000-000000000104', 8, 336000.00, FALSE, '00000000-0000-4000-a000-000000000071', '00000000-0000-4000-a000-000000000101');

-- Dos ordenes de compra de clientes, con sus productos y totales.
INSERT IGNORE INTO orden_compra_cliente (id, identificador_compra, fecha, eliminado, total, estado_orden_compra, direccion_entrega, forma_pago, mp_preference_id, mp_payment_id, mp_payment_status, usuario_propietario_id, cliente_id, empleado_id) VALUES
('00000000-0000-4000-a000-000000000110', 'DEMO-CLI-001', '2026-09-10', FALSE, 37000.00, 'ENTREGADO', 'Av. San Martin 1250, Mendoza', 'BILLETERA_VIRTUAL', NULL, NULL, 'approved', NULL, NULL, NULL),
('00000000-0000-4000-a000-000000000111', 'DEMO-CLI-002', '2026-09-12', FALSE, 84000.00, 'PENDIENTE_ENTREGA', 'Calle 12 650, La Plata', 'TRANSFERENCIA', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT IGNORE INTO detalle_compra (id, orden_compra_id, producto_id, cantidad, subtotal, eliminado) VALUES
('00000000-0000-4000-a000-000000000112', '00000000-0000-4000-a000-000000000110', '00000000-0000-4000-a000-000000000070', 2, 37000.00, FALSE),
('00000000-0000-4000-a000-000000000113', '00000000-0000-4000-a000-000000000111', '00000000-0000-4000-a000-000000000071', 2, 84000.00, FALSE);

COMMIT;
