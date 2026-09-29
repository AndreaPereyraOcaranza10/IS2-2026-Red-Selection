package com.tienda.zero.controller;

import com.tienda.zero.service.ClienteService;
import com.tienda.zero.service.ProductoService;
import com.tienda.zero.service.ProveedorService;
import com.tienda.zero.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Página de inicio de la administración. El acceso a /admin/** ya está
 * restringido a ADMINISTRATIVO y JEFE en SecurityConfig.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductoService productoService;
    private final ClienteService clienteService;
    private final ProveedorService proveedorService;
    private final UsuarioService usuarioService;

    public AdminController(ProductoService productoService, ClienteService clienteService,
                           ProveedorService proveedorService, UsuarioService usuarioService) {
        this.productoService = productoService;
        this.clienteService = clienteService;
        this.proveedorService = proveedorService;
        this.usuarioService = usuarioService;
    }

    @GetMapping({"", "/"})
    public String inicio(Model model) {
        model.addAttribute("cantidadProductos", productoService.listarProductoActivo().size());
        model.addAttribute("cantidadClientes", clienteService.listarClienteActivo().size());
        model.addAttribute("cantidadProveedores", proveedorService.listarProveedorActivo().size());
        model.addAttribute("cantidadUsuarios", usuarioService.listarUsuarioActivo().size());
        return "admin/inicio";
    }
}