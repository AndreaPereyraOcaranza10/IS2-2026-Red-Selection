package com.tienda.zero.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adapta movimientos de stock antiguos a los nombres definidos por el UML actual. */
@Component
@Order(-99)
@RequiredArgsConstructor
public class StockSchemaInitializer implements ApplicationRunner {

    private static final String TABLA = "stock";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (existeColumna("cantidad") && existeColumna("movimiento")) {
            jdbcTemplate.update("UPDATE stock SET movimiento = cantidad");
            eliminarColumna("cantidad");
        }
        if (existeColumna("fecha_movimiento") && existeColumna("fecha")) {
            jdbcTemplate.update("UPDATE stock SET fecha = fecha_movimiento");
            eliminarColumna("fecha_movimiento");
        }
        if (existeColumna("orden_compra_proveedor_id")) {
            eliminarClavesForaneas("orden_compra_proveedor_id");
            eliminarColumna("orden_compra_proveedor_id");
        }
        permitirDetalleFacturaOpcional();
    }

    private void eliminarClavesForaneas(String columna) {
        List<String> restricciones = jdbcTemplate.queryForList("""
                SELECT CONSTRAINT_NAME
                FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                  AND REFERENCED_TABLE_NAME IS NOT NULL
                """, String.class, TABLA, columna);
        for (String restriccion : restricciones) {
            if (!restriccion.matches("[A-Za-z0-9_$]+")) {
                throw new IllegalStateException("Nombre de restriccion inesperado: " + restriccion);
            }
            jdbcTemplate.execute("ALTER TABLE `stock` DROP FOREIGN KEY `" + restriccion + "`");
        }
    }

    private void eliminarColumna(String columna) {
        jdbcTemplate.execute("ALTER TABLE `stock` DROP COLUMN `" + columna + "`");
    }

    private boolean existeColumna(String columna) {
        Integer cantidad = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                """, Integer.class, TABLA, columna);
        return cantidad != null && cantidad > 0;
    }

    private void permitirDetalleFacturaOpcional() {
        if (!existeColumna("detalle_factura_id")) {
            return;
        }
        String nullable = jdbcTemplate.queryForObject("""
                SELECT IS_NULLABLE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                """, String.class, TABLA, "detalle_factura_id");
        if ("YES".equalsIgnoreCase(nullable)) {
            return;
        }
        String tipo = jdbcTemplate.queryForObject("""
                SELECT COLUMN_TYPE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                """, String.class, TABLA, "detalle_factura_id");
        jdbcTemplate.execute("ALTER TABLE `stock` MODIFY COLUMN `detalle_factura_id` " + tipo + " NULL");
    }
}
