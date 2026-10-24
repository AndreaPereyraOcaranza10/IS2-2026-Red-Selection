package com.tienda.zero.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/** Migra el esquema antiguo de una linea por orden al modelo actual de orden con detalles. */
@Component
@Order(-100)
@RequiredArgsConstructor
public class OrdenCompraProveedorSchemaInitializer implements ApplicationRunner {

    private static final String TABLA_ORDEN = "orden_compra";
    private static final List<String> COLUMNAS_ANTIGUAS = List.of(
            "producto_id", "cantidad", "precio_compra", "total", "estado", "fecha_recepcion");

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (!existeColumna("cantidad") || !existeColumna("producto_id")) {
            return;
        }

        migrarDetallesAnteriores();
        migrarEstadoAnterior();
        eliminarClavesForaneas("producto_id");
        for (String columna : COLUMNAS_ANTIGUAS) {
            if (existeColumna(columna)) {
                jdbcTemplate.execute("ALTER TABLE `orden_compra` DROP COLUMN `" + columna + "`");
            }
        }
    }

    private void migrarDetallesAnteriores() {
        jdbcTemplate.update("""
                INSERT INTO detalle_orden_compra
                    (id, cantidad, precio_unitario, producto_id, orden_compra_id)
                SELECT UUID(), oc.cantidad, oc.precio_compra, oc.producto_id, oc.id
                FROM orden_compra oc
                WHERE oc.producto_id IS NOT NULL
                  AND oc.cantidad IS NOT NULL
                  AND oc.precio_compra IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1 FROM detalle_orden_compra d WHERE d.orden_compra_id = oc.id
                  )
                """);
    }

    private void migrarEstadoAnterior() {
        if (existeColumna("estado") && existeColumna("entregada")) {
            jdbcTemplate.update("UPDATE orden_compra SET entregada = true WHERE estado = 'ENTREGADA'");
        }
    }

    private void eliminarClavesForaneas(String columna) {
        List<String> restricciones = jdbcTemplate.queryForList("""
                SELECT CONSTRAINT_NAME
                FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                  AND REFERENCED_TABLE_NAME IS NOT NULL
                """, String.class, TABLA_ORDEN, columna);

        for (String restriccion : restricciones) {
            if (!restriccion.matches("[A-Za-z0-9_$]+")) {
                throw new IllegalStateException("Nombre de restriccion inesperado: " + restriccion);
            }
            jdbcTemplate.execute("ALTER TABLE `orden_compra` DROP FOREIGN KEY `" + restriccion + "`");
        }
    }

    private boolean existeColumna(String columna) {
        Integer cantidad = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                """, Integer.class, TABLA_ORDEN, columna);
        return cantidad != null && cantidad > 0;
    }
}
