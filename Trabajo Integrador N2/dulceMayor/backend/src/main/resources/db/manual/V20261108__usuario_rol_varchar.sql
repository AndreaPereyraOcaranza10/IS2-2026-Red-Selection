-- Migración manual para bases MySQL existentes.
-- Conserva los valores guardados y permite roles nuevos como JEFE_ADMINISTRATIVO.
ALTER TABLE usuario
    MODIFY COLUMN rol VARCHAR(32) NOT NULL;
