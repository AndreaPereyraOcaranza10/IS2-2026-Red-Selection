package com.example.mascotas.controladores;

import com.example.mascotas.dto.MascotaDTO;
import com.example.mascotas.errores.ErrorServicio;
import com.example.mascotas.servicios.MascotaServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaApiControlador {
    private final MascotaServicio mascotaServicio;

    public MascotaApiControlador(MascotaServicio mascotaServicio) {
        this.mascotaServicio = mascotaServicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MascotaDTO> obtener(@PathVariable String id) throws ErrorServicio {
        return ResponseEntity.ok(mascotaServicio.buscarMascota(id));
    }

    @GetMapping
    public List<MascotaDTO> porUsuario(@RequestParam String usuarioId) {
        return mascotaServicio.buscarMascotasPorUsuario(usuarioId);
    }

    @PostMapping
    public ResponseEntity<MascotaDTO> crear(@RequestBody MascotaDTO dto) throws ErrorServicio {
        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaServicio.crearDesdeDto(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MascotaDTO> actualizar(@PathVariable String id, @RequestBody MascotaDTO dto) throws ErrorServicio {
        dto.setId(id);
        return ResponseEntity.ok(mascotaServicio.actualizarDesdeDto(dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id, @RequestParam String usuarioId) throws ErrorServicio {
        mascotaServicio.eliminar(usuarioId, id);
    }
}
