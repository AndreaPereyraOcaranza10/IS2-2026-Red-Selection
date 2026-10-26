package com.tienda.zero.controller;

import com.tienda.zero.enums.TipoContacto;
import com.tienda.zero.enums.TipoTelefono;
import com.tienda.zero.model.Contacto;
import com.tienda.zero.model.ContactoCorreoElectronico;
import com.tienda.zero.model.ContactoTelefonico;
import com.tienda.zero.model.Proveedor;
import com.tienda.zero.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public String lista(Model model) {
        List<Proveedor> proveedores = proveedorService.listarProveedorActivo();
        model.addAttribute("proveedores", proveedores);
        return "admin/proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        // empty contact fields for the form
        model.addAttribute("email", "");
        model.addAttribute("telefono", "");
        model.addAttribute("tipoContactoSelected", "EMPRESA");
        model.addAttribute("tipoTelefonoSelected", "CELULAR");
        return "admin/proveedores/formulario";
    }

    @PostMapping("/nuevo")
    public String crear(@ModelAttribute Proveedor proveedor,
                        @RequestParam(required = false) String email,
                        @RequestParam(required = false) String telefono,
                        @RequestParam(required = false, defaultValue = "EMPRESA") String tipoContactoSelected,
                        @RequestParam(required = false, defaultValue = "CELULAR") String tipoTelefonoSelected) {

        List<Contacto> contactos = new ArrayList<>();

        if (email != null && !email.isBlank()) {
            ContactoCorreoElectronico ce = ContactoCorreoElectronico.builder()
                    .email(email.trim())
                    .tipoContacto(TipoContacto.valueOf(tipoContactoSelected))
                    .build();
            contactos.add(ce);
        }

        if (telefono != null && !telefono.isBlank()) {
            ContactoTelefonico ct = ContactoTelefonico.builder()
                    .telefono(telefono.trim())
                    .tipoTelefono(TipoTelefono.valueOf(tipoTelefonoSelected))
                    // default contact tipo for telephones
                    .tipoContacto(TipoContacto.EMPRESA)
                    .build();
            contactos.add(ct);
        }

        proveedorService.crearProveedor(proveedor.getRazonSocial(), contactos);
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        Proveedor p = proveedorService.buscarProveedor(id);
        model.addAttribute("proveedor", p);

        // prefill contact fields if they exist (pick first occurrences)
        String email = "";
        String telefono = "";
        String tipoContactoSelected = "EMPRESA";
        String tipoTelefonoSelected = "CELULAR";

        for (Contacto c : p.getContactos()) {
            if (c instanceof ContactoCorreoElectronico) {
                ContactoCorreoElectronico ce = (ContactoCorreoElectronico) c;
                email = ce.getEmail();
                if (ce.getTipoContacto() != null) tipoContactoSelected = ce.getTipoContacto().name();
            } else if (c instanceof ContactoTelefonico) {
                ContactoTelefonico ct = (ContactoTelefonico) c;
                telefono = ct.getTelefono();
                if (ct.getTipoTelefono() != null) tipoTelefonoSelected = ct.getTipoTelefono().name();
            }
        }

        model.addAttribute("email", email);
        model.addAttribute("telefono", telefono);
        model.addAttribute("tipoContactoSelected", tipoContactoSelected);
        model.addAttribute("tipoTelefonoSelected", tipoTelefonoSelected);

        return "admin/proveedores/formulario";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable String id,
                         @ModelAttribute Proveedor proveedor,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String telefono,
                         @RequestParam(required = false, defaultValue = "EMPRESA") String tipoContactoSelected,
                         @RequestParam(required = false, defaultValue = "CELULAR") String tipoTelefonoSelected) {

        List<Contacto> contactos = new ArrayList<>();

        if (email != null && !email.isBlank()) {
            ContactoCorreoElectronico ce = ContactoCorreoElectronico.builder()
                    .email(email.trim())
                    .tipoContacto(TipoContacto.valueOf(tipoContactoSelected))
                    .build();
            contactos.add(ce);
        }

        if (telefono != null && !telefono.isBlank()) {
            ContactoTelefonico ct = ContactoTelefonico.builder()
                    .telefono(telefono.trim())
                    .tipoTelefono(TipoTelefono.valueOf(tipoTelefonoSelected))
                    .tipoContacto(TipoContacto.EMPRESA)
                    .build();
            contactos.add(ct);
        }

        proveedorService.modificarProveedor(id, proveedor.getRazonSocial(), contactos);
        return "redirect:/admin/proveedores";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id) {
        proveedorService.eliminarProveedor(id);
        return "redirect:/admin/proveedores";
    }
}
