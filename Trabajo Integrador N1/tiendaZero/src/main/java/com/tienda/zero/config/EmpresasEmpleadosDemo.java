package com.tienda.zero.config;

import com.tienda.zero.dto.EmpresaFormDTO;
import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoEmpleado;
import com.tienda.zero.enums.TipoEmpresa;
import com.tienda.zero.model.Empresa;
import com.tienda.zero.model.Localidad;
import com.tienda.zero.repository.EmpleadoRepository;
import com.tienda.zero.service.EmpresaService;
import com.tienda.zero.service.GestionEmpleadoService;
import com.tienda.zero.service.GestionEmpresaService;
import com.tienda.zero.service.LocalidadService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;

/** Carga datos ficticios reutilizables para probar los ABM de empresas y empleados. */
@Component
@Order(4)
public class EmpresasEmpleadosDemo implements CommandLineRunner {

    private record EmpresaDemo(String razonSocial, String cuit, TipoEmpresa tipo,
                               String calle, String numero, String email) {
    }

    private record EmpleadoDemo(String nombre, String apellido, Sexo sexo, String fechaNacimiento,
                                String documento, TipoEmpleado tipo, String cuitEmpresa) {
    }

    private static final List<EmpresaDemo> EMPRESAS = List.of(
            new EmpresaDemo("Zero Deportes S.A.", "30716543210", TipoEmpresa.SEDE_CENTRAL,
                    "Avenida San Martin", "1250", "central@zerodeportes.test"),
            new EmpresaDemo("Zero Norte S.R.L.", "30709876543", TipoEmpresa.SUCURSAL,
                    "Las Heras", "840", "norte@zerodeportes.test"),
            new EmpresaDemo("Zero Cuyo S.R.L.", "30723456789", TipoEmpresa.SUCURSAL,
                    "Colon", "415", "cuyo@zerodeportes.test")
    );

    private static final List<EmpleadoDemo> EMPLEADOS = List.of(
            new EmpleadoDemo("Lucia", "Fernandez", Sexo.FEMENINO, "1992-03-14", "40111222",
                    TipoEmpleado.JEFE, "30716543210"),
            new EmpleadoDemo("Mateo", "Gimenez", Sexo.MASCULINO, "1996-08-22", "41222333",
                    TipoEmpleado.ADMINISTRATIVO, "30716543210"),
            new EmpleadoDemo("Camila", "Rojas", Sexo.FEMENINO, "1994-11-05", "42333444",
                    TipoEmpleado.ADMINISTRATIVO, "30709876543"),
            new EmpleadoDemo("Tomas", "Pereyra", Sexo.MASCULINO, "1989-06-18", "38444555",
                    TipoEmpleado.JEFE, "30709876543"),
            new EmpleadoDemo("Valentina", "Castro", Sexo.FEMENINO, "1998-01-27", "43555666",
                    TipoEmpleado.ADMINISTRATIVO, "30723456789"),
            new EmpleadoDemo("Bruno", "Molina", Sexo.MASCULINO, "1991-09-09", "39666777",
                    TipoEmpleado.ADMINISTRATIVO, "30723456789")
    );

    private final GestionEmpresaService gestionEmpresaService;
    private final GestionEmpleadoService gestionEmpleadoService;
    private final EmpresaService empresaService;
    private final LocalidadService localidadService;
    private final EmpleadoRepository empleadoRepository;

    public EmpresasEmpleadosDemo(GestionEmpresaService gestionEmpresaService,
                                 GestionEmpleadoService gestionEmpleadoService,
                                 EmpresaService empresaService,
                                 LocalidadService localidadService,
                                 EmpleadoRepository empleadoRepository) {
        this.gestionEmpresaService = gestionEmpresaService;
        this.gestionEmpleadoService = gestionEmpleadoService;
        this.empresaService = empresaService;
        this.localidadService = localidadService;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public void run(String... args) {
        Localidad localidad = localidadService.buscarLocalidadPorNombre("Ciudad de Mendoza");
        for (EmpresaDemo empresa : EMPRESAS) {
            crearEmpresaSiNoExiste(empresa, localidad);
        }
        for (EmpleadoDemo empleado : EMPLEADOS) {
            crearEmpleadoSiNoExiste(empleado);
        }
    }

    private void crearEmpresaSiNoExiste(EmpresaDemo demo, Localidad localidad) {
        if (empresaService.listarEmpresaActiva().stream().anyMatch(e -> e.getCuit().equals(demo.cuit()))) {
            return;
        }

        EmpresaFormDTO formulario = new EmpresaFormDTO();
        formulario.setRazonSocial(demo.razonSocial());
        formulario.setCuit(demo.cuit());
        formulario.setTipoEmpresa(demo.tipo());
        formulario.setCalle(demo.calle());
        formulario.setNumeracion(demo.numero());
        formulario.setBarrio("Centro");
        formulario.setReferencia("Datos ficticios para demostracion");
        formulario.setIdProvincia(localidad.getDepartamento().getProvincia().getId());
        formulario.setIdDepartamento(localidad.getDepartamento().getId());
        formulario.setIdLocalidad(localidad.getId());
        formulario.setMedioContacto("CORREO");
        formulario.setTipoContacto(TipoContacto.EMPRESA);
        formulario.setEmail(demo.email());
        gestionEmpresaService.crearEmpresa(formulario);
    }

    private void crearEmpleadoSiNoExiste(EmpleadoDemo demo) {
        if (empleadoRepository.findByTipoDocumentoAndNumeroDocumento(TipoDocumento.DNI, demo.documento()).isPresent()) {
            return;
        }
        Empresa empresa = empresaService.listarEmpresaActiva().stream()
                .filter(item -> item.getCuit().equals(demo.cuitEmpresa()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontro la empresa demo " + demo.cuitEmpresa()));

        gestionEmpleadoService.crearEmpleado(demo.nombre(), demo.apellido(), demo.sexo(),
                Date.valueOf(demo.fechaNacimiento()), TipoDocumento.DNI, demo.documento(), demo.tipo(),
                empresa.getId(), null, null);
    }
}
