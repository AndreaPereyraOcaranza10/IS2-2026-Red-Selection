package com.tienda.zero.service;

import com.tienda.zero.enums.TipoCorreo;
import com.tienda.zero.model.ConfiguracionCorreoEmpresa;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionCorreoEmpresaService {

    ConfiguracionCorreoEmpresa crearConfiguracionCorreoEmpresa(String idEmpresa, TipoCorreo tipoCorreo,
                                                               String asunto, String cuerpoHtml);

    void validar(TipoCorreo tipoCorreo, String asunto, String cuerpoHtml, String idEmpresa);

    ConfiguracionCorreoEmpresa buscarConfiguracionCorreoEmpresa(String id);

    Optional<ConfiguracionCorreoEmpresa> buscarActivaPorTipo(TipoCorreo tipoCorreo);

    ConfiguracionCorreoEmpresa modificarConfiguracionCorreoEmpresa(String id, String asunto, String cuerpoHtml);

    void eliminarConfiguracionCorreoEmpresa(String id);

    List<ConfiguracionCorreoEmpresa> listarConfiguracionCorreoEmpresa();

    List<ConfiguracionCorreoEmpresa> listarConfiguracionCorreoEmpresaActiva();
}
