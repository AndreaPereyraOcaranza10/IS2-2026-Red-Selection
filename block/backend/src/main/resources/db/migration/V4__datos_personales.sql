-- Se permiten nulos para conservar los perfiles existentes hasta que se completen.
ALTER TABLE persona ADD COLUMN dni VARCHAR(8);
ALTER TABLE persona ADD COLUMN fecha_nacimiento DATE;
