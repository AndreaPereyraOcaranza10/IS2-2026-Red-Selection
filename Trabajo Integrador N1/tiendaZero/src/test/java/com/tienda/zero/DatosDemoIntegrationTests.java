package com.tienda.zero;

import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.repository.EmpleadoRepository;
import com.tienda.zero.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DatosDemoIntegrationTests {

    private static final Set<String> CUITS = Set.of("30716543210", "30709876543", "30723456789");
    private static final Set<String> DOCUMENTOS = Set.of(
            "40111222", "41222333", "42333444", "38444555", "43555666", "39666777");

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Test
    void cargaTresEmpresasYSeisEmpleadosFicticios() {
        long empresasCargadas = empresaRepository.findByEliminadoFalse().stream()
                .filter(empresa -> CUITS.contains(empresa.getCuit()))
                .count();

        assertEquals(3, empresasCargadas);
        for (String documento : DOCUMENTOS) {
            var empleado = empleadoRepository.findByTipoDocumentoAndNumeroDocumento(TipoDocumento.DNI, documento);
            assertTrue(empleado.isPresent());
            assertTrue(CUITS.contains(empleado.orElseThrow().getEmpresa().getCuit()));
        }
    }
}
