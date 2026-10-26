package com.tienda.zero.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Actualiza la restricción heredada de cliente_id para permitir carritos de usuarios administradores. */
@Component
@RequiredArgsConstructor
public class CarritoSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        var columnas = jdbcTemplate.queryForList(
                "SELECT COLUMN_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                "orden_compra_cliente", "cliente_id");

        if (columnas.isEmpty()) return;

        Map<String, Object> columna = columnas.get(0);
        String nullable = String.valueOf(columna.get("IS_NULLABLE"));
        if ("NO".equalsIgnoreCase(nullable)) {
            String tipo = String.valueOf(columna.get("COLUMN_TYPE"));
            jdbcTemplate.execute("ALTER TABLE `orden_compra_cliente` MODIFY COLUMN `cliente_id` " + tipo + " NULL");
        }
    }
}
