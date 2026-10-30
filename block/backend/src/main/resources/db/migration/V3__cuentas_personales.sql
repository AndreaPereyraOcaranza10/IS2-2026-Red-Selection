-- Las cuentas existentes conservan sus datos, pero no permisos administrativos.
UPDATE usuario
SET rol = 'USER', credential_version = credential_version + 1, version = version + 1
WHERE rol <> 'USER';
