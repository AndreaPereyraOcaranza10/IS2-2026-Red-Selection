package com.tienda.zero.service.impl;

import com.tienda.zero.enums.TipoCorreo;
import com.tienda.zero.model.ConfiguracionCorreoEmpresa;
import com.tienda.zero.model.Empresa;
import com.tienda.zero.repository.ConfiguracionCorreoEmpresaRepository;
import com.tienda.zero.service.ConfiguracionCorreoEmpresaService;
import com.tienda.zero.service.EmpresaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfiguracionCorreoEmpresaServiceImpl implements ConfiguracionCorreoEmpresaService {

    private final ConfiguracionCorreoEmpresaRepository configuracionCorreoEmpresaRepository;
    private final EmpresaService empresaService;

    public ConfiguracionCorreoEmpresaServiceImpl(ConfiguracionCorreoEmpresaRepository configuracionCorreoEmpresaRepository,
                                                 EmpresaService empresaService) {
        this.configuracionCorreoEmpresaRepository = configuracionCorreoEmpresaRepository;
        this.empresaService = empresaService;
    }

    @Override
    @Transactional
    public ConfiguracionCorreoEmpresa crearConfiguracionCorreoEmpresa(String idEmpresa, TipoCorreo tipoCorreo,
                                                                      String asunto, String cuerpoHtml) {
        validar(tipoCorreo, asunto, cuerpoHtml, idEmpresa);

        Empresa empresa = empresaService.buscarEmpresa(idEmpresa);

        if (configuracionCorreoEmpresaRepository
                .findByEmpresaIdAndTipoCorreoAndEliminadoFalse(idEmpresa, tipoCorreo).isPresent()) {
            throw new IllegalArgumentException("Ya existe una configuración de correo de ese tipo para la empresa");
        }

        ConfiguracionCorreoEmpresa configuracion = ConfiguracionCorreoEmpresa.builder()
                .tipoCorreo(tipoCorreo)
                .asunto(asunto)
                .cuerpoHtml(cuerpoHtml)
                .empresa(empresa)
                .build();

        return configuracionCorreoEmpresaRepository.save(configuracion);
    }

    @Override
    public void validar(TipoCorreo tipoCorreo, String asunto, String cuerpoHtml, String idEmpresa) {
        if (tipoCorreo == null) {
            throw new IllegalArgumentException("El tipo de correo es obligatorio");
        }
        if (asunto == null || asunto.isBlank()) {
            throw new IllegalArgumentException("El asunto es obligatorio");
        }
        if (cuerpoHtml == null || cuerpoHtml.isBlank()) {
            throw new IllegalArgumentException("El cuerpo del correo es obligatorio");
        }
        if (idEmpresa == null || idEmpresa.isBlank()) {
            throw new IllegalArgumentException("La empresa es obligatoria");
        }
    }

    @Override
    public ConfiguracionCorreoEmpresa buscarConfiguracionCorreoEmpresa(String id) {
        return configuracionCorreoEmpresaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la configuración de correo con id: " + id));
    }

    @Override
    public Optional<ConfiguracionCorreoEmpresa> buscarActivaPorTipo(TipoCorreo tipoCorreo) {
        return configuracionCorreoEmpresaRepository.findFirstByTipoCorreoAndEliminadoFalse(tipoCorreo);
    }

    @Override
    @Transactional
    public ConfiguracionCorreoEmpresa modificarConfiguracionCorreoEmpresa(String id, String asunto, String cuerpoHtml) {
        ConfiguracionCorreoEmpresa configuracion = buscarConfiguracionCorreoEmpresa(id);

        if (configuracion.isEliminado()) {
            throw new IllegalArgumentException("No se puede modificar una configuración eliminada");
        }
        if (asunto == null || asunto.isBlank()) {
            throw new IllegalArgumentException("El asunto es obligatorio");
        }
        if (cuerpoHtml == null || cuerpoHtml.isBlank()) {
            throw new IllegalArgumentException("El cuerpo del correo es obligatorio");
        }

        configuracion.setAsunto(asunto);
        configuracion.setCuerpoHtml(cuerpoHtml);

        return configuracionCorreoEmpresaRepository.save(configuracion);
    }

    @Override
    @Transactional
    public void eliminarConfiguracionCorreoEmpresa(String id) {
        ConfiguracionCorreoEmpresa configuracion = buscarConfiguracionCorreoEmpresa(id);
        configuracion.setEliminado(true);
        configuracionCorreoEmpresaRepository.save(configuracion);
    }

    @Override
    public List<ConfiguracionCorreoEmpresa> listarConfiguracionCorreoEmpresa() {
        return configuracionCorreoEmpresaRepository.findAll();
    }

    @Override
    public List<ConfiguracionCorreoEmpresa> listarConfiguracionCorreoEmpresaActiva() {
        return configuracionCorreoEmpresaRepository.findByEliminadoFalse();
    }
}