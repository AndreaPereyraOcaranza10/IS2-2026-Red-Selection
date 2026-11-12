package redSelection.dulceMayor.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import redSelection.dulceMayor.dto.SucursalImagenSolicitudDTO;
import redSelection.dulceMayor.dto.SucursalRespuestaDTO;
import redSelection.dulceMayor.dto.SucursalSolicitudDTO;
import redSelection.dulceMayor.service.DulceMayorService;

import java.util.List;

/** Sucursales: público (sitio institucional) y ABM de administración. Solo delega en DulceMayorService. */
@RestController
public class PortalBranchController {

    private final DulceMayorService dulceMayorService;

    public PortalBranchController(DulceMayorService dulceMayorService) {
        this.dulceMayorService = dulceMayorService;
    }

    @GetMapping("/api/companies/branches")
    public List<SucursalRespuestaDTO> publicBranches() {
        return dulceMayorService.listarSucursales();
    }

    @GetMapping("/api/admin/companies/branches")
    public List<SucursalRespuestaDTO> branches() {
        return dulceMayorService.listarSucursales();
    }

    @PostMapping("/api/admin/companies/branches")
    @ResponseStatus(HttpStatus.CREATED)
    public SucursalRespuestaDTO create(@Valid @RequestBody SucursalSolicitudDTO request) {
        return dulceMayorService.crearSucursal(request);
    }

    @PutMapping("/api/admin/companies/branches/{id}")
    public SucursalRespuestaDTO update(@PathVariable String id, @Valid @RequestBody SucursalSolicitudDTO request) {
        return dulceMayorService.actualizarSucursal(id, request);
    }

    @PostMapping("/api/admin/companies/branches/{id}/images")
    public SucursalRespuestaDTO addImage(@PathVariable String id, @Valid @RequestBody SucursalImagenSolicitudDTO request) {
        return dulceMayorService.agregarFoto(id, request);
    }

    @DeleteMapping("/api/admin/companies/branches/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        dulceMayorService.eliminarSucursal(id);
    }
}