package com.concurso.abm.dto.response;

public record UsuarioResponse(
    Long id,
    String email,
    String rol,
    String proveedorAutenticacion,
    boolean tienePassword,
    RegistroResponse persona) {}
