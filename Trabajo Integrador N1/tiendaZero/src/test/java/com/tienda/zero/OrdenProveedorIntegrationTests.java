package com.tienda.zero;

import com.tienda.zero.model.FormaDePago;
import com.tienda.zero.model.OrdenCompraProveedor;
import com.tienda.zero.model.Producto;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.repository.FacturaProveedorRepository;
import com.tienda.zero.repository.OrdenCompraProveedorRepository;
import com.tienda.zero.service.FormaDePagoService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.ProveedorService;
import com.tienda.zero.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrdenProveedorIntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private OrdenCompraProveedorRepository ordenRepository;
    @Autowired private FacturaProveedorRepository facturaRepository;
    @Autowired private ProveedorService proveedorService;
    @Autowired private ProductoService productoService;
    @Autowired private FormaDePagoService formaDePagoService;
    @Autowired private StockService stockService;

    @Test
    void guardaLaOrdenYActualizaStockSolamenteAlRecibirla() throws Exception {
        Proveedor proveedor = proveedorService.listarProveedorActivo().stream().findFirst().orElseThrow();
        Producto producto = productoService.listarProductoActivo().stream().findFirst().orElseThrow();
        FormaDePago formaDePago = formaDePagoService.listarFormaDePagoActivo().stream().findFirst().orElseThrow();
        int stockInicial = stockService.calcularStockActual(producto.getId());
        Set<String> idsAnteriores = ordenRepository.findAll().stream()
                .map(OrdenCompraProveedor::getId)
                .collect(Collectors.toSet());

        mockMvc.perform(post("/admin/ordenes/nueva")
                        .with(user("administrativo@tiendazero.com").roles("ADMINISTRATIVO"))
                        .with(csrf())
                        .param("proveedor.id", proveedor.getId())
                        .param("productoId", producto.getId())
                        .param("cantidad", "7")
                        .param("precioUnitario", "1250.50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ordenes"));

        OrdenCompraProveedor orden = ordenRepository.findAll().stream()
                .filter(item -> !idsAnteriores.contains(item.getId()))
                .findFirst()
                .orElseThrow();
        assertFalse(orden.isEntregada());
        assertEquals(1, orden.getDetalles().size());
        assertEquals(stockInicial, stockService.calcularStockActual(producto.getId()));

        long numeroFactura = System.nanoTime();
        mockMvc.perform(post("/admin/ordenes/{id}/recibir", orden.getId())
                        .with(user("administrativo@tiendazero.com").roles("ADMINISTRATIVO"))
                        .with(csrf())
                        .param("numeroFactura", Long.toString(numeroFactura))
                        .param("idFormaDePago", formaDePago.getId())
                        .param("estado", "SIN_DEFINIR"))
                .andExpect(status().is3xxRedirection());

        assertTrue(ordenRepository.findById(orden.getId()).orElseThrow().isEntregada());
        assertEquals(stockInicial + 7, stockService.calcularStockActual(producto.getId()));
        assertTrue(facturaRepository.existsByOrdenCompraIdAndEstadoNotAndEliminadoFalse(
                orden.getId(), com.tienda.zero.enums.EstadoFactura.ANULADA));

        mockMvc.perform(post("/admin/ordenes/{id}/recibir", orden.getId())
                        .with(user("administrativo@tiendazero.com").roles("ADMINISTRATIVO"))
                        .with(csrf())
                        .param("numeroFactura", Long.toString(numeroFactura + 1))
                        .param("idFormaDePago", formaDePago.getId())
                        .param("estado", "SIN_DEFINIR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ordenes/" + orden.getId()));

        assertEquals(stockInicial + 7, stockService.calcularStockActual(producto.getId()));
    }
}
