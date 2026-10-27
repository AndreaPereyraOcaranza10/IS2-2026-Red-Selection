package com.tienda.zero.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Amplia la restriccion de tipo heredada al incorporar la subclase FacturaCliente. */
@Component
@RequiredArgsConstructor
public class FacturaClienteSchemaInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        var restricciones = jdbc.queryForList("""
                SELECT tc.CONSTRAINT_NAME, cc.CHECK_CLAUSE
                FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                JOIN INFORMATION_SCHEMA.CHECK_CONSTRAINTS cc
                  ON cc.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA AND cc.CONSTRAINT_NAME = tc.CONSTRAINT_NAME
                WHERE tc.TABLE_SCHEMA = DATABASE() AND tc.TABLE_NAME = 'factura' AND tc.CONSTRAINT_TYPE = 'CHECK'
                """);
        for (var restriccion : restricciones) {
            String clausula = String.valueOf(restriccion.get("CHECK_CLAUSE"));
            if (!clausula.contains("tipo_factura") || clausula.contains("CLIENTE")) continue;
            String nombre = String.valueOf(restriccion.get("CONSTRAINT_NAME"));
            if (!nombre.matches("[a-zA-Z0-9_]+")) throw new IllegalStateException("Nombre de restriccion no valido");
            jdbc.execute("ALTER TABLE factura DROP CHECK `" + nombre + "`, ADD CONSTRAINT `" + nombre
                    + "` CHECK (tipo_factura IN ('PROVEEDOR', 'CLIENTE'))");
        }
    }
}
