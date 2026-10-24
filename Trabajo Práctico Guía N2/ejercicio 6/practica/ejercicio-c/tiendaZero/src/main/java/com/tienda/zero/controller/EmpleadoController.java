package com.tienda.zero.controller;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoEmpleado;
import com.tienda.zero.model.Empleado;
import com.tienda.zero.model.Empresa;
import com.tienda.zero.service.EmpleadoService;
import com.tienda.zero.service.EmpresaService;
import com.tienda.zero.service.GestionEmpleadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/empleados")
public class EmpleadoController {

    private static final String VISTA_FORMULARIO = "admin/empleados/formulario";
    private static final String REDIRECCION_LISTA = "redirect:/admin/empleados";
    private static final DateTimeFormatter FORMATO_VIEJO = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final EmpleadoService empleadoService;
    private final GestionEmpleadoService gestionEmpleadoService;
    private final EmpresaService empresaService;

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("empleados", empleadoService.listarEmpleadoActivo());
        return "admin/empleados/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        prepararFormulario(model, new Empleado(), "", "", false);
        return VISTA_FORMULARIO;
    }

    /**
     * Los parámetros son opcionales a propósito: así una selección vacía llega al servicio, que responde
     * con un mensaje claro, en lugar de cortar antes con un error genérico.
     */
    @PostMapping("/nuevo")
    public String crear(@RequestParam(required = false) String nombre,
                        @RequestParam(required = false) String apellido,
                        @RequestParam(required = false) Sexo sexo,
                        @RequestParam(required = false) String fechaNacimiento,
                        @RequestParam(required = false) TipoDocumento tipoDocumento,
                        @RequestParam(required = false) String numeroDocumento,
                        @RequestParam(required = false) TipoEmpleado tipoEmpleado,
                        @RequestParam(required = false) String idEmpresa,
                        @RequestParam(required = false) String nombreUsuario,
                        @RequestParam(required = false) String clave,
                        Model model, RedirectAttributes redirectAttributes) {
        try {
            Date fecha = parsearFecha(fechaNacimiento);
            gestionEmpleadoService.crearEmpleado(nombre, apellido, sexo, fecha, tipoDocumento, numeroDocumento,
                    tipoEmpleado, idEmpresa, nombreUsuario, clave);
            redirectAttributes.addFlashAttribute("mensajeExito", "El empleado fue creado correctamente.");
            return REDIRECCION_LISTA;
        } catch (IllegalArgumentException e) {
            // Vuelve al formulario con lo que la persona había escrito y el motivo del error
            model.addAttribute("error", e.getMessage());
            Empleado enFormulario = armarEmpleado(null, nombre, apellido, sexo, tipoDocumento, numeroDocumento,
                    tipoEmpleado, idEmpresa);
            prepararFormulario(model, enFormulario, fechaNacimiento, nombreUsuario, false);
            return VISTA_FORMULARIO;
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Empleado e = empleadoService.buscarEmpleado(id);
            if (e.isEliminado()) {
                redirectAttributes.addFlashAttribute("mensajeError", "No se puede editar un empleado eliminado");
                return REDIRECCION_LISTA;
            }
            String fecha = e.getFechaNacimiento() != null ? e.getFechaNacimiento().toLocalDate().toString() : "";
            String usuario = e.getUsuario() != null ? e.getUsuario().getNombreUsuario() : "";
            prepararFormulario(model, e, fecha, usuario, e.getUsuario() != null);
            return VISTA_FORMULARIO;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return REDIRECCION_LISTA;
        }
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable String id,
                         @RequestParam(required = false) String nombre,
                         @RequestParam(required = false) String apellido,
                         @RequestParam(required = false) Sexo sexo,
                         @RequestParam(required = false) String fechaNacimiento,
                         @RequestParam(required = false) TipoDocumento tipoDocumento,
                         @RequestParam(required = false) String numeroDocumento,
                         @RequestParam(required = false) TipoEmpleado tipoEmpleado,
                         @RequestParam(required = false) String idEmpresa,
                         @RequestParam(required = false) String nombreUsuario,
                         @RequestParam(required = false) String clave,
                         Model model, RedirectAttributes redirectAttributes) {
        try {
            Date fecha = parsearFecha(fechaNacimiento);
            gestionEmpleadoService.modificarEmpleado(id, nombre, apellido, sexo, fecha, tipoDocumento,
                    numeroDocumento, tipoEmpleado, idEmpresa, nombreUsuario, clave);
            redirectAttributes.addFlashAttribute("mensajeExito", "El empleado fue modificado correctamente.");
            return REDIRECCION_LISTA;
        } catch (IllegalArgumentException e) {
            try {
                boolean tieneCuenta = empleadoService.buscarEmpleado(id).getUsuario() != null;
                model.addAttribute("error", e.getMessage());
                Empleado enFormulario = armarEmpleado(id, nombre, apellido, sexo, tipoDocumento, numeroDocumento,
                        tipoEmpleado, idEmpresa);
                prepararFormulario(model, enFormulario, fechaNacimiento, nombreUsuario, tieneCuenta);
                return VISTA_FORMULARIO;
            } catch (IllegalArgumentException noExiste) {
                redirectAttributes.addFlashAttribute("mensajeError", noExiste.getMessage());
                return REDIRECCION_LISTA;
            }
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            gestionEmpleadoService.eliminarEmpleado(id, principal != null ? principal.getName() : null);
            redirectAttributes.addFlashAttribute("mensajeExito", "El empleado fue eliminado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return REDIRECCION_LISTA;
    }

    private void prepararFormulario(Model model, Empleado empleado, String fechaNacimiento,
                                    String nombreUsuario, boolean tieneCuenta) {
        model.addAttribute("empleado", empleado);
        model.addAttribute("fechaNacimientoStr", fechaNacimiento != null ? fechaNacimiento : "");
        model.addAttribute("nombreUsuarioForm", nombreUsuario != null ? nombreUsuario : "");
        model.addAttribute("tieneCuenta", tieneCuenta);
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        model.addAttribute("tiposEmpleado", TipoEmpleado.values());
        model.addAttribute("empresas", empresaService.listarEmpresaActiva());
    }

    /** Un empleado sin guardar con lo que se escribió en el formulario, para volver a mostrarlo. */
    private Empleado armarEmpleado(String id, String nombre, String apellido, Sexo sexo, TipoDocumento tipoDocumento,
                                   String numeroDocumento, TipoEmpleado tipoEmpleado, String idEmpresa) {
        Empresa empresa = null;
        if (idEmpresa != null && !idEmpresa.isBlank()) {
            empresa = Empresa.builder().id(idEmpresa).build();
        }
        return Empleado.builder()
                .id(id)
                .nombre(nombre)
                .apellido(apellido)
                .sexo(sexo)
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .tipoEmpleado(tipoEmpleado)
                .empresa(empresa)
                .build();
    }

    /** Acepta la fecha del selector del navegador (yyyy-MM-dd) y también el formato anterior (dd-MM-yyyy). */
    private Date parsearFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        try {
            return Date.valueOf(LocalDate.parse(texto.trim()));
        } catch (DateTimeParseException e) {
            try {
                return Date.valueOf(LocalDate.parse(texto.trim(), FORMATO_VIEJO));
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("La fecha de nacimiento no es válida");
            }
        }
    }
}