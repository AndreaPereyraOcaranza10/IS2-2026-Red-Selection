package com.concurso.abm.service;

import com.concurso.abm.dto.request.*;
import com.concurso.abm.dto.response.*;
import com.concurso.abm.entity.*;
import com.concurso.abm.exception.DomainException;
import com.concurso.abm.mapper.RegistroMapper;
import com.concurso.abm.patterns.strategy.LocalIdentityStrategy;
import com.concurso.abm.repository.*;
import com.concurso.abm.security.JwtService;
import com.concurso.abm.validation.*;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
  private final UsuarioRepository users;
  private final PersonaRepository personas;
  private final DireccionRepository direcciones;
  private final LocalidadRepository localidades;
  private final PasswordEncoder encoder;
  private final LocalIdentityStrategy local;
  private final JwtService jwt;

  public UsuarioService(
      UsuarioRepository users,
      PersonaRepository personas,
      DireccionRepository direcciones,
      LocalidadRepository localidades,
      PasswordEncoder encoder,
      LocalIdentityStrategy local,
      JwtService jwt) {
    this.users = users;
    this.personas = personas;
    this.direcciones = direcciones;
    this.localidades = localidades;
    this.encoder = encoder;
    this.local = local;
    this.jwt = jwt;
  }

  @Transactional
  public Map<String, Object> register(AuthRequest r) {
    PasswordPolicy.validate(r.password());
    String email = r.email().strip().toLowerCase(Locale.ROOT);
    if (users.findByEmail(email).isPresent())
      throw DomainException.conflict("El email ya está registrado");
    Usuario u = new Usuario();
    u.setEmail(email);
    u.setPasswordHash(encoder.encode(r.password()));
    users.saveAndFlush(u);
    return Map.of("token", jwt.issue(u), "usuario", response(u));
  }

  @Transactional(readOnly = true)
  public Map<String, Object> login(AuthRequest r) {
    Usuario u = local.authenticate(r.email(), r.password());
    return Map.of("token", jwt.issue(u), "usuario", response(u));
  }

  private Usuario current(String id) {
    return users
        .findById(Long.valueOf(id))
        .filter(u -> u.getActivo())
        .orElseThrow(DomainException::missing);
  }

  private UsuarioResponse response(Usuario u) {
    return new UsuarioResponse(
        u.getId(),
        u.getEmail(),
        u.getRol(),
        u.getProveedorAutenticacion(),
        u.getPasswordHash() != null,
        u.getPersona() == null ? null : RegistroMapper.map(u.getPersona()));
  }

  @Transactional(readOnly = true)
  public UsuarioResponse me(String id) {
    return response(current(id));
  }

  @Transactional
  public UsuarioResponse update(String id, PerfilRequest r) {
    Usuario u = current(id);
    String email = r.email().strip().toLowerCase(Locale.ROOT);
    if (!u.getProveedorAutenticacion().equals("LOCAL") && !email.equals(u.getEmail()))
      throw DomainException.invalid("El email de Google se administra con Google");
    if (users.findByEmail(email).filter(x -> !x.getId().equals(u.getId())).isPresent())
      throw DomainException.conflict("El email ya está registrado");
    var locality =
        localidades
            .lockActiveById(r.direccion().localidadId())
            .orElseThrow(DomainException::missing);
    // Copy-on-write prevents editing addresses or people shared with another account.
    if (locality.getDepartamento().getEliminado()
        || locality.getDepartamento().getProvincia().getEliminado()
        || locality.getDepartamento().getProvincia().getPais().getEliminado()) {
      throw DomainException.invalid("La ubicación seleccionada ya no está disponible");
    }
    Persona person = u.getPersona();
    if (person == null
        || person.getEliminado()
        || users.findAll().stream()
            .anyMatch(
                x ->
                    x.getActivo()
                        && !x.getId().equals(u.getId())
                        && x.getPersona() != null
                        && x.getPersona().getId().equals(u.getPersona().getId())))
      person = new Persona();
    Direccion old = person.getDireccion();
    Direccion address = old;
    if (old == null
        || old.getEliminado()
        || personas.findByEliminadoFalseOrderByIdAsc().stream()
            .anyMatch(
                x ->
                    x.getDireccion().getId().equals(old.getId())
                        && (u.getPersona() == null || !x.getId().equals(u.getPersona().getId()))))
      address = new Direccion();
    address.setLocalidad(locality);
    address.setCalle(r.direccion().calle().strip());
    address.setNumeracion(r.direccion().numeracion().strip());
    address.setBarrio(DomainRules.text(r.direccion().barrio()));
    address.setManzanaPiso(DomainRules.text(r.direccion().manzanaPiso()));
    address.setCasaDepartamento(DomainRules.text(r.direccion().casaDepartamento()));
    address.setReferencia(DomainRules.text(r.direccion().referencia()));
    person.setDireccion(direcciones.saveAndFlush(address));
    person.setNombre(r.nombre().strip());
    person.setApellido(r.apellido().strip());
    person.setDni(r.dni().strip());
    person.setFechaNacimiento(r.fechaNacimiento());
    u.setPersona(personas.saveAndFlush(person));
    u.setEmail(email);
    users.saveAndFlush(u);
    return response(u);
  }

  @Transactional
  public void password(String id, PasswordRequest r) {
    Usuario u = current(id);
    if (u.getPasswordHash() == null)
      throw DomainException.invalid("Tu cuenta utiliza autenticación externa con Google");
    if (!encoder.matches(r.actual(), u.getPasswordHash()))
      throw DomainException.invalid("La contraseña actual es incorrecta");
    PasswordPolicy.validate(r.nueva());
    if (!r.nueva().equals(r.confirmacion()))
      throw DomainException.invalid("La confirmación no coincide");
    if (encoder.matches(r.nueva(), u.getPasswordHash()))
      throw DomainException.invalid("La nueva contraseña debe ser diferente");
    u.setPasswordHash(encoder.encode(r.nueva()));
    u.setCredentialVersion(u.getCredentialVersion() + 1);
    users.saveAndFlush(u);
  }
}
