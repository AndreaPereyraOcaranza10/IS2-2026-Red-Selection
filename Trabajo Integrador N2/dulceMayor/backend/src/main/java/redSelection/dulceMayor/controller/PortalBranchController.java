package redSelection.dulceMayor.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import redSelection.dulceMayor.dto.UbicacionResueltaDTO;
import redSelection.dulceMayor.enums.TipoContacto;
import redSelection.dulceMayor.enums.TipoEmpresa;
import redSelection.dulceMayor.model.ContactoCorreoElectronico;
import redSelection.dulceMayor.model.Direccion;
import redSelection.dulceMayor.model.DulceMayor;
import redSelection.dulceMayor.repository.DulceMayorRepository;
import redSelection.dulceMayor.service.UbicacionService;

import java.util.List;

@RestController
public class PortalBranchController {
    private final DulceMayorRepository sucursales;
    private final UbicacionService ubicaciones;

    public PortalBranchController(DulceMayorRepository sucursales, UbicacionService ubicaciones) {
        this.sucursales = sucursales;
        this.ubicaciones = ubicaciones;
    }

    @GetMapping("/api/companies/branches")
    @Transactional
    public List<BranchResponse> publicBranches() {
        return sucursales.findByEliminadoFalse().stream()
                .filter(branch -> branch.getTipoEmpresa() == TipoEmpresa.SUCURSAL)
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(this::toResponse).toList();
    }

    @GetMapping("/api/admin/companies/branches")
    @Transactional
    public List<BranchResponse> branches() {
        return publicBranches();
    }

    @PostMapping("/api/admin/companies/branches")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public BranchResponse create(@Valid @RequestBody BranchRequest request) {
        UbicacionResueltaDTO location = ubicaciones.resolver(
                request.provinciaId(), request.departamentoId(), request.localidadId());
        ContactoCorreoElectronico contact = ContactoCorreoElectronico.builder()
                .tipoContacto(TipoContacto.EMPRESA).observacion("Contacto de sucursal")
                .principal(true).email(request.email().trim()).build();
        DulceMayor branch = DulceMayor.builder()
                .nombre(request.razonSocial().trim()).razonSocial(request.razonSocial().trim())
                .cuit(request.cuit().trim()).tipoEmpresa(TipoEmpresa.SUCURSAL)
                .direccion(address(request, location)).contactos(List.of(contact)).build();
        return toResponse(sucursales.save(branch));
    }

    @PutMapping("/api/admin/companies/branches/{id}")
    @Transactional
    public BranchResponse update(@PathVariable String id, @Valid @RequestBody BranchRequest request) {
        DulceMayor branch = findBranch(id);
        UbicacionResueltaDTO location = ubicaciones.resolver(
                request.provinciaId(), request.departamentoId(), request.localidadId());
        Direccion address = branch.getDireccion();
        address.setCalle(request.calle().trim());
        address.setNumeracion(request.numeracion().trim());
        address.setBarrio(clean(request.barrio()));
        address.setReferencia(clean(request.referencia()));
        address.setManzanaPiso(clean(request.manzanaPiso()));
        address.setCasaDepartamento(clean(request.casaDepartamento()));
        address.setProvinciaId(location.provincia().id());
        address.setProvinciaNombre(location.provincia().nombre());
        address.setDepartamentoId(location.departamento().id());
        address.setDepartamentoNombre(location.departamento().nombre());
        address.setLocalidadId(location.localidad().id());
        address.setLocalidadNombre(location.localidad().nombre());
        branch.setNombre(request.razonSocial().trim());
        branch.setRazonSocial(request.razonSocial().trim());
        branch.setCuit(request.cuit().trim());
        branch.getContactos().stream().filter(c -> c instanceof ContactoCorreoElectronico)
                .forEach(c -> c.setEliminado(true));
        branch.getContactos().add(ContactoCorreoElectronico.builder()
                .tipoContacto(TipoContacto.EMPRESA).observacion("Contacto de sucursal")
                .principal(true).email(request.email().trim()).build());
        return toResponse(sucursales.save(branch));
    }

    @DeleteMapping("/api/admin/companies/branches/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable String id) {
        findBranch(id).setEliminado(true);
    }

    private DulceMayor findBranch(String id) {
        DulceMayor branch = sucursales.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal inexistente."));
        if (branch.getTipoEmpresa() != TipoEmpresa.SUCURSAL) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal inexistente.");
        }
        return branch;
    }

    private Direccion address(BranchRequest request, UbicacionResueltaDTO location) {
        return Direccion.builder().calle(request.calle().trim()).numeracion(request.numeracion().trim())
                .barrio(clean(request.barrio())).referencia(clean(request.referencia()))
                .manzanaPiso(clean(request.manzanaPiso())).casaDepartamento(clean(request.casaDepartamento()))
                .provinciaId(location.provincia().id()).provinciaNombre(location.provincia().nombre())
                .departamentoId(location.departamento().id()).departamentoNombre(location.departamento().nombre())
                .localidadId(location.localidad().id()).localidadNombre(location.localidad().nombre())
                .build();
    }

    private DulceMayor findBranchForRequest(String id) { return findBranch(id); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private BranchResponse toResponse(DulceMayor branch) {
        Direccion d = branch.getDireccion();
        String email = branch.getContactos().stream()
                .filter(c -> !c.isEliminado() && c instanceof ContactoCorreoElectronico)
                .map(c -> ((ContactoCorreoElectronico) c).getEmail()).findFirst().orElse("");
        return new BranchResponse(branch.getId(), branch.getRazonSocial(), branch.getCuit(), d.getCalle(),
                d.getNumeracion(), d.getBarrio(), d.getManzanaPiso(), d.getCasaDepartamento(), d.getReferencia(),
                d.getLocalidadId(), d.getLocalidadNombre(), d.getDepartamentoId(), d.getDepartamentoNombre(), d.getProvinciaId(), d.getProvinciaNombre(), email);
    }

    public record BranchRequest(@NotBlank String razonSocial, @NotBlank String cuit,
                                @NotBlank String calle, @NotBlank String numeracion,
                                String barrio, String manzanaPiso, String casaDepartamento, String referencia,
                                @NotBlank String provinciaId, @NotBlank String departamentoId,
                                @NotBlank String localidadId, @Email @NotBlank String email) {}
    public record BranchResponse(String id, String razonSocial, String cuit, String calle, String numeracion,
                                 String barrio, String manzanaPiso, String casaDepartamento, String referencia,
                                 String localidadId, String localidad, String departamentoId, String departamento, String provinciaId, String provincia, String email) {}
}