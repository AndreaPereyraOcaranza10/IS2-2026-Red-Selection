package com.tienda.zero.config;

import com.tienda.zero.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class DatosGeograficosIniciales implements CommandLineRunner {

    private final PaisService paisService;
    private final ProvinciaService provinciaService;
    private final DepartamentoService departamentoService;
    private final LocalidadService localidadService;
    private final NacionalidadService nacionalidadService;

    public DatosGeograficosIniciales(PaisService paisService, ProvinciaService provinciaService,
                                     DepartamentoService departamentoService, LocalidadService localidadService,
                                     NacionalidadService nacionalidadService) {
        this.paisService = paisService;
        this.provinciaService = provinciaService;
        this.departamentoService = departamentoService;
        this.localidadService = localidadService;
        this.nacionalidadService = nacionalidadService;
    }

    @Override
    public void run(String... args) {
        if (paisService.listarPaisActivo().isEmpty()) {
            paisService.crearPais("Argentina");
        }
        var argentina = paisService.buscarPaisPorNombre("Argentina");

        if (provinciaService.listarProvinciaActivo(argentina.getId()).isEmpty()) {
            provinciaService.crearProvincia("Mendoza", argentina.getId());
        }
        var mendoza = provinciaService.buscarProvinciaPorNombre("Mendoza");

        if (departamentoService.listarDepartamentoActivo(mendoza.getId()).isEmpty()) {
            departamentoService.crearDepartamento("Capital", mendoza.getId());
        }
        var capital = departamentoService.buscarDepartamentoPorNombre("Capital");

        if (localidadService.listarLocalidadActivo(capital.getId()).isEmpty()) {
            localidadService.crearLocalidad("Ciudad de Mendoza", "5500", capital.getId());
        }

        if (nacionalidadService.listarNacionalidadActiva().isEmpty()) {
            nacionalidadService.crearNacionalidad("Argentina");
        }
    }
}
