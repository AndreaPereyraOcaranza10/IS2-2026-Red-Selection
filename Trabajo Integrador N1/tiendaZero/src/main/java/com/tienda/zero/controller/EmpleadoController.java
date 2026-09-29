package com.tienda.zero.controller;

import com.tienda.zero.enums.Sexo;
import com.tienda.zero.enums.TipoDocumento;
import com.tienda.zero.enums.TipoEmpleado;
import com.tienda.zero.enums.TipoUsuario;
import com.tienda.zero.model.Empleado;
import com.tienda.zero.model.Usuario;
import com.tienda.zero.service.EmpleadoService;
import com.tienda.zero.service.EmpresaService;
import com.tienda.zero.service.PersonaService;
import com.tienda.zero.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final EmpresaService empresaService;
    private final UsuarioService usuarioService;
    private final PersonaService personaService;

    private final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("empleados", empleadoService.listarEmpleadoActivo());
        return "admin/empleados/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("empleado", new Empleado());
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        model.addAttribute("tiposEmpleado", TipoEmpleado.values());
        model.addAttribute("empresas", empresaService.listarEmpresaActiva());
        return "admin/empleados/formulario";
    }

    @PostMapping("/nuevo")
    public String crear(@RequestParam String nombre,
                        @RequestParam String apellido,
                        @RequestParam Sexo sexo,
                        @RequestParam String fechaNacimiento,
                        @RequestParam TipoDocumento tipoDocumento,
                        @RequestParam String numeroDocumento,
                        @RequestParam TipoEmpleado tipoEmpleado,
                        @RequestParam String idEmpresa,
                        @RequestParam(required = false) String nombreUsuario,
                        @RequestParam(required = false) String clave) {
        LocalDate ld = LocalDate.parse(fechaNacimiento, FORMATO);
        Date sqlDate = Date.valueOf(ld);
        Empleado empleado = empleadoService.crearEmpleado(nombre, apellido, sexo, sqlDate, tipoDocumento, numeroDocumento, tipoEmpleado, idEmpresa);

        if (nombreUsuario != null && !nombreUsuario.isBlank() && clave != null && !clave.isBlank()) {
            TipoUsuario rol = TipoUsuario.valueOf(tipoEmpleado.name());
            Usuario usuario = usuarioService.crearUsuario(nombreUsuario, clave, rol);
            personaService.asociarUsuarioPersona(empleado.getId(), usuario.getId());
        }

        return "redirect:/admin/empleados";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        Empleado e = empleadoService.buscarEmpleado(id);
        model.addAttribute("empleado", e);
        model.addAttribute("sexos", Sexo.values());
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        model.addAttribute("tiposEmpleado", TipoEmpleado.values());
        model.addAttribute("empresas", empresaService.listarEmpresaActiva());
        // string date for form
        if (e.getFechaNacimiento() != null) {
            model.addAttribute("fechaNacimientoStr", e.getFechaNacimiento().toLocalDate().format(FORMATO));
        } else {
            model.addAttribute("fechaNacimientoStr", "");
        }
        return "admin/empleados/formulario";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable String id,
                         @RequestParam String nombre,
                         @RequestParam String apellido,
                         @RequestParam Sexo sexo,
                         @RequestParam String fechaNacimiento,
                         @RequestParam TipoDocumento tipoDocumento,
                         @RequestParam String numeroDocumento,
                         @RequestParam TipoEmpleado tipoEmpleado,
                         @RequestParam String idEmpresa,
                         @RequestParam(required = false) String nombreUsuario,
                         @RequestParam(required = false) String clave) {
        LocalDate ld = LocalDate.parse(fechaNacimiento, FORMATO);
        Date sqlDate = Date.valueOf(ld);
        empleadoService.modificarEmpleado(id, nombre, apellido, sexo, sqlDate, tipoDocumento, numeroDocumento, tipoEmpleado, idEmpresa);

        // optionally create and associate usuario if provided and persona has none
        if (nombreUsuario != null && !nombreUsuario.isBlank() && clave != null && !clave.isBlank()) {
            Empleado empleado = empleadoService.buscarEmpleado(id);
            if (empleado.getUsuario() == null) {
                TipoUsuario rol = TipoUsuario.valueOf(tipoEmpleado.name());
                Usuario usuario = usuarioService.crearUsuario(nombreUsuario, clave, rol);
                personaService.asociarUsuarioPersona(empleado.getId(), usuario.getId());
            }
        }

        return "redirect:/admin/empleados";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id) {
        empleadoService.eliminarEmpleado(id);
        return "redirect:/admin/empleados";
    }
}
