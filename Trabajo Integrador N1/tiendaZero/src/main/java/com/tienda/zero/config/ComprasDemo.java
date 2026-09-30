package com.tienda.zero.config;

import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.service.FormaDePagoService;
import com.tienda.zero.service.ProveedorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/*
 * Carga una forma de pago de cada tipo y un proveedor de ejemplo, para poder probar el circuito de
 * compras (orden de compra -> recepción -> factura -> stock) sin cargar esos datos a mano.
 * Es idempotente: solo carga cada grupo si todavía no hay ninguno activo.
 * Cuando el equipo tenga las pantallas de proveedores y formas de pago, se puede borrar este archivo.
 */
@Component
@Order(3)
public class ComprasDemo implements CommandLineRunner {

    private final FormaDePagoService formaDePagoService;
    private final ProveedorService proveedorService;

    public ComprasDemo(FormaDePagoService formaDePagoService, ProveedorService proveedorService) {
        this.formaDePagoService = formaDePagoService;
        this.proveedorService = proveedorService;
    }

    @Override
    public void run(String... args) {
        if (formaDePagoService.listarFormaDePagoActivo().isEmpty()) {
            formaDePagoService.crearFormaDePago(TipoPago.EFECTIVO, "Pago en efectivo");
            formaDePagoService.crearFormaDePago(TipoPago.TRANSFERENCIA, "Transferencia bancaria");
            formaDePagoService.crearFormaDePago(TipoPago.BILLETERA_VIRTUAL, "Mercado Pago");
        }
        if (proveedorService.listarProveedorActivo().isEmpty()) {
            proveedorService.crearProveedor("Textil Andes S.R.L.", new ArrayList<>());
        }
    }
}