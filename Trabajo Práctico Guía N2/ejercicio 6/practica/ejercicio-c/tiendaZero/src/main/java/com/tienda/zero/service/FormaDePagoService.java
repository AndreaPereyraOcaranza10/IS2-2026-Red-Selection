package com.tienda.zero.service;

import com.tienda.zero.enums.TipoPago;
import com.tienda.zero.model.FormaDePago;

import java.util.List;

public interface FormaDePagoService {

    FormaDePago crearFormaDePago(TipoPago tipo, String observacion);

    void validar(TipoPago tipo);

    FormaDePago buscarFormaDePago(String id);

    FormaDePago modificarFormaDePago(String id, TipoPago tipo, String observacion);

    void eliminarFormaDePago(String id);

    List<FormaDePago> listarFormaDePago();

    List<FormaDePago> listarFormaDePagoActivo();
}